package org.sobotics.guttenberg.finders;

import java.util.List;

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
	}
	
	public void collectData() {
		this.fetchRelatedQuestions();
	}
	
	private void fetchRelatedQuestions() {
		
	}
	
}
