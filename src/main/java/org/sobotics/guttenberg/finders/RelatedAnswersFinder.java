package org.sobotics.guttenberg.finders;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sobotics.guttenberg.utils.ApiUtils;
import org.sobotics.guttenberg.utils.FilePathUtils;

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
    
    
    public List<JsonObject> fetchRelatedAnswers() {
        //The question_ids of all the new answers
        String idString = "";
        int n = 0;
        for (Integer id : this.targedIds) {
            idString += n++ == 0 ? id : ";"+id;
        }
        
        System.out.println(idString);
        
        Properties prop = new Properties();

        try{
            prop.load(new FileInputStream(FilePathUtils.loginPropertiesFile));
        }
        catch (IOException e){
            LOGGER.error("Could not load login.properties", e);
        }
        
        LOGGER.info("Fetch the linked/related questions...");
        
        try {
            JsonObject relatedQuestions = ApiUtils.getRelatedQuestionsByIds(idString, "stackoverflow", prop.getProperty("apikey", ""));
            LOGGER.info("Related done");
            JsonObject linkedQuestions = ApiUtils.getLinkedQuestionsByIds(idString, "stackoverflow", prop.getProperty("apikey", ""));
            LOGGER.info("linked done");
            
            String relatedIds = "";

            for (JsonElement question : relatedQuestions.get("items").getAsJsonArray()) {
                int id = question.getAsJsonObject().get("question_id").getAsInt();
                //System.out.println("Add: "+id);
                relatedIds += id+";";
            }
            
            for (JsonElement question : linkedQuestions.get("items").getAsJsonArray()) {
                int id = question.getAsJsonObject().get("question_id").getAsInt();
                //System.out.println("Add: "+id);
                relatedIds += id+";";
            }
            
            if (relatedIds.length() > 0) {
                relatedIds = relatedIds.substring(0, relatedIds.length()-1);
                
                List<JsonObject> relatedFinal = new ArrayList<JsonObject>();
                
                boolean hasMore = true;
                int i = 1;
                
                while (i <= 3) {
                    LOGGER.info("Fetch page "+i);
                    JsonObject relatedAnswers = ApiUtils.getAnswersToQuestionsByIdString(relatedIds, "stackoverflow", prop.getProperty("apikey", ""));
                    //System.out.println(relatedAnswers);
                    
                    for (JsonElement answer : relatedAnswers.get("items").getAsJsonArray()) {
                        JsonObject answerObject = answer.getAsJsonObject();
                        relatedFinal.add(answerObject);
                    }
                    
                    JsonElement hasMoreElement = relatedAnswers.get("has_more");
                    
                    if (hasMoreElement != null && hasMoreElement.getAsBoolean() == false)
                        break;
                    
                    i++;
                }
                                
                LOGGER.info("Collected "+relatedFinal.size()+" answers");
                return relatedFinal;
            } else {
                LOGGER.warn("No ids found!");
            }
            
            
        } catch (IOException e) {
            LOGGER.error("Error in RelatedAnswersFinder", e);
            return new ArrayList<JsonObject>();
        }
        
        
        
        return new ArrayList<JsonObject>();
    }
    
    
}
