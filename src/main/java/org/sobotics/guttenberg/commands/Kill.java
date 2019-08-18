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

public class Kill implements SpecialCommand {

  private static final Logger LOGGER = LoggerFactory.getLogger(Kill.class);

  private static final String CMD = "kill";
  private final Message message;


  public Kill(Message message) {
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
      LOGGER.warn("User " + user.getName() + " tried to kill the bot!");
      room.replyTo(message.getId(), "Sorry, but only room-owners and moderators can use this command (@FelixSFD)");
      return;
    }

    LOGGER.error("KILLED BY " + user.getName());
    System.exit(0);
  }


  @Override
  public String description() {
    return "Kills the currently active instance";
  }


  @Override
  public String name() {
    return CMD;
  }


  @Override
  public boolean availableInStandby() {
    return false;
  }

}
