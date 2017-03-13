package org.sobotics.guttenberg.commands;

import org.sobotics.guttenberg.services.RunnerService;
import org.sobotics.guttenberg.utils.CommandUtils;

import fr.tunaki.stackoverflow.chat.Message;
import fr.tunaki.stackoverflow.chat.Room;
import fr.tunaki.stackoverflow.chat.User;

public class Kill implements SpecialCommand {

	private final Message message;
    
    public Kill(Message message) {
        this.message = message;
    }
    
	@Override
	public boolean validate() {
		return CommandUtils.checkForCommand(message.getPlainContent(), "kill");
	}

	@Override
	public void execute(Room room, RunnerService instance) {
		User user = message.getUser();
    	
    	if (!user.isModerator() && !user.isRoomOwner()) {
    		room.replyTo(message.getId(), "Sorry, but only room-owners and moderators can use this command");
    		return;
    	}
    	
    	System.out.println("KILLED BY "+user.getName());
    	System.exit(0);
	}

	@Override
	public String description() {
		return "Kills the currently active instance";
	}

	@Override
	public String name() {
		return "kill";
	}

	@Override
	public boolean availableInStandby() {
		return false;
	}

}
