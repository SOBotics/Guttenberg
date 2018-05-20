package org.sobotics.guttenberg.entities;

import java.io.FileInputStream;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sobotics.guttenberg.utils.FilePathUtils;
import org.sobotics.guttenberg.utils.FileUtils;

/**
 * Represents a plagiarized post
 * */
public class PostMatch implements Comparable<PostMatch>{
	
	private static final Logger LOGGER = LoggerFactory.getLogger(PostMatch.class);
	
	private Post target;
	private Post original;
	private List<String> reasons = new ArrayList<String>();
	private double totalScore = 0;
	private String copyPastorReasonString = "";
	
	public PostMatch(Post targetPost, Post originalPost) {
		this.target = targetPost;
		this.original = originalPost;
	}
	
	public Post getTarget() {
		return this.target;
	}
	
	public Post getOriginal() {
		return this.original;
	}
	
	public void addReason(String reason, double score) {
		if (!reasons.contains(reason)) {
			//add reason
			this.reasons.add(reason);
			//add score
			this.totalScore += score;
		}
	}
	
	/**
	 * Adds the reason to the String sent to CopyPastor
	 * (can't be called by addReason() because this would include the score in the description)
	 * */
	public void addReasonToCopyPastorString(String reason, double score) {
		LOGGER.debug("Adding reason \"" + reason + "\" (Score: " + score + ") to string for CopyPastor");
		//#171: don't check the array. Check the String instead
		if (!copyPastorReasonString.contains(reason)) {
			LOGGER.debug("Reason doesn't exist in string yet");
			double roundedScore = Math.round(score*100.0)/100.0;
			
			if (copyPastorReasonString.length() > 0)
				this.copyPastorReasonString += ",";
			
			this.copyPastorReasonString += reason + ":" + roundedScore;
			LOGGER.debug("New copyPastorReasonString: " + this.copyPastorReasonString);
		}
	}
	
	public List<String> getReasonStrings() {
		return this.reasons;
	}
	
	public String getCopyPastorReasonString() {
		return this.copyPastorReasonString;
	}
	
	public double getTotalScore() {
		return this.totalScore;
	}
	
	/**
	 * Returns if the target and original post have the same author
	 * */
	public boolean isRepost() {
		return target.getAnswerer().getUserId() == original.getAnswerer().getUserId();
	}
	
	/**
	 * Checks if the match is valid to be reported.
	 * 
	 * A PostMatch is valid, if both posts are longer than the minimum length.
	 * 
	 * @return true if the match is valid to be reported
	 * */
	public boolean isValidMatch() {
		int minimumLength = 200;
		Properties quantifiers = new Properties();
        try {
        	quantifiers = FileUtils.getPropertiesFromFile(FilePathUtils.generalPropertiesFile);
        	minimumLength = new Integer(quantifiers.getProperty("minimumPostLength", "200"));
        } catch (Throwable e) {
        	LOGGER.warn("Could not load quantifiers from general.properties. Using hardcoded", e);
        }
        
		int lengthOne = this.original.getCleanBodyMarkdown().length();
		int lengthTwo = this.target.getCleanBodyMarkdown().length();
		
		
		//#114: The original post should be at least 5 minutes older than the target
		Instant targetCreation = this.target.getAnswerCreationDate();
		Instant originalCreation = this.original.getAnswerCreationDate();
		long minutes = ChronoUnit.MINUTES.between(originalCreation, targetCreation);
		boolean minimumTimeSpanChecked = minutes >= 5;
		
		//#130: Time-span for reposts: 0 minutes
		if (this.isRepost())
			minimumTimeSpanChecked = targetCreation.getEpochSecond() > originalCreation.getEpochSecond();
		
		LOGGER.trace("minimumTimeSpanChecked: " + minimumTimeSpanChecked);
		LOGGER.trace("Minimum length: " + minimumLength);
		LOGGER.trace("Length one: " + lengthOne);
		LOGGER.trace("Length two: " + lengthTwo);
			
		return lengthOne >= minimumLength && lengthTwo >= minimumLength && minimumTimeSpanChecked;
	}

	@Override
	public int compareTo(PostMatch o) {
		return Double.compare(o.getTotalScore(),getTotalScore());
	}
}
