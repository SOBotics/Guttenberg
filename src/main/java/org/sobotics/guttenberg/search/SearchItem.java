package org.sobotics.guttenberg.search;

import org.sobotics.guttenberg.entities.Post;
import org.sobotics.guttenberg.utils.PostUtils;

/**
 * Item in search result, containing information if from Stack Overflow or not
 * @author Petter Friberg
 *
 */
public class SearchItem {
	
	private String title;
	private String link;
	private boolean onsite;
	
	public SearchItem(String title, String link, boolean onsite) {
		super();
		this.title = title;
		this.link = link;
		this.onsite = onsite;
	}
	
	public String getTitle() {
		return title;
	}
	public void setTitle(String title) {
		this.title = title;
	}
	public String getLink() {
		return link;
	}
	public void setLink(String link) {
		this.link = link;
	}
	public boolean isOnsite() {
		return onsite;
	}
	public void setOnsite(boolean offsite) {
		this.onsite = offsite;
	}
	public boolean isPost(Post p) {
		return this.onsite && this.getLink()!=null && (this.getLink().contains(String.valueOf(p.getAnswerID())) || this.getLink().contains(String.valueOf(p.getQuestionID())));
	}
	
	public boolean isOnsite(boolean onsite){
		return this.isOnsite() == onsite;
	}
	
	public boolean isQuestion(){
		return getLink()!=null && getLink().toLowerCase().contains("stackoverflow.com/q");
	}
	
	public boolean isAnswer(){
		return getLink()!=null && getLink().toLowerCase().contains("stackoverflow.com/a");
	}
	
	public Integer getIdSOPost(){
		return PostUtils.getIdFromLink(getLink());
	}

}
