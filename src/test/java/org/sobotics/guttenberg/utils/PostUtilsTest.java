package org.sobotics.guttenberg.utils;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author owen
 */
public class PostUtilsTest {
    
    public PostUtilsTest() {
    }
    
    @BeforeClass
    public static void setUpClass() {
    }
    
    @AfterClass
    public static void tearDownClass() {
    }
    
    @Before
    public void setUp() {
    }
    
    @After
    public void tearDown() {
    }

    @Test
    public void testSeparateBodyParts() {
        JsonParser parser = new JsonParser();
        JsonObject answer = parser.parse("{\"body_markdown\":\"p1\\n\\np2\\n\\np3\\n\\n>quote\\n\\n    code\"}").getAsJsonObject();
        PostUtils.separateBodyParts(answer);
        
        System.out.println(answer.get("body_plain").toString());
        
        assertEquals("p1\np2\np3\n", answer.get("body_plain").getAsString());
        assertEquals("    code\n", answer.get("body_code").getAsString());
        assertEquals(">quote\n", answer.get("body_quote").getAsString());
    }
    
}
