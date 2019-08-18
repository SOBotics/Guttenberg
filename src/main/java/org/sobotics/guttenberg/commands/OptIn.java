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
import org.sobotics.chatexchange.chat.User;
import org.sobotics.guttenberg.services.RunnerService;
import org.sobotics.guttenberg.utils.CommandUtils;
import org.sobotics.guttenberg.utils.FilePathUtils;
import org.sobotics.guttenberg.utils.FileUtils;

import java.io.IOException;

public class OptIn implements SpecialCommand {

  private static final String CMD = "opt-in";
  private Message message;


  public OptIn(Message message) {
    this.message = message;
  }


  @Override
  public boolean validate() {
    return CommandUtils.checkForCommand(message.getPlainContent(), CMD);
  }


  @Override
  public void execute(Room room, RunnerService instance) {
    User user = message.getUser();
    long userId = user.getId();
    String userName = user.getName();
    String filename = FilePathUtils.optedUsersFile;

    String data = CommandUtils.extractData(message.getPlainContent()).trim();

    String pieces[] = data.split(" ");

    if (pieces.length >= 1) {
      double minScore = -1;
      boolean whenInRoom = true;
      try {
        minScore = new Double(pieces[0]);
      } catch (Throwable e) {
        room.replyTo(message.getId(), "Invalid minimum score.");
        return;
      }

      if (pieces.length >= 2) {
        if (pieces[1].equals("always")) {
          whenInRoom = false;
        }
      }

      String optMessage = userId + ",\"" + userName + "\"" + "," + room.getRoomId() + "," + whenInRoom;

      minScore = Math.round(minScore * 100.0) / 100.0;

      try {
        if (FileUtils.checkIfLineInFileStartsWith(filename, optMessage)) {
          room.replyTo(message.getId(), "You've already been added");
        } else {
          optMessage += "," + minScore;
          FileUtils.appendToFile(filename, optMessage);
          room.replyTo(message.getId(), "You will be notified about possible plagiarism with a score of " + minScore + " or higher.");
        }
      } catch (IOException e) {
        e.printStackTrace();
      }

    } else {
      room.replyTo(message.getId(), "Invalid command. Correct usage: `opt-in <score> <always?>`");
    }
  }


  @Override
  public String description() {
    return "Get notified about possible plagiarism with a certain score. Usage: opt-in <score> <always?>";
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
