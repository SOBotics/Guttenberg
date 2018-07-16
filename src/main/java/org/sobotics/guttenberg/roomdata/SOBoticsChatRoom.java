package org.sobotics.guttenberg.roomdata;

import org.sobotics.chatexchange.chat.ChatHost;
import org.sobotics.chatexchange.chat.Room;
import org.sobotics.chatexchange.chat.event.MessagePostedEvent;
import org.sobotics.chatexchange.chat.event.MessageReplyEvent;
import org.sobotics.chatexchange.chat.event.UserMentionedEvent;

import java.util.function.Consumer;

import org.sobotics.guttenberg.commandlists.SoBoticsCommandsList;
import org.sobotics.guttenberg.printers.PostPrinter;
import org.sobotics.guttenberg.printers.SoBoticsPostPrinter;
import org.sobotics.guttenberg.services.RunnerService;
import org.sobotics.guttenberg.utils.PostUtils;

/**
 * Created by bhargav.h on 28-Dec-16.
 */
public class SOBoticsChatRoom implements BotRoom{
    @Override
    public int getRoomId() {
        return 111347;
    }
    
    @Override
	public ChatHost getHost() {
		return ChatHost.STACK_OVERFLOW;
	}
    
    @Override
	public boolean getIsProductionRoom() {
		return true;
	}

    @Override
    public Consumer<UserMentionedEvent> getMention(Room room, RunnerService instance) {
        return event->new SoBoticsCommandsList().mention(room, event, true, instance);
    }
    
    @Override
	public Consumer<MessagePostedEvent> getMessage(Room room, RunnerService instance) {
    	return event->new SoBoticsCommandsList().globalCommand(room, event, instance);
	}

    @Override
    public Consumer<MessageReplyEvent> getReply(Room room) {
        return event-> PostUtils.reply(room, event, true);
    }

    @Override
    public PostPrinter getPostPrinter() {
        return new SoBoticsPostPrinter();
    }

    @Override
    public boolean getIsLogged() {
        return true;
    }

}
