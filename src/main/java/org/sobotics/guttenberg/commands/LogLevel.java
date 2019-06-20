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

package org.sobotics.guttenberg.commands;

import org.apache.log4j.Level;
import org.apache.log4j.LogManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sobotics.chatexchange.chat.Message;
import org.sobotics.chatexchange.chat.Room;
import org.sobotics.chatexchange.chat.User;
import org.sobotics.guttenberg.services.RunnerService;
import org.sobotics.guttenberg.utils.CommandUtils;
import org.sobotics.guttenberg.utils.FilePathUtils;
import org.sobotics.guttenberg.utils.FileUtils;
import org.sobotics.redunda.PingService;

import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Properties;

public class LogLevel implements SpecialCommand {
  private static final Logger LOGGER = LoggerFactory.getLogger(LogLevel.class);

  private static final String CMD = "log";
  private final Message message;


  public LogLevel(Message message) {
    this.message = message;
  }


  @Override
  public boolean validate() {
    return CommandUtils.checkForCommand(message.getPlainContent(), CMD);
  }


  @Override
  public void execute(Room room, RunnerService instance) {
    User user = message.getUser();

    if (!user.isModerator() && !user.isRoomOwner()) {
      LOGGER.warn("User " + user.getName() + " tried to change the logging-level!");
      room.replyTo(message.getId(), "You are not allowed to change the logging-level.");
      return;
    }

    String allowedLevels = "error|warn|info|debug|trace";
    String levelString = "warn";
    String instanceParam = null;
    String paramString = CommandUtils.extractData(message.getPlainContent());
    String[] params = paramString.split(" ");

    LOGGER.debug(params.length + " parameters");

    if (params.length == 1) {
      if (params[0].matches(allowedLevels)) {
        //No instance given
        levelString = params[0];
        LOGGER.debug("No instance specified. Changing level on all instances...");
      } else {
        //Not a valid level
        room.replyTo(message.getId(), params[0] + " is not a valid logging-level!");
        LOGGER.debug(params[0] + " is not a valid logging-level!");
        return;
      }
    } else if (params.length == 2) {
      //when two arguments are passed, the first one is the instance-name
      instanceParam = params[0];

      if (params[1].matches(allowedLevels)) {
        levelString = params[1];
      } else {
        //Not a valid level
        room.replyTo(message.getId(), params[1] + " is not a valid logging-level!");
        return;
      }
    } else {
      room.replyTo(message.getId(), "Invalid number of arguments!");
      return;
    }

    LOGGER.debug("Level " + levelString + " found.");

    if (instanceParam == null || instanceParam.equalsIgnoreCase(PingService.location)) {
      //the logging-level for this instance should be changed
      Level newLevel = Level.toLevel(levelString);
      LogManager.getRootLogger().setLevel(newLevel);
      FileOutputStream fOut = null;

      try {
        Properties loggingProp = new Properties();
        loggingProp = FileUtils.getPropertiesFromFile(FilePathUtils.loggerPropertiesFile);
        loggingProp.setProperty("level", levelString.toLowerCase());

        fOut = new FileOutputStream(FilePathUtils.loggerPropertiesFile);

        loggingProp.store(fOut, null);

        fOut.close();
      } catch (Exception e) {
        LOGGER.error("Error while saving the new log-level to the logging.properties", e);
      } finally {
        if (fOut != null) {
          try {
            fOut.close();
          } catch (IOException e) {
            LOGGER.error("Could not close FileOutputStream!", e);
          }
        }
      }

      LOGGER.info("Logging-level successfully changed to " + levelString);
      room.send("Instance *" + PingService.location + "* is now using the logging-level **" + levelString.toUpperCase() + "**");
    }
  }


  @Override
  public String description() {
    return "Sets the logging-level. Usage: log <instance> <error|warn|info|debug|trace>";
  }


  @Override
  public String name() {
    return CMD;
  }


  @Override
  public boolean availableInStandby() {
    return true;
  }
}
