package org.sobotics.guttenberg.utils;

import com.google.gson.JsonObject;

/**
 * Utilities for dealing with posts.
 * @author ArtOfCode
 */
public class PostUtils {
    public static void separateBodyParts(JsonObject answer) {
        String markdown = answer.get("body_markdown").getAsString();
        String[] paragraphs = markdown.split("\\n{2,}");
        
        String plain = "", code = "", quote = "";
        for (String paragraph : paragraphs) {
            if (paragraph.trim().charAt(0) == '>') {
                quote += paragraph + "\n";
            }
            else if (paragraph.startsWith("    ")) {
                code += paragraph + "\n";
            }
            else {
                plain += paragraph + "\n";
            }
        }
        
        answer.addProperty("body_plain", plain);
        answer.addProperty("body_code", code);
        answer.addProperty("body_quote", quote);
    }
}
