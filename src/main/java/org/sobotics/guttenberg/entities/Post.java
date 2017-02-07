package org.sobotics.guttenberg.entities;

import com.google.gson.JsonObject;
import org.jetbrains.annotations.NotNull;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.time.Instant;
import java.util.Arrays;

/**
 * Created by bhargav.h on 11-Sep-16.
 */
public class Post {
    private String title;
    private Instant answerCreationDate;
    private Integer answerID;
    private Integer questionID;
    private String body;
    private String bodyMarkdown;
    private SOUser answerer;
    private double score = 0;

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public Instant getAnswerCreationDate() {
        return answerCreationDate;
    }

    public void setAnswerCreationDate(Instant answerCreationDate) {
        this.answerCreationDate = answerCreationDate;
    }

    public Integer getAnswerID() {
        return answerID;
    }

    public void setAnswerID(Integer answerID) {
        this.answerID = answerID;
    }

    public Integer getQuestionID() {
        return questionID;
    }

    public void setQuestionID(Integer questionID) {
        this.questionID = questionID;
    }

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }

    public String getBodyMarkdown() {
        return bodyMarkdown;
    }

    public void setBodyMarkdown(String bodyMarkdown) {
        this.bodyMarkdown = bodyMarkdown;
    }

    public SOUser getAnswerer() {
        return answerer;
    }

    public void setAnswerer(SOUser answerer) {
        this.answerer = answerer;
    }

    @Override
    public String toString() {

        JsonObject json = getJson();
        return json.toString();
    }

    @NotNull
    private JsonObject getJson() {
        JsonObject json = new JsonObject();

        json.addProperty("title" , title );
        json.addProperty("answerCreationDate" , answerCreationDate.toString());
        json.addProperty("answerID" , answerID);
        json.addProperty("body" , body );
        json.addProperty("bodyMarkdown" , bodyMarkdown);
        json.add("answerer" , answerer.getJson());
        return json;

    }
    
    public String getCodeOnly() {
    	String codeOnly = "";
    	
    	Document doc = Jsoup.parse(body);
    	Elements pres = doc.getElementsByTag("pre");
    	
    	Elements codes = new Elements();
    	
    	for (Element pre: pres) {
    		Elements codeInPre = pre.getElementsByTag("code");
    		for (Element code : codeInPre) {
    			codes.add(code);
    		}
    	}
    	
    	for (Element code : codes) {
    		codeOnly += code.html();
    	}
    	
		return codeOnly;
    }
    
    public String getPlaintext() {
    	Document doc = Jsoup.parse(body);
    	Elements pres = doc.getElementsByClass("prettyprint");
    	for (Element pre: pres) {
    		pre.remove();
    	}
    	
    	
    	return doc.text();
    }
    
    public void setScore(double newScore) {
    	this.score = newScore;
    }
    
    public double getScore() {
    	return this.score;
    }
}
