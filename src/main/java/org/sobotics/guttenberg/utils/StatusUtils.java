package org.sobotics.guttenberg.utils;

import java.time.Instant;
import java.util.concurrent.atomic.AtomicInteger;

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
	 * The time when the last successful check was started
	 * */
	public static Instant lastSucceededExecutionStarted = null;
	
	/**
	 * The number of new answers that have been checked
	 * */
	public static AtomicInteger numberOfCheckedTargets = new AtomicInteger(0);
	
	/**
	 * The number of posts that were reported in chat as possible plagiarism
	 * */
	public static AtomicInteger numberOfReportedPosts = new AtomicInteger(0);
	
	/**
	 * The remaining api quota
	 * */
	public static AtomicInteger remainingQuota = new AtomicInteger(-1);
	
	/**
	 * Stores if Guttenberg asked for help after several failed executions.
	 * */
	public static boolean askedForHelp = false;
}
