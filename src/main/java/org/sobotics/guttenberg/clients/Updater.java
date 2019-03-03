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

package org.sobotics.guttenberg.clients;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sobotics.guttenberg.commands.Status;
import org.sobotics.guttenberg.utils.Version;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Checks for updates
 */
public class Updater {

  private static final Logger LOGGER = LoggerFactory.getLogger(Updater.class);

  private final Version currentVersion;
  private Version newVersion = new Version("0.0.0");


  public Updater() {
    //get current version
    Properties guttenbergProperties = new Properties();

    try {
      InputStream is = Status.class.getResourceAsStream("/guttenberg.properties");
      guttenbergProperties.load(is);
    } catch (IOException e) {
      LOGGER.error("Could not load properties", e);
    }

    LOGGER.debug("Loaded properties");

    String versionString = guttenbergProperties.getProperty("version", "0.0.0");
    currentVersion = new Version(versionString);

    LOGGER.info("Current version: " + currentVersion.get());

    //get all files in the folder
    String regex = "guttenberg-([0-9.]*)\\.jar";
    Pattern pattern = Pattern.compile(regex);

    File[] files = new File("./").listFiles();
    if (files != null) {
      for (File file : files) {
        if (file.isFile()) {
          String name = file.getName();
          LOGGER.debug("File: " + name);
          Matcher matcher = pattern.matcher(name);
          matcher.find();
          LOGGER.debug("Init matcher");
          String v = "";

          try {
            v = matcher.group(1);
          } catch (Exception e) {
            LOGGER.warn("Filename " + name + " didn't match the pattern.", e);
          }


          if (v != null && !v.isEmpty()) {
            LOGGER.debug("Matched");
            Version version = new Version(v);
            LOGGER.debug("Found version " + version.get());
            if (currentVersion.compareTo(version) < 0) {
              //higher than current version
              if (newVersion.compareTo(version) < 0) {
                //higher than next version
                newVersion = version;
              }
            }
          }

        } // if
      } // foreach files
    } // if files

  }


  /**
   * Checks if a new version is available and updates
   *
   * @return false if no update required; true if update successful
   * @throws Exception when update failed
   */
  public boolean updateIfAvailable() throws Exception {
    if (currentVersion.compareTo(newVersion) < 0) {
      LOGGER.info("New version available: " + newVersion.get());
      try {
        Runtime.getRuntime().exec("nohup java -cp guttenberg-" + newVersion.get() + ".jar org.sobotics.guttenberg.clients.Client");
        System.exit(0);
        return true;
      } catch (IOException e) {
        LOGGER.error("Update failed!", e);
        throw new Exception("Update failed!");
      }
    } else {
      LOGGER.info("No update required");
      return false;
    }
  }


  public Version getNewVersion() {
    return newVersion;
  }


  public Version getCurrentVersion() {
    return currentVersion;
  }

}
