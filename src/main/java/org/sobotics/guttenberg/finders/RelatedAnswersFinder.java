package org.sobotics.guttenberg.finders;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sobotics.guttenberg.entities.Post;
import org.sobotics.guttenberg.services.ApiService;
import org.sobotics.guttenberg.utils.FilePathUtils;
import org.sobotics.guttenberg.utils.PostUtils;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

/**
 * Collects all related answers in less API calls
 * */
public class RelatedAnswersFinder {
    
    private static final Logger LOGGER = LoggerFactory.getLogger(RelatedAnswersFinder.class);
    
    /**
     * The question_ids of the targeted answers
     * */
    List<Integer> targedIds;
    
    public RelatedAnswersFinder(List<Integer> ids) {
        this.targedIds = ids;
    }
    
    
    public List<Post> fetchRelatedAnswers() {
        //The question_ids of all the new answers
        String idString = "";
        int n = 0;
        for (Integer id : this.targedIds) {
            idString += n++ == 0 ? id : ";"+id;
        }
        
        System.out.println(idString);
        
        if (idString.length() < 2)
        	return new ArrayList<Post>();
        
        Properties prop = new Properties();

        try{
            prop.load(new FileInputStream(FilePathUtils.loginPropertiesFile));
        }
        catch (IOException e){
            LOGGER.error("Could not load login.properties", e);
        }
        
        LOGGER.debug("Fetching the linked/related questions...");
        
        try {
        	JsonObject relatedQuestions = ApiService.defaultService.getRelatedQuestionsByIds(idString);
            LOGGER.debug("Related done");
            JsonObject linkedQuestions = ApiService.defaultService.getLinkedQuestionsByIds(idString);
            LOGGER.debug("linked done");
            
            String relatedIds = "";

            for (JsonElement question : relatedQuestions.get("items").getAsJsonArray()) {
                int id = question.getAsJsonObject().get("question_id").getAsInt();
                LOGGER.trace("Add: "+id);
                relatedIds += id+";";
            }
            
            for (JsonElement question : linkedQuestions.get("items").getAsJsonArray()) {
                int id = question.getAsJsonObject().get("question_id").getAsInt();
                LOGGER.trace("Add: "+id);
                relatedIds += id+";";
            }
            
            if (relatedIds.length() > 0) {
                relatedIds = relatedIds.substring(0, relatedIds.length()-1);
                
                List<JsonObject> relatedFinal = new ArrayList<JsonObject>();
                
                int i = 1;
                
                while (i <= 2) {
                	LOGGER.debug("Fetch page "+i);
                	JsonObject relatedAnswers = ApiService.defaultService.getAnswersToQuestionsByIdString(relatedIds, i);
                    LOGGER.trace("Related answers:\n" + relatedAnswers);
                    
                    for (JsonElement answer : relatedAnswers.get("items").getAsJsonArray()) {
                        JsonObject answerObject = answer.getAsJsonObject();
                        relatedFinal.add(answerObject);
                    }
                    
                    JsonElement hasMoreElement = relatedAnswers.get("has_more");
                    
                    if (hasMoreElement != null && hasMoreElement.getAsBoolean() == false)
                    	break;
                    
                    i++;
                }
                
                List<Post> relatedPosts = new ArrayList<Post>();
                for (JsonElement item : relatedFinal) {
                	Post post = PostUtils.getPost(item.getAsJsonObject());
                	relatedPosts.add(post);
                }
                                
                LOGGER.debug("Collected "+relatedFinal.size()+" answers");
                
                return relatedPosts;
            } else {
                LOGGER.warn("No ids found!");
            }
            
            
        } catch (IOException e) {
            LOGGER.error("Error in RelatedAnswersFinder", e);
            return new ArrayList<Post>();
        }
        
        
        
        return new ArrayList<Post>();
    }
    
    
}
