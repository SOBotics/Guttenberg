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
import org.sobotics.chatexchange.chat.ChatHost;
import org.sobotics.chatexchange.chat.Room;
import org.sobotics.guttenberg.utils.StatusUtils;

import java.time.Instant;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

public class SelfCheckService {
  private static final Logger LOGGER = LoggerFactory.getLogger(SelfCheckService.class);

  private ScheduledExecutorService executorService;
  private RunnerService instance;

  private AtomicInteger oldApiQuota = new AtomicInteger(-1);


  public SelfCheckService(RunnerService runner) {
    this.instance = runner;
    this.executorService = Executors.newSingleThreadScheduledExecutor();
  }


  private void check() throws Throwable {
    //check quota
    if (oldApiQuota.get() < StatusUtils.remainingQuota.get()) {
      //check if it's the first launch
      if (oldApiQuota.get() != -1) {
        for (Room room : instance.getChatRooms()) {
          //only in SOBotics and SEBotics
          if ((room.getHost() == ChatHost.STACK_OVERFLOW && room.getRoomId() == 111347)
            || (room.getHost() == ChatHost.STACK_EXCHANGE && room.getRoomId() == 54445)) {
            room.send("API-quota rolled over at " + oldApiQuota);
          }
        }
      }
    }

    oldApiQuota = StatusUtils.remainingQuota;

    //check execution
    if (StatusUtils.askedForHelp)
      return;

    Instant now = Instant.now();
    Instant lastSuccess = StatusUtils.lastExecutionFinished;

    long difference = now.getEpochSecond() - lastSuccess.getEpochSecond();

    if (difference > 15 * 60) {
      for (Room room : this.instance.getChatRooms()) {
        if (room.getRoomId() == 111347) {
          room.send("@FelixSFD Please help me! The last successful execution finished at " + StatusUtils.lastExecutionFinished);
          StatusUtils.askedForHelp = true;
        }
      }
    }
  }


  private void secureCheck() {
    try {
      check();
    } catch (Throwable e) {
      LOGGER.error("Error in SelfCheckService", e);
    }
  }


  public void start() {
    executorService.scheduleAtFixedRate(() -> secureCheck(), 1, 5, TimeUnit.MINUTES);
  }
}
