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
import org.sobotics.guttenberg.utils.PostUtils;

/**
 * Item in search result, containing information if from Stack Overflow or not
 *
 * @author Petter Friberg
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
    return this.onsite && this.getLink() != null && (this.getLink().contains(String.valueOf(p.getAnswerID())) || this.getLink().contains(String.valueOf(p.getQuestionID())));
  }


  public boolean isOnsite(boolean onsite) {
    return this.isOnsite() == onsite;
  }


  public boolean isQuestion() {
    return getLink() != null && getLink().toLowerCase().contains("stackoverflow.com/q");
  }


  public boolean isAnswer() {
    return getLink() != null && getLink().toLowerCase().contains("stackoverflow.com/a");
  }


  public Integer getIdSOPost() {
    return PostUtils.getIdFromLink(getLink());
  }

}
