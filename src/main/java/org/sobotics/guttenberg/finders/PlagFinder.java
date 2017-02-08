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
import org.sobotics.guttenberg.utils.ApiUtils;
import org.sobotics.guttenberg.utils.FilePathUtils;
import org.sobotics.guttenberg.utils.PostUtils;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import info.debatty.java.stringsimilarity.JaroWinkler;

/**
 * Checks an answer for plagiarism by collecting similar answers from different sources.
 * */
public class PlagFinder {
    
    private static final Logger LOGGER = LoggerFactory.getLogger(PlagFinder.class);
    
    /**
     * The answer to check
     * */
    private Post targetAnswer;
    
    /**
     * A list of answers that are somehow related to targetAnswer.
     * */
    public List<Post> relatedAnswers;
    
    private double jaroScore = 0;
    
    private Post jaroAnswer;
    
    /**
     * Initializes the PlagFinder with an answer that should be checked for plagiarism
     * */
    public PlagFinder(JsonObject jsonObject) {
        this.targetAnswer = PostUtils.getPost(jsonObject);
        this.relatedAnswers = new ArrayList<Post>();
    }
    
    public PlagFinder(Post post) {
    	this.targetAnswer = post;
    	this.relatedAnswers = new ArrayList<Post>();
    }
    
    public PlagFinder(Post target, List<Post> related) {
        this.targetAnswer = target;
        this.relatedAnswers = related;
    }
    
    public void collectData() {
        this.relatedAnswers = new ArrayList<Post>();
        this.fetchRelatedAnswers();
        LOGGER.info("RelatedAnswers: "+this.relatedAnswers.size());
    }
    
    private void fetchRelatedAnswers() {
        int targetId = this.targetAnswer.getQuestionID();
        int targetAnswerId = this.targetAnswer.getAnswerID();
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
                    Post answerPost = PostUtils.getPost(answerObject);
                    
                    int answerId = answerPost.getAnswerID();
                    
                    if (answerId != targetAnswerId)
                        this.relatedAnswers.add(answerPost);
                }
                
                
            } else {
                LOGGER.warn("No ids found!");
            }
            
        } catch (IOException e) {
            LOGGER.error("ERROR", e);
            return;
        }
    }
    
    
    public Post getMostSimilarAnswer() {
        String targetBodyMarkdown = this.targetAnswer.getBodyMarkdown();
        String targetCodeOnly = this.targetAnswer.getCodeOnly();
        String targetPlaintext = this.targetAnswer.getPlaintext();
        Instant targetDate = this.targetAnswer.getAnswerCreationDate();
        double highscore = 0;
        Post closestMatch = this.targetAnswer;
                
        JaroWinkler jw = new JaroWinkler();
                
        for (Post answer : this.relatedAnswers) {
            String answerBodyMarkdown = answer.getBodyMarkdown();
            String answerCodeOnly = answer.getCodeOnly();
            String answerPlaintext = answer.getPlaintext();
            Instant answerDate = answer.getAnswerCreationDate();
            //double jaroWinklerScore = jw.similarity(targetText, answerBody);
            
            double jwBodyMarkdown = jw.similarity(targetBodyMarkdown, answerBodyMarkdown) * 1;
            double jwCodeOnly = jw.similarity(targetCodeOnly, answerCodeOnly) * 3;
            double jwPlaintext = jw.similarity(answerPlaintext, targetPlaintext) * 1;
            
            //LOGGER.info("bodyMarkdown: "+jwBodyMarkdown+"; codeOnly: "+jwCodeOnly+"; plaintext: "+jwPlaintext);
            
            /*double jaroWinklerScore = (jwBodyMarkdown > 0 ? jwBodyMarkdown : 1)*0.92 
            		* (jwCodeOnly > 0 ? jwCodeOnly : 1)*1.0 
            		* (jwPlaintext > 0 ? jwPlaintext : 1)*0.95;
            */
            double usedScores = (jwBodyMarkdown > 0 ? 1 : 0)
            		+ (jwCodeOnly > 0 ? 3 : 0)
            		+ (jwPlaintext > 0 ? 1 : 0);
            double jaroWinklerScore = ((jwBodyMarkdown > 0 ? jwBodyMarkdown : 0)*0.9 
            		+ (jwCodeOnly > 0 ? jwCodeOnly : 0)*1.0 
            		+ (jwPlaintext > 0 ? jwPlaintext : 0)*0.95) / usedScores;
            
            
            //LOGGER.info("Score: "+jaroWinklerScore+"\nUsed scores: "+usedScores+"\nbodyMarkdown: "+jwBodyMarkdown+"\ncodeOnly: "+jwCodeOnly+"\nplaintext: "+jwPlaintext);
            
            if (jwBodyMarkdown > 0.9)
            	jaroWinklerScore = jwBodyMarkdown;
            
            if (highscore < jaroWinklerScore && targetDate.isAfter(answerDate)) {
                //new highscore
                highscore = jaroWinklerScore;
                answer.setScore(highscore);
                closestMatch = answer;
            }
        }
        LOGGER.info("Score: "+highscore);
        
        this.jaroScore = highscore;
        this.jaroAnswer = closestMatch;
        
        return highscore > 0 ? closestMatch : null;
    }
    
    
    public Post getTargetAnswer() {
        return this.targetAnswer;
    }
    
    public Integer getTargetAnswerId() {
        return this.targetAnswer.getAnswerID();
    }
    
    public double getJaroScore() {
        return this.jaroScore;
    }
    
    public Post getJaroAnswer() {
        return this.jaroAnswer;
        //return this.jaroScore > 0.7 ? this.jaroAnswer : null;
    }
    
}
