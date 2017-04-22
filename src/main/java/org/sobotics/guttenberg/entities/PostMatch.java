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
	private List<String> reasons = new ArrayList<String>();
	private double totalScore = 0;
	
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
	
	@Deprecated
	public void addReason(Reason reason) {
		if (!reasons.contains(reason.description())) {
			//add reason
			this.reasons.add(reason.description());
			//add score
			this.totalScore += reason.score();
		}
	}
	
	public void addReason(String reason, double score) {
		if (!reasons.contains(reason)) {
			//add reason
			this.reasons.add(reason);
			//add score
			this.totalScore += score;
		}
	}
	
	public List<String> getReasonStrings() {
		return this.reasons;
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
}
