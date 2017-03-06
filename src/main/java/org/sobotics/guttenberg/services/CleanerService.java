package org.sobotics.guttenberg.services;

import java.io.File;
import java.util.Date;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import fr.tunaki.stackoverflow.chat.Room;

public class CleanerService {
	private Room room;
    private ScheduledExecutorService cleanerService;

    public CleanerService(Room room){
        this.room = room;
        this.cleanerService = Executors.newSingleThreadScheduledExecutor();
    }
    
    public void clean() {
    	cleanOldLogfiles();
    }
    
    /**
     * Deletes all files in ./logs that are older than 3 days
     *
     * @see http://stackoverflow.com/a/15042885/4687348
     * */
    private void cleanOldLogfiles() {
    	int keepLogsForDays = 3;
        File logsDir = new File("./logs");
        
        for (File file : logsDir.listFiles()) {
        	long diff = new Date().getTime() - file.lastModified();

        	if (diff > keepLogsForDays * 24 * 60 * 60 * 1000) {
        		try {
        			file.delete();
        		} catch (SecurityException e) {
        			e.printStackTrace();
        		}
        	}
        }
    }
    
    public void start(){
        Runnable cleaner = () -> clean();
        cleanerService.scheduleAtFixedRate(cleaner, 0, 4, TimeUnit.HOURS);
    }

    public void stop(){
        cleanerService.shutdown();
    }
}
