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
import org.sobotics.guttenberg.entities.OptedInUser;
import org.sobotics.guttenberg.entities.Post;
import org.sobotics.guttenberg.finders.PlagFinder;
import org.sobotics.guttenberg.finders.RelatedAnswersFinder;
import org.sobotics.guttenberg.printers.SoBoticsPostPrinter;
import org.sobotics.guttenberg.utils.FilePathUtils;
import org.sobotics.guttenberg.utils.StatusUtils;
import org.sobotics.guttenberg.utils.UserUtils;

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
		Instant startTime = Instant.now();
		Properties props = new Properties();
		LOGGER.info("Executing at - "+startTime);
		//NewAnswersFinder answersFinder = new NewAnswersFinder();
		
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
		for (PlagFinder finder : plagFinders) {
			@SuppressWarnings("unused")
			Post originalAnswer = finder.getMostSimilarAnswer();
			double score = finder.getJaroScore();
			double minimumScore = 0.78;
			
			try {
				double s = new Double(props.getProperty("minimumScore", "0.78"));
				if (s > 0) {
					minimumScore = s;
				}
			} catch (Throwable e) {
				LOGGER.warn("Could not convert score from properties-file to double. Using hardcoded", e);
			}
			
			if (score > minimumScore) {
				for (Room room : this.chatRooms) {
					List<OptedInUser> pingUsersList = UserUtils.pingUserIfApplicable(score, room.getRoomId());
					if (room.getRoomId() == 111347) {
						SoBoticsPostPrinter printer = new SoBoticsPostPrinter();
						String report = printer.print(finder);
						String pings = " (";
						
						if (!finder.matchedPostIsRepost()) {
							//only ping if it's not a repost
							for (OptedInUser user : pingUsersList) {
	                            if (!user.isWhenInRoom() || (user.isWhenInRoom() && UserUtils.checkIfUserInRoom(room, user.getUser().getUserId()))) {
	                                pings+=(" @"+user.getUser().getUsername().replace(" ",""));
	                            }
	                        }
						}
						
						if (pings.length() > 2) {
							report += pings + " )";
						}
						
						room.send(report);
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
	
    
    /*public void resetExecutors() {
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
    }*/
    
}
