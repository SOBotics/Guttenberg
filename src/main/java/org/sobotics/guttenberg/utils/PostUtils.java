package org.sobotics.guttenberg.utils;

import java.time.Instant;
import java.util.stream.StreamSupport;

import org.sobotics.guttenberg.entities.Post;
import org.sobotics.guttenberg.entities.SOUser;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

public class PostUtils {
	public static Post getPost(JsonObject answer){

        Post np = new Post();

        JsonObject answererJSON = answer.get("owner").getAsJsonObject();

        np.setAnswerCreationDate(Instant.ofEpochSecond(answer.get("creation_date").getAsInt()));
        np.setAnswerID(answer.get("answer_id").getAsInt());
        np.setQuestionID(answer.get("question_id").getAsInt());
        np.setBody(answer.get("body").getAsString());
        np.setBodyMarkdown(JsonUtils.escapeHtmlEncoding(answer.get("body_markdown").getAsString()));

        SOUser answerer = new SOUser();

        try{
            answerer.setReputation(answererJSON.get("reputation").getAsLong());
            answerer.setUsername(JsonUtils.escapeHtmlEncoding(answererJSON.get("display_name").getAsString()));
            answerer.setUserType(answererJSON.get("user_type").getAsString());
            answerer.setUserId(answererJSON.get("user_id").getAsInt());
        }
        catch (Exception e){
            System.out.println("ANSWERER"+answererJSON);
            e.printStackTrace();
        }

        np.setAnswerer(answerer);

        return np;

    }
	
	/**
	 * Splits a post into code, plaintext and quotes
	 * 
	 * @author ArtOfCode
	 * */
	public static JsonObject separateBodyParts(Post post) {
		JsonObject result = new JsonObject();
        String markdown = post.getBodyMarkdown();
        String[] paragraphs = markdown.split("\\n{2,}");
        
        String plain = "", code = "", quote = "";
        for (String paragraph : paragraphs) {
            if (paragraph.trim().charAt(0) == '>') {
                quote += paragraph + "\n";
            }
            else if (paragraph.startsWith("    ")) {
                code += paragraph + "\n";
            }
            else {
                plain += paragraph + "\n";
            }
        }
        
        result.addProperty("body_plain", plain);
        result.addProperty("body_code", code);
        result.addProperty("body_quote", quote);
        
        return result;
    }
}
