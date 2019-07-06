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

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.sobotics.guttenberg.entities.Post;

/**
 * Class that finds and extract search terms from Post
 * TODO: This has to refined.
 *
 * @author Petter Friberg
 */
public class SearchTerms {

  private String query;
  private String exactTerm;
  private Post post;


  public SearchTerms(Post p) {
    this.post = p;
    this.query = getQuery(this.post);
    this.exactTerm = getExactTerm(this.post);
  }


  /**
   * Get query to search on
   *
   * @param post
   * @return
   */
  private String getQuery(Post post) {
    String retValue = getSearchStringFromCode(post);
    if (retValue == null) {
      retValue = getCommentFromCode(post);
    }
    if (retValue == null) {
      retValue = getLineFromText(post);
    }
    return retValue.trim();
  }


  /**
   * Get exact term to search on
   *
   * @param post
   * @return
   */
  private String getExactTerm(Post post) {
    String retVal = getCommentFromCode(post);
    if (retVal == null) {
      retVal = getFistLineFromCode(post);
    }
    if (retVal == null) {
      retVal = getLineFromText(post);
    }

    if (retVal != null) {
      String[] reduce = retVal.split("\n|\"|;|:");
      retVal = reduce[0];
    }


    return retVal;
  }


  private String getLineFromText(Post post) {
    String body = post.getBody();
    Document doc = Jsoup.parse(body);
    String[] lines = doc.body().text().split("\\.|\n|\"|;|:");
    String maxLine = null;
    for (String line : lines) {
      if (maxLine == null || maxLine.length() < line.length()) {
        maxLine = line;
      }
    }

    return maxLine;
  }


  private String getSearchStringFromCode(Post post) {
    String body = post.getBody();
    Document doc = Jsoup.parse(body);
    Element code = doc.select("code").first();
    if (code == null) {
      return null;
    }
    String text = code.text();
    return getLine(text, 70);
  }


  private String getFistLineFromCode(Post post) {
    String body = post.getBody();
    Document doc = Jsoup.parse(body);
    Element code = doc.select("code").first();
    if (code == null) {
      return null;
    }
    String text = code.text();
    if (text.contains("\n")) {
      return text.split("\n")[0];
    }
    return text;

  }


  /**
   * Search from comment in code, current marked by //
   *
   * @param post
   * @return
   */
  private String getCommentFromCode(Post post) {
    Document doc = Jsoup.parse(post.getBody());
    Element code = doc.select("code").first();
    if (code == null) {
      return null;
    }
    String text = code.text();
    String[] lines = text.split("\n");
    for (String s : lines) {
      if (s.replaceAll(" ", "").startsWith("//")) {
        return s.substring(2, s.length()).trim();
      }
    }
    return null;
  }


  /**
   * Get a line that is longer then minCharLength
   *
   * @param text,          text
   * @param minCharLength, min length
   * @return
   */
  private String getLine(String text, int minCharLength) {
    String[] lines = text.split("\n");
    for (String s : lines) {
      if (s.length() > minCharLength) {
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


  @Override
  public String toString() {
    return "Query: " + query + ", exact term:" + exactTerm;
  }


}
