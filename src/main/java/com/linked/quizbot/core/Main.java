package com.linked.quizbot.core;

import java.util.Arrays;

import com.linked.quizbot.Constants;
import com.linked.quizbot.utils.Users;

public class Main {
	public static void usageMain(){
		String s="";
		s+= "Usage: \n\tjava -jar QuizBot.jar [BOTTOKEN] [TESTGUILDID] [TESTCHANNELID] [USERID]\n";
		s+= "\t\t BOTTOKEN: the bot token, TESTGUILDID: the guild id to test in, TESTCHANNELID: the channel id to test in\n";
		s+= "\t\t USERID: the user id to test with, if running the bot in testing mode\n";
		s+= "Example: \n\tjava -jar QuizBot.jar 123456789012345678abc 123456789012345678 123456789012345678 123456789012345678\n";
		s+= "If you want to run the bot in production mode, you can use:\n\tjava -jar QuizBot.jar 123456789012345678abc";
		System.out.println(s);
	}
	public static void main (String[] args) {
		if (args.length <1 || Arrays.asList("help", "-h", "--help").contains(args[0].toLowerCase())) {
			usageMain();
			return;
		}
		Constants.AREWETESTING = args.length>=3;
		if (Constants.AREWETESTING && args.length < 3) {
			System.out.println("You must provide the bot token, guild id, channel id and user id in testing mode.");
			usageMain();
			return;
		} 
		if (!Constants.AREWETESTING && args.length < 1) {
			System.out.println("You must provide the bot token in production mode.");
			usageMain();
			return;
		}
		if(args[0].isEmpty()){
			System.out.println("You must provide a bot token that is not empty.");
			usageMain();
			return;
		}
		
		Constants.TOKEN = args[0];
		if (args.length>=2){
			Constants.DEBUGGUILDID = args[1];
		}
		if (args.length>=3){
			Constants.DEBUGCHANNELID = args[2];
		}
		if (args.length>=4) {
			Constants.AUTHORID = args[3];
		}
		BotCore.jda= null;
		Users.loadAllUsers();
		if (Constants.isBugFree()){
			BotCore.startJDA();
		}
		CommandLineInterface.execute();

		if (BotCore.jda!=null) BotCore.jda.shutdownNow();
	}
}