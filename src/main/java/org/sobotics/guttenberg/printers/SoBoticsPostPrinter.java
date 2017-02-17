package org.sobotics.guttenberg.printers;

import org.sobotics.guttenberg.finders.PlagFinder;
import org.sobotics.guttenberg.utils.PrintUtils;

/**
 * Created by bhargav.h on 20-Oct-16.
 */
public class SoBoticsPostPrinter implements PostPrinter {

    public final long roomId = 111347;

    @Override
    public String print(PlagFinder finder) {
    	
    	double score = Math.round(finder.getJaroScore()*100.0)/100.0;
    	String link = "https://stackoverflow.com/a/"+finder.getJaroAnswer().getAnswerID();
    	String targetLink = "https://stackoverflow.com/a/"+finder.getTargetAnswer().getAnswerID();
    	
    	String tag = finder.getTargetAnswer().getMainTag();
    	
    	String post;
    	
    	if (!finder.matchedPostIsRepost()) {
    		//plagiarism; different users
    		post = PrintUtils.printDescription()+"[tag:"+ tag +"] [Possible plagiarism]("+targetLink+") with a score of **"+ score +"**. [Original post]("+link+")";
    	} else {
    		//duplicated answer; same user
    		post = PrintUtils.printDescription()+"[tag:"+ tag +"] [Possible repost]("+targetLink+") with a score of **"+ score +"**. [Original post]("+link+")";
      }
        return post;
    }
}
