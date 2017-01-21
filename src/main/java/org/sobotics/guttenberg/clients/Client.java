package org.sobotics.guttenberg.clients;

import java.io.FileInputStream;
import java.io.IOException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import org.sobotics.guttenberg.roomdata.BotRoom;
import org.sobotics.guttenberg.roomdata.SOBoticsChatRoom;
import org.sobotics.guttenberg.utils.FilePathUtils;
import org.sobotics.guttenberg.utils.StatusUtils;

import fr.tunaki.stackoverflow.chat.StackExchangeClient;


/**
 * The main class
 * */
public class Client {

	public static void main(String[] args) {
		System.out.println("Hello, World!");
		System.out.println("Load properties...");
		
		Properties prop = new Properties();

        try{
            prop.load(new FileInputStream(FilePathUtils.loginPropertiesFile));
        }
        catch (IOException e){
        	e.printStackTrace();
        	System.out.println("Could not load login.properties! Shutting down...");
        	return;
        }
		
		System.out.println("Initialize chat...");
		StackExchangeClient seClient = new StackExchangeClient(prop.getProperty("email"), prop.getProperty("password"));
		
		List<BotRoom> rooms = new ArrayList<>();
        rooms.add(new SOBoticsChatRoom());
		
        System.out.println("Launch Guttenberg...");
        
		Guttenberg guttenberg = new Guttenberg(seClient, rooms);
		
		guttenberg.start();
		
		StatusUtils.startupDate = Instant.now();
		System.out.println(StatusUtils.startupDate + " - Successfully launched Guttenberg!");
	}

}
