package org.sobotics.guttenberg.commands;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import java.util.jar.JarFile;

import org.sobotics.guttenberg.clients.Client;
import org.sobotics.guttenberg.clients.Guttenberg;
import org.sobotics.guttenberg.utils.ApiUtils;
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
		Properties prop2 = new Properties();
		
        try{
            prop.load(new FileInputStream(FilePathUtils.loginPropertiesFile));
            //prop2.load(new FileInputStream("src/main/resources/guttenberg.properties"));
            try (InputStream is = Status.class.getResourceAsStream("/guttenberg.properties")) {
            	prop2.load(is);
            }
        }
        catch (IOException e){
            e.printStackTrace();
        }
		
		
        StringBuilder status = new StringBuilder();
        status.append("Running since: "+Client.startupDate);
        
        if (room.getRoomId() == 111347) {
        	status.append("\nLast execution finished: "+Guttenberg.lastExecutionFinished);
        	status.append("\nLocation: "+prop.getProperty("location", "undefined"));
        }
        
        String version = prop2.getProperty("version", "undefined");
        status.append("\nVersion: "+version);
        status.append("\nChecked "+Guttenberg.numberOfCheckedTargets+" targets and reported "+Guttenberg.numberOfReportedPosts);
        
		
		room.replyTo(message.getId(), status.toString());
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
