package org.sobotics.guttenberg.utils;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicInteger;

import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.parser.Parser;
import org.slf4j.Logger;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;

/**
 * Created by bhargav.h on 10-Sep-16.
 * AKA, TunaLib - All code is courtesy of Lord Tunaki
 */
public class JsonUtils {
    public static synchronized JsonObject get(String url, String... data) throws IOException {
    	Connection.Response response = Jsoup.connect(url).data(data).method(Connection.Method.GET).ignoreContentType(true).ignoreHttpErrors(true).execute();
        String json = response.body();
        if (response.statusCode() != 200) {
            throw new IOException("HTTP " + response.statusCode() + " fetching URL " + (url) + ". Body is: " + response.body());
        }
        JsonObject root;
		try {
			root = new JsonParser().parse(json).getAsJsonObject();
		} catch (JsonSyntaxException e) {
			System.out.println("Error pasrsing json: " + json);
			throw e;
		}
        
        if (root.has("quota_remaining"))
            StatusUtils.remainingQuota = new AtomicInteger(root.get("quota_remaining").getAsInt());
        
        return root;
    }
    public static synchronized JsonObject post(String url, String... data) throws IOException {
        Connection.Response response = Jsoup.connect(url).data(data).method(Connection.Method.POST).ignoreContentType(true).ignoreHttpErrors(true).execute();
        String json = response.body();
        if (response.statusCode() != 200) {
            throw new IOException("HTTP " + response.statusCode() + " fetching URL " + (url) + ". Body is: " + response.body());
        }
        JsonObject root = new JsonParser().parse(json).getAsJsonObject();
        
        if (root.has("quota_remaining"))
            StatusUtils.remainingQuota = new AtomicInteger(root.get("quota_remaining").getAsInt());
        
        return root;
    }
    public static void handleBackoff(Logger LOGGER, JsonObject root) {
        if (root.has("backoff")) {
            int backoff = root.get("backoff").getAsInt();
            LOGGER.warn("Backing off {} seconds", backoff);
            try {
                Thread.sleep(1000 * backoff);
            } catch (InterruptedException e) {
                LOGGER.error("Couldn't backoff for {} seconds, was interrupted!", backoff, e);
            }
        }
    }
    public static String escapeHtmlEncoding(String message) {
        return Parser.unescapeEntities(JsonUtils.sanitizeChatMessage(message), false).trim();
    }
    public static String sanitizeChatMessage(String message) {
        return message.replaceAll("(\\[|\\]|_|\\*|`)", "\\\\$1");
    }

}
