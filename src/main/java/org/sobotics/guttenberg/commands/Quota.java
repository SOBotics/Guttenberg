package org.sobotics.guttenberg.commands;

import org.sobotics.guttenberg.utils.CommandUtils;
import org.sobotics.guttenberg.utils.FilePathUtils;
import org.sobotics.guttenberg.utils.StatusUtils;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sobotics.guttenberg.services.RunnerService;

import fr.tunaki.stackoverflow.chat.Message;
import fr.tunaki.stackoverflow.chat.Room;

public class Quota implements SpecialCommand {

	private static final Logger LOGGER = LoggerFactory.getLogger(Quota.class);
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
    	Properties prop = new Properties();

        try{
            prop.load(new FileInputStream(FilePathUtils.loginPropertiesFile));
        }
        catch (IOException e){
            LOGGER.error("Error: ", e);
            return;
        }
        
        room.send("The remaining quota on "+prop.getProperty("location", "undefined")+" is: "+StatusUtils.remainingQuota);
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
