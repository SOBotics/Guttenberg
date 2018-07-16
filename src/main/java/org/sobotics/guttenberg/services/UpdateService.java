package org.sobotics.guttenberg.services;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sobotics.guttenberg.clients.Updater;

import org.sobotics.chatexchange.chat.Room;

public class UpdateService {
	private ScheduledExecutorService executorService;
	private RunnerService instance;
	
	private static final Logger LOGGER = LoggerFactory.getLogger(UpdateService.class);
	
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
					room.send("Rebooting for update to version "+updater.getNewVersion().get());
				}
				room.leave();
			}
			try {
				wait(10);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			System.exit(0);
		}
	}
	
	public void start() {
		executorService.scheduleAtFixedRate(()->updateIfNeeded(), 1, 30, TimeUnit.MINUTES);
	}
}
