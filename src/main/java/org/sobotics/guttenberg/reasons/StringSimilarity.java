package org.sobotics.guttenberg.reasons;

import java.io.FileInputStream;
import java.io.IOException;
import java.time.Instant;
import java.util.List;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sobotics.guttenberg.entities.Post;
import org.sobotics.guttenberg.utils.FilePathUtils;

import info.debatty.java.stringsimilarity.JaroWinkler;

/**
 * Checks the similarity of two posts
 * */
public class StringSimilarity implements Reason {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(StringSimilarity.class);

	private Post target;
	private List<Post> originals;
	
	public StringSimilarity(Post target, List<Post> originalPosts) {
		this.target = target;
		this.originals = originalPosts;
	}
	
	public static boolean similarityOf(Post targetPost, Post originalPost) {
		String targetBodyMarkdown = targetPost.getBodyMarkdown();
        String targetCodeOnly = targetPost.getCodeOnly();
        String targetPlaintext = targetPost.getPlaintext();
        String targetQuotes = targetPost.getQuotes();
        
        Properties quantifiers = new Properties();
        try {
        	quantifiers.load(new FileInputStream(FilePathUtils.generalPropertiesFile));
        } catch (IOException e) {
        	LOGGER.warn("Could not load quantifiers from general.properties. Using hardcoded", e);
        }
        
        double quantifierBodyMarkdown = 1;
        double quantifierCodeOnly = 1;
        double quantifierPlaintext = 1;
        double quantifierQuotes = 1;
        
        try {
        	quantifierBodyMarkdown = new Double(quantifiers.getProperty("quantifierBodyMarkdown", "1"));
        	quantifierCodeOnly = new Double(quantifiers.getProperty("quantifierCodeOnly", "1"));
        	quantifierPlaintext = new Double(quantifiers.getProperty("quantifierPlaintext", "1"));
        	quantifierQuotes = new Double(quantifiers.getProperty("quantifierQuotes", "1"));
        } catch (Throwable e) {
        	LOGGER.warn("Using hardcoded value", e);
        }
        
        JaroWinkler jw = new JaroWinkler();
        
        String originalBodyMarkdown = originalPost.getBodyMarkdown();
        String originalCodeOnly = originalPost.getCodeOnly();
        String originalPlaintext = originalPost.getPlaintext();
        String originalQuotes = originalPost.getQuotes();
        
        double jwBodyMarkdown = jw.similarity(targetBodyMarkdown, originalBodyMarkdown)
        		* quantifierBodyMarkdown;
        double jwCodeOnly = originalCodeOnly != null ? ( jw.similarity(targetCodeOnly, originalCodeOnly)
        		* quantifierCodeOnly) : 0;
        double jwPlaintext = originalPlaintext != null ? ( jw.similarity(originalPlaintext, targetPlaintext)
        		* quantifierPlaintext) : 0;
        double jwQuotes = originalQuotes != null ? ( jw.similarity(originalQuotes, targetQuotes)
        		* quantifierQuotes) : 0;
        
        //LOGGER.info("bodyMarkdown: "+jwBodyMarkdown+"; codeOnly: "+jwCodeOnly+"; plaintext: "+jwPlaintext);
        
        double usedScores = (jwBodyMarkdown > 0 ? quantifierBodyMarkdown : 0)
        		+ (jwCodeOnly > 0 ? quantifierCodeOnly : 0)
        		+ (jwPlaintext > 0 ? quantifierPlaintext : 0)
        		+ (jwQuotes > 0 ? quantifierQuotes : 0);
        double jaroWinklerScore = ((jwBodyMarkdown > 0 ? jwBodyMarkdown : 0) 
        		+ (jwCodeOnly > 0 ? jwCodeOnly : 0)
        		+ (jwPlaintext > 0 ? jwPlaintext : 0)
        		+ (jwQuotes > 0 ? jwQuotes : 0)) / usedScores;
        
        if (jwBodyMarkdown > 0.9)
        	return true;
        
        //TODO: limit still hardcoded!
        if(jaroWinklerScore > 0.8)
        	return true;
        
		return false;
	}
	
	@Override
	public boolean check() {
		boolean matched = false;
		for (Post post : this.originals) {
			boolean similar = StringSimilarity.similarityOf(this.target, post);
			if (similar == true)
				matched = true;
		}
		
		return matched;
	}

	@Override
	public String description() {
		return "String similarity";
	}

	@Override
	public double score() {
		// TODO Auto-generated method stub
		return 1;
	}

	@Override
	public List<Post> matchedPosts() {
		// TODO Auto-generated method stub
		return null;
	}

}
