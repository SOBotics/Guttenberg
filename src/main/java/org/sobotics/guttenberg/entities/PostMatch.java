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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sobotics.guttenberg.utils.FilePathUtils;
import org.sobotics.guttenberg.utils.FileUtils;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

/**
 * Represents a plagiarized post
 */
public class PostMatch implements Comparable<PostMatch> {

  private static final Logger LOGGER = LoggerFactory.getLogger(PostMatch.class);

  private final Post target;
  private final Post original;
  private final List<String> reasons = new ArrayList<>();
  private double totalScore = 0;
  private String copyPastorReasonString = "";


  public PostMatch(Post targetPost, Post originalPost) {
    target = targetPost;
    original = originalPost;
  }


  public Post getTarget() {
    return target;
  }


  public Post getOriginal() {
    return original;
  }


  public void addReason(String reason, double score) {
    if (!reasons.contains(reason)) {
      //add reason
      reasons.add(reason);
      //add score
      totalScore += score;
    }
  }


  /**
   * Adds the reason to the String sent to CopyPastor
   * (can't be called by addReason() because this would include the score in the description)
   */
  public void addReasonToCopyPastorString(String reason, double score) {
    LOGGER.debug("Adding reason \"" + reason + "\" (Score: " + score + ") to string for CopyPastor");
    //#171: don't check the array. Check the String instead
    if (!copyPastorReasonString.contains(reason)) {
      LOGGER.debug("Reason doesn't exist in string yet");
      double roundedScore = Math.round(score * 100.0) / 100.0;

      if (!copyPastorReasonString.isEmpty())
        copyPastorReasonString += ",";

      copyPastorReasonString += reason + ":" + roundedScore;
      LOGGER.debug("New copyPastorReasonString: " + copyPastorReasonString);
    }
  }


  public List<String> getReasonStrings() {
    return reasons;
  }


  public String getCopyPastorReasonString() {
    return copyPastorReasonString;
  }


  public double getTotalScore() {
    return this.totalScore;
  }


  /**
   * Returns if the target and original post have the same author
   */
  public boolean isRepost() {
    return target.getAnswerer().getUserId() == original.getAnswerer().getUserId();
  }


  /**
   * Checks if the match is valid to be reported.
   * <p>
   * A PostMatch is valid, if both posts are longer than the minimum length.
   *
   * @return true if the match is valid to be reported
   */
  public boolean isValidMatch() {
    int minimumLength = 200;
    Properties quantifiers = new Properties();
    try {
      quantifiers = FileUtils.getPropertiesFromFile(FilePathUtils.generalPropertiesFile);
      minimumLength = new Integer(quantifiers.getProperty("minimumPostLength", "200"));
    } catch (Throwable e) {
      LOGGER.warn("Could not load quantifiers from general.properties. Using hardcoded", e);
    }

    int lengthOne = original.getCleanBodyMarkdown().length();
    int lengthTwo = target.getCleanBodyMarkdown().length();


    //#114: The original post should be at least 5 minutes older than the target
    Instant targetCreation = target.getAnswerCreationDate();
    Instant originalCreation = original.getAnswerCreationDate();
    long minutes = ChronoUnit.MINUTES.between(originalCreation, targetCreation);
    boolean minimumTimeSpanChecked = minutes >= 5;

    //#130: Time-span for reposts: 0 minutes
    if (this.isRepost())
      minimumTimeSpanChecked = targetCreation.getEpochSecond() > originalCreation.getEpochSecond();

    LOGGER.trace("minimumTimeSpanChecked: " + minimumTimeSpanChecked);
    LOGGER.trace("Minimum length: " + minimumLength);
    LOGGER.trace("Length one: " + lengthOne);
    LOGGER.trace("Length two: " + lengthTwo);

    return lengthOne >= minimumLength && lengthTwo >= minimumLength && minimumTimeSpanChecked;
  }


  @Override
  public int compareTo(PostMatch o) {
    return Double.compare(o.getTotalScore(), getTotalScore());
  }
}
