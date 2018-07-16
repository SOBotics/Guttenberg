package org.sobotics.guttenberg.commands;

import org.sobotics.guttenberg.utils.CommandUtils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sobotics.redunda.PingService;
import org.sobotics.guttenberg.services.RunnerService;

import org.sobotics.chatexchange.chat.Message;
import org.sobotics.chatexchange.chat.Room;

/**
 * Created by bhargav.h on 30-Sep-16.
 */
public class Alive implements SpecialCommand {

	private static final Logger LOGGER = LoggerFactory.getLogger(Alive.class);
	private static final String CMD = "alive";
    private final Message message;

    public Alive(Message message) {
        this.message = message;
    }

    @Override
    public boolean validate() {
        return CommandUtils.checkForCommand(message.getPlainContent(), CMD);
    }

    @Override
    public void execute(Room room, RunnerService instance) {  
    	LOGGER.warn("Someone wants to know, if I'm alive");
        room.send("The instance "+PingService.location+ " is running; Standby: "+PingService.standby.toString());
    }

    @Override
    public String description() {
        return "Returns a test reply to inform that the bot is alive";
    }

    @Override
    public String name() {
        return CMD;
    }

	@Override
	public boolean availableInStandby() {
		return true;
	}
}
