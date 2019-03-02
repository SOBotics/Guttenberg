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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

public class CheckUtils {
  private static final Logger LOGGER = LoggerFactory.getLogger(CheckUtils.class);


  /**
   * Checks if a user is blacklisted on a certain site
   */
  public static boolean checkIfUserIsBlacklisted(long userId, String host) {
    try {
      return FileUtils.checkIfInFile(FilePathUtils.blacklistedUsersFile, host + ":" + String.valueOf(userId));
    } catch (IOException e) {
      LOGGER.error("Could not check if user is blacklisted! Assuming, he's not...", e);
      return false;
    }
  }
}
