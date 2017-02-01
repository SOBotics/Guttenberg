package org.sobotics.guttenberg.commands;

import fr.tunaki.stackoverflow.chat.Room;

import org.sobotics.guttenberg.clients.Guttenberg;

/**
 * Created by bhargav.h on 30-Sep-16.
 */
public interface SpecialCommand {
    public boolean validate();
    public void execute(Room room, Guttenberg instance);
    public String description();
    public String name();
}
