package org.sobotics.guttenberg.utils;

import java.io.IOException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sobotics.guttenberg.clients.Guttenberg;
import org.sobotics.guttenberg.entities.Post;
import org.sobotics.guttenberg.entities.SOUser;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import fr.tunaki.stackoverflow.chat.ChatHost;
import fr.tunaki.stackoverflow.chat.Message;
import fr.tunaki.stackoverflow.chat.Room;
import fr.tunaki.stackoverflow.chat.event.PingMessageEvent;

public class PostUtils {

	private static final Logger LOGGER = LoggerFactory.getLogger(Guttenberg.class);

	private PostUtils() {
		super();
	}

	public static Post getPost(JsonObject answer) {

		Post np = new Post();

		JsonObject answererJSON = answer.get("owner").getAsJsonObject();

		np.setAnswerCreationDate(Instant.ofEpochSecond(answer.get("creation_date").getAsInt()));
		np.setAnswerID(answer.get("answer_id").getAsInt());
		np.setQuestionID(answer.get("question_id").getAsInt());
		np.setBody(answer.get("body").getAsString());
		np.setBodyMarkdown(JsonUtils.escapeHtmlEncoding(answer.get("body_markdown").getAsString()));
		np.setUnescapedBodyMarkdown(answer.get("body_markdown").getAsString());

		JsonArray jsonTags = new JsonArray();

		try {
			jsonTags = answer.get("tags").getAsJsonArray();

			List<String> tags = new ArrayList<String>();

			for (JsonElement tag : jsonTags) {
				tags.add(tag.getAsString());
			}

			np.setTags(tags);
		} catch (Throwable e) {
			// LOGGER.warn("No tags found");
		}

		SOUser answerer = new SOUser();

		try {
			answerer.setReputation(answererJSON.get("reputation").getAsLong());
			answerer.setUsername(JsonUtils.escapeHtmlEncoding(answererJSON.get("display_name").getAsString()));
			answerer.setUserType(answererJSON.get("user_type").getAsString());
			answerer.setUserId(answererJSON.get("user_id").getAsInt());
		} catch (NullPointerException e) {
			
		}

		np.setAnswerer(answerer);

		return np;

	}

	/**
	 * Splits a post into code, plaintext and quotes
	 * 
	 * @author ArtOfCode
	 */
	public static JsonObject separateBodyParts(Post post) {
		JsonObject result = new JsonObject();
		String markdown = post.getBodyMarkdown();
		String[] paragraphs = markdown.split("\\n{2,}");

		String plain = "", code = "", quote = "";
		for (String paragraph : paragraphs) {
			if (paragraph.trim().charAt(0) == '>') {
				quote += paragraph + "\n";
			} else if (paragraph.startsWith("    ")) {
				code += paragraph + "\n";
			} else {
				plain += paragraph + "\n";
			}
		}

		result.addProperty("body_plain", plain);
		result.addProperty("body_code", code);
		result.addProperty("body_quote", quote);

		return result;
	}

	public static List<String> getCodeParagraphs(String markdown) {
		List<String> output = new ArrayList<String>();

		String[] paragraphs = markdown.split("\\n");
		String buffer = "";

		for (String paragraph : paragraphs) {
			if (paragraph.startsWith("    ")) {
				// found a codeblock -> add to buffer
				buffer += paragraph + "\n";
			} else {
				// a non-code paragraph. This resets the codeblock
				// -> write buffer to array; then clear buffer
				if (buffer.length() > 4) {
					output.add(buffer);
				}
				buffer = "";
			}
		}

		return output;
	}

	public static void reply(Room room, PingMessageEvent event, boolean isReply) {
		Message message = event.getMessage();
		Message parentMessage = room.getMessage(event.getParentMessageId());
		long parentMessageId = parentMessage.getId();
		System.out.println(message.getContent());
		/*
		 * if (CheckUtils.checkIfUserIsBlacklisted(event.getUserId())){
		 * System.out.println("Blacklisted user"); return; }
		 */
		
		PostUtils.checkPingForFeedback(room, event);
		
		// check if message is a report before editing it
		boolean isReport = true;
		if (!parentMessage.getPlainContent().startsWith("[ [")) {
			if (parentMessage.getPlainContent().startsWith("---")) {
				LOGGER.info("This post has already been handled");
				return;
			}

			isReport = false;
		}

		String newMessage = "";
		boolean isValidFeedback = false;
		if (CommandUtils.checkForCommand(message.getContent(), "k")) {
			newMessage = "---" + parentMessage.getPlainContent() + "--- k by " + message.getUser().getName();
			isValidFeedback = true;
		}
		if (CommandUtils.checkForCommand(message.getContent(), "f")) {
			newMessage = "---" + parentMessage.getPlainContent() + "--- f by " + message.getUser().getName();
			isValidFeedback = true;
		}

		if (!isReport && isValidFeedback) {
			room.replyTo(message.getId(), "You can only send feedback to reports. This message wasn't one.");
			return;
		}

		// if newMessage longer than 10, edit it
		if (newMessage.length() > 10) {
			try {
				room.edit(parentMessageId, newMessage);
			} catch (Throwable e) {
				LOGGER.info("Could not edit message", e);
			}
		}
	}

	public static Integer getIdFromLink(String link) {
		if (link == null) {
			return null;
		}

		if (link.contains("/")) {
			String[] parts = link.split("/");
			for (String s : parts) {
				if (s.matches("\\d+")) {
					try {
						return Integer.parseInt(s);
					} catch (NumberFormatException e) {
						// Do nothing not a number
					}
				}
			}
		}

		String suid = link.replaceAll("[^\\d.]", "");
		try {
			return Integer.parseInt(suid);
		} catch (NumberFormatException e) {
			LOGGER.error(suid + " can't be parsed");
			return null;
		}

	}
	
	
	public static String storeReport(Post target, Post original) throws IOException {
		Properties prop = Guttenberg.getLoginProperties();
		
		String url = prop.getProperty("copypastor_url", "http://guttenberg.sobotics.org:5000")+"/posts/create";
		JsonObject output = JsonUtils.post(url,
						"key", prop.getProperty("copypastor_key", "no_key"),
		                "url_one","//stackoverflow.com/a/"+target.getAnswerID(),
		                "url_two","//stackoverflow.com/a/"+original.getAnswerID(),
		                "title_one","Possible Plagiarism",
		                "title_two", "Original Post",
		                "date_one",""+target.getAnswerCreationDate().getEpochSecond(),
		                "date_two",""+original.getAnswerCreationDate().getEpochSecond(),
		                "body_one",target.getUnescapedBodyMarkdown(),
		                "body_two",original.getUnescapedBodyMarkdown());
		
		return prop.getProperty("copypastor_url", "http://guttenberg.sobotics.org:5000") + "/posts/" + output.get("post_id").getAsString();
	}
	
	/**
	 * Sends the feedback to CopyPastor
	 * */
	public static void checkPingForFeedback(Room room, PingMessageEvent ping) {
		LOGGER.info("Checking message for feedback");
		int reportId = -1;
		Message reportMsg = null;
		Message pingMsg = ping.getMessage();
		long parentMsg = ping.getParentMessageId();
		
		if (parentMsg == -1) {
			//this wasn't a reply
			return;
		}
		
		reportMsg = room.getMessage(parentMsg);
		
		if (reportMsg != null) {
			reportId = PostUtils.getReportIdFromChatMessage(reportMsg);
		}
		
		if (reportId == -1)
			return;
		
		
		try {
			if (CommandUtils.checkForCommand(pingMsg.getContent(), "k") || CommandUtils.checkForCommand(pingMsg.getContent(), "tp")) {
				PostUtils.storeFeedback(room, ping, reportId, "tp");
			}
			if (CommandUtils.checkForCommand(pingMsg.getContent(), "f") || CommandUtils.checkForCommand(pingMsg.getContent(), "fp")) {
				PostUtils.storeFeedback(room, ping, reportId, "fp");
			}
		} catch (IOException e) {
			LOGGER.error("Could not save feedback!", e);
		} // try-catch
	} // checkPingForFeedback
	
	/**
	 * Returns the CopyPastor report ID
	 * -1, if the message wasn't a report
	 * */
	public static int getReportIdFromChatMessage(Message message) {
		Properties prop = Guttenberg.getLoginProperties();
		boolean isMyMessage = message.getUser().getName().equalsIgnoreCase(prop.getProperty("username", "Guttenberg"));
		String plain = message.getPlainContent();
		
		try {
			Pattern pattern = Pattern.compile("\\[ \\[Guttenberg\\].*\\[CopyPastor]\\(.*\\/posts\\/(?<reportId>\\d*)\\)");
			Matcher matcher = pattern.matcher(plain);
			
			if (isMyMessage && matcher.find() ) {
				String reportId = matcher.group("reportId");
				try {
					return Integer.parseInt(reportId);
				}
				catch (NumberFormatException e) {
					LOGGER.warn(e.getMessage());
					return -1;
				} // try-catch
			} else {
				return -1;
			} // else-if
		} catch (Exception e) {
			LOGGER.error("FATAL ERROR! RegEx-Pattern might be broken!", e);
			return -1;
		} // try-catch
	} // getReportIdFromChatMessage
	
	/**
	 * Sends the feedback to CopyPastor
	 * @param ping The PingMessageEvent that contains the feedback
	 * @param reportId The ID of the CopyPastor-Report
	 * @param feedback tp or fp
	 * */
	public static void storeFeedback(Room room, PingMessageEvent ping, int reportId, String feedback) throws IOException {
		Properties prop = Guttenberg.getLoginProperties();
		
		String url = prop.getProperty("copypastor_url", "http://guttenberg.sobotics.org:5000")+"/feedback/create";
		JsonObject output = JsonUtils.post(url,
						"key", prop.getProperty("copypastor_key", "no_key"),
						"post_id", ""+reportId,
						"feedback_type", feedback,
						"username", ping.getUserName(),
						"link", "https://chat." + PostUtils.getChatHostAsString(room.getHost()) + "/users/"+ping.getUserId()
		                );
		
		String status = output.get("status").getAsString();
		if (!status.equalsIgnoreCase("success")) {
			String statusMsg = output.get("message").getAsString();
			if (ping.getMessage().getUser().isRoomOwner()) {
				ping.getRoom().replyTo(ping.getMessage().getId(), statusMsg);
			} else {
				ping.getRoom().replyTo(ping.getMessage().getId(), "Your feedback could not be saved.");
			}
			
			LOGGER.error(statusMsg);
		} // if
	} // storeFeedback
	
	/**
	 * Ugly workaround to get <code>ChatHost.getName()</code>, which is not public
	 * https://github.com/Tunaki/chatexchange/issues/5
	 * */
	public static String getChatHostAsString(ChatHost host) {
		switch (host) {
			case STACK_OVERFLOW:
				return "stackoverflow.com";
			case STACK_EXCHANGE:
				return "stackexchange.com";
			case META_STACK_EXCHANGE:
				return "meta.stackexchange.com";
			default:
				return "stackoverflow.com"; 
		}
	}
} // class
