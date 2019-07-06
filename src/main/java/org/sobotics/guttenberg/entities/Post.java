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

package org.sobotics.guttenberg.entities;

import com.google.gson.JsonObject;
import org.jetbrains.annotations.NotNull;
import org.sobotics.guttenberg.utils.PostUtils;

import java.time.Instant;
import java.util.List;

/**
 * Created by bhargav.h on 11-Sep-16.
 */
public class Post {
  private String title;
  private Instant answerCreationDate;
  private Integer answerID;
  private Integer questionID;
  private String body;
  private String bodyMarkdown;
  /**
   * Unescaped markdown. That's required to post it to SOBotics/CopyPastor
   */
  private String unescapedBodyMarkdown;
  private SOUser answerer;
  private List<String> tags;

  private String codeOnly;
  private String plaintext;
  private String quotes;

  private double score = 0;


  public String getTitle() {
    return title;
  }


  public void setTitle(String title) {
    this.title = title;
  }


  public Instant getAnswerCreationDate() {
    return answerCreationDate;
  }


  public void setAnswerCreationDate(Instant answerCreationDate) {
    this.answerCreationDate = answerCreationDate;
  }


  public Integer getAnswerID() {
    return answerID;
  }


  public void setAnswerID(Integer answerID) {
    this.answerID = answerID;
  }


  public Integer getQuestionID() {
    return questionID;
  }


  public void setQuestionID(Integer questionID) {
    this.questionID = questionID;
  }


  public String getBody() {
    return body;
  }


  public void setBody(String body) {
    this.body = body;
  }


  public String getBodyMarkdown() {
    return bodyMarkdown;
  }


  /**
   * Returns a cleaner Version of the body_markdown
   * It removes the markdown used to create JS-snippets
   */
  public String getCleanBodyMarkdown() {
    String md = getBodyMarkdown();

    //#150: Snippets still match
    md = md.replaceAll("<!-- begin snippet:.*-->|<!-- language:.*-->|<!-- end snippet.*-->", "");

    return md;
  }


  public void setBodyMarkdown(String bodyMarkdown) {
    this.bodyMarkdown = bodyMarkdown;
  }


  public SOUser getAnswerer() {
    return answerer;
  }


  public void setAnswerer(SOUser answerer) {
    this.answerer = answerer;
  }


  public void setTags(List<String> newTags) {
    this.tags = newTags;
  }


  public List<String> getTags() {
    return this.tags;
  }


  public String getMainTag() {
    return !tags.isEmpty() ? tags.get(0) : "";
  }


  @Override
  public String toString() {

    JsonObject json = getJson();
    return json.toString();
  }


  @NotNull
  private JsonObject getJson() {
    JsonObject json = new JsonObject();

    json.addProperty("title", title);
    json.addProperty("answerCreationDate", answerCreationDate.toString());
    json.addProperty("answerID", answerID);
    json.addProperty("body", body);
    json.addProperty("bodyMarkdown", bodyMarkdown);
    json.add("answerer", answerer.getJson());
    return json;

  }


  public String getCodeOnly() {
    return codeOnly != null ? codeOnly : "";
  }


  public String getPlaintext() {
    return plaintext != null ? plaintext : "";
  }


  public String getQuotes() {
    return quotes != null ? quotes : "";
  }


  public void setScore(double newScore) {
    score = newScore;
  }


  public double getScore() {
    return score;
  }


  public void parsePost() {
    JsonObject parts = PostUtils.separateBodyParts(this);

    codeOnly = parts.get("body_code").getAsString();
    quotes = parts.get("body_quote").getAsString();

    String plain = parts.get("body_plain").getAsString();
    plain.replaceFirst("\\d*\\s*up\\s*vote\\s*\\d*\\s*down\\s*vote", "");
    plain.replaceAll("<!--.*-->", "");
    plaintext = plain;
  }


  public String getUnescapedBodyMarkdown() {
    return unescapedBodyMarkdown;
  }


  public void setUnescapedBodyMarkdown(String unescapedBodyMarkdown) {
    this.unescapedBodyMarkdown = unescapedBodyMarkdown;
  }


  @Override
  public boolean equals(Object o) {
    if (o instanceof Post) {
      return hashCode() == o.hashCode();
    }
    return false;
  }


  @Override
  public int hashCode() {
    if (answerID != null && answerID > 0) {
      return ("A" + answerID).hashCode();
    }
    if (questionID != null && questionID > 0) {
      return ("Q" + questionID).hashCode();
    }
    return super.hashCode();
  }

}
