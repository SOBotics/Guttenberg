package org.sobotics.guttenberg.printers;

import java.util.List;

import org.sobotics.guttenberg.finders.PlagFinder;

/**
 * Created by bhargav.h on 20-Oct-16.
 */
public class SoBoticsPostPrinter implements PostPrinter {

    public final long roomId = 111347;

    @Override
    public String print(PlagFinder finder) {
    	
    	double score = Math.round(finder.getJaroScore()*100.0)/100.0;
    	String link = finder.getJaroAnswer().get("link").getAsString();
    	String targetLink = "https://stackoverflow.com/a/"+finder.getTargetAnswer().get("answer_id").getAsString();
    	
    	String post = "[ [Guttenberg](https://git.io/vMrPa) ] [Possible plagiarism]("+targetLink+") with a score of **"+ score +"**. [Original post]("+link+")";
    	
        return post;
    }
}
