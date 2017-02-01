package org.sobotics.guttenberg.commands;

import java.io.IOException;
import java.util.Properties;
import java.io.InputStream;

import org.sobotics.guttenberg.utils.CommandUtils;
import org.sobotics.guttenberg.clients.Guttenberg;

import fr.tunaki.stackoverflow.chat.Message;
import fr.tunaki.stackoverflow.chat.Room;

/**
 *
 * @author owen
 */
public class Reboot implements SpecialCommand {
    private final Message message;
    
    public Reboot(Message message) {
        this.message = message;
    }
    
    @Override
    public boolean validate() {
        return CommandUtils.checkForCommand(message.getPlainContent(), "reboot");
    }

    @Override
    public void execute(Room room, Guttenberg instance) {
        System.out.println("REBOOT COMMAND");
        String[] args = message.getPlainContent().split(" ");
        if (args.length >= 3) {
            String rebootType = args[2];
            if (rebootType.equals("hard")) {
                this.hardReboot(room);
            }
            else if (rebootType.equals("soft")) {
                this.softReboot(room, instance);
            }
        }
        else {
            room.replyTo(message.getId(), "You didn't specify a reboot type, assuming hard.");
            this.hardReboot(room);
        }
    }

    @Override
    public String description() {
        return "Returns a test reply to inform that the bot is alive";
    }

    @Override
    public String name() {
        return "alive";
    }
    
    private void softReboot(Room room, Guttenberg instance) {
        instance.resetExecutors();
        room.replyTo(message.getId(), "Reset executor threads. To shutdown and restart, use `reboot hard`.");
    }
    
    private void hardReboot(Room room) {
        try {
            Properties properties = new Properties();
            InputStream is = Status.class.getResourceAsStream("/guttenberg.properties");
            properties.load(is);

            String versionString = (String)properties.get("version");

            Runtime.getRuntime().exec("nohup java -cp guttenberg-" + versionString + ".jar org.sobotics.guttenberg.clients.Client");
            System.exit(0);
        }
        catch (IOException e) {
            e.printStackTrace();
            room.replyTo(message.getId(), "**Reboot failed:** '" + e.getMessage() + "'.");
        }
    }
}
