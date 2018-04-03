package org.sobotics.guttenberg.printers;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sobotics.guttenberg.entities.PostMatch;
import org.sobotics.guttenberg.utils.PostUtils;
import org.sobotics.guttenberg.utils.PrintUtils;

/**
 * Created by bhargav.h on 20-Oct-16.
 */
public class SoBoticsPostPrinter implements PostPrinter {

    public final long roomId = 111347;
    
    private static final Logger LOGGER = LoggerFactory.getLogger(SoBoticsPostPrinter.class);

	@Override
	public String print(PostMatch match) {
		String message;
		String reportLink = null;
		String reasonsList = "";
		
		String targetLink = "//stackoverflow.com/a/"+match.getTarget().getAnswerID()+"/4687348";
		String originalLink = "//stackoverflow.com/a/"+match.getOriginal().getAnswerID()+"/4687348";
		
		for (String reason : match.getReasonStrings()) {
			reasonsList += reason+"; ";
		}
		
		LOGGER.debug("PostPrinter reasons: " + reasonsList);
		
		String plagOrRepost = match.isRepost() ? "repost" : "plagiarism";
		
		double roundedTotalScore = Math.round(match.getTotalScore()*100.0)/100.0;
		
		try {
			reportLink = PostUtils.storeReport(match);
		}
		catch (IOException e) {
			LOGGER.error("Error while sending the report to CopyPastor!", e);
		}
		
		
		message = PrintUtils.printDescription(reportLink)+"["+match.getTarget().getAnswerID()+"]("+targetLink+") is possible "+plagOrRepost+" of ["+match.getOriginal().getAnswerID()+"]("+originalLink+")";
		message += "; **Reasons:** "+reasonsList;
		message += "**"+roundedTotalScore+"**; ";
		return message;
	}
}
