package org.sobotics.guttenberg.clients;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import org.apache.log4j.Level;
import org.apache.log4j.LogManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sobotics.redunda.DataService;
import org.sobotics.redunda.PingService;
import org.sobotics.guttenberg.commands.Status;
import org.sobotics.guttenberg.roomdata.BotRoom;
import org.sobotics.guttenberg.roomdata.SOBoticsChatRoom;
import org.sobotics.guttenberg.roomdata.SOGuttenbergTestingFacility;
import org.sobotics.guttenberg.services.RunnerService;
import org.sobotics.guttenberg.utils.FilePathUtils;
import org.sobotics.guttenberg.utils.StatusUtils;

import fr.tunaki.stackoverflow.chat.StackExchangeClient;


/**
 * The main class
 * */
public class Client {
    
    private static final Logger LOGGER = LoggerFactory.getLogger(Client.class);

    private Client(){
    	super();
    }
    
    public static void main(String[] args) {
    	Properties loggerProperties = new Properties();
    	try{
    		loggerProperties.load(new FileInputStream(FilePathUtils.loggerPropertiesFile));
    		
    		String levelStr = loggerProperties.getProperty("level");
    		Level newLevel = Level.toLevel(levelStr, Level.ERROR);
    		LogManager.getRootLogger().setLevel(newLevel);
        }
        catch (Throwable e){
            LOGGER.error("Could not load logger.properties! Using default log-level ERROR.", e);
        }
    	
        LOGGER.info("============================");
        LOGGER.info("=== Launching Guttenberg ===");
        LOGGER.info("============================");
        LOGGER.info("Loading properties...");
        
        Properties prop = new Properties();

        try{
            prop.load(new FileInputStream(FilePathUtils.loginPropertiesFile));
        }
        catch (IOException e){
            e.printStackTrace();
            LOGGER.error("Error: ", e);
            LOGGER.error("Could not load login.properties! Shutting down...");
            return;
        }
        
        Guttenberg.setLoginProperties(prop);
        
        LOGGER.info("Initializing chat...");
        StackExchangeClient seClient = new StackExchangeClient(prop.getProperty("email"), prop.getProperty("password"));
        
        List<BotRoom> rooms = new ArrayList<>();
        rooms.add(new SOBoticsChatRoom());
        rooms.add(new SOGuttenbergTestingFacility());
        
        //get current version
        Properties guttenbergProperties = new Properties();
        String version = "0.0.0";
        try{
            InputStream is = Status.class.getResourceAsStream("/guttenberg.properties");
            guttenbergProperties.load(is);
            version = guttenbergProperties.getProperty("version", "0.0.0");
            LOGGER.info("Running on version " + version);
        }
        catch (IOException e){
            LOGGER.error("Could not load properties", e);
        }
        
        LOGGER.info("Connecting to Redunda...");
        PingService redunda = new PingService(prop.getProperty("redunda_apikey", ""), version);
        String productionInstance = prop.getProperty("production_instance", "false");
        
        
        //track files for synchronization
        DataService redundaData = redunda.buildDataService();
        redundaData.trackFile(FilePathUtils.optedUsersFile);
        redundaData.trackFile(FilePathUtils.generalPropertiesFile);
        redundaData.trackFile(FilePathUtils.blacklistedUsersFile);
        
        if (productionInstance.equals("false")) {
        	redunda.setDebugging(true);
        	LOGGER.info("Set Redunda debugging to true");
        } else {
        	//not debugging
        	LOGGER.info("Start synchronization...");
            redundaData.syncAndStart();
            LOGGER.info("Synchronization finished!");
        }
        
        //first check manually, so that RunnerService will know the status before posting the welcome message
        boolean isOnStandby = redunda.checkStandbyStatus();
        if (isOnStandby)
        	LOGGER.info("Launching in standby...");
        
        redunda.start();
        
        
        LOGGER.debug("Initialize RunnerService...");
        
        RunnerService runner = new RunnerService(seClient, rooms);
        
        runner.start();
        
        StatusUtils.startupDate = Instant.now();
        LOGGER.info("Successfully launched Guttenberg!");
    }

}
