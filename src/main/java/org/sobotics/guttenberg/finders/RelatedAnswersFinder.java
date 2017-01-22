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

/**
 * Collects all related answers in less API calls
 * */
public class RelatedAnswersFinder {
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
            e.printStackTrace();
        }
        
        System.out.println("Fetch the linked/related questions...");
        
        try {
			JsonObject relatedQuestions = ApiUtils.getRelatedQuestionsByIds(idString, "stackoverflow", prop.getProperty("apikey", ""));
			System.out.println("Related done");
			JsonObject linkedQuestions = ApiUtils.getLinkedQuestionsByIds(idString, "stackoverflow", prop.getProperty("apikey", ""));
			System.out.println("linked done");
			
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
				
				JsonObject relatedAnswers = ApiUtils.getAnswersToQuestionsByIdString(relatedIds, "stackoverflow", prop.getProperty("apikey", ""));
				//System.out.println(relatedAnswers);
				for (JsonElement answer : relatedAnswers.get("items").getAsJsonArray()) {
					JsonObject answerObject = answer.getAsJsonObject();
					relatedFinal.add(answerObject);
				}
				
				System.out.println("Collected "+relatedFinal.size()+" answers");
				return relatedFinal;
			} else {
				System.out.println("No ids found!");
			}
			
			
        } catch (IOException e) {
			System.out.println("Error in RelatedAnswersFinder");
			e.printStackTrace();
			return null;
		}
		
		
		
		return null;
	}
	
	
}
