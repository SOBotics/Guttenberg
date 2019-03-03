package org.sobotics.guttenberg.commands;

import java.io.IOException;
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
import org.sobotics.guttenberg.printers.SoBoticsPostPrinter;
import org.sobotics.guttenberg.search.SearchTerms;
import org.sobotics.guttenberg.search.UserAnswer;
import org.sobotics.guttenberg.search.UserAnswerLine;
import org.sobotics.guttenberg.services.RunnerService;
import org.sobotics.guttenberg.utils.ApiUtils;
import org.sobotics.guttenberg.utils.CommandUtils;
import org.sobotics.guttenberg.utils.PostUtils;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

/**
 * Command to search for plagiarism using internet
 * 
 * @author Petter Friberg
 *
 */
public class CheckInternet implements SpecialCommand {

	private static final Logger LOGGER = LoggerFactory.getLogger(CheckInternet.class);
	protected static final String STACKOVERFLOW = "stackoverflow";
	protected static final String ITEMS = "items";
	private static final String CMD = "checkinternet";

	protected Message message;

	public CheckInternet(Message message) {
		this.message = message;

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

		Integer postId = PostUtils.getIdFromLink(cmd.substring(index, cmd.length()));

		if (postId == null) {
			room.replyTo(message.getId(), "Could not find answer id, check command syntax");
			return;
		}

		Properties prop = Guttenberg.getLoginProperties();
		Post post = null;

		LOGGER.info("Executing command on user id: " + postId);

		try {

			post = getPost(postId, prop);
			if (post == null) {
				room.replyTo(message.getId(), "Could not find post id: " + postId + " with api call");
				return;
			}

			int relatedHits;
			int SEApiHits = 0;
			int goggleHits = 0;

			PlagFinder plagFinder = new PlagFinder(post);
			plagFinder.collectData();
			relatedHits = plagFinder.getRelatedAnswers().size();

			UserAnswer answer = new UserAnswer(post);
			UserAnswerLine search = answer.getSearchString(true);
			String apiSearch = search.getSearch();

			try {
				SEApiHits = plagFinder.addSEApiSearch(apiSearch);
			} catch (IOException e) {
				LOGGER.error("Error executing SE Search", e);
			}

			SearchTerms st = null;
			// Search on google if last ok and SE api hits either = 0 or too
			// many >50 bad search)
			st = new SearchTerms(post);
			LOGGER.info(st.toString());
			try {
				goggleHits = plagFinder.addGoogleSearchData(st);
			} catch (IOException e) {
				LOGGER.error("Error executing Google Search",e);
			}

			List<PostMatch> matches = plagFinder.matchesForReasons(true);

			String message = "[" + post.getAnswerID() + "](https://stackoverflow.com/a/" + post.getAnswerID() + ") Related/Linked: (" + relatedHits + ")";
			message += ", SEAPI=" + apiSearch + " (" + SEApiHits + ")";
			message += ", Google=" + st.getQuery() + " exact=" + st.getExactTerm() + " (" + goggleHits + ")";
			sendChatMessage(room, message);

			for (PostMatch postMatch : matches) {
				if (postMatch.getTotalScore() > 0.75) {
					outputDirectHit(room, postMatch);
				}
			}

		} catch (IOException e) {
			LOGGER.error("Error calling API", e);
			room.replyTo(message.getId(), "Error calling search, maybe we ran out of quota");
		}

	}
	
	protected void sendChatMessage(Room room, String message){
		room.send(message);
		try {
			Thread.sleep(3000); //Throttle some for chat.
		} catch (InterruptedException e) {
			//Do nothing
		}
	}

	protected void outputDirectHit(Room room, PostMatch postMatch) {
		SoBoticsPostPrinter printer = new SoBoticsPostPrinter();
		room.send(printer.print(postMatch));
	}


	


	
	
	


	/**
	 * Load a Post from a post id (refrator to add to post util?
	 * 
	 * @param postId,
	 *            id post (it would be better if a Long)
	 * @param prop,
	 *            properties (maybe they should be static application wide)
	 * @return
	 * @throws IOException
	 */
	public Post getPost(int postId, Properties prop) throws IOException {
		Post post = null;
		JsonObject answer = ApiUtils.getAnswerDetailsById(postId, STACKOVERFLOW, prop.getProperty("apikey", ""));
		if (answer != null && answer.has(ITEMS)) {
			for (JsonElement element : answer.get(ITEMS).getAsJsonArray()) {
				JsonObject object = element.getAsJsonObject();
				post = PostUtils.getPost(object);
				break;
			}
		}
		return post;
	}

	@Override
	public String description() {
		return "Checks post for plagiarism using internet: checkinternet <answerId>";
	}

	@Override
	public String name() {
		return CMD;
	}

	@Override
	public boolean availableInStandby() {
		return false;
	}


}
