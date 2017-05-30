package org.sobotics.guttenberg.clients;

import java.io.FileInputStream;
import java.io.IOException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sobotics.guttenberg.finders.NewAnswersFinder;
import org.sobotics.redunda.PingService;
import org.sobotics.guttenberg.entities.Post;
import org.sobotics.guttenberg.entities.PostMatch;
import org.sobotics.guttenberg.finders.PlagFinder;
import org.sobotics.guttenberg.finders.RelatedAnswersFinder;
import org.sobotics.guttenberg.printers.SoBoticsPostPrinter;
import org.sobotics.guttenberg.utils.FilePathUtils;
import org.sobotics.guttenberg.utils.StatusUtils;

import fr.tunaki.stackoverflow.chat.Room;

/**
 * Fetches and analyzes the data from the API
 * */
public class Guttenberg {    
    
    private static final Logger LOGGER = LoggerFactory.getLogger(Guttenberg.class);
    
    private final List<Room> chatRooms;
    
    public Guttenberg(List<Room> rooms) {
    	this.chatRooms = rooms;
    }
	
	/**
	 * Executes `execute()` and catches all the errors
	 * 
	 * @see http://stackoverflow.com/a/24902026/4687348
	 * */
	public void secureExecute() {
		try {
			execute();
		} catch (Throwable e) {
			LOGGER.error("Error throws in execute()", e);
		}
	}
	
	public void execute() throws Throwable {
		boolean standbyMode = PingService.standby.get();
		if (standbyMode == true) {
			LOGGER.info("STANDBY - " + Instant.now());
			return;
		}
		
		Instant startTime = Instant.now();
		Properties props = new Properties();
		LOGGER.info("Executing at - "+startTime);
		
		try {
			props.load(new FileInputStream(FilePathUtils.generalPropertiesFile));
		} catch (IOException e) {
			LOGGER.warn("Could not load general properties", e);
		}
		
		
		//Fetch recent answers / The targets
		List<Post> recentAnswers = NewAnswersFinder.findRecentAnswers();
		//Fetch their question_ids
		List<Integer> ids = new ArrayList<Integer>();
		for (Post answer : recentAnswers) {
			Integer id = answer.getQuestionID();
			if (!ids.contains(id))
				ids.add(id);
		}
		
		
		//Initialize the PlagFinders
		List<PlagFinder> plagFinders = new ArrayList<PlagFinder>();
		
		for (Post answer : recentAnswers) {
			PlagFinder plagFinder = new PlagFinder(answer);
			plagFinders.add(plagFinder);
		}
		
		//fetch all /questions/ids/answers sort them later
		RelatedAnswersFinder related = new RelatedAnswersFinder(ids);
		List<Post> relatedAnswersUnsorted = related.fetchRelatedAnswers();
		
		if (relatedAnswersUnsorted.isEmpty()) {
			LOGGER.warn("No related answers could be fetched. Skipping this execution...");
			return;
		}
		
		LOGGER.info("Add the answers to the PlagFinders...");
		//add relatedAnswers to the PlagFinders
		for (PlagFinder finder : plagFinders) {
			Integer targetId = finder.getTargetAnswerId();
			//System.out.println("TargetID: "+targetId);
			
			for (Post relatedItem : relatedAnswersUnsorted) {
				//System.out.println(relatedItem);
				if (relatedItem.getAnswerID() != null && relatedItem.getAnswerID() != targetId) {
					finder.relatedAnswers.add(relatedItem);
					//System.out.println("Added answer: "+relatedItem);
				}
			}
		}
		
		LOGGER.info("There are "+plagFinders.size()+" PlagFinders");
		LOGGER.info("Find the duplicates...");
		//Let PlagFinders find the best match
		List<PostMatch> allMatches = new ArrayList<PostMatch>();
		for (PlagFinder finder : plagFinders) {
			List<PostMatch> matchesInFinder = finder.matchesForReasons();
			
			if (matchesInFinder != null) {
				LOGGER.info("Found "+matchesInFinder.size()+" PostMatches in this PlagFinder");
				allMatches.addAll(matchesInFinder);
				
				for (PostMatch match : matchesInFinder) {
					if (match.isValidMatch()) {
						SoBoticsPostPrinter printer = new SoBoticsPostPrinter();
						String message = printer.print(match);
						
						for (Room room : this.chatRooms) {
							room.send(message);
						}
					}
				}
			}
			
		}
		
		StatusUtils.lastSucceededExecutionStarted = startTime;
		StatusUtils.lastExecutionFinished = Instant.now();
		LOGGER.info("Finished at - "+StatusUtils.lastExecutionFinished);
	}
    
}
