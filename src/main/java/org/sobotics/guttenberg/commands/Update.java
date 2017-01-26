package org.sobotics.guttenberg.commands;

import org.sobotics.guttenberg.clients.Updater;
import org.sobotics.guttenberg.utils.CommandUtils;

import fr.tunaki.stackoverflow.chat.Message;
import fr.tunaki.stackoverflow.chat.Room;

public class Update implements SpecialCommand {

	private Message message;

    public Update(Message message) {
        this.message = message;
    }
	
    @Override
	public boolean validate() {
		return CommandUtils.checkForCommand(message.getPlainContent(),"update");
	}

	@Override
	public void execute(Room room) {
		System.out.println("Load updater...");
		Updater updater = new Updater();
		System.out.println("Check for updates...");
		
		int update = updater.updateIfAvailable(); 
		
		if (update == -1) {
			room.send("Automatic update failed!");
		} else if (update == 1) {
			room.send("Rebooting for update to version "+updater.getNewVersion().get());
			room.leave();
			try {
				wait(10);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			System.exit(0);
		}
	}

	@Override
	public String description() {
		return "Checks, if an update is available";
	}

	@Override
	public String name() {
		return "update";
	}

}
