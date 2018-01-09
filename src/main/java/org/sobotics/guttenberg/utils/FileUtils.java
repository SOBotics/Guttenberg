package org.sobotics.guttenberg.utils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.Scanner;

/**
 * Created by bhargav.h on 30-Sep-16.
 */
public class FileUtils {

    public static void appendToFile(String filename, String word) throws IOException{
        Files.write(Paths.get(filename), Arrays.asList(word), StandardOpenOption.APPEND, StandardOpenOption.WRITE);
    }
    public static boolean checkIfInFile(String filename, String message) throws IOException{
        List<String> lines = readFile(filename);
        for (String line:lines){
            if(line.trim().toLowerCase().equals(message.trim().toLowerCase())){
                return true;
            }
        }
        return false;
    }
    
    public static boolean checkIfLineInFileStartsWith(String filename, String message) throws IOException{
        List<String> lines = readFile(filename);
        for (String line:lines){
            if(line.trim().toLowerCase().startsWith(message.trim().toLowerCase())){
                return true;
            }
        }
        return false;
    }
    
    public static List<String> readFile(String filename) throws IOException{
        return Files.readAllLines(Paths.get(filename));
    }
    public static void removeFromFile(String filename, String message) throws IOException{
        List<String> lines = readFile(filename);
        List<String> newLines = new ArrayList<>();
        for (String line:lines){
            if(line.trim().toLowerCase().equals(message.trim().toLowerCase())){
                continue;
            }
            newLines.add(line);
        }
        Files.write(Paths.get(filename), newLines, StandardOpenOption.TRUNCATE_EXISTING,StandardOpenOption.WRITE);
    }
    public static void removeFromFileStartswith(String filename, String message) throws IOException{
        List<String> lines = readFile(filename);
        List<String> newLines = new ArrayList<>();
        for (String line:lines){
            if(line.trim().toLowerCase().startsWith(message.trim().toLowerCase())){
                continue;
            }
            newLines.add(line);
        }
        Files.write(Paths.get(filename), newLines, StandardOpenOption.TRUNCATE_EXISTING,StandardOpenOption.WRITE);
    }
    public static String readLineFromFileStartswith(String filename, String message) throws IOException{
        List<String> lines = readFile(filename);
        for (String line:lines){
            if(line.trim().toLowerCase().startsWith(message.trim().toLowerCase())){
                return line.trim();
            }
        }
        return null;
    }
    public static void removeFromFileLine(String filename, int lineNumber) throws IOException{
        List<String> lines = readFile(filename);
        List<String> newLines = new ArrayList<>();
        int i = 0;
        for (i = 0; i<lines.size();i++){
            if(i+1==lineNumber){
                continue;
            }
            newLines.add(lines.get(i));
        }
        Files.write(Paths.get(filename), newLines, StandardOpenOption.TRUNCATE_EXISTING,StandardOpenOption.WRITE);
    }
    public static void removeFromFileLines(String filename, int lineNumbers[]) throws IOException{
        List<String> lines = readFile(filename);
        List<String> newLines = new ArrayList<>();
        Integer i;

        for (i = 0; i<lines.size();i++){
            if(Arrays.asList(Arrays.stream(lineNumbers).boxed().toArray(Integer[]::new)).contains(i+1)){
                continue;

            }
            newLines.add(lines.get(i));
        }
        Files.write(Paths.get(filename), newLines, StandardOpenOption.TRUNCATE_EXISTING,StandardOpenOption.WRITE);
    }

    public static void createNewFile(String filename, String data) throws IOException{
        Files.write(Paths.get(filename), Arrays.asList(data), StandardOpenOption.CREATE_NEW,StandardOpenOption.WRITE);
    }
    
    /**
     * Returns a random line of a file
     * @author <a href="https://stackoverflow.com/users/27198/itay-maman">Itay Maman</a>
     * @source <a href="https://stackoverflow.com/a/2218067/4687348">Stack Overflow</a>
     * */
    public static String randomLine(File f) throws FileNotFoundException
    {
       String result = null;
       Random rand = new Random();
       int n = 0;
       for(Scanner sc = new Scanner(f); sc.hasNext(); )
       {
          ++n;
          String line = sc.nextLine();
          if(rand.nextInt(n) == 0)
             result = line;         
       }

       return result;      
    }
}
