package org.sobotics.guttenberg.reasons;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sobotics.guttenberg.entities.Post;

import info.debatty.java.stringsimilarity.JaroWinkler;

/**
 * This reason returns posts that have at least one exactly matching paragraph with the target.
 * */
public class ExactParagraphMatch implements Reason {

	private static final Logger LOGGER = LoggerFactory.getLogger(ExactParagraphMatch.class);

	private Post target;
	private List<Post> originals;
	private List<Post> matchedPosts = new ArrayList<Post>();
	private double score = -1;
	
	public ExactParagraphMatch(Post target, List<Post> originalPosts) {
		this.target = target;
		this.originals = originalPosts;
	}
	
	@Override
	public boolean check() {
		JaroWinkler jw = new JaroWinkler();
		boolean matched = false;
		
		
		
		for (Post original : this.originals) {
			
		}
		
		return matched;
	}

	@Override
	public String description() {
		return "Exact paragraph match";
	}

	@Override
	public double score() {
		return 0;
	}

	@Override
	public List<Post> matchedPosts() {
		return this.matchedPosts;
	}

}
