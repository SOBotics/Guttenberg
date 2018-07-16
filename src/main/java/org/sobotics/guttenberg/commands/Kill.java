package org.sobotics.guttenberg.commands;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sobotics.guttenberg.services.RunnerService;
import org.sobotics.guttenberg.utils.CommandUtils;

import org.sobotics.chatexchange.chat.Message;
import org.sobotics.chatexchange.chat.Room;
import org.sobotics.chatexchange.chat.User;

public class Kill implements SpecialCommand {

	private static final Logger LOGGER = LoggerFactory.getLogger(Kill.class);
	
	private static final String CMD = "kill";
	private final Message message;
    
    public Kill(Message message) {
        this.message = message;
    }
    
	@Override
	public boolean validate() {
		return CommandUtils.checkForCommand(message.getPlainContent(), CMD);
	}

	@Override
	public void execute(Room room, RunnerService instance) {
		User user = message.getUser();
    	
    	if (!user.isModerator() && !user.isRoomOwner()) {
    		LOGGER.warn("User " + user.getName() + " tried to kill the bot!");
    		room.replyTo(message.getId(), "Sorry, but only room-owners and moderators can use this command (@FelixSFD)");
    		return;
    	}
    	
    	LOGGER.error("KILLED BY "+user.getName());
    	System.exit(0);
	}

	@Override
	public String description() {
		return "Kills the currently active instance";
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
