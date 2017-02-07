package org.sobotics.guttenberg.commands;

import java.io.IOException;

import org.sobotics.guttenberg.clients.Guttenberg;
import org.sobotics.guttenberg.utils.CommandUtils;
import org.sobotics.guttenberg.utils.FilePathUtils;
import org.sobotics.guttenberg.utils.FileUtils;

import fr.tunaki.stackoverflow.chat.Message;
import fr.tunaki.stackoverflow.chat.Room;
import fr.tunaki.stackoverflow.chat.User;

public class OptOut implements SpecialCommand {

    private Message message;

    public OptOut(Message message) {
        this.message = message;
    }

    @Override
    public boolean validate() {
        return CommandUtils.checkForCommand(message.getPlainContent(),"opt-out");
    }

    @Override
    public void execute(Room room, Guttenberg instance) {
        User user = message.getUser();
        long userId = user.getId();
        String userName = user.getName();
        String filename = FilePathUtils.optedUsersFile;

        String optMessage = userId+",\""+userName+"\""+","+room.getRoomId()+",";
        
        try {
            if (FileUtils.checkIfLineInFileStartsWith(filename, optMessage)) {
                FileUtils.removeFromFileStartswith(filename, optMessage);
                room.replyTo(message.getId(), "You've been removed.");
            } else {
                room.replyTo(message.getId(), "You are not opted-in or already opted-out.");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public String description() {
        return "Removed you from the list";
    }

    @Override
    public String name() {
        return "opt-out";
    }

}
