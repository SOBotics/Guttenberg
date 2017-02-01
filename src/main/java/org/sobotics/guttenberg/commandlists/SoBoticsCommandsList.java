package org.sobotics.guttenberg.commandlists;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sobotics.guttenberg.commands.*;

import fr.tunaki.stackoverflow.chat.Message;
import fr.tunaki.stackoverflow.chat.Room;
import fr.tunaki.stackoverflow.chat.event.PingMessageEvent;

import org.sobotics.guttenberg.clients.Guttenberg;


/**
 * Created by bhargav.h on 28-Oct-16.
 */
public class SoBoticsCommandsList {

    private static final Logger LOGGER = LoggerFactory.getLogger(SoBoticsCommandsList.class);

    public void mention(Room room, PingMessageEvent event, boolean isReply, Guttenberg instance){
        /*if(CheckUtils.checkIfUserIsBlacklisted(event.getUserId()))
            return;*/

        Message message = event.getMessage();
        System.out.println("Mention:"+message);
        List<SpecialCommand> commands = new ArrayList<>(Arrays.asList(
            new Alive(message),
            new Check(message),
            new ClearHelp(message),
            new Quota(message),
            new Say(message),
            new Status(message),
            new Update(message),
            new Pfiatdi(message),
            new Reboot(message)
        ));

        commands.add(new Commands(message,commands));
        
        for(SpecialCommand command: commands){
            if(command.validate()){
                command.execute(room, instance);
            }
        }
        
        LOGGER.info(event.getMessage().getContent());
    }
}
