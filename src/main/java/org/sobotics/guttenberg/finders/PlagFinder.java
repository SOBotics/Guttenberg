package org.sobotics.guttenberg.finders;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import org.sobotics.guttenberg.utils.ApiUtils;
import org.sobotics.guttenberg.utils.FilePathUtils;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import info.debatty.java.stringsimilarity.JaroWinkler;

/**
 * Checks an answer for plagiarism by collecting similar answers from different sources.
 * */
public class PlagFinder {
	/**
	 * The answer to check
	 * */
	private JsonObject targetAnswer;
	
	/**
	 * A list of answers that are somehow related to targetAnswer.
	 * */
	private List<JsonObject> relatedAnswers;
	
	private double jaroScore = 0;
	
	private JsonObject jaroAnswer;
	
	/**
	 * Initializes the PlagFinder with an answer that should be checked for plagiarism
	 * */
	public PlagFinder(JsonObject jsonObject) {
		this.targetAnswer = jsonObject;
	}
	
	public void collectData() {
		this.relatedAnswers = new ArrayList<JsonObject>();
		this.fetchRelatedAnswers();
		System.out.println("RelatedAnswers: "+this.relatedAnswers.size());
	}
	
	private void fetchRelatedAnswers() {
		int targetId = this.targetAnswer.get("question_id").getAsInt();
		System.out.println("Target: "+targetId);
		Properties prop = new Properties();

        try{
            prop.load(new FileInputStream(FilePathUtils.loginPropertiesFile));
        }
        catch (IOException e){
            e.printStackTrace();
        }
		
		try {
			JsonObject relatedQuestions = ApiUtils.getRelatedQuestionsById(targetId, "stackoverflow", prop.getProperty("apikey", ""));
			JsonObject linkedQuestions = ApiUtils.getLinkedQuestionsById(targetId, "stackoverflow", prop.getProperty("apikey", ""));
			//System.out.println("Answer: "+relatedQuestions);
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
				
				
				System.out.println("Related question IDs: "+relatedIds);
				System.out.println("Fetching all answers...");
				
				JsonObject relatedAnswers = ApiUtils.getAnswersToQuestionsByIdString(relatedIds, "stackoverflow", prop.getProperty("apikey", ""));
				//System.out.println(relatedAnswers);
				for (JsonElement answer : relatedAnswers.get("items").getAsJsonArray()) {
					JsonObject answerObject = answer.getAsJsonObject();
					this.relatedAnswers.add(answerObject);
				}
				
				
			} else {
				System.out.println("No ids found!");
			}
			
		} catch (IOException e) {
			System.out.println("ERROR");
			e.printStackTrace();
		}
	}
	
	
	public JsonObject getMostSimilarAnswer() {
		String targetText = this.targetAnswer.get("body_markdown").getAsString();
		double highscore = 0;
		JsonObject closestMatch = this.targetAnswer;
		closestMatch.addProperty("jaro_winkler", 0);
				
		JaroWinkler jw = new JaroWinkler();
				
		for (JsonObject answer : this.relatedAnswers) {
			String answerBody = answer.get("body_markdown").getAsString();
			double jaroWinklerScore = jw.similarity(targetText, answerBody);
			if (highscore < jaroWinklerScore) {
				//new highscore
				highscore = jaroWinklerScore;
				answer.addProperty("jaro_winkler", jaroWinklerScore);
				closestMatch = answer;
			}
		}
		System.out.println("Score: "+highscore);
		
		this.jaroScore = highscore;
		
		return highscore > 0 ? closestMatch : null;
	}
	
	
	public JsonObject getTargetAnswer() {
		return this.targetAnswer;
	}
	
	public double getJaroScore() {
		return this.jaroScore;
	}
	
	public JsonObject getJaroAnswer() {
		return this.jaroScore > 0.7 ? this.jaroAnswer : null;
	}
	
}
