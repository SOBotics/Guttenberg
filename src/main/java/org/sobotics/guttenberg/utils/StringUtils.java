package org.sobotics.guttenberg.utils;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

public class StringUtils {
	public static String plainTextFromHtml(String input) {
        Document doc = Jsoup.parse("<body>"+input+"</body>");
        doc.getElementsByTag("a").remove();
        doc.getElementsByTag("code").remove();
        doc.getElementsByTag("img").remove();
        doc.getElementsByTag("pre").remove();
        doc.getElementsByTag("blockquote").remove();
        return doc.text();
    }
}
