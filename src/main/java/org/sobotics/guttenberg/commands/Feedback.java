package org.sobotics.guttenberg.commands;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sobotics.guttenberg.services.RunnerService;
import org.sobotics.guttenberg.utils.CommandUtils;
import org.sobotics.guttenberg.utils.PostUtils;

import fr.tunaki.stackoverflow.chat.Message;
import fr.tunaki.stackoverflow.chat.Room;
import fr.tunaki.stackoverflow.chat.event.PingMessageEvent;

/**
 * Created by bhargav.h on 29-Nov-16.
 */
public class Feedback implements SpecialCommand {

	private static final Logger LOGGER = LoggerFactory.getLogger(Feedback.class);
	
    private Message message;
    private PingMessageEvent event;
    private Room room;

    public Feedback(Message message, PingMessageEvent ping, Room room) {
        this.message = message;
        this.event = ping;
        this.room = room;
    }

    @Override
    public boolean validate() {
        return CommandUtils.checkForCommand(message.getPlainContent(),"feedback");
    }

    @Override
    public void execute(Room room, RunnerService instance) {
    	boolean isSELink = false;
    	int reportId = -1;
        String args[] = CommandUtils.extractData(message.getPlainContent()).trim().split(" ");

        if(args.length!=2){
            room.send("Error in arguments passed");
            return;
        }

        String word = args[0];
        String type = args[1];

        if(word.contains("/"))
        {
            word = CommandUtils.getAnswerId(word);
            isSELink = true;
        }
        
        try {
        	reportId = Integer.parseInt(word);
        } catch (Exception e) {
        	LOGGER.warn("Invalid report-ID", e);
        }
        
        if (reportId == -1)
        	return;
        
        LOGGER.debug("Sending feedback " + type + " for report " + reportId);
        
        try {
			if (type.equalsIgnoreCase("tp") || type.equalsIgnoreCase("k")) {
				if (!isSELink) {
					PostUtils.storeFeedback(this.room, this.event, reportId, "tp");
				}
			}
			
			if (type.equalsIgnoreCase("fp") || type.equalsIgnoreCase("f")) {
				if (!isSELink) {
					PostUtils.storeFeedback(this.room, this.event, reportId, "fp");
				}
			}
		} catch (IOException e) {
			LOGGER.error("Could not store feedback!", e);
		}
    }

    @Override
    public String description() {
        return "Provides feedback on a given report";
    }

    @Override
    public String name() {
        return "feedback";
    }

	@Override
	public boolean availableInStandby() {
		return false;
	}
}
