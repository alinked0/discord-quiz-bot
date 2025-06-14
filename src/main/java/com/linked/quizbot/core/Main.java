package com.linked.quizbot.core;

import java.util.Arrays;
import java.util.Scanner;

import com.linked.quizbot.Constants;
import com.linked.quizbot.commands.list.HelpCommand;
import com.linked.quizbot.events.MessageListener;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.requests.GatewayIntent;

import com.linked.quizbot.events.ReactionListener;
import com.linked.quizbot.events.SlashCommandListener;
import com.linked.quizbot.events.readyEvent;
import com.linked.quizbot.utils.UserData;

import net.dv8tion.jda.api.entities.Activity;

public class Main {
	public static String getStatus(){
		String s = "";
		s+=" isBugFree: "+Constants.isBugFree();
		s+="\n Prefixe: "+Constants.CMDPREFIXE;
		s+= "\n NumberOfUsers: "+UserData.allUserLists.size();
		return s;
	}
	public static void usageMain(){
		String s="";
		s+= "Usage: \n\tjava -jar QuizBot.jar [BOTTOKEN] [TESTGUILDID] [TESTCHANNELID] [USERID]\n";
		s+= "\t\t BOTTOKEN: the bot token, TESTGUILDID: the guild id to test in, TESTCHANNELID: the channel id to test in\n";
		s+= "\t\t USERID: the user id to test with, if running the bot in testing mode\n";
		s+= "Example: \n\tjava -jar QuizBot.jar 123456789012345678abc 123456789012345678 123456789012345678 123456789012345678\n";
		s+= "If you want to run the bot in production mode, you can use:\n\tjava -jar QuizBot.jar 123456789012345678abc";
		System.out.println(s);
	}
	public static void usageInner(){
		String s="";
		s+= "Usage: \n\t$ [COMMAND]\n";
		s+= "\t\t stop, shutdown, exit: to stop the bot\n";
		s+= "\t\t status: to get the bot status\n";
		s+= "Example: \n\t$ stop\n";
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
		
		JDA jda = JDABuilder.createDefault(Constants.TOKEN,
			GatewayIntent.GUILD_MESSAGES, 
			GatewayIntent.MESSAGE_CONTENT,
			GatewayIntent.GUILD_MEMBERS, 
			GatewayIntent.DIRECT_MESSAGES, 
			GatewayIntent.DIRECT_MESSAGE_REACTIONS, 
			GatewayIntent.GUILD_MESSAGE_REACTIONS
		).setActivity(Activity.playing(Constants.CMDPREFIXE+HelpCommand.CMDNAME)).build();

		jda.addEventListener(
			new SlashCommandListener(), 
			new ReactionListener(), 
			new MessageListener(),
			new readyEvent()
		);
		try {
			jda.awaitReady();
		}catch (InterruptedException e){
			e.printStackTrace();
		}catch(IllegalStateException e){
			e.printStackTrace();
		}
		Scanner scanner = new Scanner(System.in);
        String input="";
        while (!BotCore.isShutingDown()) {
            System.out.print("$ ");
            input = scanner.nextLine().toLowerCase();
			System.out.println(" Input: " + input);
			switch (input) {
				case "stop","shutdown","exit":
					BotCore.SHUTINGDOWN = true;
					System.out.println("Bot will soon shutdown");
					break;
				case "status":
					System.out.println(getStatus());
					break;
				default:
					usageInner();
					break;
			}
        }
		scanner.close();
		jda.shutdown();
	}
}