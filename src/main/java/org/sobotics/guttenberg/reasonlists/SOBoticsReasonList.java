/*
 * Copyright (C) 2019 SOBotics (https://sobotics.org) and contributors on GitHub
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

package org.sobotics.guttenberg.reasonlists;

import org.sobotics.guttenberg.entities.Post;
import org.sobotics.guttenberg.reasons.ExactParagraphMatch;
import org.sobotics.guttenberg.reasons.Reason;
import org.sobotics.guttenberg.reasons.StringSimilarity;

import java.util.ArrayList;
import java.util.List;

public class SOBoticsReasonList implements ReasonList {

  private final Post target;
  private final List<Post> originals;


  public SOBoticsReasonList(Post target, List<Post> originalPosts) {
    this.target = target;
    this.originals = originalPosts;
  }


  @Override
  public List<Reason> reasons() {
    return reasons(false);
  }


  @Override
  public List<Reason> reasons(boolean ignoringScores) {
    List<Reason> reasons = new ArrayList<>();

    reasons.add(new StringSimilarity(this.target, this.originals, ignoringScores));
    reasons.add(new ExactParagraphMatch(this.target, this.originals));


    return reasons;
  }


  @Override
  public String site() {
    return "stackoverflow";
  }

}
