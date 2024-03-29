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

package org.sobotics.guttenberg.commandlists;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sobotics.chatexchange.chat.Message;
import org.sobotics.chatexchange.chat.Room;
import org.sobotics.chatexchange.chat.User;
import org.sobotics.chatexchange.chat.event.MessagePostedEvent;
import org.sobotics.chatexchange.chat.event.PingMessageEvent;
import org.sobotics.guttenberg.commands.*;
import org.sobotics.guttenberg.services.RunnerService;
import org.sobotics.guttenberg.utils.CheckUtils;
import org.sobotics.guttenberg.utils.FilePathUtils;
import org.sobotics.guttenberg.utils.FileUtils;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;

/**
 * Created by bhargav.h on 28-Oct-16.
 */
public class SoBoticsCommandsList {

  private static final Logger LOGGER = LoggerFactory.getLogger(SoBoticsCommandsList.class);


  public void mention(Room room, PingMessageEvent event, boolean isReply, RunnerService instance) {
    if (CheckUtils.checkIfUserIsBlacklisted(event.getUserId(), room.getHost().getBaseUrl())) {
      LOGGER.info("Blacklisted user " + event.getUserName() + " replied to the bot.");
      return;
    }

    Message message = event.getMessage();
    LOGGER.info("Mention by " + event.getUserId() + ": " + message.getContent());
    List<SpecialCommand> commands = new ArrayList<>(Arrays.asList(
      new Alive(message),
      new CheckInternet(message),
      new CheckUser(message),
      new Check(message),
      new ClearHelp(message),
      new Feedback(message, event, room),
      new LogLevel(message),
      //new OptIn(message),
      //new OptOut(message),
      new Quota(message),
      new Say(message),
      new Status(message),
      new Update(message),
      new Reboot(message)
    ));

    commands.add(new Commands(message, commands));

    for (SpecialCommand command : commands) {
      if (command.validate()) {
        if (command instanceof CheckUser) {
          User user = event.getUser().get();
          if (!user.isModerator() && !user.isRoomOwner()) {
            room.replyTo(message.getId(), "This command can only be executed by RO or moderator");
            return;
          }
        }
        // Ideally this should be implemented in the validate method of the checkuser command.
        try {
          command.execute(room, instance);
        } catch (Exception e) {
          LOGGER.error("Error executing command!", e);
          room.send("Error executing command: " + e.getMessage());
        }
      }
    }
  }


  public void globalCommand(Room room, MessagePostedEvent event, RunnerService instance) {
    // only ROs (and Generic Bot) should execute global commands!
    try {
      User user = event.getUser().get();
      if (!user.isModerator() && !user.isRoomOwner() && user.getId() != 7481043L)
        return;
    } catch (Throwable e) {
      LOGGER.warn("Could not verify privileges of that user. Don't execute the command.", e);
      return;
    }

    Message message = event.getMessage();

    LOGGER.info("Message: " + message.getContent());

    int cp = Character.codePointAt(message.getPlainContent(), 0);

    if (cp == 128642 || (cp >= 128644 && cp <= 128650)) {
      room.send("[🚃](http://bit.ly/2nRi9kX)");
      return;
    }


    //we hate fun
    try {
      String lowercaseMsg = message.getPlainContent().toLowerCase();
      if (lowercaseMsg.contains("cat")) {
        File file = new File("./data/catgifs.txt");
        String img = FileUtils.randomLine(file);
        if (img != null && !img.isEmpty())
          room.send(img);
      }
      if (lowercaseMsg.contains("trump")) {
        File file = new File("./data/drumpf.txt");
        String img = FileUtils.randomLine(file);
        if (img != null && !img.isEmpty())
          room.send(img);
      }
    } catch (Throwable ignore) {
    }


    // return immediately, if @gut is part of the message!
    String username = "";

    Properties prop = new Properties();

    try {
      prop = FileUtils.getPropertiesFromFile(FilePathUtils.loginPropertiesFile);
      username = prop.getProperty("username").substring(0, 3).toLowerCase();
    } catch (IOException e) {
      LOGGER.error("Could not load login.properties", e);
      username = "gut";
    }

    boolean containsUsername = message.getPlainContent().toLowerCase().contains("@" + username);
    if (containsUsername) {
      return;
    }

    List<SpecialCommand> commands = new ArrayList<>(Arrays.asList(
      new Alive(message)
    ));

    for (SpecialCommand command : commands) {
      if (command.validate()) {
        command.execute(room, instance);
      }
    }
  }
}
