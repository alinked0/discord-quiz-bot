package com.linked.quizbot.core;

import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;

import com.linked.quizbot.Constants;
import com.linked.quizbot.commands.BotCommand;
import com.linked.quizbot.utils.Users;

public class CommandLineInterface {
	public static String getStatus(){
		String s = "";
		s+= String.format("isBugFree:%s\n", Constants.isBugFree());
		s+= String.format("Prefixe:%s\n", Constants.CMDPREFIXE);
		s+= String.format("NumberOfUsers:%s\n", Users.allUsers.size());
		s+= String.format("IsOnline:%s\n", BotCore.jda!=null);
		return s;
	}
	public static String usage(){
		String s="";
		s+= "Usage: \n\t$ [COMMAND]\n";
		s+= String.format("\t%s\t%s\n", "status","gets the bot status");
		s+= String.format("\t%s\t%s\n", "stop, shutdown","stops the bots");
		s+= String.format("\t%s\t%s\n", "exit","stops the bot, kills this process");
		s+= String.format("\t%s\t%s\n", "start"," starts the bot and connects it to discord");
		s+= String.format("\t%s\t%s\n", "q![BotCommand] [Argumments]","executes a command and returns a text output");
		s+= "Example: \n";
        s+= "\t$ status\n";
        s+= "\t$ q!collection\n";
        s+= "\t$ q!help\n";
		return s;
	}
	private static final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
	public static void execute (Scanner scanner) {
		boolean exiting = false;
		while (!exiting) {
			System.out.print("$ ");
			String input = scanner.nextLine().toLowerCase();
			switch (input) {
				case "exit" -> {
					BotCore.shutDown();
					scanner.close();
					exiting = true;
				}
				case "shutdown", "stop" -> {
					BotCore.shutDown();
					BotCore.SHUTINGDOWN = false;
				}
				case "startjda", "start" -> {
					if (BotCore.jda == null){
                        BotCore.startJDA();
					} else {
                        System.out.println("Bot is already online. Use the command stop, to put an end it.");
                    }
				}
				case "status", "stat"-> {
					System.out.print(getStatus());
				}
				case "help" -> {
					System.out.println(usage());
				}
				default -> {
					try {
						Future<?> future = scheduler.submit(() -> {
							List<String> out = execute(input, Constants.AUTHORID);
							for (String s: out){
								System.out.println(s);
							}
						});
						while(!future.isDone()){
							if (BotCore.isShutingDown()){
								future.resultNow();
								future.cancel(true);
								break;
							}
						}
					}catch(Exception e){
						e.printStackTrace();
					}
					
				}
			}
		}
		if (BotCore.jda!=null) BotCore.jda.shutdownNow();
	}
	public static List<String> execute(String message, String userId) {
		String content = message;
		List<String> arguments=new ArrayList<>();
		List<String> tmp = parsePrefixe(userId, message);
		if (tmp.isEmpty()){
			return List.of(usage());
		}
		message = tmp.getLast();
		arguments.add(tmp.get(0));
		tmp = parseBotCommand(message);
		if (tmp.isEmpty()){
			return List.of(String.format("Prefixe:%s, Cmd:None", arguments.get(0)));
		}
		message = tmp.getLast();
		arguments.add(tmp.get(0));
		BotCommand cmd = BotCommand.getCommandByName(arguments.get(1));

		if(BotCore.isShutingDown()){
			BotCommand.CommandCategory category = cmd.getCategory();
			if(category.equals(BotCommand.CommandCategory.EDITING) || category.equals(BotCommand.CommandCategory.GAME)){
				return List.of(Constants.UPDATEEXPLANATION);
			}
		}
		
		String cmndLineArgs = message;
		arguments = cmd.parseArguments(cmndLineArgs);

		if (!Constants.isBugFree()) {
			System.out.print("  $> "+content.replace("\n", "").replace("\t", ""));
			System.out.print(" ; arguments size="+arguments.size()+":");
			for (int i=0; i<arguments.size(); i++) { 
				System.out.print(arguments.get(i).replace("\n", "").replace("\t", "")+":");
			}
			System.out.print("\n");
		}
		return cmd.execute(userId, arguments).getAsText();
	}
	public static List<String> parsePrefixe(String userId, String message){
		String prefixe;
		String userPrefixe = Users.get(userId).getPrefix();
		// Do not preccess if no known cmd prefixe can fit in message
		if ((userPrefixe!=null && message.length() < userPrefixe.length()) && message.length() < Constants.CMDPREFIXE.length()){
			return List.of();
		}
		int cmdPrefixeLen;
		// Prioritise user defined prefixe, if not found search for the default prefixe
        if (userPrefixe!=null && userPrefixe.length()<=Constants.CMDPREFIXE.length() && Constants.CMDPREFIXE.startsWith(userPrefixe)
            && Constants.CMDPREFIXE.length()<=message.length() && message.startsWith(Constants.CMDPREFIXE)){
            prefixe = Constants.CMDPREFIXE;
        }else if (userPrefixe!=null && message.startsWith(userPrefixe)){
			prefixe = userPrefixe;
        }else if (message.startsWith(Constants.CMDPREFIXE)){
            prefixe = Constants.CMDPREFIXE;
		}else {
			return List.of();
		}
		cmdPrefixeLen = prefixe.length();
		String remainingMessage = message.substring(cmdPrefixeLen).trim();
		// return a list containing the prefixe that was found and the rest of the unprocessed arguments
		return List.of(prefixe, remainingMessage);
	}
	public static List<String> parseBotCommand(String message){
		String userCmdName = null;
		if (message.length()<=0) {
			return List.of();
		}
		userCmdName = message.split(" ")[0];
		BotCommand cmd = BotCommand.getCommandByName(userCmdName);
		if (cmd!=null){
			String remainingMessage = message.substring(userCmdName.length()).trim();
			return List.of(cmd.getName(), remainingMessage);
		}
		return List.of();
	}
}