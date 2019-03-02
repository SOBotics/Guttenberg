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

package org.sobotics.guttenberg.roomdata;

import org.sobotics.chatexchange.chat.ChatHost;
import org.sobotics.chatexchange.chat.Room;
import org.sobotics.chatexchange.chat.event.MessagePostedEvent;
import org.sobotics.chatexchange.chat.event.MessageReplyEvent;
import org.sobotics.chatexchange.chat.event.UserMentionedEvent;
import org.sobotics.guttenberg.commandlists.SoBoticsCommandsList;
import org.sobotics.guttenberg.printers.PostPrinter;
import org.sobotics.guttenberg.printers.SoBoticsPostPrinter;
import org.sobotics.guttenberg.services.RunnerService;
import org.sobotics.guttenberg.utils.PostUtils;

import java.util.function.Consumer;

/**
 * Created by bhargav.h on 28-Dec-16.
 */
public class SOBoticsChatRoom implements BotRoom {
  @Override
  public int getRoomId() {
    return 111347;
  }


  @Override
  public ChatHost getHost() {
    return ChatHost.STACK_OVERFLOW;
  }


  @Override
  public boolean getIsProductionRoom() {
    return true;
  }


  @Override
  public Consumer<UserMentionedEvent> getMention(Room room, RunnerService instance) {
    return event -> new SoBoticsCommandsList().mention(room, event, true, instance);
  }


  @Override
  public Consumer<MessagePostedEvent> getMessage(Room room, RunnerService instance) {
    return event -> new SoBoticsCommandsList().globalCommand(room, event, instance);
  }


  @Override
  public Consumer<MessageReplyEvent> getReply(Room room) {
    return event -> PostUtils.reply(room, event, true);
  }


  @Override
  public PostPrinter getPostPrinter() {
    return new SoBoticsPostPrinter();
  }


  @Override
  public boolean getIsLogged() {
    return true;
  }

}
