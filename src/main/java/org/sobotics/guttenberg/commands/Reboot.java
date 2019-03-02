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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sobotics.chatexchange.chat.Message;
import org.sobotics.chatexchange.chat.Room;
import org.sobotics.chatexchange.chat.User;
import org.sobotics.guttenberg.services.RunnerService;
import org.sobotics.guttenberg.utils.CommandUtils;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * @author owen
 */
public class Reboot implements SpecialCommand {

  private static final Logger LOGGER = LoggerFactory.getLogger(Reboot.class);

  private static final String CMD = "reboot";
  private final Message message;


  public Reboot(Message message) {
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
      LOGGER.warn("User " + user.getName() + " tried to reboot the bot!");
      room.replyTo(message.getId(), "Sorry, but only room-owners and moderators can use this command (@FelixSFD)");
      return;
    }


    LOGGER.warn("REBOOT COMMAND executed by " + user.getName());
    String[] args = message.getPlainContent().split(" ");
    if (args.length >= 3) {
      String rebootType = args[2];
      if (rebootType.equals("hard")) {
        this.hardReboot(room);
      } else if (rebootType.equals("soft")) {
        this.softReboot(room, instance);
      }
    } else {
      room.send("You didn't specify a reboot type. Assuming soft.");
      this.softReboot(room, instance);
    }
  }


  @Override
  public String description() {
    return "Reboots Guttenberg. Usage: `reboot <soft|hard>`";
  }


  @Override
  public String name() {
    return CMD;
  }


  private void softReboot(Room room, RunnerService instance) {
    instance.reboot();
    room.send("Reset executor threads. To shutdown and restart, use `reboot hard`.");
  }


  private void hardReboot(Room room) {
    LOGGER.warn("Hard reboot...");
    try {
      Properties properties = new Properties();
      InputStream is = Status.class.getResourceAsStream("/guttenberg.properties");
      properties.load(is);

      String versionString = (String) properties.get("version");

      LOGGER.info("Booting version " + versionString);

      Runtime.getRuntime().exec("nohup java -cp guttenberg-" + versionString + ".jar org.sobotics.guttenberg.clients.Client");
      System.exit(0);
    } catch (IOException e) {
      LOGGER.error("Hard reboot failed!", e);
      room.replyTo(message.getId(), "**Reboot failed!** (cc @FelixSFD)");
    }
  }


  @Override
  public boolean availableInStandby() {
    return false;
  }
}
