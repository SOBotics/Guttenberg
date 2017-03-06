package org.sobotics.guttenberg.roomdata;

import java.util.function.Consumer;

import org.sobotics.guttenberg.commandlists.SoBoticsCommandsList;
import org.sobotics.guttenberg.printers.PostPrinter;
import org.sobotics.guttenberg.printers.SoBoticsPostPrinter;
import org.sobotics.guttenberg.services.RunnerService;

import fr.tunaki.stackoverflow.chat.ChatHost;
import fr.tunaki.stackoverflow.chat.Room;
import fr.tunaki.stackoverflow.chat.event.UserMentionedEvent;

public class SOGuttenbergTestingFacility implements BotRoom {

	@Override
	public int getRoomId() {
		return 54445;
	}
	
	@Override
	public boolean getIsProductionRoom() {
		return false;
	}
	
	@Override
	public ChatHost getHost() {
		return ChatHost.STACK_EXCHANGE;
	}

	@Override
    public Consumer<UserMentionedEvent> getMention(Room room, RunnerService instance) {
        return event->new SoBoticsCommandsList().mention(room, event, true, instance);
    }

    /*@Override
    public Consumer<MessageReplyEvent> getReply(Room room) {
        return event-> PostUtils.reply(room, event, true);
    }*/

    @Override
    public PostPrinter getPostPrinter() {
        return new SoBoticsPostPrinter();
    }

    @Override
    public boolean getIsLogged() {
        return true;
    }

}
