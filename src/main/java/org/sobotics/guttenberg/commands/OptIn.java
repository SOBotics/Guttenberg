package org.sobotics.guttenberg.commands;

import java.io.IOException;

import org.sobotics.guttenberg.services.RunnerService;
import org.sobotics.guttenberg.utils.CommandUtils;
import org.sobotics.guttenberg.utils.FilePathUtils;
import org.sobotics.guttenberg.utils.FileUtils;

import org.sobotics.chatexchange.chat.Message;
import org.sobotics.chatexchange.chat.Room;
import org.sobotics.chatexchange.chat.User;

public class OptIn implements SpecialCommand {

	private static final String CMD = "opt-in";
	private Message message;

    public OptIn(Message message) {
        this.message = message;
    }

    @Override
    public boolean validate() {
        return CommandUtils.checkForCommand(message.getPlainContent(), CMD);
    }

	@Override
	public void execute(Room room, RunnerService instance) {
		User user = message.getUser();
        long userId = user.getId();
        String userName = user.getName();
        String filename = FilePathUtils.optedUsersFile;

        String data = CommandUtils.extractData(message.getPlainContent()).trim();

        String pieces[] = data.split(" ");
        
        if(pieces.length>=1){
        	double minScore = -1;
        	boolean whenInRoom = true;
        	try {
        		minScore = new Double(pieces[0]);
        	} catch (Throwable e){
        		room.replyTo(message.getId(), "Invalid minimum score.");
        		return;
        	}
        	
        	if (pieces.length >= 2) {
        		if (pieces[1].equals("always")) {
        			whenInRoom = false;
        		}
        	}
        	
        	String optMessage = userId+",\""+userName+"\""+","+room.getRoomId()+","+whenInRoom;
        	
        	minScore = Math.round(minScore*100.0)/100.0;
        	
        	try {
				if (FileUtils.checkIfLineInFileStartsWith(filename, optMessage)) {
					room.replyTo(message.getId(), "You've already been added");
				} else {
					optMessage += ","+minScore;
					FileUtils.appendToFile(filename, optMessage);
					room.replyTo(message.getId(), "You will be notified about possible plagiarism with a score of "+minScore+" or higher.");
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
        	
        } else {
        	room.replyTo(message.getId(), "Invalid command. Correct usage: `opt-in <score> <always?>`");
        }
	}

	@Override
	public String description() {
		return "Get notified about possible plagiarism with a certain score. Usage: opt-in <score> <always?>";
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
