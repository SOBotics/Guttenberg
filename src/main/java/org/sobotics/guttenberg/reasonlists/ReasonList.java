package org.sobotics.guttenberg.reasonlists;

import java.util.List;

import org.sobotics.guttenberg.reasons.*;

/**
 * Lists `Reason`s to be applied on posts of a site
 * */
public interface ReasonList {
	/**
	 * The list of reasons to be applied on this site
	 * 
	 * Contains only initialized objects
	 * */
	public List<Reason> reasons();
	
	/**
	 * @return The site where this list will be applied
	 * */
	public String site();
}
