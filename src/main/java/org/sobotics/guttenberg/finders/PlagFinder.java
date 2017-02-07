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

import info.debatty.java.stringsimilarity.JaroWinkler;
import org.sobotics.guttenberg.utils.PostUtils;

/**
 * Checks an answer for plagiarism by collecting similar answers from different sources.
 * */
public class PlagFinder {
    
    private static final Logger LOGGER = LoggerFactory.getLogger(PlagFinder.class);
    
    /**
     * The answer to check
     * */
    private JsonObject targetAnswer;
    
    /**
     * A list of answers that are somehow related to targetAnswer.
     * */
    public List<JsonObject> relatedAnswers;
    
    private double jaroScore = 0;
    
    private JsonObject jaroAnswer;
    
    /**
     * Initializes the PlagFinder with an answer that should be checked for plagiarism
     * */
    public PlagFinder(JsonObject jsonObject) {
        this.targetAnswer = jsonObject;
        this.relatedAnswers = new ArrayList<JsonObject>();
    }
    
    public PlagFinder(JsonObject target, List<JsonObject> related) {
        this.targetAnswer = target;
        this.relatedAnswers = related;
    }
    
    public void collectData() {
        this.relatedAnswers = new ArrayList<JsonObject>();
        this.fetchRelatedAnswers();
        LOGGER.info("RelatedAnswers: "+this.relatedAnswers.size());
    }
    
    private void fetchRelatedAnswers() {
        int targetId = this.targetAnswer.get("question_id").getAsInt();
        int targetAnswerId = this.targetAnswer.get("answer_id").getAsInt();
        LOGGER.info("Target: "+targetId);
        Properties prop = new Properties();

        try{
            prop.load(new FileInputStream(FilePathUtils.loginPropertiesFile));
        }
        catch (IOException e){
            LOGGER.error("Could not load login.properties", e);
            return;
        }
        
        try {
            JsonObject relatedQuestions = ApiUtils.getRelatedQuestionsById(targetId, "stackoverflow", prop.getProperty("apikey", ""));
            JsonObject linkedQuestions = ApiUtils.getLinkedQuestionsById(targetId, "stackoverflow", prop.getProperty("apikey", ""));
            //System.out.println("Answer: "+relatedQuestions);
            String relatedIds = targetId+";";

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
                
                
                LOGGER.info("Related question IDs: "+relatedIds);
                LOGGER.info("Fetching all answers...");
                
                JsonObject relatedAnswers = ApiUtils.getAnswersToQuestionsByIdString(relatedIds, "stackoverflow", prop.getProperty("apikey", ""));
                //System.out.println(relatedAnswers);
                for (JsonElement answer : relatedAnswers.get("items").getAsJsonArray()) {
                    JsonObject answerObject = answer.getAsJsonObject();
                    
                    int answerId = answerObject.get("answer_id").getAsInt();
                    
                    if (answerId != targetAnswerId)
                        this.relatedAnswers.add(answerObject);
                }
                
                
            } else {
                LOGGER.warn("No ids found!");
            }
            
        } catch (IOException e) {
            LOGGER.error("ERROR", e);
            return;
        }
    }
    
    
    public JsonObject getMostSimilarAnswer() {
        PostUtils.separateBodyParts(this.targetAnswer);
        String targetPlain = this.targetAnswer.get("body_plain").getAsString();
        String targetCode = this.targetAnswer.get("body_code").getAsString();
        String targetQuote = this.targetAnswer.get("body_quote").getAsString();
        
        int targetDate = this.targetAnswer.get("creation_date").getAsInt();
        double highscore = 0;
        JsonObject closestMatch = this.targetAnswer;
        closestMatch.addProperty("jaro_winkler", 0);
                
        JaroWinkler jw = new JaroWinkler();
                
        for (JsonObject answer : this.relatedAnswers) {
            PostUtils.separateBodyParts(answer);
            String answerPlain = answer.get("body_plain").getAsString();
            String answerCode = answer.get("body_code").getAsString();
            String answerQuote = answer.get("body_quote").getAsString();
            
            int answerDate = answer.get("last_activity_date").getAsInt();
            
            double plainSimilarity = jw.similarity(targetPlain, answerPlain);
            double codeSimilarity = jw.similarity(targetCode, answerCode);
            double quoteSimilarity = jw.similarity(targetQuote, answerQuote);
            
            double jaroWinklerScore = (plainSimilarity * 0.7) + (quoteSimilarity * 0.5) + codeSimilarity;
            
            if (highscore < jaroWinklerScore && targetDate > answerDate) {
                //new highscore
                highscore = jaroWinklerScore;
                answer.addProperty("jaro_winkler", jaroWinklerScore);
                closestMatch = answer;
            }
        }
        LOGGER.info("Score: "+highscore);
        
        this.jaroScore = highscore;
        this.jaroAnswer = closestMatch;
        
        return highscore > 0 ? closestMatch : null;
    }
    
    
    public JsonObject getTargetAnswer() {
        return this.targetAnswer;
    }
    
    public Integer getTargetAnswerId() {
        return this.targetAnswer.get("answer_id").getAsInt();
    }
    
    public double getJaroScore() {
        return this.jaroScore;
    }
    
    public JsonObject getJaroAnswer() {
        return this.jaroAnswer;
        //return this.jaroScore > 0.7 ? this.jaroAnswer : null;
    }
    
}
