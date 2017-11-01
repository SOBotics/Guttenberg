package org.sobotics.guttenberg.commands;

import org.sobotics.guttenberg.utils.CommandUtils;
import org.sobotics.guttenberg.services.RunnerService;

import fr.tunaki.stackoverflow.chat.Message;
import fr.tunaki.stackoverflow.chat.Room;

/**
 * Created by bhargav.h on 30-Sep-16.
 */
public class Say implements SpecialCommand {
	private static final String CMD = "say";
    private final Message message;

    public Say(Message message) {
        this.message = message;
    }

    @Override
    public boolean validate() {
        return CommandUtils.checkForCommand(message.getPlainContent(), CMD);
    }

    @Override
    public void execute(Room room, RunnerService instance) {
        room.send(CommandUtils.extractData(message.getPlainContent()));
    }

    @Override
    public String description() {
        return "Echoes the user input";
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
