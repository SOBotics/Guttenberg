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

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.sobotics.guttenberg.entities.Post;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class UserAnswer {

  private List<UserAnswerLine> texts;
  private List<UserAnswerLine> codes;
  private List<UserAnswerLine> cites;
  private List<UserAnswerLine> comments;
  private String content;


  public UserAnswer(Post post) {
    super();
    init(post);
  }


  private void init(Post post) {
    this.texts = new ArrayList<>();
    this.codes = new ArrayList<>();
    this.cites = new ArrayList<>();
    this.comments = new ArrayList<>();
    String body = post.getBody();
    Document doc = Jsoup.parse(body);
    this.content = doc.text();
    Elements bc = doc.select("blockquote").select("p");
    for (Element e : bc) {
      this.cites.add(new UserAnswerLine(LineType.CITE, e.text()));
    }
    // remove blockquote
    doc.select("blockquote").remove();

    Elements c = doc.select("code");
    for (Element e : c) {
      String[] cs = e.text().split("\n");
      for (String s : cs) {
        this.codes.add(new UserAnswerLine(LineType.CODE, s));
      }
    }
    // remove code in pre
    doc.select("pre").remove();
    Elements text = doc.select("p");
    for (Element e : text) {
      boolean islink = !e.select("a").isEmpty();
      boolean iscode = !e.select("code").isEmpty();
      String cs[] = e.text().split("\n");
      this.texts.add(new UserAnswerLine(LineType.TEXT, e.text(), islink, iscode));
    }

    Elements li = doc.select("li");
    for (Element e : li) {
      boolean islink = !e.select("a").isEmpty();
      boolean iscode = !e.select("code").isEmpty();
      this.texts.add(new UserAnswerLine(LineType.TEXT, e.text(), islink, iscode));
    }

    for (UserAnswerLine line : this.codes) {
      String comment = line.getComment();
      if (comment != null) {
        this.comments.add(new UserAnswerLine(LineType.COMMENT, comment));
      }
    }

    Collections.sort(this.cites);
    Collections.sort(this.codes);
    Collections.sort(this.texts);
    Collections.sort(this.comments);
  }


  public UserAnswerLine getSearchString(boolean onlyText) {

    UserAnswerLine text = null;
    UserAnswerLine comment = null;
    UserAnswerLine code = null;
    UserAnswerLine cite = null;
    for (UserAnswerLine line : this.texts) {
      if (line.containsCode() || line.containsLink()) {
        continue;
      }
      text = line;
      break;
    }

    if (!onlyText) {
      if (!comments.isEmpty() && !onlyText) {
        comment = comments.get(0);
      }

      for (UserAnswerLine line : this.codes) {
        if (line.isComment()) {
          continue;
        }
        code = line;
        break;
      }
      for (UserAnswerLine line : this.cites) {
        if (line.isComment()) {
          continue;
        }
        cite = line;
        break;
      }
    }

    if (comment != null && comment.length() > 50) {
      return comment;
    }

    if (text != null && text.length() > 50) {
      return text;
    }

    if (code != null && code.length() > 50) {
      return code;
    }

    if (cite != null && cite.length() > 50) {
      return cite;
    }

    if (code != null) {
      return code;
    }

    if (text != null) {
      return text;
    }

    if (cite != null) {
      return cite;
    }

    if (content != null && content.length() > 100 && content.indexOf(' ', 90)>0) {
      return new UserAnswerLine(LineType.TEXT, content.substring(0, content.indexOf(' ', 90)));
    }
    return new UserAnswerLine(LineType.TEXT, content);
  }


  public UserAnswerLine getFirst(List<UserAnswerLine> type, int minLength) {
    return getFirst(type, minLength, true, true);
  }


  public UserAnswerLine getFirst(List<UserAnswerLine> type, int minLength, boolean allowLink, boolean allowCode) {
    for (UserAnswerLine line : type) {
      if (line.containsLink() && !allowCode || line.containsCode() && !allowCode) {
        continue;
      }
      if (line.length() < minLength) {
        return null;
      }
      return line;
    }
    return null;
  }

}
