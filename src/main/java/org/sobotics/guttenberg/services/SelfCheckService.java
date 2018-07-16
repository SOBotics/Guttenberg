package org.sobotics.guttenberg.services;

import java.time.Instant;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import org.sobotics.redunda.PingService;
import org.sobotics.guttenberg.utils.StatusUtils;

import org.sobotics.chatexchange.chat.ChatHost;
import org.sobotics.chatexchange.chat.Room;

public class SelfCheckService {
	private ScheduledExecutorService executorService;
	private RunnerService instance;
	
	private AtomicInteger oldApiQuota = new AtomicInteger(-1);
	
	public SelfCheckService(RunnerService runner) {
		this.instance = runner;
		this.executorService = Executors.newSingleThreadScheduledExecutor();
	}
	
	private void check() throws Throwable {
		boolean standbyMode = PingService.standby.get();
		if (standbyMode == true) {
			return;
		}
		
		//check quota
		if (oldApiQuota.get() < StatusUtils.remainingQuota.get()) {
			//check if it's the first launch
			if (oldApiQuota.get() != -1) {
				for (Room room : instance.getChatRooms()) {
					//only in SOBotics and SEBotics
					if ((room.getHost() == ChatHost.STACK_OVERFLOW && room.getRoomId() == 111347) 
							|| (room.getHost() == ChatHost.STACK_EXCHANGE && room.getRoomId() == 54445)) {
						room.send("API-quota rolled over at "+oldApiQuota);
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
				
		if (difference > 15*60) {
			for (Room room : this.instance.getChatRooms()) {
				if (room.getRoomId() == 111347) {
					room.send("@FelixSFD Please help me! The last successful execution finished at "+StatusUtils.lastExecutionFinished);
					StatusUtils.askedForHelp = true;
				}
			}
		}
	}
	
	public void secureCheck() {
		try {
			this.check();
		} catch (Throwable e) {
			e.printStackTrace();
		}
	}
	
	public void start() {
		executorService.scheduleAtFixedRate(()->secureCheck(), 1, 5, TimeUnit.MINUTES);
	}
}
