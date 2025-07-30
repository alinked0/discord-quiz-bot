package com.linked.quizbot.core;

import java.util.Arrays;
import java.util.Scanner;

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
		
		Constants.AREWETESTING = false;
		Scanner scanner = new Scanner(System.in);
		if (args.length ==0 || args[0].isEmpty()) {
			System.out.print("Input bot token: ");
			Constants.TOKEN =scanner.nextLine().trim();
		} else if (args.length ==1 && !args[0].isEmpty()){
			Constants.TOKEN = args[0];
		} else if(args.length ==4){
			for (int i=1; i<4; ++i){
				if (args[i].isEmpty() || args[i].length()<Constants.DISCORDIDLENMIN){
					usageMain();
					scanner.close();
					return;
				}
			}
			Constants.AREWETESTING = true;
			Constants.TOKEN = args[0];
			Constants.DEBUGGUILDID = args[1];
			Constants.DEBUGCHANNELID = args[2];
			Constants.AUTHORID = args[3];
			
		} else {
			usageMain();
			scanner.close();
			return;
		}
		BotCore.jda= null;
		Users.loadAllUsers();
		if(Constants.isBugFree() || Constants.AUTHORID.isEmpty()){
			BotCore.startJDA();
		}else {
			CommandLineInterface.execute(scanner);
			if (BotCore.jda!=null) BotCore.jda.shutdownNow();
		}

	}
}
