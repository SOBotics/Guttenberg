package org.sobotics.guttenberg.printers;

import org.sobotics.guttenberg.finders.PlagFinder;

/**
 * Created by bhargav.h on 20-Oct-16.
 */
public class SoBoticsPostPrinter implements PostPrinter {

    public final long roomId = 111347;

    @Override
    public String print(PlagFinder finder) {
        
        double score = Math.round(finder.getJaroScore()*100.0)/100.0;
        String link = "https://stackoverflow.com/a/"+finder.getJaroAnswer().get("answer_id").getAsString();
        String targetLink = "https://stackoverflow.com/a/"+finder.getTargetAnswer().get("answer_id").getAsString();
        
        int userOne = finder.getJaroAnswer().get("owner").getAsJsonObject().get("user_id").getAsInt();
        int userTwo = finder.getTargetAnswer().get("owner").getAsJsonObject().get("user_id").getAsInt();
        
        String post;
        
        if (userOne != userTwo) {
            //plagiarism; different users
            post = "[ [Guttenberg](https://git.io/vMrPa) ] [Possible plagiarism]("+targetLink+") with a score of **"+ score +"**. [Original post]("+link+")";
        } else {
            //duplicated answer; same user
            String user = finder.getTargetAnswer().get("owner").getAsJsonObject().get("display_name").getAsString();
            post = "[ [Guttenberg](https://git.io/vMrPa) ] [Possible repost]("+targetLink+") with a score of **"+ score +"**. [Original post]("+link+")";
            //post = "[ [Guttenberg](https://git.io/vMrPa) ] User ["+user+"](http://stackoverflow.com/users/"+userTwo+") posted [this answer]("+targetLink+") after his [original answer]("+link+"). Score: **"+ score +"**";
        }
                
        return post;
    }
}
