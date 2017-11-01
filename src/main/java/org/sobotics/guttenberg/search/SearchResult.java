package org.sobotics.guttenberg.search;

import java.util.ArrayList;
import java.util.List;

import org.sobotics.guttenberg.entities.Post;
import org.sobotics.guttenberg.entities.PostMatch;

/**
 * Search result return from Internet Search
 * @author Petter Friberg
 *
 */
public class SearchResult {

	private Post post;
	private List<SearchItem> items;
	private PostMatch postMatch;

	public SearchResult(Post post){
		this.post = post;
		
	}
	
	public SearchItem getFirstResult(boolean onsite){
		for (SearchItem si : getItems()) {
			if (si.isOnsite(onsite)){
				return si;
			}
		}
		return null;
	}
	
	public List<SearchItem> getItems() {
		if (items==null){
			items = new ArrayList<>();
		}
		return items;
	}

	public void setItems(List<SearchItem> items) {
		this.items = items;
	}

	public List<SearchItem> getOnSiteResults() {
		List<SearchItem> retVal = new ArrayList<>();
		for (SearchItem si : getItems()) {
			if (si.isOnsite()){
				retVal.add(si);
			}
		}
		
		return retVal;
	}
	
	public List<Integer> getIdQuestions(){
		List<Integer> retVal = new ArrayList<>();
		for (SearchItem si : items) {
			if (si.isOnsite()&&si.isQuestion()){
				Integer id = si.getIdSOPost();
				if (id!=null){
					retVal.add(id);
				}
			}
		}
		return retVal;
	}

	public PostMatch getPostMatch() {
		return postMatch;
	}

	public void setPostMatch(PostMatch postMatch) {
		this.postMatch = postMatch;
	}

	public Post getPost() {
		return post;
	}

	
}
