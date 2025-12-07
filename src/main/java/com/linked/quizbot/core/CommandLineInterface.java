package com.linked.quizbot.core;

import java.util.List;
import java.util.ArrayList;
import java.util.Scanner;

import org.jetbrains.annotations.Nullable;

import com.linked.quizbot.Constants;
import com.linked.quizbot.commands.BotCommand;
import com.linked.quizbot.commands.CommandOutput;
import com.linked.quizbot.commands.list.HelpCommand;
import com.linked.quizbot.utils.Users;

/**
 * CommandLineInterface is a class that provides a command line interface for the bot.
 * It allows users to interact with the bot through commands.
 * It also provides methods to execute commands and get the status of the bot.
 */
public class CommandLineInterface {
	public static String getStatus(){
		String s = "";
		s+= String.format("isBugFree:%s\n", BotCore.isBugFree());
		s+= String.format("Prefix:%s\n", Constants.CMDPREFIXE);
		s+= String.format("IsOnline:%s\n", BotCore.isOnline());
		s+= String.format("NumberOfUsers:%d\n", Users.allUsers.size());
		return s;
	}
	
	/**
	 * Provides a detailed usage message for the command-line interface.
	 * @return The usage string.
	 */
	public static String usage(){
		String s="";
		s+= "Usage: \n\t[COMMAND]\n";
		s+= String.format("\t%s\t%s\n", "status, stat","gets the bot status");
		s+= String.format("\t%s\t%s\n", "stop, shutdown, disconnectjda","stops the bot and disconnects from Discord");
		s+= String.format("\t%s\t%s\n", "exit","stops the bot, disconnects, and kills this process");
		s+= String.format("\t%s\t%s\n", "start, connectjda, connect","starts the bot and connects it to Discord");
		s+= String.format("\t%s\t%s\n", "private","sets the bot to private/testing mode");
		s+= String.format("\t%s\t%s\n", "public","sets the bot to public mode");
		s+= String.format("\t%s\t%s\n", "load, reload","reloads all user data from disk");
		s+= String.format("\t%s\t%s\n", "help","displays this usage message");
		s+= String.format("\t%s\t%s\n", "q![BotCommand] [Argumments]","executes a bot command (e.g., q!help)");
		
		s+= "\nExample: \n";
		s+= "\tstatus\n";
		s+= "\tstart\n";
		s+= "\tq!collection\n";
		s+= "\tq!help\n";
		s+= "\texit\n";
		return s;
	}
	
	public static void execute (Scanner scanner) {
		boolean exiting = false;
		CommandOutput output;
		List<String> out;
		while (!exiting) {
			String input = scanner.nextLine().toLowerCase();
			switch (input) {
				case "exit" -> {
					BotCore.shutDown();
					scanner.close();
					exiting = true;
					System.out.println(Constants.INFO + "Exited.");
					return;
				}
				case "shutdown", "stop", "disconnectjda", "disconnect" -> {
					BotCore.shutDown();
					BotCore.SHUTINGDOWN = false;
					System.out.println(String.format(Constants.INFO + "Bot nolonger online.", BotCore.isBugFree()?"publicly":"privately"));
				}
				case "public" -> {
					BotCore.areWeTesting = false;
					System.out.println(String.format(Constants.INFO + "Bot is %s %s.", BotCore.isBugFree()?"publicly":"privately", BotCore.isOnline()?"online":"offline"));
				}
				case "private" -> {
					BotCore.areWeTesting = true;
					System.out.println(String.format(Constants.INFO + "Bot is %s %s.", BotCore.isBugFree()?"publicly":"privately", BotCore.isOnline()?"online":"offline"));
				}
				case "connectjda", "connect", "startjda", "start" -> {
					if (BotCore.jda == null){
						BotCore.startJDA();
						System.out.println(String.format(Constants.INFO + "Bot is %s online.", BotCore.isBugFree()?"publicly":"privately"));
					} else {
						System.out.println(String.format(Constants.INFO + "Bot is already %s online.", BotCore.isBugFree()?"publicly":"privately"));
					}
				}
				case "status", "stat"-> {
					System.out.print(getStatus());
				}
				case "load", "reload"-> {
					Users.loadAllUsers();
					System.out.println(Constants.IO + "Loaded All data from disk.");
				}
				case "help" -> {
					System.out.println(usage());
				}
				default -> {
					try {
						output = execute(input, Constants.ADMINID, null);
						if (output!=null){
							out = output.getAsText();
							for (String s: out){System.out.println(s);}
						}
					}catch(Exception e){
						System.err.printf(Constants.ERROR + "An error occured while executing :%s\n",Constants.MAGENTA + input+ Constants.RESET);
						e.printStackTrace();
					}
					
				}
			}
		}
	}
	public static CommandOutput execute(String message, String userId, @Nullable List<String> attachements) {
		long start = System.nanoTime();
		CommandOutput.Builder ouitput = new CommandOutput.Builder();
		List<String> arguments=new ArrayList<>();
		List<String> tmp = parsePrefix(userId, message);
		if (tmp.isEmpty()){
			return null;
		}
		
		arguments.add(tmp.getFirst());
		tmp = parseBotCommand(tmp.getLast());
		if (tmp.isEmpty()){
			return BotCommand.getCommandByName(HelpCommand.CMDNAME).execute(userId, List.of());
		}
		
		arguments.add(tmp.getFirst());
		BotCommand cmd = BotCommand.getCommandByName(arguments.get(1));
		
		if(BotCore.isShutingDown()){
			BotCommand.CommandCategory category = cmd.getCategory();
			if(category.equals(BotCommand.CommandCategory.EDITING) || category.equals(BotCommand.CommandCategory.GAME)){
				return ouitput.add(Constants.UPDATEEXPLANATION).build();
			}
		}
		arguments = (tmp.size()>=2)?cmd.parseArguments(tmp.getLast()):cmd.parseArguments("");
		if(attachements!=null)
		for (List<String> l :attachements.stream().map(a-> cmd.parseArguments(a)).toList()) {
			if (!l.isEmpty() && !l.getFirst().isBlank()){
				arguments.addAll(l);
			}
		}
		
		System.out.printf(Constants.INFO + "%s, time=`%.3f ms`, argc=%d",cmd.getName(), (System.nanoTime() - start) / 1000000.00, arguments.size());
		if (!BotCore.isBugFree() && !arguments.isEmpty()) {
			System.out.print(";");
			for (int i=0; i<arguments.size(); i++) {
				System.out.print(arguments.get(i).replace("[\\n \\t]", ""));
				if (i==arguments.size()-1) {System.out.print(";\n");}
				else {System.out.print("::");}
			}
		}else {System.out.print(";\n");}
		return cmd.execute(userId, arguments);
	}
	
	public static List<String> parsePrefix(String userId, String message){
		String prefix;
		String userPrefix = Users.get(userId).getPrefix();
		// Do not preccess if no known cmd prefix can fit in message
		if ((userPrefix!=null && message.length() < userPrefix.length()) && message.length() < Constants.CMDPREFIXE.length()){
			return List.of();
		}
		int cmdPrefixLen;
		// Prioritise user defined prefix, if not found search for the default prefix
		if (userPrefix!=null && userPrefix.length()<=Constants.CMDPREFIXE.length() && Constants.CMDPREFIXE.startsWith(userPrefix)
			&& Constants.CMDPREFIXE.length()<=message.length() && message.startsWith(Constants.CMDPREFIXE)){
			prefix = Constants.CMDPREFIXE;
		}else if (userPrefix!=null && message.startsWith(userPrefix)){
			prefix = userPrefix;
		}else if (message.startsWith(Constants.CMDPREFIXE)){
			prefix = Constants.CMDPREFIXE;
		}else {
			return List.of();
		}
		cmdPrefixLen = prefix.length();
		String remainingMessage = message.substring(cmdPrefixLen).trim();
		// return a list containing the prefix that was found and the rest of the unprocessed arguments
		return List.of(prefix, remainingMessage);
	}
	
	public static List<String> parseBotCommand(String message){
		if (message.length()<=0) {
			return List.of();
		}
		String[] tab = message.split("\\s+");
		if (tab.length>0){
			String userCmdName = tab[0].trim().toLowerCase();
			BotCommand cmd = BotCommand.getCommandByName(userCmdName);
			if (cmd!=null){
				if (tab.length>1){
					String remainingMessage ="";
					for(int i=1; i<tab.length; ++i){
						remainingMessage+= " "+tab[i];
					}
					return List.of(cmd.getName(), remainingMessage);
				}
				return List.of(cmd.getName());
			}
		}
		return List.of();
	}
}

