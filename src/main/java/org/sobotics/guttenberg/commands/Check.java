/*
 * Copyright (C) 2019 SOBotics
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

package org.sobotics.guttenberg.commands;

import com.google.gson.JsonObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sobotics.chatexchange.chat.Message;
import org.sobotics.chatexchange.chat.Room;
import org.sobotics.guttenberg.entities.PostMatch;
import org.sobotics.guttenberg.finders.PlagFinder;
import org.sobotics.guttenberg.services.RunnerService;
import org.sobotics.guttenberg.utils.ApiUtils;
import org.sobotics.guttenberg.utils.CommandUtils;
import org.sobotics.guttenberg.utils.FilePathUtils;
import org.sobotics.guttenberg.utils.FileUtils;

import java.io.IOException;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Properties;

public class Check implements SpecialCommand {

  private static final Logger LOGGER = LoggerFactory.getLogger(Check.class);
  private static final String CMD = "check";

  private final Message message;


  public Check(Message message) {
    this.message = message;
  }


  @Override
  public boolean validate() {
    return CommandUtils.checkForCommand(message.getPlainContent(), CMD);
  }


  @Override
  public void execute(Room room, RunnerService instance) {
    String word = CommandUtils.extractData(message.getPlainContent()).trim();

    if (word.contains("/")) {
      word = CommandUtils.getAnswerId(word);
    }

    Integer answerId = new Integer(word);

    Properties prop = new Properties();

    try {
      prop = FileUtils.getPropertiesFromFile(FilePathUtils.loginPropertiesFile);
    } catch (IOException e) {
      LOGGER.error("Could not read login.properties", e);
    }


    try {
      JsonObject answer = ApiUtils.getAnswerDetailsById(answerId, "stackoverflow", prop.getProperty("apikey", ""));
      JsonObject target = answer.get("items").getAsJsonArray().get(0).getAsJsonObject();
      LOGGER.trace("Checking answer: " + target.toString());
      PlagFinder finder = new PlagFinder(target);
      finder.collectData();
      List<PostMatch> matches = finder.matchesForReasons(true);

      LOGGER.trace("Found " + matches.size() + " PostMatches");

      if (matches.size() > 0) {
        //sort the matches
        Collections.sort(matches,
          Comparator.comparingDouble(PostMatch::getTotalScore).reversed());


        //prepare printing them
        int i = 0;
        String reply = "These posts are similar to the target: ";
        for (PostMatch match : matches) {
          if (i > 5)
            break;
          double roundedTotalScore = Math.round(match.getTotalScore() * 100.0) / 100.0;
          reply += "[" + match.getOriginal().getAnswerID() + "](http://stackoverflow.com/a/" + match.getOriginal().getAnswerID() + ") (" + roundedTotalScore + "); ";
          i++;
        }

        room.replyTo(message.getId(), reply);
      } else {
        room.replyTo(message.getId(), "No similar posts found.");
      }
    } catch (IOException e) {
      LOGGER.error("Error while executing the \"check\"-command!", e);
    }
  }


  @Override
  public String description() {
    return "Checks a post for plagiarism: check <answer url>";
  }


  @Override
  public String name() {
    return CMD;
  }


  @Override
  public boolean availableInStandby() {
    return false;
  }

}
