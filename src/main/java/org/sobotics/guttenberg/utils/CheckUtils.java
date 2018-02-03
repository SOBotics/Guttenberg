package org.sobotics.guttenberg.utils;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CheckUtils {
	private static final Logger LOGGER = LoggerFactory.getLogger(CheckUtils.class);
	
	/**
	 * Checks if a user is blacklisted on a certain site
	 * */
	public static boolean checkIfUserIsBlacklisted(long userId, String host){
		try {
			return FileUtils.checkIfInFile(FilePathUtils.blacklistedUsersFile, host + ":" + String.valueOf(userId));
		} catch (IOException e) {
			LOGGER.error("Could not check if user is blacklisted! Assuming, he's not...", e);
			return false;
		}
    }
}
