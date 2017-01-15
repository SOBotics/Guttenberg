package org.sobotics.guttenberg.clients;

import fr.tunaki.stackoverflow.chat.StackExchangeClient;

/**
 * The main class
 * */
public class Client {

	public static void main(String[] args) {
		System.out.println("Hello, World!");
		
		//StackExchangeClient seClient = new StackExchangeClient(null, null);
		
		Guttenberg guttenberg = new Guttenberg(null);
		
		guttenberg.start();
	}

}
