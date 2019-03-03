/*
 * Copyright (C) 2019 SOBotics (https://sobotics.org) and contributors in GitHub
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
import org.sobotics.guttenberg.clients.Guttenberg;
import org.sobotics.guttenberg.entities.Post;
import org.sobotics.guttenberg.utils.PostUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

/**
 * This reason returns posts that have at least one exactly matching paragraph with the target.
 * */
public class ExactParagraphMatch implements Reason {

  private static final Logger LOGGER = LoggerFactory.getLogger(ExactParagraphMatch.class);

  private Post target;
  private List<Post> originals;
  private List<Post> matchedPosts = new ArrayList<Post>();
  private List<Double> scoreList = new ArrayList<Double>();
  private double score = -1;

  public static final String LABEL = "Exact paragraph match";


  public ExactParagraphMatch(Post target, List<Post> originalPosts) {
    this.target = target;
    this.originals = originalPosts;
  }


  @Override
  public boolean check() {
    LOGGER.trace("Checking for " + LABEL);
    Properties prop = Guttenberg.getGeneralProperties();
    double minimumLength = new Double(prop.getProperty("minimumExactMatchLength", "100"));

    JaroWinkler jw = new JaroWinkler();
    boolean matched = false;

    List<String> targetCodePs = PostUtils.getCodeParagraphs(this.target.getCleanBodyMarkdown());
    LOGGER.trace("Target code-only paragraphs: " + targetCodePs.size());
    for (Post original : this.originals) {
      List<String> originalCodePs = PostUtils.getCodeParagraphs(original.getCleanBodyMarkdown());
      LOGGER.trace("Original code-only paragraphs: " + targetCodePs.size());

      //Loop through targetCodePs
      for (String targetCode : targetCodePs) {
        //loop through originalCodePs
        for (String originalCode : originalCodePs) {
          double similarity = jw.similarity(targetCode, originalCode);
          if (similarity > 0.97 && originalCode.length() >= minimumLength && targetCode.length() >= minimumLength) {
            LOGGER.debug("Exact match: " + similarity + "\n" + targetCode);
            if (this.score < 0)
              this.score = 0;

            this.score++;
            matched = true;
            if (!this.matchedPosts.contains(original)) {
              this.matchedPosts.add(original);
              this.scoreList.add(1.0);
            }
          }
        }
      }
    }

    return matched;
  }


  @Override
  public String description(int index) {
    return LABEL;
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
