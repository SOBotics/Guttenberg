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

package org.sobotics.guttenberg.services;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sobotics.chatexchange.chat.Room;
import org.sobotics.guttenberg.clients.Updater;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class UpdateService {
  private static final Logger LOGGER = LoggerFactory.getLogger(UpdateService.class);
  private final ScheduledExecutorService executorService;
  private final RunnerService instance;


  public UpdateService(RunnerService runner) {
    this.instance = runner;
    this.executorService = Executors.newSingleThreadScheduledExecutor();
  }


  private void updateIfNeeded() {
    LOGGER.info("Load updater...");
    Updater updater = new Updater();
    LOGGER.info("Check for updates...");
    boolean update = false;
    try {
      update = updater.updateIfAvailable();
    } catch (Exception e) {
      LOGGER.error("Could not update", e);
      for (Room room : this.instance.getChatRooms()) {
        if (room.getRoomId() == 111347) {
          room.send("Automatic update failed!");
        }
      }
    }

    if (update) {
      for (Room room : this.instance.getChatRooms()) {
        if (room.getRoomId() == 111347) {
          room.send("Rebooting for update to version " + updater.getNewVersion().get());
        }
        room.leave();
      }
      try {
        wait(10);
      } catch (InterruptedException e) {
        LOGGER.error("Error while waiting for reboot!", e);
      }
      System.exit(0);
    }
  }


  public void start() {
    executorService.scheduleAtFixedRate(() -> updateIfNeeded(), 1, 30, TimeUnit.MINUTES);
  }
}
