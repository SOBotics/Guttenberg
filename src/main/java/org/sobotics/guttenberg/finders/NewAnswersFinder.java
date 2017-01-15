package org.sobotics.guttenberg.finders;

import java.io.IOException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import javax.json.JsonObject;

import org.sobotics.guttenberg.utils.ApiUtils;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;

/**
 * Fetches the most recent answers
 * */
public class NewAnswersFinder {	
	
	public static JsonArray findRecentAnswers() {
		//long unixTime = (long)System.currentTimeMillis()/1000;
		Instant time = Instant.now().minusSeconds(59+1);
		try {
			com.google.gson.JsonObject apiResult = ApiUtils.getFirstPageOfAnswers(time, "stackoverflow", "");
			//fetched answers
			
			JsonArray items = apiResult.get("items").getAsJsonArray();
			System.out.println(items);
			System.out.println("done");
			
			return items;
			
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return null;
	}
	
}
