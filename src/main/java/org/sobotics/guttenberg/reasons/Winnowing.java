package org.sobotics.guttenberg.reasons;

import info.debatty.java.stringsimilarity.JaroWinkler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sobotics.guttenberg.entities.Post;
import org.sobotics.guttenberg.utils.FilePathUtils;
import org.sobotics.guttenberg.utils.FileUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import static org.sotorrent.stringsimilarity.fingerprint.Variants.winnowingFourGramDice;

/**
 * Checks the similarity of two posts
 * */
public class Winnowing implements Reason {

    private static final Logger LOGGER = LoggerFactory.getLogger(Winnowing.class);

    private Post target;
    private List<Post> originals;
    private List<Post> matchedPosts = new ArrayList<Post>();
    private List<Double> scoreList = new ArrayList<Double>();
    private double score = -1;
    private boolean ignoringScore = false;

    public static final String LABEL = "Winnowing";

    public Winnowing(Post target, List<Post> originalPosts) {
        this.target = target;
        this.originals = originalPosts;
    }

    public Winnowing(Post target, List<Post> originalPosts, boolean ignoringScore) {
        this.target = target;
        this.originals = originalPosts;
        this.ignoringScore = ignoringScore;
    }
    
    public static double similarityOf(Post targetPost, Post originalPost) {
        LOGGER.debug("Checking StringSimilarity of " + targetPost.getAnswerID() + " and " + originalPost.getAnswerID());
        String targetBodyMarkdown = targetPost.getCleanBodyMarkdown();
        String targetCodeOnly = targetPost.getCodeOnly();
        String targetPlaintext = targetPost.getPlaintext();
        String targetQuotes = targetPost.getQuotes();

        String originalBodyMarkdown = originalPost.getCleanBodyMarkdown();
        String originalCodeOnly = originalPost.getCodeOnly();
        String originalPlaintext = originalPost.getPlaintext();
        String originalQuotes = originalPost.getQuotes();

        double score = winnowingFourGramDice(targetBodyMarkdown, originalBodyMarkdown);
        LOGGER.trace("Score: "+score);

        return score*2.5;
    }
    
    @Override
    public boolean check() {
        boolean matched = false;
        for (Post post : this.originals) {
            double currentScore = Winnowing.similarityOf(this.target, post);
            
            //get the report threshold
            Properties prop = new Properties();
            try {
                prop = FileUtils.getPropertiesFromFile(FilePathUtils.generalPropertiesFile);
            } catch (IOException e) {
                LOGGER.warn("Could not load general.properties. Using hardcoded value", e);
            }
            
            double minimumScore = Double.parseDouble(prop.getProperty("minimumScore", "0.8"));
            
            if (currentScore >= minimumScore || this.ignoringScore) {
                matched = true;
                
                //add post to list of matched posts
                this.matchedPosts.add(post);
                this.scoreList.add(currentScore);

                //update score
                if(this.score <= currentScore)
                    this.score = currentScore;
            }
        }
        
        return matched;
    }
    
    @Override
    public String description(int index) {
        return description(index, true);
    }
    
    @Override
    public String description(int index, boolean includingScore) {
        if (includingScore) {
            double roundedScore = Math.round(this.scoreList.get(index)*100.0)/100.0;
            String descriptionStr = score() >= 0 ? LABEL + " "+roundedScore : LABEL;
            LOGGER.trace("Description: " + descriptionStr);
            return descriptionStr;
        } else {
            return LABEL;
        }
    }

    @Override
    public double score() {
        return this.score;
    }

    @Override
    public List<Post> matchedPosts() {
        return this.matchedPosts;
    }
    
    @Override
    public List<Double> getScores() {
        return this.scoreList;
    }

}
