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

package org.sobotics.guttenberg.reasons;

import org.sobotics.guttenberg.entities.Post;

import java.util.List;

/**
 * Defines a reason why a post was reported
 */
public interface Reason {
  /**
   * Checks if the reason applies for the reason.
   *
   * @return true if the post should be reported because of that reason
   */
  boolean check();

  /**
   * The description of the reason
   *
   * @return a short description of the reason like "String similarity"
   * @parameter index The index in the `matchedPosts()`-array
   */
  String description(int index);

  /**
   * The description of the reason
   *
   * @return a short description of the reason like "String similarity"
   * @parameter index The index in the `matchedPosts()`-array
   * @parameter includingScore if false, the score won't be included in the description
   */
  String description(int index, boolean includingScore);

  /**
   * The score that specific reason reached. This has no influence on whether a post is reported or not.
   *
   * @return The score a post reached for that reason
   */
  double score();

  /**
   * The posts that was found by that filter
   *
   * @return The Posts
   */
  List<Post> matchedPosts();

  /**
   * The scores for the matched posts in the same order as `matchedPosts()`
   *
   * @return The Posts
   */
  List<Double> getScores();
}
