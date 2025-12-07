package com.linked.quizbot.core;

import java.util.Arrays;
import java.util.Scanner;

import com.linked.quizbot.Constants;
import com.linked.quizbot.utils.Attempt;
import com.linked.quizbot.utils.User;
import com.linked.quizbot.utils.Users;

/**
 * The Main class is the entry point of the QuizBot application.
 * It initializes the bot, loads user data, and starts the bot in either testing
 * or production mode based on command line arguments.
 * <p>
 * This class handles command line arguments to configure the bot's behavior and
 * provides usage instructions if needed.
 * </p>
 * 
 * @author alinked0
 * @version 1.0
 * @since 2025-02-01
 */
public class Main {
	public static void usage() {
		String s = "";
		s += "Usage: \n\tjava -jar QuizBot.jar BOTTOKEN USERID [TESTCHANNELID TESTGUILDID]\n";
		s += "\t\t BOTTOKEN: the bot token, TESTGUILDID: the guild id to test in, TESTCHANNELID: the channel id to test in\n";
		s += "\t\t USERID: the user id to test with, if running the bot in testing mode\n";
		s += "Starts a quiz bot restricted to the TESTCHANNELID int the TESTGUILDID, if none are defined, the bot will start a private channel with the USERID.\n";
		System.out.println(s);
	}
	
	public static void main (String[] args) {
		if (args.length <2 || Arrays.asList("help", "-h", "--help").contains(args[0].toLowerCase())) {
			Main.usage();
			return;
		}
		Constants.TOKEN = args[0];
		Constants.ADMINID = args[1];
		if(args.length == 4){
			Constants.DEBUGCHANNELID = args[2];
			Constants.DEBUGGUILDID = args[3];
		} else {
			Constants.DEBUGCHANNELID = null;
			Constants.DEBUGGUILDID = null;
		}
		
		Scanner scanner = new Scanner(System.in);
		BotCore.jda= null;
		Users.loadAllUsers();
		CommandLineInterface.execute(scanner);
		/*
		try{
			User u = new User("468026374557270017");
			System.out.println(u.getAttempts("abcdefg").getFirst().toJson());
		} catch(Exception e){
			e.printStackTrace();
		}
		*/
	}
}
