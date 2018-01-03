package org.sobotics.guttenberg.printers;

import java.io.IOException;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sobotics.guttenberg.clients.Guttenberg;
import org.sobotics.guttenberg.entities.Post;
import org.sobotics.guttenberg.entities.PostMatch;
import org.sobotics.guttenberg.utils.JsonUtils;
import org.sobotics.guttenberg.utils.PrintUtils;

import com.google.gson.JsonObject;

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
		
		String plagOrRepost = match.isRepost() ? "repost" : "plagiarism";
		
		double roundedTotalScore = Math.round(match.getTotalScore()*100.0)/100.0;
		
		try {
			reportLink = storeReport(match.getTarget(), match.getOriginal());
		}
		catch (IOException e) {
			LOGGER.warn(e.getMessage());
		}
		
		
		message = PrintUtils.printDescription(reportLink)+"["+match.getTarget().getAnswerID()+"]("+targetLink+") is possible "+plagOrRepost+" of ["+match.getOriginal().getAnswerID()+"]("+originalLink+")";
		message += "; **Reasons:** "+reasonsList;
		message += "**"+roundedTotalScore+"**; ";
		return message;
	}
	
	
	private String storeReport(Post target, Post original) throws IOException {
		Properties prop = Guttenberg.getLoginProperties();
	
		String url = prop.getProperty("copypastor_url", "http://guttenberg.sobotics.org:5000")+"/posts/create";
		JsonObject output = JsonUtils.post(url,
		                "url_one","//stackoverflow.com/a/"+target.getAnswerID()+"/4687348",
		                "url_two","//stackoverflow.com/a/"+original.getAnswerID()+"/4687348",
		                //"title_one",original.getTitle(),
		                //"title_two",target.getTitle(),
		                "title_one","Possible Plagiarism",
		                "title_two", "Original Post",
		                "date_one",""+target.getAnswerCreationDate().getEpochSecond(),
		                "date_two",""+original.getAnswerCreationDate().getEpochSecond(),
		                "body_one",target.getBodyMarkdown(),
		                "body_two",original.getBodyMarkdown());
		
		return prop.getProperty("copypastor_url", "http://guttenberg.sobotics.org:5000") + "/posts/" + output.get("post_id").getAsString();
	}
}
