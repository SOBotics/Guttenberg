package org.sobotics.guttenberg.finders;

import java.io.FileInputStream;
import java.io.IOException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import javax.json.JsonObject;

import org.sobotics.guttenberg.utils.ApiUtils;
import org.sobotics.guttenberg.utils.FilePathUtils;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;

/**
 * Fetches the most recent answers
 * */
public class NewAnswersFinder {	
	
	public static JsonArray findRecentAnswers() {
		//long unixTime = (long)System.currentTimeMillis()/1000;
		Instant time = Instant.now().minusSeconds(59+1);
		
		Properties prop = new Properties();

        try{
            prop.load(new FileInputStream(FilePathUtils.loginPropertiesFile));
        }
        catch (IOException e){
            e.printStackTrace();
        }
		
		try {
			com.google.gson.JsonObject apiResult = ApiUtils.getFirstPageOfAnswers(time, "stackoverflow", prop.getProperty("apikey", ""));
			//fetched answers
			
			JsonArray items = apiResult.get("items").getAsJsonArray();
			//System.out.println(items);
			System.out.println("findRecentAnswers() done with "+items.size()+" items");
			
			return items;
			
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return null;
	}
	
}
