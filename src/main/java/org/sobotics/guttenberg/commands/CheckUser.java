package org.sobotics.guttenberg.commands;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sobotics.guttenberg.clients.Guttenberg;
import org.sobotics.guttenberg.entities.Post;
import org.sobotics.guttenberg.services.RunnerService;
import org.sobotics.guttenberg.utils.ApiUtils;
import org.sobotics.guttenberg.utils.CommandUtils;
import org.sobotics.guttenberg.utils.FilePathUtils;
import org.sobotics.guttenberg.utils.JsonUtils;
import org.sobotics.guttenberg.utils.PostUtils;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import fr.tunaki.stackoverflow.chat.Message;
import fr.tunaki.stackoverflow.chat.Room;


/**
 * Command to check all post of a user.
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

		try {
			/**
			 * It would have been better with Long, 
			 * but util method takes Integer
			 */
			List<Integer> idAnswers = getUsersAnswers(prop, userId);
			if (idAnswers.isEmpty()) {
				room.send("User: " + userId + " has no answers");
				return;
			}
			JsonObject answers = ApiUtils.getAnswerDetailsByIds(idAnswers, STACKOVERFLOW, prop.getProperty("apikey", ""));
			if (answers.has(ITEMS)) {
				for (JsonElement element : answers.get(ITEMS).getAsJsonArray()) {
					JsonObject object = element.getAsJsonObject();
					Post post = PostUtils.getPost(object);
					checkPost(room,post);
					throttleForChat();
				}
			}

		} catch (IOException e) {
			LOGGER.error("Error calling API", e);
			room.replyTo(message.getId(), "Error calling search, maybe we ran out of quota");
		}
		
		room.send("Check user completed");
	}

		
	/**
	 * Get all answer of a user, probably should be refractored to PostUtils. 
	 * @param app, properties
	 * @param userId, the id of user
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
	 * @param args
	 * @throws Exception
	 */
	public static void main(String[] args) throws Exception {
		CheckUser cu = new CheckUser(null);
		Properties prop = new Properties();
		
		try {
			prop.load(new FileInputStream(FilePathUtils.loginPropertiesFile));
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
