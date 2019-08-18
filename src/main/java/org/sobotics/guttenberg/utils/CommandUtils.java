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

package org.sobotics.guttenberg.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Arrays;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by bhargav.h on 30-Sep-16.
 */
public class CommandUtils {

  private static final Logger LOGGER = LoggerFactory.getLogger(CommandUtils.class);


  public static boolean checkForCommand(String message, String command) {
    String username;

    Properties prop = new Properties();

    try {
      prop = FileUtils.getPropertiesFromFile(FilePathUtils.loginPropertiesFile);
      username = prop.getProperty("username").substring(0, 3).toLowerCase();
    } catch (IOException e) {
      LOGGER.error("Could not load login.properties", e);
      username = "gut";
    }

    boolean usernameMatch = message.split(" ")[0].toLowerCase().startsWith("@" + username) || message.split(" ")[0].toLowerCase().startsWith("@bots");

    return usernameMatch && message.split(" ")[1].toLowerCase().equals(command);
  }


  public static String extractData(String message) {
    String[] parts = message.split(" ");
    return String.join(" ", Arrays.copyOfRange(parts, 2, parts.length));
  }


  public static String checkAndRemoveMessage(String filename, String message) {
    try {
      if (FileUtils.checkIfInFile(filename, message)) {
        FileUtils.removeFromFile(filename, message);
        return "Done";
      } else {
        return ("It's not there in the file");
      }
    } catch (IOException e) {
      LOGGER.error("ERROR", e);
      return ("Failed");
    }

  }


  public static String getAnswerId(String word) {
    String[] parts = word.split("//")[1].split("/");
    if (parts[1].equals("a") || parts[1].equals("answers")) {
      word = parts[2];
    } else if (parts[1].equals("q") || parts[1].equals("questions")) {
      if (parts[4].contains("#")) {
        word = parts[4].split("#")[1];
      }
    }
    return word;
  }


  /**
   * Extracts the postId from a CopyPastor-URL
   *
   * @param input Input string
   * @throws NumberFormatException    if ID could not be read
   * @throws IllegalArgumentException if the ID is <= 0
   * @returns Post-ID
   */
  public static int getPostIdFromUrl(String input) throws NumberFormatException, IllegalArgumentException {
    int postId = -1;
    LOGGER.debug("Checking string for postId: " + input);

    try {
      postId = Integer.parseInt(input);
    } catch (NumberFormatException e) {
      String pattern = ".*\\/posts\\/(\\d*)";
      Properties props = new Properties();

      try {
        props = FileUtils.getPropertiesFromFile(FilePathUtils.loginPropertiesFile);

        String cpUrl = props.getProperty("copypastor_url");

        if (cpUrl != null) {
          LOGGER.debug("CopyPastor-URL: " + cpUrl);

          cpUrl = Pattern.quote(cpUrl);

          LOGGER.debug("Escaped CopyPastor-URL for RegEx: " + cpUrl);

          pattern = cpUrl + "\\/posts\\/(\\d*)";

          LOGGER.debug("RegEx pattern: " + pattern);
        } else {
          LOGGER.warn("CopyPastor-URL is null");
        }
      } catch (IOException ioE) {
        LOGGER.warn("Could not load general properties", ioE);
      }


      LOGGER.debug("Couldn't parse input to integer. Trying RegEx...");
      Pattern regex = Pattern.compile(pattern);
      Matcher matcher = regex.matcher(input);

      if (matcher.matches()) {
        postId = Integer.parseInt(matcher.group(1));
      } else {
        throw new NumberFormatException("Could not extract postId from string '" + input + "'! RegEx didn't match.");
      }
    }

    if (postId > 0) {
      LOGGER.debug("Found postId: " + postId);
      return postId;
    } else {
      throw new IllegalArgumentException("Invalid postId!");
    }
  }

}
