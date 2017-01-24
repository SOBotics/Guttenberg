package org.sobotics.guttenberg.commands;

import org.sobotics.guttenberg.utils.CommandUtils;
import org.sobotics.guttenberg.utils.StatusUtils;

import fr.tunaki.stackoverflow.chat.Message;
import fr.tunaki.stackoverflow.chat.Room;

public class ClearHelp implements SpecialCommand {

	private Message message;

    public ClearHelp(Message message) {
        this.message = message;
    }
	
    @Override
    public boolean validate() {
        return CommandUtils.checkForCommand(message.getPlainContent(),"clear");
    }

	@Override
	public void execute(Room room) {
		if (StatusUtils.askedForHelp == true) {
			StatusUtils.askedForHelp = false;
			room.replyTo(this.message.getId(), "Thank you for your help! :-)");
		} else {
			room.replyTo(this.message.getId(), "Thanks, but I didn't ask for help.");
		}
	}

	@Override
	public String description() {
		return "Tells the bot that everything is okay, after he warned that some executions failed";
	}

	@Override
	public String name() {
		return "clear";
	}

}
