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

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sobotics.guttenberg.entities.Post;
import org.sobotics.guttenberg.services.ApiService;
import org.sobotics.guttenberg.utils.FilePathUtils;
import org.sobotics.guttenberg.utils.FileUtils;
import org.sobotics.guttenberg.utils.PostUtils;
import org.sobotics.guttenberg.utils.StatusUtils;

import java.io.IOException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

/**
 * Fetches the most recent answers
 */
public class NewAnswersFinder {

  private static final Logger LOGGER = LoggerFactory.getLogger(NewAnswersFinder.class);


  public static List<Post> findRecentAnswers() {
    Instant time = Instant.now().minusSeconds(59 + 1);

    //Use time of last execution-start to really get ALL answers
    if (StatusUtils.lastSucceededExecutionStarted != null)
      time = StatusUtils.lastSucceededExecutionStarted;


    Properties prop = new Properties();

    try {
      prop = FileUtils.getPropertiesFromFile(FilePathUtils.loginPropertiesFile);
    } catch (IOException e) {
      LOGGER.error("Could not load login.properties", e);
      return new ArrayList<Post>();
    }

    try {
      JsonObject apiResult = ApiService.defaultService.getFirstPageOfAnswers(time);
      //fetched answers

      JsonArray items = apiResult.get("items").getAsJsonArray();
      LOGGER.trace("New answers:\n" + items);
      LOGGER.info("findRecentAnswers() done with " + items.size() + " items");
      List<Post> posts = new ArrayList<>();

      for (JsonElement item : items) {
        Post post = PostUtils.getPost(item.getAsJsonObject());
        posts.add(post);
      }

      return posts;


    } catch (IOException e) {
      LOGGER.error("Could not load recent answers", e);
      return new ArrayList<>();
    }
  }

}
