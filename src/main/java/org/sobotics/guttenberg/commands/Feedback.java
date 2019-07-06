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

package org.sobotics.guttenberg.commands;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sobotics.chatexchange.chat.Message;
import org.sobotics.chatexchange.chat.Room;
import org.sobotics.chatexchange.chat.event.PingMessageEvent;
import org.sobotics.guttenberg.services.RunnerService;
import org.sobotics.guttenberg.utils.CommandUtils;
import org.sobotics.guttenberg.utils.PostUtils;

import java.io.IOException;

/**
 * Created by bhargav.h on 29-Nov-16.
 */
public class Feedback implements SpecialCommand {

  private static final Logger LOGGER = LoggerFactory.getLogger(Feedback.class);

  private final Message message;
  private final PingMessageEvent event;
  private final Room room;


  public Feedback(Message message, PingMessageEvent ping, Room room) {
    this.message = message;
    event = ping;
    this.room = room;
  }


  @Override
  public boolean validate() {
    return CommandUtils.checkForCommand(message.getPlainContent(), "feedback");
  }


  @Override
  public void execute(Room room, RunnerService instance) {
    int reportId = -1;
    String[] args = CommandUtils.extractData(message.getPlainContent()).trim().split(" ");

    if (args.length != 2) {
      room.send("Error in arguments passed");
      return;
    }

    String word = args[0];
    String type = args[1];

    try {
      reportId = CommandUtils.getPostIdFromUrl(word);
    } catch (Exception e) {
      LOGGER.error("Report-URL could not be parsed!", e);
    }

    if (reportId == -1)
      return;

    LOGGER.debug("Sending feedback " + type + " for report " + reportId);

    try {
      if (type.equalsIgnoreCase("tp") || type.equalsIgnoreCase("k")) {
        PostUtils.storeFeedback(this.room, event, reportId, "tp");
      }

      if (type.equalsIgnoreCase("fp") || type.equalsIgnoreCase("f")) {
        PostUtils.storeFeedback(this.room, event, reportId, "fp");
      }
    } catch (IOException e) {
      LOGGER.error("Could not store feedback!", e);
    }
  }


  @Override
  public String description() {
    return "Provides feedback on a given report (report id or url)";
  }


  @Override
  public String name() {
    return "feedback";
  }


  @Override
  public boolean availableInStandby() {
    return false;
  }
}
