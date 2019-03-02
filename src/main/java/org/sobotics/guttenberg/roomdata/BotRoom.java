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
import org.sobotics.guttenberg.printers.PostPrinter;
import org.sobotics.guttenberg.services.RunnerService;

import java.util.function.Consumer;

/**
 * Created by bhargav.h on 28-Dec-16.
 */
public interface BotRoom {

  public int getRoomId();

  public ChatHost getHost();

  /**
   * true, if the server-version of Guttenberg will run in this room. false for development-rooms
   */
  public boolean getIsProductionRoom();

  public Consumer<UserMentionedEvent> getMention(Room room, RunnerService instance);

  public Consumer<MessageReplyEvent> getReply(Room room);

  public Consumer<MessagePostedEvent> getMessage(Room room, RunnerService instance);

  //public Validator getValidator();
  public PostPrinter getPostPrinter();

  public boolean getIsLogged();

}
