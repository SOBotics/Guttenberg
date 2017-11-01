package org.sobotics.guttenberg.commands;

import org.sobotics.guttenberg.clients.Updater;
import org.sobotics.guttenberg.services.RunnerService;
import org.sobotics.guttenberg.utils.CommandUtils;

import fr.tunaki.stackoverflow.chat.Message;
import fr.tunaki.stackoverflow.chat.Room;

public class Update implements SpecialCommand {
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
        System.out.println("Load updater...");
        Updater updater = new Updater();
        System.out.println("Check for updates...");
        
        boolean update = false;
        try {
            update = updater.updateIfAvailable(); 
        } catch (Exception e) {
            System.out.println(e.getMessage());
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
            e.printStackTrace();
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
