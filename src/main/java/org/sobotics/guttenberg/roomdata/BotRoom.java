package org.sobotics.guttenberg.roomdata;

import org.sobotics.chatexchange.chat.ChatHost;
import org.sobotics.chatexchange.chat.Room;
import org.sobotics.chatexchange.chat.event.MessagePostedEvent;
import org.sobotics.chatexchange.chat.event.MessageReplyEvent;
import org.sobotics.chatexchange.chat.event.UserMentionedEvent;

import java.util.function.Consumer;

import org.sobotics.guttenberg.printers.PostPrinter;
import org.sobotics.guttenberg.services.RunnerService;

/**
 * Created by bhargav.h on 28-Dec-16.
 */
public interface BotRoom {

    public int getRoomId();
    public ChatHost getHost();
    
    /**
     * true, if the server-version of Guttenberg will run in this room. false for development-rooms
     * */
    public boolean getIsProductionRoom();
    public Consumer<UserMentionedEvent> getMention(Room room, RunnerService instance);
    public Consumer<MessageReplyEvent> getReply(Room room);
    public Consumer<MessagePostedEvent> getMessage(Room room, RunnerService instance);
    //public Validator getValidator();
    public PostPrinter getPostPrinter();
    public boolean getIsLogged();

}
