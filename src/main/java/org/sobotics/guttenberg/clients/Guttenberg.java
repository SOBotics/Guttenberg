package org.sobotics.guttenberg.clients;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.sobotics.guttenberg.finders.NewAnswersFinder;
import org.sobotics.guttenberg.finders.PlagFinder;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import fr.tunaki.stackoverflow.chat.Room;
import fr.tunaki.stackoverflow.chat.StackExchangeClient;

/**
 * Fetches and analyzes the data from the API
 * */
public class Guttenberg {	
	private StackExchangeClient client;
    //private List<BotRoom> rooms;
    //private List<Room> chatRooms;
    private ScheduledExecutorService executorService;
	
	public Guttenberg(StackExchangeClient client) {
		this.client = client;
		this.executorService = Executors.newSingleThreadScheduledExecutor();
	}
	
	public void start() {
		executorService.scheduleAtFixedRate(()->execute(), 0, 59, TimeUnit.SECONDS);
	}
	
	private void execute() {
		System.out.println("Executing...");
		NewAnswersFinder answersFinder = new NewAnswersFinder();
		
		//Fetch recent answers / The targets
		
		JsonArray recentAnswers = answersFinder.findRecentAnswers();
		List<PlagFinder> plagFinders = new ArrayList<PlagFinder>();
		
		for (JsonElement answer : recentAnswers) {
			PlagFinder plagFinder = new PlagFinder(answer.getAsJsonObject());
			plagFinders.add(plagFinder);
		}
	}
}
