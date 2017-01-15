package org.sobotics.guttenberg.finders;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.sobotics.guttenberg.utils.ApiUtils;
import org.sobotics.guttenberg.utils.StringUtils;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

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
	
	/**
	 * Initializes the PlagFinder with an answer that should be checked for plagiarism
	 * */
	public PlagFinder(JsonObject jsonObject) {
		this.targetAnswer = jsonObject;
		this.relatedAnswers = new ArrayList<JsonObject>();
	}
	
	public void collectData() {
		this.fetchSoSearchResults();
		
		System.out.println("There are now "+ this.relatedAnswers.size() +" related answers.");
	}
	
	private void fetchRelatedQuestions() {
		
	}
	
	/**
	 * Fetches search-results on SO
	 * */
	private void fetchSoSearchResults() {
		String plainBody = StringUtils.plainTextFromHtml(this.targetAnswer.get("body").getAsString());
		System.out.println("Plain body:\n" + plainBody);
		
		try {
			JsonObject results = ApiUtils.getSearchExcerpts(plainBody, "stackoverflow", "");
			System.out.println("Fetched search results:\n"+results);
			int targetAnswerId = this.targetAnswer.get("answer_id").getAsInt();
			
			for (JsonElement item : results.getAsJsonArray()) {
				JsonObject object = item.getAsJsonObject();
				if (object.get("answer_id").getAsInt() != targetAnswerId) {
					this.relatedAnswers.add(object);
				}
			}
			
		} catch (IOException e) {
			System.out.println("An error occurred!");
			e.printStackTrace();
		}
	}
	
	public JsonObject getTargetAnswer() {
		return this.targetAnswer;
	}
	
}
