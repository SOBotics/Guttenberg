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
import org.sobotics.guttenberg.services.ApiService;
import org.sobotics.guttenberg.utils.FilePathUtils;
import org.sobotics.guttenberg.utils.FileUtils;
import org.sobotics.guttenberg.utils.PostUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

/**
 * Collects all related answers in less API calls
 */
public class RelatedAnswersFinder {

  private static final Logger LOGGER = LoggerFactory.getLogger(RelatedAnswersFinder.class);

  /**
   * The question_ids of the targeted answers
   */
  List<Integer> targedIds;


  public RelatedAnswersFinder(List<Integer> ids) {
    targedIds = ids;
  }


  public List<Post> fetchRelatedAnswers() {
    //The question_ids of all the new answers
    StringBuilder idString = new StringBuilder();
    int n = 0;
    for (Integer id : targedIds) {
      idString.append(n++ == 0 ? id : ";" + id);
    }

    LOGGER.debug("Related IDs: " + idString);

    if (idString.length() < 2)
      return new ArrayList<>();

    Properties prop = new Properties();

    try {
      prop = FileUtils.getPropertiesFromFile(FilePathUtils.loginPropertiesFile);
    } catch (IOException e) {
      LOGGER.error("Could not load login.properties", e);
    }

    LOGGER.debug("Fetching the linked/related questions...");

    try {
      JsonObject relatedQuestions = ApiService.defaultService.getRelatedQuestionsByIds(idString.toString());
      LOGGER.debug("Related done");
      JsonObject linkedQuestions = ApiService.defaultService.getLinkedQuestionsByIds(idString.toString());
      LOGGER.debug("linked done");

      StringBuilder relatedIds = new StringBuilder();

      for (JsonElement question : relatedQuestions.get("items").getAsJsonArray()) {
        int id = question.getAsJsonObject().get("question_id").getAsInt();
        LOGGER.trace("Add: " + id);
        relatedIds.append(id).append(";");
      }

      for (JsonElement question : linkedQuestions.get("items").getAsJsonArray()) {
        int id = question.getAsJsonObject().get("question_id").getAsInt();
        LOGGER.trace("Add: " + id);
        relatedIds.append(id).append(";");
      }

      if (relatedIds.length() > 0) {
        relatedIds = new StringBuilder(relatedIds.substring(0, relatedIds.length() - 1));

        List<JsonObject> relatedFinal = new ArrayList<>();

        int i = 1;

        while (i <= 2) {
          LOGGER.debug("Fetch page " + i);
          JsonObject relatedAnswers = ApiService.defaultService.getAnswersToQuestionsByIdString(relatedIds.toString(), i);
          LOGGER.trace("Related answers:\n" + relatedAnswers);

          for (JsonElement answer : relatedAnswers.get("items").getAsJsonArray()) {
            JsonObject answerObject = answer.getAsJsonObject();
            relatedFinal.add(answerObject);
          }

          JsonElement hasMoreElement = relatedAnswers.get("has_more");

          if (hasMoreElement != null && !hasMoreElement.getAsBoolean())
            break;

          i++;
        }

        List<Post> relatedPosts = new ArrayList<>();
        for (JsonElement item : relatedFinal) {
          Post post = PostUtils.getPost(item.getAsJsonObject());
          relatedPosts.add(post);
        }

        LOGGER.debug("Collected " + relatedFinal.size() + " answers");

        return relatedPosts;
      } else {
        LOGGER.warn("No ids found!");
      }


    } catch (IOException e) {
      LOGGER.error("Error in RelatedAnswersFinder", e);
      return new ArrayList<>();
    }


    return new ArrayList<>();
  }


}
