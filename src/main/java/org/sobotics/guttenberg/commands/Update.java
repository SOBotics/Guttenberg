package org.sobotics.guttenberg.commands;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sobotics.guttenberg.clients.Updater;
import org.sobotics.guttenberg.services.RunnerService;
import org.sobotics.guttenberg.utils.CommandUtils;

import org.sobotics.chatexchange.chat.Message;
import org.sobotics.chatexchange.chat.Room;

public class Update implements SpecialCommand {
	private static final Logger LOGGER = LoggerFactory.getLogger(Update.class);
	
	private static final String CMD = "update";
    private final Message message;

    public Update(Message message) {
        this.message = message;
    }
    
    @Override
    public boolean validate() {
        return CommandUtils.checkForCommand(message.getPlainContent(), CMD);
    }

    @Override
    public void execute(Room room, RunnerService instance) {
        LOGGER.info("Load updater...");
        Updater updater = new Updater();
        LOGGER.info("Check for updates...");
        
        boolean update = false;
        try {
            update = updater.updateIfAvailable(); 
        } catch (Exception e) {
            LOGGER.error("Update failed!", e);
            room.replyTo(message.getId(), "Update failed!");
        }
        
        if (update == true) {
            room.replyTo(message.getId(), "Rebooting for update to version "+updater.getNewVersion().get());
            room.leave();
        } else {
            room.replyTo(message.getId(), updater.getCurrentVersion().get()+" is the current release.");
        }
        
        try {
            wait(10);
        } catch (InterruptedException e) {
            LOGGER.error("Error while waiting for shutdown!", e);
        }
        System.exit(0);
    }

    @Override
    public String description() {
        return "Checks if an update is available";
    }

    @Override
    public String name() {
        return CMD;
    }

	@Override
	public boolean availableInStandby() {
		return false;
	}

}
