/*
 * Copyright (C) 2019 SOBotics
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>
 */

package org.sobotics.guttenberg.search;

import org.sobotics.guttenberg.entities.Post;
import org.sobotics.guttenberg.entities.PostMatch;

import java.util.ArrayList;
import java.util.List;

/**
 * Search result return from Internet Search
 *
 * @author Petter Friberg
 */
public class SearchResult {

  private Post post;
  private List<SearchItem> items;
  private PostMatch postMatch;


  public SearchResult(Post post) {
    this.post = post;

  }


  public SearchItem getFirstResult(boolean onsite) {
    for (SearchItem si : getItems()) {
      if (si.isOnsite(onsite)) {
        return si;
      }
    }
    return null;
  }


  public List<SearchItem> getItems() {
    if (items == null) {
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
      if (si.isOnsite()) {
        retVal.add(si);
      }
    }

    return retVal;
  }


  public List<Integer> getIdQuestions() {
    List<Integer> retVal = new ArrayList<>();
    for (SearchItem si : items) {
      if (si.isOnsite() && si.isQuestion()) {
        Integer id = si.getIdSOPost();
        if (id != null) {
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
