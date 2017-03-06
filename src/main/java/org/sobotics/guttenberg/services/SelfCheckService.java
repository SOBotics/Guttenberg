package org.sobotics.guttenberg.services;

import java.time.Instant;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.sobotics.guttenberg.utils.StatusUtils;

import fr.tunaki.stackoverflow.chat.ChatHost;
import fr.tunaki.stackoverflow.chat.Room;

public class SelfCheckService {
	private ScheduledExecutorService executorService;
	private RunnerService instance;
	
	private Integer oldApiQuota = -1;
	
	public SelfCheckService(RunnerService runner) {
		this.instance = runner;
		this.executorService = Executors.newSingleThreadScheduledExecutor();
	}
	
	private void check() throws Throwable {
		//check quota
		if (oldApiQuota < ApiService.quota) {
			//check if it's the first launch
			if (oldApiQuota != -1) {
				for (Room room : instance.getChatRooms()) {
					//only in SOBotics and SEBotics
					if ((room.getHost() == ChatHost.STACK_OVERFLOW && room.getRoomId() == 111347) 
							|| (room.getHost() == ChatHost.STACK_EXCHANGE && room.getRoomId() == 54445)) {
						room.send("API-quota rolled over at "+oldApiQuota);
					}
				}
			}
		}
		
		oldApiQuota = ApiService.quota;
		
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
