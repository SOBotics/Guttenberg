package org.sobotics.guttenberg.printers;

import org.sobotics.guttenberg.entities.PostMatch;
import org.sobotics.guttenberg.utils.PrintUtils;

/**
 * Created by bhargav.h on 20-Oct-16.
 */
public class SoBoticsPostPrinter implements PostPrinter {

    public final long roomId = 111347;

	@Override
	public String print(PostMatch match) {
		String message;
		String reasonsList = "";
		
		String targetLink = "https://stackoverflow.com/a/"+match.getTarget().getAnswerID()+"/4687348";
		String originalLink = "https://stackoverflow.com/a/"+match.getOriginal().getAnswerID()+"/4687348";
		
		for (String reason : match.getReasonStrings()) {
			reasonsList += reason+"; ";
		}
		
		String plagOrRepost = match.isRepost() ? "repost" : "plagiarism";
		
		double roundedTotalScore = Math.round(match.getTotalScore()*100.0)/100.0;
		
		message = PrintUtils.printDescription()+"["+match.getTarget().getAnswerID()+"]("+targetLink+") is possible "+plagOrRepost+" of ["+match.getOriginal().getAnswerID()+"]("+originalLink+")";
		message += "; **Reasons:** "+reasonsList;
		message += "**"+roundedTotalScore+"**; ";
		return message;
	}
}
