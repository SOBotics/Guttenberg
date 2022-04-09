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

import org.apache.log4j.Level;
import org.apache.log4j.LogManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sobotics.chatexchange.chat.StackExchangeClient;
import org.sobotics.guttenberg.commands.Status;
import org.sobotics.guttenberg.roomdata.BotRoom;
import org.sobotics.guttenberg.roomdata.SOBoticsChatRoom;
import org.sobotics.guttenberg.roomdata.SOBoticsWorkshopChatRoom;
import org.sobotics.guttenberg.services.RunnerService;
import org.sobotics.guttenberg.utils.FilePathUtils;
import org.sobotics.guttenberg.utils.FileUtils;
import org.sobotics.guttenberg.utils.StatusUtils;

import java.io.IOException;
import java.io.InputStream;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;


/**
 * The main class
 * */
public class Client {

  private static final Logger LOGGER = LoggerFactory.getLogger(Client.class);


  private Client() {
  }


  public static void main(String[] args) {
    Properties loggerProperties = new Properties();
    try {
      loggerProperties = FileUtils.getPropertiesFromFile(FilePathUtils.loggerPropertiesFile);

      String levelStr = loggerProperties.getProperty("level");
      Level newLevel = Level.toLevel(levelStr, Level.ERROR);
      LogManager.getRootLogger().setLevel(newLevel);
    } catch (Throwable e) {
      LOGGER.error("Could not load logger.properties! Using default log-level ERROR.", e);
    }

    LOGGER.info("============================");
    LOGGER.info("=== Launching Guttenberg ===");
    LOGGER.info("============================");
    LOGGER.info("Loading properties...");

    Properties login = new Properties();

    try {
      login = FileUtils.getPropertiesFromFile(FilePathUtils.loginPropertiesFile);
    } catch (IOException e) {
      LOGGER.error("Error: ", e);
      LOGGER.error("Could not load login.properties! Shutting down...");
      return;
    }

    Guttenberg.setLoginProperties(login);

    Properties general = new Properties();

    try {
      general = FileUtils.getPropertiesFromFile(FilePathUtils.generalPropertiesFile);
    } catch (IOException e) {
      LOGGER.error("Error loading general.properties: ", e);
    }

    Guttenberg.setGeneralProperties(general);

    LOGGER.info("Initializing chat...");
    StackExchangeClient seClient = new StackExchangeClient(login.getProperty("email"), login.getProperty("password"));

    List<BotRoom> rooms = new ArrayList<>();
    rooms.add(new SOBoticsChatRoom());
    rooms.add(new SOBoticsWorkshopChatRoom());
    //rooms.add(new SEBoticsChatRoom());

    //get current version
    Properties guttenbergProperties = new Properties();
    String version = "0.0.0";
    try {
      InputStream is = Status.class.getResourceAsStream("/guttenberg.properties");
      guttenbergProperties.load(is);
      version = guttenbergProperties.getProperty("version", "0.0.0");
      LOGGER.info("Running on version " + version);
    } catch (IOException e) {
      LOGGER.error("Could not load properties", e);
    }

    LOGGER.debug("Initialize RunnerService...");

    RunnerService runner = new RunnerService(seClient, rooms);

    runner.start();

    StatusUtils.startupDate = Instant.now();
    LOGGER.info("Successfully launched Guttenberg!");
  }

}
