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

import org.sobotics.chatexchange.chat.Message;
import org.sobotics.chatexchange.chat.Room;
import org.sobotics.guttenberg.services.RunnerService;
import org.sobotics.guttenberg.utils.CommandUtils;
import org.sobotics.guttenberg.utils.PrintUtils;

import java.util.List;

/**
 * Created by bhargav.h on 30-Sep-16.
 */
public class Commands implements SpecialCommand {

  private static final String CMD = "commands";
  private final Message message;
  private final List<SpecialCommand> commands;


  public Commands(Message message, List<SpecialCommand> commands) {
    this.message = message;
    this.commands = commands;
  }


  public static String padRight(String s, int n) {
    return String.format("%1$-" + n + "s", s);
  }


  @Override
  public boolean validate() {
    return CommandUtils.checkForCommand(message.getPlainContent(), CMD);
  }


  @Override
  public void execute(Room room, RunnerService instance) {
    room.replyTo(message.getId(), PrintUtils.printCommandHeader());
    String printstr = "";
    for (SpecialCommand command : commands) {
      printstr += "    " + padRight(command.name(), 15) + " - " + command.description() + "\n";
    }

    room.send(printstr);
  }


  @Override
  public String name() {
    return CMD;
  }


  @Override
  public String description() {
    return "Returns the list of commands associated with this bot";
  }


  @Override
  public boolean availableInStandby() {
    return false;
  }


}
