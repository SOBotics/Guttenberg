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
import org.sobotics.guttenberg.entities.PostMatch;
import org.sobotics.guttenberg.reasonlists.ReasonList;
import org.sobotics.guttenberg.reasonlists.SOBoticsReasonList;
import org.sobotics.guttenberg.reasons.Reason;
import org.sobotics.guttenberg.services.ApiService;
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
                
                JsonObject relatedAnswers = ApiService.defaultService.getAnswersToQuestionsByIdString(relatedIds);
                //JsonObject relatedAnswers = ApiUtils.getAnswersToQuestionsByIdString(relatedIds, "stackoverflow", prop.getProperty("apikey", ""));
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
    
    /*
    public Post getMostSimilarAnswer() {
    	LOGGER.info("Calculating...");
        String targetBodyMarkdown = this.targetAnswer.getBodyMarkdown();
        String targetCodeOnly = this.targetAnswer.getCodeOnly();
        String targetPlaintext = this.targetAnswer.getPlaintext();
        String targetQuotes = this.targetAnswer.getQuotes();
        Instant targetDate = this.targetAnswer.getAnswerCreationDate();
        double highscore = 0;
        Post closestMatch = this.targetAnswer;
        Properties quantifiers = new Properties();
        try {
        	quantifiers.load(new FileInputStream(FilePathUtils.generalPropertiesFile));
        } catch (IOException e) {
        	LOGGER.warn("Could not load quantifiers from general.properties. Using hardcoded", e);
        }
        
        double quantifierBodyMarkdown = 1;
        double quantifierCodeOnly = 1;
        double quantifierPlaintext = 1;
        double quantifierQuotes = 1;
        
        try {
        	quantifierBodyMarkdown = new Double(quantifiers.getProperty("quantifierBodyMarkdown", "1"));
        	quantifierCodeOnly = new Double(quantifiers.getProperty("quantifierCodeOnly", "1"));
        	quantifierPlaintext = new Double(quantifiers.getProperty("quantifierPlaintext", "1"));
        	quantifierQuotes = new Double(quantifiers.getProperty("quantifierQuotes", "1"));
        } catch (Throwable e) {
        	LOGGER.warn("Using hardcoded value", e);
        }
                
        JaroWinkler jw = new JaroWinkler();
                
        for (Post answer : this.relatedAnswers) {
            String answerBodyMarkdown = answer.getBodyMarkdown();
            String answerCodeOnly = answer.getCodeOnly();
            String answerPlaintext = answer.getPlaintext();
            String answerQuotes = answer.getQuotes();
            Instant answerDate = answer.getAnswerCreationDate();
            
            double jwBodyMarkdown = jw.similarity(targetBodyMarkdown, answerBodyMarkdown)
            		* quantifierBodyMarkdown;
            double jwCodeOnly = answerCodeOnly != null ? ( jw.similarity(targetCodeOnly, answerCodeOnly)
            		* quantifierCodeOnly) : 0;
            double jwPlaintext = answerPlaintext != null ? ( jw.similarity(answerPlaintext, targetPlaintext)
            		* quantifierPlaintext) : 0;
            double jwQuotes = answerQuotes != null ? ( jw.similarity(answerQuotes, targetQuotes)
            		* quantifierQuotes) : 0;
            
            //LOGGER.info("bodyMarkdown: "+jwBodyMarkdown+"; codeOnly: "+jwCodeOnly+"; plaintext: "+jwPlaintext);
            
            double usedScores = (jwBodyMarkdown > 0 ? quantifierBodyMarkdown : 0)
            		+ (jwCodeOnly > 0 ? quantifierCodeOnly : 0)
            		+ (jwPlaintext > 0 ? quantifierPlaintext : 0)
            		+ (jwQuotes > 0 ? quantifierQuotes : 0);
            double jaroWinklerScore = ((jwBodyMarkdown > 0 ? jwBodyMarkdown : 0) 
            		+ (jwCodeOnly > 0 ? jwCodeOnly : 0)
            		+ (jwPlaintext > 0 ? jwPlaintext : 0)
            		+ (jwQuotes > 0 ? jwQuotes : 0)) / usedScores;
            
            
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
    }*/
    
    
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
    
    public boolean matchedPostIsRepost() {
    	return this.targetAnswer.getAnswerer().getUserId() == this.jaroAnswer.getAnswerer().getUserId();
    }
    
    public List<PostMatch> matchesForReasons() {
    	List<PostMatch> matches = new ArrayList<PostMatch>();
    	//get reasonlist
    	ReasonList reasonList = new SOBoticsReasonList(this.targetAnswer, this.relatedAnswers);
    	
    	for (Reason reason : reasonList.reasons()) {
    		//check if the reason applies
    		if (reason.check() == true) {
    			//if yes, add (new) posts to the list
    			
    			//get matched posts for that reason
    			List<Post> matchedPosts = reason.matchedPosts();
    			
    			if (matchedPosts == null)
    				return null;
    			
    			for (Post post : matchedPosts) {
    				//check if the new post is already part of a PostMatch
    				boolean alreadyExists = false;
    				int id = post.getAnswerID();
    				int i = 0;
    				for (PostMatch existingMatch : matches) {
    					if (existingMatch.getOriginal().getAnswerID() == id) {
    						//if it exists, add the new reason
    						alreadyExists = true;
    						if (!existingMatch.reasons.contains(reason.description())) {
    							existingMatch.reasons.add(reason.description());
    						}
    						matches.set(i, existingMatch);
    					}
    					
    					i++;
    				}
    				
    				//if it doesn't exist yet, add it
    				if (!alreadyExists) {
    					PostMatch newMatch = new PostMatch(this.targetAnswer, post);
    					newMatch.reasons.add(reason.description());
    					matches.add(newMatch);
    				}
    			}
    		}
    	}
    	
    	return matches;
    }
    
}
