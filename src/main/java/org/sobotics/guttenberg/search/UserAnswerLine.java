/*
 * Copyright (C) 2019 SOBotics (https://sobotics.org) and contributors in GitHub
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

public class UserAnswerLine implements Comparable<UserAnswerLine> {

  private LineType type;
  private String text;
  private boolean containsLink;
  private boolean containsCode;


  public UserAnswerLine(LineType type, String text) {
    this(type, text, false, false);
  }


  public UserAnswerLine(LineType type, String text, boolean link, boolean code) {
    this.type = type;
    this.text = text.trim();
    this.containsLink = link;
    this.containsCode = code;
  }


  public boolean isComment() {
    return this.text.contains("//") || this.text.contains("/*");
  }


  public String getComment() {
    if (this.text.contains("//")) {
      return this.text.substring(this.text.indexOf("//") + 2, this.text.length()).trim();
    }

    int end = text.length();
    if (text.indexOf("/*") + 10 < end) {
      if (text.indexOf("/*") < text.indexOf("*/")) {
        end = text.indexOf("*/") - 1;
      }
      if (this.text.indexOf("/*") + 10 < end) {
        return this.text.substring(this.text.indexOf("/*") + 2, end).replace("*", "").trim();
      }
    }

    return null;
  }


  public String getText() {
    return text;
  }


  public void setText(String text) {
    this.text = text;
  }


  public String getSearch() {
    if (type == LineType.CODE || type == LineType.COMMENT) {
      return text.trim();
    }
    if (text.length() < 50) {
      return text;
    }
    String search = text;
    String[] lines = text.split(".");
    if (lines.length > 0) {
      search = lines[0];
    }

    if (search.length() > 50 && search.indexOf(' ', 50) >= 50) {
      search = search.substring(0, search.indexOf(' ', 50)).trim();
    }
    return search.trim();
  }


  public int length() {
    return this.text.length();
  }


  public int count() {
    return this.text.split(" ").length;
  }


  public boolean containsLink() {
    return this.containsLink;
  }


  public boolean containsCode() {
    return this.containsCode;
  }


  @Override
  public String toString() {
    return this.text;
  }


  @Override
  public int compareTo(UserAnswerLine o) {
    return o.length() - this.length();
  }


}
