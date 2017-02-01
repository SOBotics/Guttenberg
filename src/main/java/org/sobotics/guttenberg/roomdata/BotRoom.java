package org.sobotics.guttenberg.roomdata;

import fr.tunaki.stackoverflow.chat.Room;
import fr.tunaki.stackoverflow.chat.event.UserMentionedEvent;

import java.util.function.Consumer;

import org.sobotics.guttenberg.clients.Guttenberg;
import org.sobotics.guttenberg.printers.PostPrinter;

/**
 * Created by bhargav.h on 28-Dec-16.
 */
public interface BotRoom {

    public int getRoomId();
    public Consumer<UserMentionedEvent> getMention(Room room, Guttenberg instance);
    //public Consumer<MessageReplyEvent> getReply(Room room);
    //public Validator getValidator();
    public PostPrinter getPostPrinter();
    public boolean getIsLogged();

}
