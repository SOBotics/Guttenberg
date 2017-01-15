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
    	
    	double score = finder.getJaroScore();
    	System.out.println("p1");
    	//String link = finder.getJaroAnswer().get("link").getAsString();
    	
    	//String post = "[Possible plagiarism]("+link+") with a score of **"+ score +"**";
    	String post = "Possible plagiarism with a score of **"+ score +"**";
    	
        return post;
    }
}
