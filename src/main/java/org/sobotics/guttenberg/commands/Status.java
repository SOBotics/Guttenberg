package org.sobotics.guttenberg.commands;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sobotics.guttenberg.utils.CommandUtils;
import org.sobotics.guttenberg.utils.FilePathUtils;
import org.sobotics.guttenberg.utils.StatusUtils;
import org.sobotics.guttenberg.services.RunnerService;

import fr.tunaki.stackoverflow.chat.Message;
import fr.tunaki.stackoverflow.chat.Room;

public class Status implements SpecialCommand {
    
    private static final Logger LOGGER = LoggerFactory.getLogger(Status.class);
    
    private final Message message;

    public Status(Message message) {
        this.message = message;
    }
    
    @Override
    public boolean validate() {
        return CommandUtils.checkForCommand(message.getPlainContent(),"status");
    }

    @Override
    public void execute(Room room, RunnerService instance) {
        Properties prop = new Properties();
        Properties prop2 = new Properties();
        
        try{
            prop.load(new FileInputStream(FilePathUtils.loginPropertiesFile));
            InputStream is = Status.class.getResourceAsStream("/guttenberg.properties");
               prop2.load(is);
        }
        catch (IOException e){
            LOGGER.error("Could not load properties", e);
        }
        
        
        StringBuilder status = new StringBuilder();
        status.append("Running since: ").append(StatusUtils.startupDate);
        
        if (room.getRoomId() == 111347) {
            status.append("\nLast execution finished: ").append(StatusUtils.lastExecutionFinished);
            status.append("\nLocation: ").append(prop.getProperty("location", "undefined"));
        }
        
        String version = prop2.getProperty("version", "undefined");
        status.append("\nVersion: ").append(version);
        status.append("\nChecked ").append(StatusUtils.numberOfCheckedTargets).append(" targets and reported ").append(StatusUtils.numberOfReportedPosts);
        status.append("\nRemaining quota: ").append(StatusUtils.remainingQuota);
        
        //room.replyTo(message.getId(), status.toString());
        room.send(status.toString());
    }

    @Override
    public String description() {
        return "Returns statistics about the current status";
    }

    @Override
    public String name() {
        return "status";
    }

	@Override
	public boolean availableInStandby() {
		return true;
	}

}
