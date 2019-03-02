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

package org.sobotics.guttenberg.services;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sobotics.chatexchange.chat.Room;
import org.sobotics.chatexchange.chat.StackExchangeClient;
import org.sobotics.chatexchange.chat.event.EventType;
import org.sobotics.chatexchange.chat.event.MessagePostedEvent;
import org.sobotics.chatexchange.chat.event.UserMentionedEvent;
import org.sobotics.guttenberg.clients.Guttenberg;
import org.sobotics.guttenberg.roomdata.BotRoom;
import org.sobotics.guttenberg.utils.FilePathUtils;
import org.sobotics.guttenberg.utils.FileUtils;
import org.sobotics.guttenberg.utils.StatusUtils;
import org.sobotics.redunda.PingService;
import org.sobotics.redunda.PingServiceDelegate;

import java.io.IOException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

public class RunnerService implements PingServiceDelegate {
  private static final Logger LOGGER = LoggerFactory.getLogger(RunnerService.class);
  private StackExchangeClient client;
  private List<BotRoom> rooms;
  private List<Room> chatRooms;
  private ScheduledExecutorService executorService;


  public RunnerService(StackExchangeClient client, List<BotRoom> rooms) {
    this.client = client;
    this.rooms = rooms;
    chatRooms = new ArrayList<>();
  }


  public void start() {
    Properties prop = new Properties();
    try {
      prop = FileUtils.getPropertiesFromFile(FilePathUtils.loginPropertiesFile);
    } catch (IOException e) {
      LOGGER.error("login.properties could not be loaded!", e);
    }

    String isProductionInstance = prop.getProperty("production_instance", "false");

    for (BotRoom room : rooms) {
      if (isProductionInstance.equals("true") == room.getIsProductionRoom()) {
        Room chatroom = client.joinRoom(room.getHost(), room.getRoomId());

        //start services
        CleanerService cleaner = new CleanerService(chatroom);
        cleaner.start();
        SelfCheckService selfCheck = new SelfCheckService(this);
        selfCheck.start();
        UpdateService update = new UpdateService(this);
        update.start();

        if (prop.getProperty("production_instance").equals("true")) {
          //only post the welcome message, when not on standby
          if (PingService.standby.get() == false) {
            chatroom.send("[Guttenberg](http://stackapps.com/q/7197/43403) launched (SERVER VERSION; Instance [_" + prop.getProperty("location", "undefined") + "_](https://redunda.sobotics.org/bots/4/bot_instances))");
          }
        } else {
          chatroom.send("[Guttenberg](http://stackapps.com/q/7197/43403) launched (DEVELOPMENT VERSION; Instance _" + prop.getProperty("location") + "_)");
        }

        chatRooms.add(chatroom);

        Consumer<UserMentionedEvent> mention = room.getMention(chatroom, this);
        Consumer<MessagePostedEvent> messagePosted = room.getMessage(chatroom, this);
        if (mention != null) {
          chatroom.addEventListener(EventType.USER_MENTIONED, mention);
        }
        if (messagePosted != null) {
          chatroom.addEventListener(EventType.MESSAGE_POSTED, messagePosted);
        }
        if (room.getReply(chatroom) != null)
          chatroom.addEventListener(EventType.MESSAGE_REPLY, room.getReply(chatroom));
      }

    }

    executorService = Executors.newSingleThreadScheduledExecutor();
    run();
  }


  public void run() {
    Guttenberg instance = new Guttenberg(this.chatRooms);
    executorService.scheduleAtFixedRate(() -> execute(instance), 20, 59 + 1, TimeUnit.SECONDS);
  }


  private void execute(Guttenberg guttenberg) {
    guttenberg.secureExecute();
  }


  public void stop() {
    executorService.shutdown();
  }


  public void reboot() {
    this.stop();
    executorService = Executors.newSingleThreadScheduledExecutor();
    this.run();
    for (int i = 0; i < rooms.size(); i++) {
      if (rooms.get(i).getIsLogged()) {
        Room room = chatRooms.get(i);
        room.send("Rebooted at " + Instant.now());
        try {
          Thread.sleep(1000);
        } catch (InterruptedException e) {
          e.printStackTrace();
        }
      }
    }
  }


  public List<Room> getChatRooms() {
    return this.chatRooms;
  }


  @Override
  public void standbyStatusChanged(boolean newStatus) {
    LOGGER.info("New standby status: " + newStatus);
    if (newStatus == false) {
      StatusUtils.lastExecutionFinished = Instant.now();
      StatusUtils.lastSucceededExecutionStarted = Instant.now().minusSeconds(30);
    }
  }
}
