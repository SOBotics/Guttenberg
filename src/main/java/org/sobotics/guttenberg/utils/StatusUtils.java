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

package org.sobotics.guttenberg.utils;

import java.time.Instant;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Provides statistics about the current status since launch
 */
public class StatusUtils {
  public static Instant startupDate = null;

  /**
   * The time when the last check was finished
   */
  public static Instant lastExecutionFinished = null;

  /**
   * The time when the last successful check was started
   */
  public static Instant lastSucceededExecutionStarted = null;

  /**
   * The number of new answers that have been checked
   */
  public static AtomicInteger numberOfCheckedTargets = new AtomicInteger(0);

  /**
   * The number of posts that were reported in chat as possible plagiarism
   */
  public static AtomicInteger numberOfReportedPosts = new AtomicInteger(0);

  /**
   * The remaining api quota
   */
  public static AtomicInteger remainingQuota = new AtomicInteger(-1);

  /**
   * Stores if Guttenberg asked for help after several failed executions.
   */
  public static boolean askedForHelp = false;
}
