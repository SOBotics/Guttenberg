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

import java.io.File;
import java.util.Date;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class CleanerService {
  private Room room;
  private final ScheduledExecutorService cleanerService;

  private static final Logger LOGGER = LoggerFactory.getLogger(CleanerService.class);


  public CleanerService(Room room) {
    this.room = room;
    this.cleanerService = Executors.newSingleThreadScheduledExecutor();
  }


  public void clean() {
    cleanOldLogfiles();
  }


  /**
   * Deletes all files in ./logs that are older than 3 days
   *
   * @see http://stackoverflow.com/a/15042885/4687348
   */
  private void cleanOldLogfiles() {
    int keepLogsForDays = 3;
    File logsDir = new File("./logs");

    for (File file : logsDir.listFiles()) {
      long diff = new Date().getTime() - file.lastModified();

      if (diff > keepLogsForDays * 24 * 60 * 60 * 1000) {
        try {
          file.delete();
        } catch (SecurityException e) {
          LOGGER.error("Faild to delete logfiles!", e);
        }
      }
    } // foreach
  }


  public void start() {
    Runnable cleaner = this::clean;
    cleanerService.scheduleAtFixedRate(cleaner, 0, 4, TimeUnit.HOURS);
  }


  public void stop() {
    cleanerService.shutdown();
  }
}
