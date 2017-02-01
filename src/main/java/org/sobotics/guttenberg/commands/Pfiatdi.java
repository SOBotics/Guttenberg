package org.sobotics.guttenberg.commands;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.sobotics.guttenberg.utils.CommandUtils;

import fr.tunaki.stackoverflow.chat.Message;
import fr.tunaki.stackoverflow.chat.Room;

/**
 * Created by bhargav.h on 30-Sep-16.
 */
public class Pfiatdi implements SpecialCommand {

    private final Message message;

    public Pfiatdi(Message message) {
        this.message = message;
    }

    @Override
    public boolean validate() {
        return CommandUtils.checkForCommand(message.getPlainContent(),"cya") || 
                CommandUtils.checkForCommand(message.getPlainContent(),"o/") ||
                CommandUtils.checkForCommand(message.getPlainContent(),"bye");
    }

    @Override
    public void execute(Room room) {
        List<String> array = new ArrayList<>();
        array.add("[Pfiat di!](http://www.dictionary-german-english.com/en/dictionary-german-english/pfiat+di)");
        //array.add("[Pfiat di!](http://german.stackexchange.com/q/254)  o/");
        //array.add("[Pfiats eich!](http://german.stackexchange.com/q/254)");
        //array.add("[Pfüad di Gott](http://german.stackexchange.com/q/254) o/");
        
        int rnd = new Random().nextInt(array.size());
        
        room.send(array.get(rnd));
    }

    @Override
    public String description() {
        return "Psscht. Hier derfsch it naluaga! // Shhht. Don't look at this!";
    }

    @Override
    public String name() {
        return "secret";
    }
}
