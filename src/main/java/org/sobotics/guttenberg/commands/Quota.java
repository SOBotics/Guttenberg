package org.sobotics.guttenberg.commands;

import org.sobotics.guttenberg.utils.CommandUtils;
import org.sobotics.guttenberg.utils.StatusUtils;
import org.sobotics.guttenberg.services.RunnerService;

import fr.tunaki.stackoverflow.chat.Message;
import fr.tunaki.stackoverflow.chat.Room;

public class Quota implements SpecialCommand {

    private final Message message;

    public Quota(Message message) {
        this.message = message;
    }
    
    @Override
    public boolean validate() {
        return CommandUtils.checkForCommand(message.getPlainContent(),"quota");
    }

    @Override
    public void execute(Room room, RunnerService instance) {
        room.replyTo(this.message.getId(), "The remaining quota is: "+StatusUtils.remainingQuota);
    }

    @Override
    public String description() {
        return "Returns the remaining api-quota";
    }

    @Override
    public String name() {
        return "quota";
    }

	@Override
	public boolean availableInStandby() {
		return false;
	}

}
