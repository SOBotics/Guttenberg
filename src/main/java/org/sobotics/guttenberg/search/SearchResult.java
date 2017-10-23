package org.sobotics.guttenberg.search;

import java.util.ArrayList;
import java.util.List;

/**
 * Search result return from Internet Search
 * @author Petter Friberg
 *
 */
public class SearchResult {

	private List<SearchItem> items;

	
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
	
}
