package org.sobotics.guttenberg.commands;

import org.sobotics.chatexchange.chat.Room;

import org.sobotics.guttenberg.services.RunnerService;

/**
 * Created by bhargav.h on 30-Sep-16.
 */
public interface SpecialCommand {
    public boolean validate();
    public void execute(Room room, RunnerService instance);
    public String description();
    public String name();
    public boolean availableInStandby();
}
