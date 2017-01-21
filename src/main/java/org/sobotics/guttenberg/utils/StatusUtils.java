package org.sobotics.guttenberg.utils;

import java.time.Instant;

/**
 * Provides statistics about the current status since launch
 * */
public class StatusUtils {
	public static Instant startupDate = null;
	
	/**
	 * The time when the last check was finished
	 * */
	public static Instant lastExecutionFinished = null;
	
	/**
	 * The number of new answers that have been checked
	 * */
	public static int numberOfCheckedTargets = 0;
	
	/**
	 * The number of posts that were reported in chat as possible plagiarism
	 * */
	public static int numberOfReportedPosts = 0;
}
