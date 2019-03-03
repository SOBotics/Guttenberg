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

package org.sobotics.guttenberg.printers;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sobotics.guttenberg.entities.PostMatch;
import org.sobotics.guttenberg.utils.PostUtils;
import org.sobotics.guttenberg.utils.PrintUtils;

import java.io.IOException;

/**
 * Created by bhargav.h on 20-Oct-16.
 */
public class SoBoticsPostPrinter implements PostPrinter {

  private static final Logger LOGGER = LoggerFactory.getLogger(SoBoticsPostPrinter.class);

  @Deprecated
  public final long roomId = 111347;


  @Override
  public String print(PostMatch match) {
    String message;
    String reportLink = null;
    StringBuilder reasonsList = new StringBuilder();

    String targetLink = "//stackoverflow.com/a/" + match.getTarget().getAnswerID() + "/4687348";
    String originalLink = "//stackoverflow.com/a/" + match.getOriginal().getAnswerID() + "/4687348";

    for (String reason : match.getReasonStrings()) {
      reasonsList.append(reason).append("; ");
    }

    LOGGER.debug("PostPrinter reasons: " + reasonsList);

    String plagOrRepost = match.isRepost() ? "repost" : "plagiarism";

    double roundedTotalScore = Math.round(match.getTotalScore() * 100.0) / 100.0;

    try {
      reportLink = PostUtils.storeReport(match);
    } catch (IOException e) {
      LOGGER.error("Error while sending the report to CopyPastor!", e);
    }


    message = PrintUtils.printDescription(reportLink) + "[" + match.getTarget().getAnswerID() + "](" + targetLink + ") is possible " + plagOrRepost + " of [" + match.getOriginal().getAnswerID() + "](" + originalLink + ")";
    message += "; **Reasons:** " + reasonsList;
    message += "**" + roundedTotalScore + "**; ";
    return message;
  }
}
