package org.sobotics.guttenberg.finders;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import org.sobotics.guttenberg.utils.ApiUtils;
import org.sobotics.guttenberg.utils.FilePathUtils;
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
		List<String> linesToSearch = StringUtils.searchableLinesOfBodyMarkdown(this.targetAnswer.get("body_markdown").getAsString());
		
		//System.out.println("Plain body:\n" + linesToSearch);
		
		try {
			Properties prop = new Properties();

	        try{
	            prop.load(new FileInputStream(FilePathUtils.loginPropertiesFile));
	        }
	        catch (IOException e){
	            e.printStackTrace();
	        }
	        
	        String apiKey = prop.getProperty("apikey", "");
			
			for (String line : linesToSearch) {
				if (line.length() > 9) {
					System.out.println("Search for: " + line);
					
					JsonObject results = ApiUtils.getSearchExcerpts(line, "stackoverflow", apiKey);
					
					System.out.println("Search done");
					
					if (results.getAsJsonArray().size() == 0) return;
					
					System.out.println("Fetched search results:\n"+results.get("items").getAsJsonArray().size());
					int targetAnswerId = this.targetAnswer.get("answer_id").getAsInt();
					System.out.println("Target: " + targetAnswerId);
					
					for (JsonElement item : results.getAsJsonArray()) {
						System.out.println("point1");
						JsonObject object = item.getAsJsonObject();
						System.out.println("point2");
						if (object.get("answer_id").getAsInt() != targetAnswerId) {
							System.out.println("Add: "+ object.get("answer_id").getAsInt());
							this.relatedAnswers.add(object);
						} else {
							System.out.println("Did not add: " + object.get("answer_id").getAsInt());
						}
					}
				} else {
					System.out.println("Line too short");
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
