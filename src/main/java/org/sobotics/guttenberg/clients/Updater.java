package org.sobotics.guttenberg.clients;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.sobotics.guttenberg.commands.Status;
import org.sobotics.guttenberg.utils.FilePathUtils;
import org.sobotics.guttenberg.utils.Version;

/**
 * Checks for updates
 * */
public class Updater {

	private Version currentVersion;
	private Version newVersion = new Version("0.0.0");
	
	public Updater() {
		//get current version
		Properties guttenbergProperties = new Properties();
		
		try{
            InputStream is = Status.class.getResourceAsStream("/guttenberg.properties");
            guttenbergProperties.load(is);
        }
        catch (IOException e){
        	System.out.println("Could not load properties");
            e.printStackTrace();
        }
		
		System.out.println("Loaded properties");
		
		String versionString = guttenbergProperties.getProperty("version", "0.0.0");
		this.currentVersion = new Version(versionString);
		
		System.out.println("Current version: "+this.currentVersion.get());
		
		//get all files in the folder
		String regex = "guttenberg-([0-9.]*)\\.jar";
		Pattern pattern = Pattern.compile(regex);
		
		File[] files = new File("./").listFiles();
		for (File file : files) {
		    if (file.isFile()) {
		    	String name = file.getName();
		    	System.out.println("File: "+name);
		    	Matcher matcher = pattern.matcher(name);
		    	matcher.find();
		    	System.out.println("Init matcher");
		    	String v = "";
		    	
		    	try {
		    		v = matcher.group(1);
		    	} catch (Exception e) {
		    		e.printStackTrace();
		    		System.out.println("ERROR");
		    	}
		    	
		    	
		    	System.out.println("Matched");
		    	if (v != null && v.length() > 0) {

			    	Version version = new Version(v);
			    	
			    	if (this.currentVersion.compareTo(version) == -1) {
			    		//higher than current version
			    		if (this.newVersion.compareTo(version) == -1) {
			    			//higher than next version
			    			this.newVersion = version;
			    		}
			    	}
		    	}
		    	
		    }
		}
		
	}
	
	/**
	 * Checks if a new version is available and updates
	 * */
	public void updateIfAvailable() {
		if (this.currentVersion.compareTo(this.newVersion) == -1) {
			System.out.println("New version available: "+this.newVersion.get());
		} else {
			System.out.println("No update required");
		}
	}
	
}
