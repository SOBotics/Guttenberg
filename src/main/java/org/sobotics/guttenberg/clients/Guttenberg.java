package org.sobotics.guttenberg.clients;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sobotics.guttenberg.finders.NewAnswersFinder;
import org.sobotics.guttenberg.entities.OptedInUser;
import org.sobotics.guttenberg.finders.PlagFinder;
import org.sobotics.guttenberg.finders.RelatedAnswersFinder;
import org.sobotics.guttenberg.printers.SoBoticsPostPrinter;
import org.sobotics.guttenberg.roomdata.BotRoom;
import org.sobotics.guttenberg.utils.FilePathUtils;
import org.sobotics.guttenberg.utils.StatusUtils;
import org.sobotics.guttenberg.utils.UserUtils;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import fr.tunaki.stackoverflow.chat.ChatHost;
import fr.tunaki.stackoverflow.chat.Room;
import fr.tunaki.stackoverflow.chat.StackExchangeClient;
import fr.tunaki.stackoverflow.chat.event.EventType;
import fr.tunaki.stackoverflow.chat.event.UserMentionedEvent;

/**
 * Fetches and analyzes the data from the API
 * */
public class Guttenberg {    
    
    private static final Logger LOGGER = LoggerFactory.getLogger(Guttenberg.class);
    
    private final StackExchangeClient client;
    private final List<BotRoom> rooms;
    private final List<Room> chatRooms;
    private ScheduledExecutorService executorService;
    private ScheduledExecutorService executorServiceCheck;
    private ScheduledExecutorService executorServiceUpdate;
    private ScheduledExecutorService executorServiceLogCleaner;
    
    public Guttenberg(StackExchangeClient client, List<BotRoom> rooms) {
        this.client = client;
        this.rooms = rooms;
        this.executorService = Executors.newSingleThreadScheduledExecutor();
        this.executorServiceCheck = Executors.newSingleThreadScheduledExecutor();
        this.executorServiceUpdate = Executors.newSingleThreadScheduledExecutor();
        chatRooms = new ArrayList<>();
        
        //this.setLogfile();
    }
    
    public void start() {
        for(BotRoom room:rooms){
            Room chatroom = client.joinRoom(ChatHost.STACK_OVERFLOW ,room.getRoomId());

            if(room.getRoomId()==111347){
                //check if Guttenberg is running on the server
                Properties prop = new Properties();

                try{
                    prop.load(new FileInputStream(FilePathUtils.loginPropertiesFile));
                }
                catch (IOException e){
                    LOGGER.error("login.properties could not be loaded!", e);
                }
                
                if (prop.getProperty("location").equals("server")) {
                    chatroom.send("Grias di o/ (SERVER VERSION)" );
                } else {
                    chatroom.send("Grias di o/ (DEVELOPMENT VERSION; "+prop.getProperty("location")+")" );
                }
            }

            chatRooms.add(chatroom);
            
            Consumer<UserMentionedEvent> mention = room.getMention(chatroom, this);
            if(mention != null) {
                chatroom.addEventListener(EventType.USER_MENTIONED, mention);
            }
            /*if(room.getReply(chatroom)!=null)
                chatroom.addEventListener(EventType.MESSAGE_REPLY, room.getReply(chatroom));*/
        }
        
        
        executorService.scheduleAtFixedRate(()->secureExecute(), 15, 59, TimeUnit.SECONDS);
        executorServiceCheck.scheduleAtFixedRate(()->checkLastExecution(), 3, 5, TimeUnit.MINUTES);
        executorServiceUpdate.scheduleAtFixedRate(()->update(), 0, 30, TimeUnit.MINUTES);
        executorServiceLogCleaner.scheduleAtFixedRate(()->cleanLogs(), 0, 4, TimeUnit.HOURS);
    }
    
    /**
     * Executes `excecute()` and catches all the errors
     * 
     * @see http://stackoverflow.com/a/24902026/4687348
     * */
    private void secureExecute() {
        try {
            execute();
        } catch (Throwable e) {
            LOGGER.error("Error throws in execute()", e);
        }
    }
    
    private void execute() throws Throwable {
        Instant startTime = Instant.now();
        LOGGER.info("Executing at - "+startTime);
        //NewAnswersFinder answersFinder = new NewAnswersFinder();
        
        //Fetch recent answers / The targets
        JsonArray recentAnswers = NewAnswersFinder.findRecentAnswers();
        
        //Fetch their question_ids
        List<Integer> ids = new ArrayList<Integer>();
        for (JsonElement answer : recentAnswers) {
            Integer id = answer.getAsJsonObject().get("question_id").getAsInt();
            if (!ids.contains(id))
                ids.add(id);
        }
        
        
        //Initialize the PlagFinders
        List<PlagFinder> plagFinders = new ArrayList<PlagFinder>();
        
        for (JsonElement answer : recentAnswers) {
            PlagFinder plagFinder = new PlagFinder(answer.getAsJsonObject());
            plagFinders.add(plagFinder);
        }
        
        //fetch all /questions/ids/answers sort them later
        
        RelatedAnswersFinder related = new RelatedAnswersFinder(ids);
        List<JsonObject> relatedAnswersUnsorted = related.fetchRelatedAnswers();
        
        if (relatedAnswersUnsorted.isEmpty()) {
            LOGGER.warn("No related answers could be fetched. Skipping this execution...");
            return;
        }
        
        LOGGER.info("Add the answers to the PlagFinders...");
        //add relatedAnswers to the PlagFinders
        for (PlagFinder finder : plagFinders) {
            Integer targetId = finder.getTargetAnswerId();
            //System.out.println("TargetID: "+targetId);
            
            for (JsonObject relatedItem : relatedAnswersUnsorted) {
                //System.out.println(relatedItem);
                if (relatedItem.has("answer_id") && relatedItem.get("answer_id").getAsInt() != targetId) {
                    finder.relatedAnswers.add(relatedItem);
                    //System.out.println("Added answer: "+relatedItem);
                }
            }
        }
        
        LOGGER.info("Find the duplicates...");
        //Let PlagFinders find the best match
        for (PlagFinder finder : plagFinders) {
            JsonObject otherAnswer = finder.getMostSimilarAnswer();
            double score = finder.getJaroScore();
            if (score > 0.77) {
                for (Room room : this.chatRooms) {
                    List<OptedInUser> pingUsersList = UserUtils.pingUserIfApplicable(score, room.getRoomId());
                    if (room.getRoomId() == 111347) {
                        SoBoticsPostPrinter printer = new SoBoticsPostPrinter();
                        String report = printer.print(finder);
                        String pings = "(";
                        for (OptedInUser user : pingUsersList) {
                            if (!user.isWhenInRoom() || (user.isWhenInRoom() && UserUtils.checkIfUserInRoom(room, user.getUser().getUserId()))) {
                                pings+=(" @"+user.getUser().getUsername().replace(" ",""));
                            }
                        }
                        
                        if (pings.length() > 1) {
                            report += pings + " )";
                        }
                        
                        room.send(report);
                        //room.send(printer.print(finder));
                        //LOGGER.info("Posted: "+printer.print(finder));
                        StatusUtils.numberOfReportedPosts.incrementAndGet();
                    }
                }
            } else {
                //LOGGER.info("Score "+finder.getJaroScore()+" too low");
            }
            
            StatusUtils.numberOfCheckedTargets.incrementAndGet();
            
        }
        
        StatusUtils.lastSucceededExecutionStarted = startTime;
        StatusUtils.lastExecutionFinished = Instant.now();
        LOGGER.info("Finished at - "+StatusUtils.lastExecutionFinished);
    }
    
    /**
     * Checks when the last execution was successful. After a certain amount of time without succeeded executions, FelixSFD will be pinged.
     * */
    private void checkLastExecution() {
        if (StatusUtils.askedForHelp)
            return;
        
        Instant now = Instant.now();
        Instant lastSuccess = StatusUtils.lastExecutionFinished;
        
        long difference = now.getEpochSecond() - lastSuccess.getEpochSecond();
        
        //Instant criticalDate = now.minus(15, ChronoUnit.MINUTES);
        
        if (difference > 15*60) {
            for (Room room : this.chatRooms) {
                if (room.getRoomId() == 111347) {
                    room.send("@FelixSFD Please help me! The last successful execution finished at "+StatusUtils.lastExecutionFinished);
                    StatusUtils.askedForHelp = true;
                }
            }
        }
        
    }
    
    /**
     * Checks for updates
     * */
    private void update() {
        LOGGER.info("Load updater...");
        Updater updater = new Updater();
        LOGGER.info("Check for updates...");
        boolean update = false;
        try {
            update = updater.updateIfAvailable(); 
        } catch (Exception e) {
            LOGGER.error("Could not update", e);
            for (Room room : this.chatRooms) {
                if (room.getRoomId() == 111347) {
                    room.send("Automatic update failed!");
                }
            }
        }
        
        if (update) {
            for (Room room : this.chatRooms) {
                if (room.getRoomId() == 111347) {
                    room.send("Rebooting for update to version "+updater.getNewVersion().get());
                }
                room.leave();
            }
            try {
                wait(10);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            System.exit(0);
        }
        
    }
    
    /**
     * Removed old logfiles
     * 
     * @see http://stackoverflow.com/a/15042885/4687348
     * */
    private void cleanLogs() {
        int keepLogsForDays = 3;
        File logsDir = new File("./logs");
        
        for (File file : logsDir.listFiles()) {
            long diff = new Date().getTime() - file.lastModified();

            if (diff > keepLogsForDays * 24 * 60 * 60 * 1000) {
                try {
                    file.delete();
                } catch (SecurityException e) {
                    LOGGER.error("Could not delete file", e);
                }
            }
        }
    }
    
    public void resetExecutors() {
        executorService.shutdownNow();
        executorServiceCheck.shutdownNow();
        executorServiceUpdate.shutdownNow();
        executorServiceLogCleaner.shutdownNow();
        
        executorService = Executors.newSingleThreadScheduledExecutor();
        executorServiceCheck = Executors.newSingleThreadScheduledExecutor();
        executorServiceUpdate = Executors.newSingleThreadScheduledExecutor();
        executorServiceLogCleaner = Executors.newSingleThreadScheduledExecutor();
        
        executorService.scheduleAtFixedRate(()->secureExecute(), 15, 59, TimeUnit.SECONDS);
        executorServiceCheck.scheduleAtFixedRate(()->checkLastExecution(), 3, 5, TimeUnit.MINUTES);
        executorServiceUpdate.scheduleAtFixedRate(()->update(), 0, 30, TimeUnit.MINUTES);
        executorServiceLogCleaner.scheduleAtFixedRate(()->cleanLogs(), 0, 4, TimeUnit.HOURS);
    }
    
}
