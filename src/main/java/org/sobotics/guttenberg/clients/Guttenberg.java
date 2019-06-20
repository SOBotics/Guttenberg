/*
 * Copyright (C) 2019 SOBotics (https://sobotics.org) and contributors on GitHub
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>
 */

package org.sobotics.guttenberg.clients;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sobotics.chatexchange.chat.Room;
import org.sobotics.guttenberg.entities.Post;
import org.sobotics.guttenberg.entities.PostMatch;
import org.sobotics.guttenberg.finders.NewAnswersFinder;
import org.sobotics.guttenberg.finders.PlagFinder;
import org.sobotics.guttenberg.finders.RelatedAnswersFinder;
import org.sobotics.guttenberg.printers.SoBoticsPostPrinter;
import org.sobotics.guttenberg.utils.StatusUtils;
import org.sobotics.redunda.PingService;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

/**
 * Fetches and analyzes the data from the API
 * */
public class Guttenberg {

  private static final Logger LOGGER = LoggerFactory.getLogger(Guttenberg.class);

  private final List<Room> chatRooms;
  private static Properties loginProperties;
  private static Properties generalProperties;


  public Guttenberg(List<Room> rooms) {
    chatRooms = rooms;
  }


  /**
   * Executes `execute()` and catches all the errors
   *
   * @see http://stackoverflow.com/a/24902026/4687348
   */
  public void secureExecute() {
    try {
      execute();
    } catch (Throwable e) {
      LOGGER.error("Error thrown in execute()", e);
    }
  }


  public void execute() throws Throwable {
    boolean standbyMode = PingService.standby.get();
    if (standbyMode) {
      LOGGER.info("STANDBY - Abort execute()");
      return;
    }

    Instant startTime = Instant.now();
    LOGGER.info("Starting Guttenberg.execute() ...");


    //Fetch recent answers / The targets
    List<Post> recentAnswers = NewAnswersFinder.findRecentAnswers();
    StatusUtils.numberOfCheckedTargets.addAndGet(recentAnswers.size());
    //Fetch their question_ids
    List<Integer> ids = new ArrayList<>();
    for (Post answer : recentAnswers) {
      Integer id = answer.getQuestionID();
      if (!ids.contains(id))
        ids.add(id);
    }


    //Initialize the PlagFinders
    List<PlagFinder> plagFinders = new ArrayList<>();

    for (Post answer : recentAnswers) {
      PlagFinder plagFinder = new PlagFinder(answer);
      plagFinders.add(plagFinder);
    }

    //fetch all /questions/ids/answers sort them later
    RelatedAnswersFinder related = new RelatedAnswersFinder(ids);
    List<Post> relatedAnswersUnsorted = related.fetchRelatedAnswers();

    if (relatedAnswersUnsorted.isEmpty()) {
      LOGGER.warn("No related answers could be fetched. Skipping this execution...");
      return;
    }

    LOGGER.debug("Add the answers to the PlagFinders...");
    //add relatedAnswers to the PlagFinders
    for (PlagFinder finder : plagFinders) {
      Integer targetId = finder.getTargetAnswerId();
      LOGGER.trace("Check targetID: " + targetId);

      for (Post relatedItem : relatedAnswersUnsorted) {
        LOGGER.trace("Related item: " + relatedItem);
        if (relatedItem.getAnswerID() != null && relatedItem.getAnswerID() != targetId) {
          finder.relatedAnswers.add(relatedItem);
          LOGGER.trace("Added answer: " + relatedItem);
        }
      }
    }

    LOGGER.debug("There are " + plagFinders.size() + " PlagFinders");
    LOGGER.debug("Find the duplicates...");
    //Let PlagFinders find the best match
    List<PostMatch> allMatches = new ArrayList<>();
    for (PlagFinder finder : plagFinders) {
      List<PostMatch> matchesInFinder = finder.matchesForReasons();

      if (matchesInFinder != null) {
        LOGGER.info("Found " + matchesInFinder.size() + " PostMatches in this PlagFinder");
        allMatches.addAll(matchesInFinder);

        for (PostMatch match : matchesInFinder) {
          if (match.isValidMatch()) {
            StatusUtils.numberOfReportedPosts.incrementAndGet();
            SoBoticsPostPrinter printer = new SoBoticsPostPrinter();
            String message = printer.print(match);

            for (Room room : chatRooms) {
              room.send(message);
            }
          }
        }
      }

    }

    StatusUtils.lastSucceededExecutionStarted = startTime;
    StatusUtils.lastExecutionFinished = Instant.now();
    LOGGER.info("Guttenberg.execute() finished");
  }


  public static Properties getLoginProperties() {
    if (loginProperties == null) {
      throw new NullPointerException("The login properties have not been instanced");
    }

    return loginProperties;
  }


  public static Properties getGeneralProperties() {
    if (generalProperties == null) {
      throw new NullPointerException("The general properties have not been instanced");
    }

    return generalProperties;
  }


  public static void setLoginProperties(Properties loginProperties) {
    Guttenberg.loginProperties = loginProperties;
  }


  public static void setGeneralProperties(Properties generalProperties) {
    Guttenberg.generalProperties = generalProperties;
  }
}
