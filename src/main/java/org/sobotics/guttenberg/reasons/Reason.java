package org.sobotics.guttenberg.reasons;

import java.util.List;

import org.sobotics.guttenberg.entities.Post;

/**
 * Defines a reason why a post was reported
 * */
public interface Reason {	
	/**
	 * Checks if the reason applies for the reason.
	 * 
	 * @return true if the post should be reported because of that reason
	 * */
	public boolean check();
	
	/**
	 * The description of the reason
	 * 
	 * @return a short description of the reason like "String similarity"
	 * */
	public String description();
	
	public String description(int index);
	
	/**
	 * The score that specific reason reached. This has no influence on whether a post is reported or not.
	 * 
	 * @return The score a post reached for that reason
	 * */
	public double score();
	
	/**
	 * The posts that was found by that filter
	 * 
	 * @return The Posts
	 * */
	public List<Post> matchedPosts();
	
	public List<Double> getScores();
}
