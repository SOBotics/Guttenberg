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

import org.sobotics.guttenberg.reasons.Reason;

import java.util.List;

/**
 * Lists `Reason`s to be applied on posts of a site
 */
public interface ReasonList {
  /**
   * The list of reasons to be applied on this site
   * <p>
   * Contains only initialized objects
   */
  List<Reason> reasons();

  /**
   * The list of reasons to be applied on this site
   * <p>
   * Contains only initialized objects.
   *
   * @parameter ignoringScores If true, every reason will ignore the minimum score to mark it as "applied"
   */
  List<Reason> reasons(boolean ignoringScores);

  /**
   * @return The site where this list will be applied
   */
  String site();
}
