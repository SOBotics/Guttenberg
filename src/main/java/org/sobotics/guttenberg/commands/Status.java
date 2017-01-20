package org.sobotics.guttenberg.commands;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

import org.sobotics.guttenberg.clients.Client;
import org.sobotics.guttenberg.clients.Guttenberg;
import org.sobotics.guttenberg.utils.CommandUtils;
import org.sobotics.guttenberg.utils.FilePathUtils;

import fr.tunaki.stackoverflow.chat.Message;
import fr.tunaki.stackoverflow.chat.Room;

public class Status implements SpecialCommand {
	
	private Message message;

    public Status(Message message) {
        this.message = message;
    }
    
	@Override
    public boolean validate() {
        return CommandUtils.checkForCommand(message.getPlainContent(),"status");
    }

	@Override
	public void execute(Room room) {
		Properties prop = new Properties();

        try{
            prop.load(new FileInputStream(FilePathUtils.loginPropertiesFile));
        }
        catch (IOException e){
            e.printStackTrace();
        }
		
		
		String status = "Running since: "+Client.startupDate;
		status += "Last execution finished: "+Guttenberg.lastExecutionFinished;
		status += "\nLocation: "+prop.getProperty("location", "undefined");
		
		//Get version
		String version = this.getClass().getPackage().getSpecificationVersion();
		status += "\nVersion: "+version;
		
		
		//JarFile file = new JarFile("./Guttenberg.jar");
		
		room.replyTo(message.getId(), status);
	}

	@Override
	public String description() {
		return "Returns statistics about the current status";
	}

	@Override
	public String name() {
		return "status";
	}

}
