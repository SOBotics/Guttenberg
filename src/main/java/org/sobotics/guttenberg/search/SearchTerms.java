package org.sobotics.guttenberg.search;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.sobotics.guttenberg.entities.Post;

/**
 * Class that finds and extract search terms from Post
 * TODO: This has to refined.
 * @author Petter Friberg
 *
 */
public class SearchTerms {
	
	private String query;
	private String exactTerm;
	private Post post;
	
	public SearchTerms(Post p){
		this.post = p;
		this.query = getQuery(this.post);
		//this.exactTerm = getExactTerm(this.post);
	}
	
	/**
	 * Get query to search on
	 * @param post
	 * @return
	 */
	private String getQuery(Post post) {
		String retValue = getSearchStringFromCode(post);
		if (retValue==null){
			retValue = getCommentFromCode(post);
		}
		if (retValue==null){
			retValue = getLineFromText(post);
		}
		return "\"" + retValue.trim() + "\"";
	}
	
	/**
	 * Get exact term to search on
	 * @param post
	 * @return
	 */
	private String getExactTerm(Post post) {
		String retVal = getCommentFromCode(post);
		if (retVal==null){
			retVal = getFistLineFromCode(post);
		}
		return retVal;
	}

	private String getLineFromText(Post post) {
		String body = post.getBody();
		Document doc = Jsoup.parse(body);
		String[] lines = doc.body().text().split("\\.|\n|\"|;|:");
		String maxLine = null;
		for (String line : lines) {
			if (maxLine==null || maxLine.length()<line.length()){
				maxLine = line;
			}
		}
		
		return maxLine;
	}

	private String getSearchStringFromCode(Post post) {
		String body = post.getBody();
		Document doc = Jsoup.parse(body);
		Element code = doc.select("code").first();
		if (code==null){
			return null;
		}
		String text = code.text();
		return getLine(text,70);
	}
	
	private String getFistLineFromCode(Post post) {
		String body = post.getBody();
		Document doc = Jsoup.parse(body);
		Element code = doc.select("code").first();
		if (code==null){
			return null;
		}
		String text = code.text();
		if (text.contains("\n")){
			return text.split("\n")[0];
		}
		return text;
		
	}
	
	/**
	 * Search from comment in code, current marked by //
	 * @param post
	 * @return
	 */
	private String getCommentFromCode(Post post) {
		Document doc = Jsoup.parse(post.getBody());
		Element code = doc.select("code").first();
		if (code==null){
			return null;
		}
		String text = code.text();
		String[] lines = text.split("\n");
		for (String s : lines) {
			if (s.replaceAll(" ", "").startsWith("//")){
				return s.substring(2, s.length()).trim();
			}
		}
		return null;
	}

	/**
	 * Get a line that is longer then minCharLength
	 * @param text, text
	 * @param minCharLength, min length
	 * @return
	 */
	private String getLine(String text, int minCharLength) {
		String[] lines = text.split("\n");
		for (String s : lines) {
			if (s.length()>minCharLength){
				return s;
			}
		}
		return null;
	}


	
	public String getQuery() {
		return query;
	}
	public void setQuery(String query) {
		this.query = query;
	}
	public String getExactTerm() {
		return exactTerm;
	}
	public void setExactTerm(String exactMatch) {
		this.exactTerm = exactMatch;
	}

	public Post getPost() {
		return post;
	}
	

}
