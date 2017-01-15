package org.sobotics.guttenberg.utils;

/**
 * Created by bhargav.h on 11-Sep-16.
 */
public class PrintUtils {

    public static String printDescription(){
        return " [ [Guttenberg](https://github.com/SOBotics/Guttenberg) ] ";
    }
    public static String printStackAppsPost(){
        return "No StackApps post yet";
    }
    public static String printHelp(){
        return "I'm a bot, searching for plagiarism on Stack Overflow. "+wikiLink()+" Use commands to view a list of commands.";
    }
    private static String wikiLink(){
        return "The guide and the wiki for the project [are present here](https://github.com/SOBotics/Guttenberg). Well. Not really at the moment.";
    }
    public static String printCommandHeader(){
        return "The list of commands are as follows: "+wikiLink();
    }

}
