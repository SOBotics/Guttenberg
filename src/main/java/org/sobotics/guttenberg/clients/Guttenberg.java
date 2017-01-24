package org.sobotics.guttenberg.clients;

import java.io.FileInputStream;
import java.io.IOException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.sobotics.guttenberg.finders.NewAnswersFinder;
import org.sobotics.guttenberg.finders.PlagFinder;
import org.sobotics.guttenberg.finders.RelatedAnswersFinder;
import org.sobotics.guttenberg.printers.SoBoticsPostPrinter;
import org.sobotics.guttenberg.roomdata.BotRoom;
import org.sobotics.guttenberg.utils.FilePathUtils;
import org.sobotics.guttenberg.utils.StatusUtils;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import fr.tunaki.stackoverflow.chat.ChatHost;
import fr.tunaki.stackoverflow.chat.Room;
import fr.tunaki.stackoverflow.chat.StackExchangeClient;
import fr.tunaki.stackoverflow.chat.event.EventType;

/**
 * Fetches and analyzes the data from the API
 * */
public class Guttenberg {	
	
	private StackExchangeClient client;
    private List<BotRoom> rooms;
    private List<Room> chatRooms;
    private ScheduledExecutorService executorService;
	
	public Guttenberg(StackExchangeClient client, List<BotRoom> rooms) {
		this.client = client;
		this.rooms = rooms;
		this.executorService = Executors.newSingleThreadScheduledExecutor();
		chatRooms = new ArrayList<>();
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
                    e.printStackTrace();
                }
            	
                if (prop.getProperty("location").equals("server")) {
                	chatroom.send("Grias di o/ (SERVER VERSION)" );
                } else {
                	chatroom.send("Grias di o/ (DEVELOPMENT VERSION; "+prop.getProperty("location")+")" );
                }
            }

            chatRooms.add(chatroom);
            if(room.getMention(chatroom)!=null)
                chatroom.addEventListener(EventType.USER_MENTIONED, room.getMention(chatroom));
            /*if(room.getReply(chatroom)!=null)
                chatroom.addEventListener(EventType.MESSAGE_REPLY, room.getReply(chatroom));*/
        }
		
		
		executorService.scheduleAtFixedRate(()->execute(), 0, 59, TimeUnit.SECONDS);
	}
	
	private void execute() {
		Instant startTime = Instant.now();
		System.out.println("Executing at - "+startTime);
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
			System.out.println("No related answers could be fetched. Skipping this execution...");
			return;
		}
		
		System.out.println("Add the answers to the PlagFinders...");
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
		
		System.out.println("Find the duplicates...");
		//Let PlagFinders find the best match
		for (PlagFinder finder : plagFinders) {
			JsonObject otherAnswer = finder.getMostSimilarAnswer();
			if (finder.getJaroScore() > 0.77) {
				for (Room room : this.chatRooms) {
					if (room.getRoomId() == 111347) {
						SoBoticsPostPrinter printer = new SoBoticsPostPrinter();
						room.send(printer.print(finder));
						System.out.println("Posted: "+printer.print(finder));
						StatusUtils.numberOfReportedPosts.incrementAndGet();
					} else {
						System.out.println("Not SOBotics");
					}
				}
			} else {
				System.out.println("Score "+finder.getJaroScore()+" too low");
			}
			
			StatusUtils.numberOfCheckedTargets.incrementAndGet();
			
		}
		
		StatusUtils.lastSucceededExecutionStarted = startTime;
		StatusUtils.lastExecutionFinished = Instant.now();
		System.out.println("Finished at - "+StatusUtils.lastExecutionFinished);
	}
}
