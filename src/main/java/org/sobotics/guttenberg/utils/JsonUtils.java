/*
 * Copyright (C) 2019 SOBotics
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>
 */

package org.sobotics.guttenberg.utils;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.parser.Parser;
import org.slf4j.Logger;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by bhargav.h on 10-Sep-16.
 * AKA, TunaLib - All code is courtesy of Lord Tunaki
 */
public class JsonUtils {
  public static JsonObject get(String url, String... data) throws IOException {
    Connection.Response response = Jsoup.connect(url).data(data).method(Connection.Method.GET).ignoreContentType(true).ignoreHttpErrors(true).execute();
    String json = response.body();
    if (response.statusCode() != 200) {
      throw new IOException("HTTP " + response.statusCode() + " fetching URL " + (url) + ". Body is: " + response.body());
    }
    JsonObject root = new JsonParser().parse(json).getAsJsonObject();

    if (root.has("quota_remaining"))
      StatusUtils.remainingQuota = new AtomicInteger(root.get("quota_remaining").getAsInt());

    return root;
  }


  public static JsonObject post(String url, String... data) throws IOException {
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
