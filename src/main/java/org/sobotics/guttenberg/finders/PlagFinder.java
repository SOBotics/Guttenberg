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
import org.sobotics.guttenberg.search.InternetSearch;
import org.sobotics.guttenberg.search.SearchResult;
import org.sobotics.guttenberg.search.SearchTerms;
import org.sobotics.guttenberg.services.ApiService;
import org.sobotics.guttenberg.utils.ApiUtils;
import org.sobotics.guttenberg.utils.PostUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Checks an answer for plagiarism by collecting similar answers from different sources.
 * */
public class PlagFinder {

  private static final Logger LOGGER = LoggerFactory.getLogger(PlagFinder.class);

  /**
   * The answer to check
   */
  private final Post targetAnswer;

  /**
   * A list of answers that are somehow related to targetAnswer.
   */
  public List<Post> relatedAnswers;


  /**
   * Initializes the PlagFinder with an answer that should be checked for plagiarism
   */
  public PlagFinder(JsonObject jsonObject) {
    targetAnswer = PostUtils.getPost(jsonObject);
    relatedAnswers = new ArrayList<>();
  }


  public PlagFinder(Post post) {
    targetAnswer = post;
    relatedAnswers = new ArrayList<>();
  }


  public PlagFinder(Post target, List<Post> related) {
    targetAnswer = target;
    relatedAnswers = related;
  }


  public void collectData() {
    relatedAnswers = new ArrayList<>();
    fetchRelatedAnswers();
    LOGGER.debug("Number of related answers: " + relatedAnswers.size());
  }


  /**
   * Search on google
   *
   * @param st, the SearchTerms
   * @return number of answer hits
   * @throws IOException
   */
  public int addGoogleSearchData(SearchTerms st) throws IOException {
    InternetSearch is = new InternetSearch();
    SearchResult result = is.google(targetAnswer, st);
    if (result.getItems().isEmpty()) {
      return 0;
    }
    List<Post> googleAnswers = new ArrayList<>();
    List<Integer> ids = result.getIdQuestions();

    if (!ids.isEmpty()) {
      String relatedIds = ids.stream().map(id -> String.valueOf(id)).collect(Collectors.joining(";"));
      JsonObject ra = ApiService.defaultService.getAnswersToQuestionsByIdString(relatedIds);
      for (JsonElement answer : ra.get("items").getAsJsonArray()) {
        JsonObject answerObject = answer.getAsJsonObject();
        Post answerPost = PostUtils.getPost(answerObject);
        if (answerPost.getAnswerID().intValue() != targetAnswer.getAnswerID().intValue()) {
          googleAnswers.add(answerPost);
        }
      }
    }
    //add to head
    int totaleFound = googleAnswers.size();
    googleAnswers.addAll(relatedAnswers);
    relatedAnswers = googleAnswers;
    return totaleFound;

  }


  /**
   * Search on SE api
   *
   * @param query, the query to search
   * @return number of hits
   * @throws IOException
   */
  public int addSEApiSearch(String query) throws IOException {
    JsonObject json = ApiService.defaultService.getSearcExcerpts(query);

    List<Integer> answerIds = new ArrayList<>();
    if (json.has("items")) {
      for (JsonElement element : json.get("items").getAsJsonArray()) {
        JsonObject object = element.getAsJsonObject();
        if (object.has("answer_id")) {
          int id = object.get("answer_id").getAsInt();
          if (id != targetAnswer.getAnswerID()) {
            answerIds.add(id);
          }
        }
      }
    }

    List<Post> seApiAnswers = new ArrayList<>();
    if (!answerIds.isEmpty()) {
      JsonObject ra = ApiService.defaultService.getAnswerDetailsByIds(answerIds);
      if (json.has("items")) {
        for (JsonElement answer : ra.get("items").getAsJsonArray()) {
          JsonObject answerObject = answer.getAsJsonObject();
          seApiAnswers.add(PostUtils.getPost(answerObject));
        }
      }
    }

    if (seApiAnswers.size() > 50) {
      seApiAnswers = seApiAnswers.subList(0, 49);
    }

    int totaleFound = seApiAnswers.size();
    seApiAnswers.addAll(relatedAnswers);
    relatedAnswers = seApiAnswers;
    return totaleFound;
  }


  private void fetchRelatedAnswers() {
    int targetId = this.targetAnswer.getQuestionID();
    int targetAnswerId = this.targetAnswer.getAnswerID();
    LOGGER.debug("Fetching related answers to: " + targetId);


    try {
      JsonObject relatedQuestions = ApiUtils.getRelatedQuestionsById(targetId, "stackoverflow", ApiService.defaultService.getApiKey());
      JsonObject linkedQuestions = ApiUtils.getLinkedQuestionsById(targetId, "stackoverflow", ApiService.defaultService.getApiKey());
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


  public List<Post> getRelatedAnswers() {
    return relatedAnswers;
  }
    
}
