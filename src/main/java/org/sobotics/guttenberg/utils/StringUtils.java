package org.sobotics.guttenberg.utils;

import java.util.ArrayList;
import java.util.List;

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
	
	public static List<String> searchableLinesOfBodyMarkdown(String input) {
		String[] lines = input.split("\\n");
		
		List<String> searchableLines = new ArrayList<String>();
		
		for (String line : lines) {
			//starts with four spaces -> code -> don't search!
			if (!line.startsWith("    ")) {
				searchableLines.add(line);
			}
		}
		
		return searchableLines;
	}
	
}
