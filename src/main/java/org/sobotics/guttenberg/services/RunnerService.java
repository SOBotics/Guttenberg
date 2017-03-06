package org.sobotics.guttenberg.services;

import java.io.FileInputStream;
import java.io.IOException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sobotics.guttenberg.clients.Guttenberg;
import org.sobotics.guttenberg.roomdata.BotRoom;
import org.sobotics.guttenberg.utils.FilePathUtils;

import fr.tunaki.stackoverflow.chat.Room;
import fr.tunaki.stackoverflow.chat.StackExchangeClient;
import fr.tunaki.stackoverflow.chat.event.EventType;
import fr.tunaki.stackoverflow.chat.event.UserMentionedEvent;

public class RunnerService {
	private StackExchangeClient client;
    private List<BotRoom> rooms;
    private List<Room> chatRooms;
    private ScheduledExecutorService executorService;
    
    private static final Logger LOGGER = LoggerFactory.getLogger(RunnerService.class);
    
    public RunnerService(StackExchangeClient client, List<BotRoom> rooms) {
        this.client = client;
        this.rooms = rooms;
        chatRooms = new ArrayList<>();
    }
    
    public void start() {
    	Properties prop = new Properties();
    	try{
            prop.load(new FileInputStream(FilePathUtils.loginPropertiesFile));
        }
        catch (IOException e){
            LOGGER.error("login.properties could not be loaded!", e);
        }
    	
    	String botLocation = prop.getProperty("location", "undefined-location");
    	
        for(BotRoom room:rooms){
        	if (botLocation.equals("server") == room.getIsProductionRoom()) {
        		Room chatroom = client.joinRoom(room.getHost(), room.getRoomId());
        		
        		//start services
        		CleanerService cleaner = new CleanerService(chatroom);
        		cleaner.start();
        		SelfCheckService selfCheck = new SelfCheckService(this);
        		selfCheck.start();
        		UpdateService update = new UpdateService(this);
        		update.start();
        		
        		if (prop.getProperty("location").equals("server")) {
                    chatroom.send("[Guttenberg](http://stackapps.com/q/7197/43403) launched (SERVER VERSION)" );
                } else {
                    chatroom.send("[Guttenberg](http://stackapps.com/q/7197/43403) launched (DEVELOPMENT VERSION; "+prop.getProperty("location")+")" );
                }
        		
        		chatRooms.add(chatroom);
                
                Consumer<UserMentionedEvent> mention = room.getMention(chatroom, this);
                if(mention != null) {
                    chatroom.addEventListener(EventType.USER_MENTIONED, mention);
                }
                /*if(room.getReply(chatroom)!=null)
                    chatroom.addEventListener(EventType.MESSAGE_REPLY, room.getReply(chatroom));*/
        	}

        }
        
        executorService = Executors.newSingleThreadScheduledExecutor();
        run();
    }
    
    public void run() {
    	Guttenberg instance = new Guttenberg(this.chatRooms);
    	executorService.scheduleAtFixedRate(()->execute(instance), 20, 59+1, TimeUnit.SECONDS);
    }
    
    private void execute(Guttenberg guttenberg) {
    	guttenberg.secureExecute();
    }
    
    public void stop(){
        executorService.shutdown();
    }
    
    public void reboot(){
        this.stop();
        executorService = Executors.newSingleThreadScheduledExecutor();
        this.run();
        for(int i = 0;i<rooms.size(); i++){
            if(rooms.get(i).getIsLogged()) {
                Room room = chatRooms.get(i);
                room.send("Rebooted at " + Instant.now());
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }
    
    public List<Room> getChatRooms() {
    	return this.chatRooms;
    }
}
