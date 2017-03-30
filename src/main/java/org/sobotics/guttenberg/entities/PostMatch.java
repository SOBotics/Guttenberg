package org.sobotics.guttenberg.entities;

import java.util.ArrayList;
import java.util.List;

import org.sobotics.guttenberg.reasons.Reason;

/**
 * Represents a plagiarized post
 * */
public class PostMatch {
	private Post target;
	private Post original;
	public List<String> reasons = new ArrayList<String>();
	
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
	
	/**
	 * Returns if the target and original post have the same author
	 * */
	public boolean isRepost() {
		return target.getAnswerer().getUserId() == original.getAnswerer().getUserId();
	}
}
