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

package org.sobotics.guttenberg.services;

import com.google.gson.JsonObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sobotics.guttenberg.utils.ApiUtils;
import org.sobotics.guttenberg.utils.FilePathUtils;
import org.sobotics.guttenberg.utils.FileUtils;
import org.sobotics.guttenberg.utils.JsonUtils;

import java.io.IOException;
import java.time.Instant;
import java.util.List;
import java.util.Properties;

/**
 * Created by bhargav.h on 29-Sep-16.
 */
public class ApiService {

  private static final Logger LOGGER = LoggerFactory.getLogger(ApiService.class);
  public static ApiService defaultService = new ApiService("stackoverflow");
  private static int quota = 0;
  private String apiKey;
  private String autoflagKey;
  private String autoflagToken;
  private String userId;
  private String site;


  private ApiService(String site) {
    Properties prop = new Properties();

    try {
      prop = FileUtils.getPropertiesFromFile(FilePathUtils.loginPropertiesFile);
    } catch (IOException e) {
      LOGGER.error("Failed to load login.properties", e);
    }

    this.site = site;
    this.apiKey = prop.getProperty("apikey");
    this.autoflagKey = prop.getProperty("autoflagkey");
    this.autoflagToken = prop.getProperty("autoflagtoken");
    this.userId = prop.getProperty("userid");


  }


  public JsonObject getRelatedQuestionsByIds(String questionIds) throws IOException {
    JsonObject questionJson = ApiUtils.getRelatedQuestionsByIds(questionIds, site, apiKey);
    JsonUtils.handleBackoff(LOGGER, questionJson);
    quota = questionJson.get("quota_remaining").getAsInt();
    return questionJson;
  }


  public JsonObject getLinkedQuestionsByIds(String questionIds) throws IOException {
    JsonObject questionJson = ApiUtils.getLinkedQuestionsByIds(questionIds, site, apiKey);
    JsonUtils.handleBackoff(LOGGER, questionJson);
    quota = questionJson.get("quota_remaining").getAsInt();
    return questionJson;
  }


  public JsonObject getAnswersToQuestionsByIdString(String questionIds) throws IOException {
    return getAnswersToQuestionsByIdString(questionIds, 1);
  }


  public JsonObject getAnswersToQuestionsByIdString(String questionIds, Integer page) throws IOException {
    JsonObject answersJson = ApiUtils.getAnswersToQuestionsByIdString(questionIds, page, site, apiKey);
    JsonUtils.handleBackoff(LOGGER, answersJson);
    quota = answersJson.get("quota_remaining").getAsInt();
    return answersJson;
  }


  public JsonObject getQuestionDetailsByIds(List<Integer> questionIdList) throws IOException {
    JsonObject questionJson = ApiUtils.getQuestionDetailsByIds(questionIdList, site, apiKey);
    JsonUtils.handleBackoff(LOGGER, questionJson);
    quota = questionJson.get("quota_remaining").getAsInt();
    return questionJson;
  }


  public JsonObject getQuestionDetailsById(Integer questionId) throws IOException {
    JsonObject questionJson = ApiUtils.getQuestionDetailsById(questionId, site, apiKey);
    JsonUtils.handleBackoff(LOGGER, questionJson);
    quota = questionJson.get("quota_remaining").getAsInt();
    return questionJson;
  }


  public JsonObject getAnswerDetailsById(Integer answerId) throws IOException {
    JsonObject answerJson = ApiUtils.getAnswerDetailsById(answerId, site, apiKey);
    JsonUtils.handleBackoff(LOGGER, answerJson);
    quota = answerJson.get("quota_remaining").getAsInt();
    return answerJson;
  }



  public JsonObject getAnswerDetailsByIds(List<Integer> answerIdList) throws IOException {
    return getAnswerDetailsByIds(answerIdList, "asc", "creation");
  }


  public JsonObject getAnswerDetailsByIds(List<Integer> answerIdList, String order, String sort) throws IOException {
    JsonObject answerJson = ApiUtils.getAnswerDetailsByIds(answerIdList, order, sort, site, apiKey);
    JsonUtils.handleBackoff(LOGGER, answerJson);
    quota = answerJson.get("quota_remaining").getAsInt();
    return answerJson;
  }


  public JsonObject getFirstPageOfAnswers(Instant fromTimestamp) throws IOException {
    JsonObject answersJson = ApiUtils.getFirstPageOfAnswers(fromTimestamp, site, apiKey);
    JsonUtils.handleBackoff(LOGGER, answersJson);
    quota = answersJson.get("quota_remaining").getAsInt();
    return answersJson;
  }


  public JsonObject getSearcExcerpts(String search) throws IOException {
    JsonObject answersJson = ApiUtils.getSearcExcerpts(search, site, apiKey);
    JsonUtils.handleBackoff(LOGGER, answersJson);
    quota = answersJson.get("quota_remaining").getAsInt();
    return answersJson;
  }


  public JsonObject getAnswerFlagOptions(Integer answerId) throws IOException {
    JsonObject flagOptionsJson = ApiUtils.getAnswerFlagOptions(answerId, site, autoflagKey, autoflagToken);
    JsonUtils.handleBackoff(LOGGER, flagOptionsJson);
    quota = flagOptionsJson.get("quota_remaining").getAsInt();
    return flagOptionsJson;
  }


  public JsonObject flagAnswer(Integer answerId, Integer flagType) throws IOException {
    JsonObject flaggedPost = ApiUtils.FlagAnswer(answerId, flagType, site, autoflagKey, autoflagToken);
    JsonUtils.handleBackoff(LOGGER, flaggedPost);
    quota = flaggedPost.get("quota_remaining").getAsInt();
    return flaggedPost;
  }


  public int getQuota() {
    return quota;
  }


  public String getApiKey() {
    return apiKey;
  }


  public String getAutoflagKey() {
    return autoflagKey;
  }


  public String getAutoflagToken() {
    return autoflagToken;
  }


  public String getUserId() {
    return userId;
  }


  public String getSite() {
    return site;
  }
}
