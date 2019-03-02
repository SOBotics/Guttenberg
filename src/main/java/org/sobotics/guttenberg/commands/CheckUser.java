package org.sobotics.guttenberg.commands;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sobotics.chatexchange.chat.Message;
import org.sobotics.chatexchange.chat.Room;
import org.sobotics.guttenberg.clients.Guttenberg;
import org.sobotics.guttenberg.entities.Post;
import org.sobotics.guttenberg.entities.PostMatch;
import org.sobotics.guttenberg.finders.PlagFinder;
import org.sobotics.guttenberg.search.SearchTerms;
import org.sobotics.guttenberg.search.UserAnswer;
import org.sobotics.guttenberg.search.UserAnswerLine;
import org.sobotics.guttenberg.services.ApiService;
import org.sobotics.guttenberg.services.RunnerService;
import org.sobotics.guttenberg.utils.ApiUtils;
import org.sobotics.guttenberg.utils.CommandUtils;
import org.sobotics.guttenberg.utils.FilePathUtils;
import org.sobotics.guttenberg.utils.FileUtils;
import org.sobotics.guttenberg.utils.JsonUtils;
import org.sobotics.guttenberg.utils.PostUtils;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

/**
 * Command to check all post of a user.
 * 
 * @author Petter Friberg
 *
 */

public class CheckUser extends CheckInternet {

	private static final Logger LOGGER = LoggerFactory.getLogger(CheckUser.class);
	private static final String CMD = "checkuser";
	private static final int MIN_LENGTH_FOR_VALID_API_SEARCH = 30;

	public CheckUser(Message message) {
		super(message);

	}

	@Override
	public boolean validate() {
		return CommandUtils.checkForCommand(message.getPlainContent(), CMD);
	}

	@Override
	public void execute(Room room, RunnerService instance) {
		String cmd = message.getPlainContent();

		// Get userid
		int index = cmd.indexOf(CMD);
		if (index == -1) {
			LOGGER.warn("This command should not have been invoked with: " + message.getPlainContent());
			return;
		}
		
		

		Integer userId = PostUtils.getIdFromLink(cmd.substring(index, cmd.length()));
		if (userId == null) {
			room.replyTo(message.getId(), "Could not find user id in command please check your syntax");
			return;
		}
		Properties prop = Guttenberg.getLoginProperties();
		LOGGER.info("Executing command on user id: " + userId);
		
		boolean log = cmd.contains("-log");
		
		boolean googleOk = true;
		try {
			/**
			 * It would have been better with Long, but util method takes
			 * Integer
			 */
			List<Integer> idAnswers = getUsersAnswers(prop, userId);
			if (idAnswers.isEmpty()) {
				room.send("User: " + userId + " has no answers");
				return;
			}
			room.send("Check user: " + userId + " - START");
			JsonObject answers = ApiService.defaultService.getAnswerDetailsByIds(idAnswers, "desc","votes");
			int nr = 1;
			if (answers.has(ITEMS)) {
						
				for (JsonElement element : answers.get(ITEMS).getAsJsonArray()) {
					//notify that was are still working
					if (nr%25==0){
						room.send("Checked " + nr + " answer, continuing to search");
					}
					
					JsonObject object = element.getAsJsonObject();
					Post post = PostUtils.getPost(object);
					int relatedHits;
					int SEApiHits=0;
					int goggleHits=0;
					
					PlagFinder plagFinder = new PlagFinder(post);
					plagFinder.collectData();
					relatedHits = plagFinder.getRelatedAnswers().size();
					
					UserAnswer answer = new UserAnswer(post);
					UserAnswerLine search = answer.getSearchString(true);
					String apiSearch = search.getSearch();
					
					try {
						if (apiSearch.length()>MIN_LENGTH_FOR_VALID_API_SEARCH){
							SEApiHits = plagFinder.addSEApiSearch(apiSearch);
						}
					} catch (Exception e) {
						LOGGER.error("Error executing SE Search", e);
					}
					
					SearchTerms st=null;
					//Search on google if last ok and SE api hits either = 0 or too many >50 bad search)
					if (googleOk && (SEApiHits<=0 || SEApiHits>50)){
						st = new SearchTerms(post);
						LOGGER.info(st.toString());
						try {
							goggleHits = plagFinder.addGoogleSearchData(st);
						} catch (IOException e) {
							googleOk = false;
							LOGGER.error("Error executing Google Search - turn off google");
						}
					}
					
					List<PostMatch> matches = plagFinder.matchesForReasons(true);
					
					if (log){
						String message = "[" + post.getAnswerID()+ "](https://stackoverflow.com/a/" + post.getAnswerID() + ") Related/Linked: (" + relatedHits + ")";
						if (apiSearch.length()>MIN_LENGTH_FOR_VALID_API_SEARCH){
							message += ", SEAPI=" + apiSearch +" (" + SEApiHits + ")";
						}
						if (st!=null){
							message += ", Google=" + st.getQuery() + " exact=" + st.getExactTerm() + " (" + goggleHits + ")";
						}
						sendChatMessage(room, message);
					}
					
					for (PostMatch postMatch : matches) {
						if (postMatch.getTotalScore()>0.75){
							outputDirectHit(room, postMatch);
						}
					}
					
					try {
						Thread.sleep(200); //Throttle some for API
					} catch (InterruptedException e) {
						//do nothing
					} 
					nr++;									
				}
			}


		} catch (IOException e) {
			LOGGER.error("Error calling API", e);
			room.replyTo(message.getId(), "Error executing search -  " + e.getMessage());
		}
		room.send("Terminated search");
		
	}
	
	
	

	/**
	 * Get all answer of a user, probably should be refractored to PostUtils.
	 * 
	 * @param app,
	 *            properties
	 * @param userId,
	 *            the id of user
	 * @return List of Integer
	 * @throws IOException
	 */

	public List<Integer> getUsersAnswers(Properties app, long userId) throws IOException {

		List<Integer> answerIds = new ArrayList<>();
		String url = "http://api.stackexchange.com/2.2/users/" + userId + "/answers";

		JsonObject json = JsonUtils.get(url, "order", "desc", "sort", "votes", "site", STACKOVERFLOW, "pagesize", "100", "page", "1", "key",
				app.getProperty("apikey", ""));

		if (json.has(ITEMS)) {
			for (JsonElement element : json.get(ITEMS).getAsJsonArray()) {
				JsonObject object = element.getAsJsonObject();
				if (object.has("answer_id")) {
					answerIds.add(object.get("answer_id").getAsInt());
				}
			}
		}
		return answerIds;
	}

	@Override
	public String description() {
		return "Checks posts of user for plagiarism: checkuser <userId>";
	}

	@Override
	public String name() {
		return CMD;
	}

	@Override
	public boolean availableInStandby() {
		return false;
	}

	/**
	 * Only for local off chat testing
	 * 
	 * @param args
	 * @throws Exception
	 */
	public static void main(String[] args) throws Exception {
		CheckUser cu = new CheckUser(null);
		Properties prop = new Properties();

		try {
			prop = FileUtils.getPropertiesFromFile(FilePathUtils.loginPropertiesFile);
		} catch (IOException e) {
			LOGGER.error("Could not read login.properties", e);
		}

		List<Integer> ret = cu.getUsersAnswers(prop, 5292302);
		System.out.println(ret);

		JsonObject answers = ApiUtils.getAnswerDetailsByIds(ret, STACKOVERFLOW, prop.getProperty("apikey", ""));
		if (answers.has(ITEMS)) {
			for (JsonElement element : answers.get(ITEMS).getAsJsonArray()) {
				JsonObject object = element.getAsJsonObject();
				System.out.println(object);
			}
		}

	}

}
