package org.sobotics.guttenberg.finders;

import java.io.FileInputStream;
import java.io.IOException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sobotics.guttenberg.entities.Post;
import org.sobotics.guttenberg.services.ApiService;
import org.sobotics.guttenberg.utils.FilePathUtils;
import org.sobotics.guttenberg.utils.PostUtils;
import org.sobotics.guttenberg.utils.StatusUtils;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

/**
 * Fetches the most recent answers
 * */
public class NewAnswersFinder {    
    
    private static final Logger LOGGER = LoggerFactory.getLogger(NewAnswersFinder.class);
    
    public static List<Post> findRecentAnswers() {
        //long unixTime = (long)System.currentTimeMillis()/1000;
        Instant time = Instant.now().minusSeconds(59+1);
        
        //Use time of last execution-start to really get ALL answers
        if (StatusUtils.lastSucceededExecutionStarted != null)
            time = StatusUtils.lastSucceededExecutionStarted;
        
        
        Properties prop = new Properties();

        try{
            prop.load(new FileInputStream(FilePathUtils.loginPropertiesFile));
        }
        catch (IOException e){
            LOGGER.error("Could not load login.properties", e);
            return new ArrayList<Post>();
        }
        
        try {
            JsonObject apiResult = ApiService.defaultService.getFirstPageOfAnswers(time);
        	
        	//com.google.gson.JsonObject apiResult = ApiUtils.getFirstPageOfAnswers(time, "stackoverflow", prop.getProperty("apikey", ""));
            //fetched answers
            
            JsonArray items = apiResult.get("items").getAsJsonArray();
            //System.out.println(items);
            LOGGER.info("findRecentAnswers() done with "+items.size()+" items");
            List<Post> posts = new ArrayList<Post>();
            
            for (JsonElement item : items) {
            	Post post = PostUtils.getPost(item.getAsJsonObject());
            	posts.add(post);
            }
            
            return posts;
            
            
        } catch (IOException e) {
            LOGGER.error("Could not load recent answers", e);
            return new ArrayList<Post>();
        }
    }
    
}
