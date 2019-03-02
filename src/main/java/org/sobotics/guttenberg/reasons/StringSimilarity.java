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

package org.sobotics.guttenberg.reasons;

import info.debatty.java.stringsimilarity.JaroWinkler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sobotics.guttenberg.entities.Post;
import org.sobotics.guttenberg.utils.FilePathUtils;
import org.sobotics.guttenberg.utils.FileUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

/**
 * Checks the similarity of two posts
 */
public class StringSimilarity implements Reason {

  public static final String LABEL = "String similarity";
  private static final Logger LOGGER = LoggerFactory.getLogger(StringSimilarity.class);
  private Post target;
  private List<Post> originals;
  private List<Post> matchedPosts = new ArrayList<Post>();
  private List<Double> scoreList = new ArrayList<Double>();
  private double score = -1;
  private boolean ignoringScore = false;


  public StringSimilarity(Post target, List<Post> originalPosts) {
    this.target = target;
    this.originals = originalPosts;
  }


  public StringSimilarity(Post target, List<Post> originalPosts, boolean ignoringScore) {
    this.target = target;
    this.originals = originalPosts;
    this.ignoringScore = ignoringScore;
  }


  public static double similarityOf(Post targetPost, Post originalPost) {
    LOGGER.debug("Checking StringSimilarity of " + targetPost.getAnswerID() + " and " + originalPost.getAnswerID());
    String targetBodyMarkdown = targetPost.getCleanBodyMarkdown();
    String targetCodeOnly = targetPost.getCodeOnly();
    String targetPlaintext = targetPost.getPlaintext();
    String targetQuotes = targetPost.getQuotes();

    Properties quantifiers = new Properties();
    try {
      quantifiers = FileUtils.getPropertiesFromFile(FilePathUtils.generalPropertiesFile);
    } catch (IOException e) {
      LOGGER.warn("Could not load quantifiers from general.properties. Using hardcoded", e);
    }

    double quantifierBodyMarkdown = 1;
    double quantifierCodeOnly = 1;
    double quantifierPlaintext = 1;
    double quantifierQuotes = 1;

    try {
      quantifierBodyMarkdown = new Double(quantifiers.getProperty("quantifierBodyMarkdown", "1"));
      quantifierCodeOnly = new Double(quantifiers.getProperty("quantifierCodeOnly", "1"));
      quantifierPlaintext = new Double(quantifiers.getProperty("quantifierPlaintext", "1"));
      quantifierQuotes = new Double(quantifiers.getProperty("quantifierQuotes", "1"));
    } catch (Throwable e) {
      LOGGER.warn("Using hardcoded value", e);
    }

    JaroWinkler jw = new JaroWinkler();

    String originalBodyMarkdown = originalPost.getCleanBodyMarkdown();
    String originalCodeOnly = originalPost.getCodeOnly();
    String originalPlaintext = originalPost.getPlaintext();
    String originalQuotes = originalPost.getQuotes();

    double jwBodyMarkdown = jw.similarity(targetBodyMarkdown, originalBodyMarkdown)
      * quantifierBodyMarkdown;
    double jwCodeOnly = originalCodeOnly != null ? (jw.similarity(targetCodeOnly, originalCodeOnly)
      * quantifierCodeOnly) : 0;
    double jwPlaintext = originalPlaintext != null ? (jw.similarity(originalPlaintext, targetPlaintext)
      * quantifierPlaintext) : 0;
    double jwQuotes = originalQuotes != null ? (jw.similarity(originalQuotes, targetQuotes)
      * quantifierQuotes) : 0;

    LOGGER.trace("bodyMarkdown: " + jwBodyMarkdown + "; codeOnly: " + jwCodeOnly + "; plaintext: " + jwPlaintext);

    double usedScores = (jwBodyMarkdown > 0 ? quantifierBodyMarkdown : 0)
      + (jwCodeOnly > 0 ? quantifierCodeOnly : 0)
      + (jwPlaintext > 0 ? quantifierPlaintext : 0)
      + (jwQuotes > 0 ? quantifierQuotes : 0);
    double jaroWinklerScore = ((jwBodyMarkdown > 0 ? jwBodyMarkdown : 0)
      + (jwCodeOnly > 0 ? jwCodeOnly : 0)
      + (jwPlaintext > 0 ? jwPlaintext : 0)
      + (jwQuotes > 0 ? jwQuotes : 0)) / usedScores;

    LOGGER.trace("Score: " + jaroWinklerScore);

    if (jwBodyMarkdown > 0.9) {
      return jwBodyMarkdown;
    }

    if (jaroWinklerScore > 0.01) {
      return jaroWinklerScore;
    }

    return -1;
  }


  @Override
  public boolean check() {
    boolean matched = false;
    for (Post post : this.originals) {
      double currentScore = StringSimilarity.similarityOf(this.target, post);

      //get the report threshold
      Properties prop = new Properties();
      try {
        prop = FileUtils.getPropertiesFromFile(FilePathUtils.generalPropertiesFile);
      } catch (IOException e) {
        LOGGER.warn("Could not load general.properties. Using hardcoded value", e);
      }

      double minimumScore = new Double(prop.getProperty("minimumScore", "0.8"));

      if (currentScore >= minimumScore || this.ignoringScore == true) {
        matched = true;

        //add post to list of matched posts
        this.matchedPosts.add(post);
        this.scoreList.add(currentScore);

        //update score
        if (this.score <= currentScore)
          this.score = currentScore;
      }
    }

    return matched;
  }


  @Override
  public String description(int index) {
    return description(index, true);
  }


  @Override
  public String description(int index, boolean includingScore) {
    if (includingScore) {
      double roundedScore = Math.round(this.scoreList.get(index) * 100.0) / 100.0;
      String descriptionStr = score() >= 0 ? LABEL + " " + roundedScore : LABEL;
      LOGGER.trace("Description: " + descriptionStr);
      return descriptionStr;
    } else {
      return LABEL;
    }
  }


  @Override
  public double score() {
    return this.score;
  }


  @Override
  public List<Post> matchedPosts() {
    return this.matchedPosts;
  }


  @Override
  public List<Double> getScores() {
    return this.scoreList;
  }

}
