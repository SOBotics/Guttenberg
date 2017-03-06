package org.sobotics.guttenberg.commands;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sobotics.guttenberg.finders.PlagFinder;
import org.sobotics.guttenberg.services.RunnerService;
import org.sobotics.guttenberg.utils.ApiUtils;
import org.sobotics.guttenberg.utils.CommandUtils;
import org.sobotics.guttenberg.utils.FilePathUtils;
import org.sobotics.guttenberg.clients.Guttenberg;

import com.google.gson.JsonObject;

import fr.tunaki.stackoverflow.chat.Message;
import fr.tunaki.stackoverflow.chat.Room;

public class Check implements SpecialCommand {

    private static final Logger LOGGER = LoggerFactory.getLogger(Check.class);

    private final Message message;

    public Check(Message message) {
        this.message = message;
    }
    
    @Override
    public boolean validate() {
        return CommandUtils.checkForCommand(message.getPlainContent(),"check");
    }

    @Override
    public void execute(Room room, RunnerService instance) {
        String word = CommandUtils.extractData(message.getPlainContent()).trim();
        Integer returnValue = 0;

        if(word.contains(" ")){
            String parts[] = word.split(" ");
        }
        if(word.contains("/"))
        {                
            word = CommandUtils.getAnswerId(word);
        }

        Integer answerId = new Integer(word);

        Properties prop = new Properties();

        try{
            prop.load(new FileInputStream(FilePathUtils.loginPropertiesFile));
        }
        catch (IOException e){
            LOGGER.error("Could not read login.properties", e);
        }


        try {
            JsonObject answer = ApiUtils.getAnswerDetailsById(answerId, "stackoverflow", prop.getProperty("apikey", ""));
            JsonObject target = answer.get("items").getAsJsonArray().get(0).getAsJsonObject();
            PlagFinder finder = new PlagFinder(target);
            finder.collectData();
            finder.getMostSimilarAnswer();
            double score = Math.round(finder.getJaroScore()*100.0)/100.0;
            String link = "http://stackoverflow.com/a/"+finder.getJaroAnswer().getAnswerID();

            if (score > 0) {
                String reply = "The closest match with a score of **"+score+"** is [this post]("+link+").";
                room.replyTo(message.getId(), reply);
            } else {
                room.replyTo(message.getId(), "There are no similar posts.");
            }  
        } catch (IOException e) {
            LOGGER.error("ERROR", e);
        }
    }

    @Override
    public String description() {
        return "Checks a post for plagiarism: check <answer url>";
    }

    @Override
    public String name() {
        return "check";
    }

}
