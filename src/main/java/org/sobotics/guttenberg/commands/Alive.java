package org.sobotics.guttenberg.commands;

import org.sobotics.guttenberg.utils.CommandUtils;
import org.sobotics.guttenberg.utils.FilePathUtils;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sobotics.redunda.PingService;
import org.sobotics.guttenberg.commandlists.SoBoticsCommandsList;
import org.sobotics.guttenberg.services.RunnerService;

import fr.tunaki.stackoverflow.chat.Message;
import fr.tunaki.stackoverflow.chat.Room;

/**
 * Created by bhargav.h on 30-Sep-16.
 */
public class Alive implements SpecialCommand {

	private static final Logger LOGGER = LoggerFactory.getLogger(Alive.class);
    private final Message message;

    public Alive(Message message) {
        this.message = message;
    }

    @Override
    public boolean validate() {
        return CommandUtils.checkForCommand(message.getPlainContent(),"alive");
    }

    @Override
    public void execute(Room room, RunnerService instance) {
    	Properties prop = new Properties();

        try{
            prop.load(new FileInputStream(FilePathUtils.loginPropertiesFile));
        }
        catch (IOException e){
            LOGGER.error("Error: ", e);
            room.replyTo(message.getId(), "Maybe. But something strange is going on!");
            return;
        }
        room.send("The instance "+prop.getProperty("location", "undefined")+ " is running.\nStandby: "+PingService.standby.toString());
    }

    @Override
    public String description() {
        return "Returns a test reply to inform that the bot is alive";
    }

    @Override
    public String name() {
        return "alive";
    }

	@Override
	public boolean availableInStandby() {
		return true;
	}
}
