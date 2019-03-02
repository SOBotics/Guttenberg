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

package org.sobotics.guttenberg.finders;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sobotics.guttenberg.entities.Post;
import org.sobotics.guttenberg.entities.PostMatch;
import org.sobotics.guttenberg.reasonlists.ReasonList;
import org.sobotics.guttenberg.reasonlists.SOBoticsReasonList;
import org.sobotics.guttenberg.reasons.Reason;
import org.sobotics.guttenberg.services.ApiService;
import org.sobotics.guttenberg.utils.ApiUtils;
import org.sobotics.guttenberg.utils.FilePathUtils;
import org.sobotics.guttenberg.utils.FileUtils;
import org.sobotics.guttenberg.utils.PostUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

/**
 * Checks an answer for plagiarism by collecting similar answers from different sources.
 */
public class PlagFinder {

  private static final Logger LOGGER = LoggerFactory.getLogger(PlagFinder.class);
  /**
   * A list of answers that are somehow related to targetAnswer.
   */
  public List<Post> relatedAnswers;
  /**
   * The answer to check
   */
  private Post targetAnswer;


  /**
   * Initializes the PlagFinder with an answer that should be checked for plagiarism
   */
  public PlagFinder(JsonObject jsonObject) {
    this.targetAnswer = PostUtils.getPost(jsonObject);
    this.relatedAnswers = new ArrayList<Post>();
  }


  public PlagFinder(Post post) {
    this.targetAnswer = post;
    this.relatedAnswers = new ArrayList<Post>();
  }


  public PlagFinder(Post target, List<Post> related) {
    this.targetAnswer = target;
    this.relatedAnswers = related;
  }


  public void collectData() {
    this.relatedAnswers = new ArrayList<Post>();
    this.fetchRelatedAnswers();
    LOGGER.debug("Number of related answers: " + this.relatedAnswers.size());
  }


  private void fetchRelatedAnswers() {
    int targetId = this.targetAnswer.getQuestionID();
    int targetAnswerId = this.targetAnswer.getAnswerID();
    LOGGER.debug("Fetching related answers to: " + targetId);
    Properties prop = new Properties();

    try {
      prop = FileUtils.getPropertiesFromFile(FilePathUtils.loginPropertiesFile);
    } catch (IOException e) {
      LOGGER.error("Could not load login.properties", e);
      return;
    }

    try {
      JsonObject relatedQuestions = ApiUtils.getRelatedQuestionsById(targetId, "stackoverflow", prop.getProperty("apikey", ""));
      JsonObject linkedQuestions = ApiUtils.getLinkedQuestionsById(targetId, "stackoverflow", prop.getProperty("apikey", ""));
      LOGGER.trace("Related questions: " + relatedQuestions);
      LOGGER.trace("Linked questions: " + linkedQuestions);
      String relatedIds = targetId + ";";

      for (JsonElement question : relatedQuestions.get("items").getAsJsonArray()) {
        int id = question.getAsJsonObject().get("question_id").getAsInt();
        LOGGER.trace("Add: " + id);
        relatedIds += id + ";";
      }

      for (JsonElement question : linkedQuestions.get("items").getAsJsonArray()) {
        int id = question.getAsJsonObject().get("question_id").getAsInt();
        LOGGER.trace("Add: " + id);
        relatedIds += id + ";";
      }

      if (relatedIds.length() > 0) {
        relatedIds = relatedIds.substring(0, relatedIds.length() - 1);


        LOGGER.debug("Related question IDs: " + relatedIds);
        LOGGER.debug("Fetching all answers...");

        JsonObject relatedAnswers = ApiService.defaultService.getAnswersToQuestionsByIdString(relatedIds);

        for (JsonElement answer : relatedAnswers.get("items").getAsJsonArray()) {
          JsonObject answerObject = answer.getAsJsonObject();
          LOGGER.trace("Related answer: " + answer);
          Post answerPost = PostUtils.getPost(answerObject);

          int answerId = answerPost.getAnswerID();

          if (answerId != targetAnswerId)
            this.relatedAnswers.add(answerPost);
        }


      } else {
        LOGGER.warn("No ids found!");
      }

    } catch (IOException e) {
      LOGGER.error("ERROR", e);
      return;
    }
  }


  public Post getTargetAnswer() {
    return this.targetAnswer;
  }


  public Integer getTargetAnswerId() {
    return this.targetAnswer.getAnswerID();
  }


  /**
   * Returns the PostMatch objects.
   * <p>
   * Calls matchesForReasons(boolean) with a default value of false
   */
  public List<PostMatch> matchesForReasons() {
    return matchesForReasons(false);
  }


  /**
   * Returns the PostMatch objects.
   *
   * @parameter ignoringScores If true, the reasons will ignore any minimum scores
   */
  public List<PostMatch> matchesForReasons(boolean ignoringScores) {
    List<PostMatch> matches = new ArrayList<PostMatch>();
    //get reasonlist
    ReasonList reasonList = new SOBoticsReasonList(this.targetAnswer, this.relatedAnswers);

    for (Reason reason : reasonList.reasons(ignoringScores)) {
      LOGGER.debug("Checking reasons. Ignoring score: " + ignoringScores);
      //check if the reason applies
      if (reason.check()) {
        //if yes, add (new) posts to the list

        //get matched posts for that reason
        List<Post> matchedPosts = reason.matchedPosts();
        List<Double> scores = reason.getScores();
        int n = 0; //counts the matched posts to get the score correctly from "scores"

        if (matchedPosts == null)
          return null;

        for (Post post : matchedPosts) {
          //check if the new post is already part of a PostMatch
          boolean alreadyExists = false;
          int id = post.getAnswerID();
          int i = 0;
          for (PostMatch existingMatch : matches) {
            if (existingMatch.getOriginal().getAnswerID() == id) {
              //if it exists, add the new reason
              alreadyExists = true;
              LOGGER.debug("Adding reason " + reason.description(i) + " with score " + scores.get(n));
              existingMatch.addReason(reason.description(i), scores.get(n));

              existingMatch.addReasonToCopyPastorString(reason.description(i, false), scores.get(n));

              matches.set(i, existingMatch);
            }

            i++;
          }

          //if it doesn't exist yet, add it
          if (!alreadyExists) {
            PostMatch newMatch = new PostMatch(this.targetAnswer, post);
            newMatch.addReason(reason.description(i), scores.get(n));
            //#176: add the reason to the string for CopyPastor as well
            newMatch.addReasonToCopyPastorString(reason.description(i, false), scores.get(n));
            LOGGER.trace("Adding PostMatch " + newMatch);
            matches.add(newMatch);
          }

          n++;
        }
      }
    }

    return matches;
  }

}
