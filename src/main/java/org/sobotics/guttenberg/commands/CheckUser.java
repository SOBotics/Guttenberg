package org.sobotics.guttenberg.commands;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Formatter;
import java.util.List;
import java.util.Locale;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sobotics.guttenberg.clients.Guttenberg;
import org.sobotics.guttenberg.entities.Post;
import org.sobotics.guttenberg.printers.SoBoticsPostPrinter;
import org.sobotics.guttenberg.search.SearchItem;
import org.sobotics.guttenberg.search.SearchResult;
import org.sobotics.guttenberg.search.SearchTerms;
import org.sobotics.guttenberg.services.RunnerService;
import org.sobotics.guttenberg.utils.ApiUtils;
import org.sobotics.guttenberg.utils.CommandUtils;
import org.sobotics.guttenberg.utils.FilePathUtils;
import org.sobotics.guttenberg.utils.FileUtils;
import org.sobotics.guttenberg.utils.JsonUtils;
import org.sobotics.guttenberg.utils.PostUtils;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import org.sobotics.chatexchange.chat.Message;
import org.sobotics.chatexchange.chat.Room;

/**
 * Command to check all post of a user.
 * 
 * @author Petter Friberg
 *
 */

public class CheckUser extends CheckInternet {

	private static final Logger LOGGER = LoggerFactory.getLogger(CheckUser.class);
	private static final String CMD = "checkuser";

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
		List<SearchResult> results = new ArrayList<>();
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
			JsonObject answers = ApiUtils.getAnswerDetailsByIds(idAnswers, STACKOVERFLOW, prop.getProperty("apikey", ""));
			int nr = 1;
			if (answers.has(ITEMS)) {
				for (JsonElement element : answers.get(ITEMS).getAsJsonArray()) {
					JsonObject object = element.getAsJsonObject();
					Post post = PostUtils.getPost(object);
					SearchTerms st = new SearchTerms(post);
					LOGGER.info(st.toString());
					SearchResult result = checkPost(post, st);
					if (result != null) {
						results.add(result);
						if (result.getPostMatch() != null && result.getPostMatch().getTotalScore() > 0.75) {
							outputDirectHit(room, result);
							throttleForChat();
						}
					}
					if (nr>50){
						room.send("I have hit maximum number of post that I check on same user 50 (api-quota problem)");
						break;
					}
					nr++;
					
				}
			}


		} catch (IOException e) {
			LOGGER.error("Error calling API", e);
			room.replyTo(message.getId(), "Error calling search, maybe we ran out of quota");
		}

		if (!results.isEmpty()) {
			printReport(room, results);
		}
		
	}

	private void outputDirectHit(Room room, SearchResult result) {
		SoBoticsPostPrinter printer = new SoBoticsPostPrinter();
		room.send(printer.print(result.getPostMatch()));
	}

	private void printReport(Room room, List<SearchResult> results) {
		StringBuilder sb = new StringBuilder();
		Formatter formatter = new Formatter(sb, Locale.US);
		formatter.format("%6s%6s%-40s%-50s%-50s", "#", "Score", " Post", "On-Site", "Off-site");
		sb.append("\n    ").append(new String(new char[65]).replace("\0", "-"));

		int i = 1;
		for (SearchResult sr : results) {
			sb.append("\n");
			SearchItem bestSOPost = sr.getFirstResult(true);
			SearchItem bestOffSitePost = sr.getFirstResult(false);

			double score = 0d;
			String postLink = "https://stackoverflow.com/a/" + sr.getPost().getAnswerID();
			String onsiteLink = "";
			String offsiteLink = "";
			if (sr.getPostMatch() != null) {
				score = sr.getPostMatch().getTotalScore();
				onsiteLink = "https://stackoverflow.com/a/" + sr.getPostMatch().getOriginal().getAnswerID();
			} else {
				if (bestSOPost != null) {
					onsiteLink = bestSOPost.getLink();
				}
			}
			if (bestOffSitePost != null) {
				offsiteLink = bestOffSitePost.getLink();
			}

			formatter.format("%6d%6.2f%-40s%-50s%-50s", i, score, " " + postLink, onsiteLink, offsiteLink);
			i++;
		}
		room.send(sb.toString());
		formatter.close();
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

		JsonObject json = JsonUtils.get(url, "sort", "activity", "site", STACKOVERFLOW, "pagesize", "100", "page", "1", "order", "desc", "key",
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
