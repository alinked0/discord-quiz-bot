package com.linked.quizbot.commands;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import com.linked.quizbot.Constants;
import com.linked.quizbot.commands.list.AddListCommand;
import com.linked.quizbot.commands.list.CollectionCommand;
import com.linked.quizbot.commands.list.CreateListCommand;
import com.linked.quizbot.commands.list.CreateTagCommand;
import com.linked.quizbot.commands.list.CurrentCommand;
import com.linked.quizbot.commands.list.DeleteListCommand;
import com.linked.quizbot.commands.list.EmbedCommand;
import com.linked.quizbot.commands.list.EndCommand;
import com.linked.quizbot.commands.list.ExplainCommand;
import com.linked.quizbot.commands.list.HelpCommand;
import com.linked.quizbot.commands.list.HistoryCommand;
import com.linked.quizbot.commands.list.InviteCommand;
import com.linked.quizbot.commands.list.LeaderBoardCommand;
import com.linked.quizbot.commands.list.UseAutoNextCommand;
import com.linked.quizbot.commands.list.NextCommand;
import com.linked.quizbot.commands.list.PingCommand;
import com.linked.quizbot.commands.list.PreviousCommand;
import com.linked.quizbot.commands.list.RawListCommand;
import com.linked.quizbot.commands.list.RemoveListCommand;
import com.linked.quizbot.commands.list.RemoveTagCommand;
import com.linked.quizbot.commands.list.RenameListCommand;
import com.linked.quizbot.commands.list.SetPrefixCommand;
import com.linked.quizbot.commands.list.StartCommand;
import com.linked.quizbot.commands.list.TagsCommand;
import com.linked.quizbot.commands.list.AddTagCommand;
import com.linked.quizbot.commands.list.UseButtonsCommand;
import com.linked.quizbot.commands.list.UserInfoCommand;
import com.linked.quizbot.commands.list.ViewCommand;
import com.linked.quizbot.core.BotCore;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Message.Attachment;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.emoji.Emoji;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;

/**
 * The  BotCommand is an abstract class that represents any command that can be ran by the bot.
 * 
 * This class takes insparation from {@link https://github.com/Tran-Antoine/Askigh-Bot/}
 * @author alinked0
 * @version 1.0
 * @since 2025-02-01
 * @see BotCommand.CommandCategory
 * @see CommandOutput
 */
public abstract class BotCommand {
	private final static Map<BotCommand.CommandCategory, Set<BotCommand>> commandByCategory = new HashMap<>();
	public static Random rand = new Random();
	
	// Static set to hold all command instances, initialized once.
	private static final Set<BotCommand> ALL_COMMANDS;
	private static final Set<BotCommand> PUBLIC_COMMANDS;
	private static final Set<BotCommand> PRIVATE_COMMANDS;
	
	// Static block to initialize the command sets.
	static {
		PUBLIC_COMMANDS = new HashSet<>();
		PRIVATE_COMMANDS = new HashSet<>();
		ALL_COMMANDS = new HashSet<>();
		PUBLIC_COMMANDS.addAll(List.of(
			new AddListCommand(),
			new AddTagCommand(),
			new CreateListCommand(),
			new CreateTagCommand(),
			new CollectionCommand(),
			new CurrentCommand(),
			new DeleteListCommand(),
			new EmbedCommand(),
			new EndCommand(),
			new ExplainCommand(),
			new HelpCommand(),
			new HistoryCommand(),
			new InviteCommand(),
			new LeaderBoardCommand(),
			new NextCommand(),
			new PingCommand(),
			new PreviousCommand(),
			new RawListCommand(),
			new RenameListCommand(),
			new RemoveTagCommand(),
			new SetPrefixCommand(),
			new StartCommand(),
			new TagsCommand(),
			new UseAutoNextCommand(),
			new UseButtonsCommand(),
			new UserInfoCommand(),
			new ViewCommand()
		));
		PRIVATE_COMMANDS.addAll(List.of(
			new RemoveListCommand()
		));
		ALL_COMMANDS.addAll(PUBLIC_COMMANDS);
		ALL_COMMANDS.addAll(PRIVATE_COMMANDS);
	}
	/**
	 * This enum represents the different categories of commands that can be used in the bot.
	 */
	public static enum CommandCategory {
		GAME, NAVIGATION, EDITING, READING, OTHER;
		private String name;
		static {
			GAME.name = "Game";
			NAVIGATION.name = "Navigation";
			EDITING.name = "Editing";
			READING.name = "Reading";
			OTHER.name = "Other";
		}
		public static Set<CommandCategory> getCategories() {
			Set<CommandCategory> res = new HashSet<>();
			res.addAll(Arrays.asList(GAME, NAVIGATION, EDITING, READING, OTHER));
			return res;
		}
		public String toString(){
			return name;
		}
	}
	
	/**
	 * Executes the command with the given userId and arguments.
	 * @param userId The ID of the user who is executing the command.
	 * @param args The arguments passed to the command.
	 * @return The output of the command execution.
	 */
	public abstract CommandOutput execute(String userId,  List<String> args);
	
	/**
	 * This is a getter method that returns the name of the command.
	 * @return The name of the command as a String.
	 * @pure
	 */
	public abstract String getName();
	
	/**
	 * @return The category of the command as a CommandCategory enum.
	 * @pure
	 */
	public BotCommand.CommandCategory getCategory(){
		return BotCommand.CommandCategory.OTHER;
	}
	
	/**
	 * @return A list of abbreviations as Strings.
	 * @pure
	 */
	public List<String> getAbbreviations(){
		return List.of();
	}
	
	/**
	 * Returns a list of OptionData objects that represent the options for the command.
	 * It is mainly use to create the slash command.
	 * @return A list of OptionData objects.
	 * @pure
	 */
	public List<OptionData> getOptionData(){
		List<OptionData> res = new ArrayList<>();
		return res;
	}
	
	/**
	 * @return A String that describes the command.
	 * @pure
	 */
	public abstract String getDescription();
	
	/**
	 * @return A String that contains detailed examples of the command or a message indicating that no examples were found.
	 * @pure
	 */
	public String getDetailedExamples(){
		return "no examples found.";
	}
	
	/**
	 * @return A set of BotCommand objects that represent all commands.
	 * @pure
	 */
	public static Set<BotCommand> getCommands() {
		return PUBLIC_COMMANDS;
	}
	
	/**
	 * Parses the command line arguments and returns a list of non-empty arguments.
	 * @param cmndLineArgs The command line arguments as a String.
	 * @return A list of non-empty arguments as Strings.
	 */
	public List<String> parseArguments(String cmndLineArgs){
		int k = 0;
		List<String> res = new ArrayList<>();
		String s;
		String [] tmp = cmndLineArgs.split("\\s+");
		for (; k<tmp.length; ++k){
			s= tmp[k].trim();
			if (!s.isBlank()){
				res.add(s);
			}
		}
		return res;
	}
	
	/** 
	 * Splits a JSON-like string into a list of JSON objects.
	 * It handles nested JSON objects by keeping track of the opening and closing braces.
	 * This method is useful for parsing JSON strings that may contain nested objects.
	 * @param argumment The JSON-like string to be split.
	 * @return A list of JSON objects as Strings. 
	 */
	public static  List<String> splitJson(String argumment){
		int k = 0;
		int start = -1;
		List<String> res = new ArrayList<>();
		String[] l = argumment.split("");
		int i=0;
		for (i = 0; i<argumment.length(); i++){
			if (l[i].equals("{")){
				start = i;
				break;
			}
		}
		if (start==-1){return res;}
		for (; i<argumment.length(); i++){
			if (l[i].equals("{")){k+=1;}
			else if (l[i].equals("}")){k-=1;}
			if (k==0){
				res.add(argumment.substring(start, i+1));
				start = i+1;
				if (i+1<argumment.length()) res.addAll(splitJson(argumment.substring(start)));
				return res;
			}
		}
		return res;
	}
	
	/**
	 * Retrieves arguments from an attachment associated with a user.
	 * It downloads the attachment from the provided URL, reads its content, and splits it into a list of arguments.
	 * If the attachment is null, it returns an empty list.
	 * @param userId
	 * @param attachment
	 * @ensures \result != null
	 * @ensures \result.size() <= 1
	 * @return a list of arguments If the attachment is not null else it returns an empty list.
	 */
	public static List<String> getArgFromAttachment(String userId, Attachment attachment){
		List<String> res = new ArrayList<>();
		if (attachment==null){
			return res;
		}
		String tmpStr = "";
		try {
			URL website = new URL(attachment.getUrl());
			String path = Constants.LISTSPATH+Constants.SEPARATOR+userId+Constants.SEPARATOR+"tmp"+Constants.SEPARATOR+System.currentTimeMillis();
			File f = new File(path);
			if(!f.getParentFile().exists()) {
				f.getParentFile().mkdirs();
			}
			ReadableByteChannel rbc = Channels.newChannel(website.openStream());
			FileOutputStream fos = new FileOutputStream(f);
			fos.getChannel().transferFrom(rbc, 0, Long.MAX_VALUE);
			BufferedReader fd = Files.newBufferedReader(f.toPath());
			String k = "";
			do {
				k=fd.readLine();
				tmpStr+= k;
			}while(k!=null);
			fd.close();
			f.delete();
			fos.close();
			if (!tmpStr.isEmpty()){
				res.addAll(BotCommand.splitJson(tmpStr));
			}
		} catch (IOException e) {
			System.err.println(Constants.ERROR + "(BotCommand.getArgFromAttachment) An error occurred while taking an attachment.");
			e.printStackTrace();
		}
		return res;
	}
	
	/**
	 * Retrieves arguments from a list of attachments associated with a user.
	 * @param userId
	 * @param c The list of attachments to retrieve arguments from.
	 * @ensures \result != null
	 * @ensures \result.size() <= c.size()
	 * @return A list of arguments extracted from the attachments. If the list is null, it returns an empty list.
	 * @see #getArgFromAttachment(String, Attachment)
	 */
	public static List<String> getArgFromAttachments(String userId, List<Attachment> c){
		List<String> res = new ArrayList<>();
		if (c==null){
			return res;
		}
		for (Attachment attachment : c){
			res.addAll(getArgFromAttachment( userId, attachment));
		}
		return res;
	}
	
	/**
	 * Retrieves a command by its name or abbreviation.
	 * @param name The name or abbreviation of the command to retrieve.
	 * @ensures \result != null <==> \result.getName().equals(name) || \result.getAbbreviations().contains(name)
	 * @return The BotCommand object that matches the given name or abbreviation, or null if no match is found.
	 */
	public static BotCommand getCommandByName(String name) {
		for (BotCommand cmd : BotCommand.ALL_COMMANDS) {
			if(cmd.getName().equals(name) || cmd.getAbbreviations().contains(name)) {
				return cmd;
			}
		}
		return null;
	}
	
	/**
	 * Attempts to parse the argument as an emoji using the JDA library.
	 * @param arg The argument string that may contain an emoji.
	 * @return The parsed Emoji object if successful, or null if the argument cannot be parsed as an emoji.
	 */
	public static Emoji getEmojiFromArg(String arg){
		try {
			return Emoji.fromFormatted(arg);
		} catch (IllegalArgumentException e) {
			System.err.println(Constants.ERROR + "Failed to parse emoji from argument: " + arg + " - " + e.getMessage());
			return null;
		}
	}
	
	/**
	 * Retrieves the ID of a user from a given argument string.
	 * It checks if the argument starts with "<@" to identify a user mention,
	 * and if not, it searches through all users in the JDA instance to find a match based on the argument.
	 * If an approximate match is found, it returns the user ID of that match.
	 * If the argument is a raw ID of the correct length, it returns that ID directly.
	 * @param arg The argument string that may contain a user mention or ID.
	 * @param jda The JDA instance used to access all users.
	 * @requires jda != null
	 * @ensures \result != null <==> \result.length() >= Constants.DISCORDIDLENMIN && \result.length() <= Constants.DISCORDIDLENMAX
	 * @return The user ID as a String if found, or null if no match is found.
	 * @pure
	 */
	public static String getIdFromArg(String arg, JDA jda) {
		long start = System.nanoTime();
		if (arg.startsWith("<@")) {
			return arg.substring(2, arg.length()-1);
		}
		
		String approxiUserId = null;
		int minDistTo0 = Integer.MAX_VALUE;
		
		Set<User> allUsers = new HashSet<>();
		allUsers.addAll(jda.getUsers());
		allUsers.addAll(BotCore.getAllUsers());
		for (User u : allUsers){
			if (!u.isBot()) { // Ignore bot users
				ArrayList<String> identifiers = new ArrayList<>();
				String userId = u.getId();
				String userName = u.getName().toLowerCase();
				String userEffectiveName = u.getEffectiveName().toLowerCase(); // Nickname in server
				String userTag = u.getAsTag().toLowerCase(); // Username#Discriminator (if not new system)
				identifiers.addAll(List.of(userId, userName, userEffectiveName, userTag));
				
				if (!BotCore.isBugFree()) { // Debug logging
					System.out.printf(Constants.INFO + "%s %s %s %s;\n", userId, userName, userEffectiveName,userTag);
				}
				
				String lowerArg = arg.toLowerCase();
				for (String s : identifiers){
					// Exact match first
					if(lowerArg.equals(s)){
						return userId;
					}
					// Simple difference for approximate matching 
					// TODO improve with Levenshtein distance
					int v = Math.abs(lowerArg.compareTo(s));
					if(v < minDistTo0){
						approxiUserId = userId;
						minDistTo0 = v;
					}
				}
			}
		}
		if (!BotCore.isBugFree()) { // Debug logging
			System.out.printf(Constants.INFO + "approxiUserId %s;\n", approxiUserId);
			System.out.printf(Constants.INFO + "time getIdFromArg = %.3f ms%n", (System.nanoTime() - start) / 1000000.00);
		}
		
		// If the arg itself is a raw ID of the correct length
		if (Constants.DISCORDIDLENMIN<=arg.length() && arg.length()<=Constants.DISCORDIDLENMAX){
			return arg;
		}
		return approxiUserId;
	}
	
	/**
	 * Returns the slash command data for this command.
	 * It creates a SlashCommandData object with the command's name, description, and options.
	 * @return A SlashCommandData object representing this command.
	 * @pure
	 */
	public SlashCommandData getSlashCommandData(){
		return Commands.slash(getName(), getDescription()).addOptions(getOptionData());
	}
	
	/**
	 * @return A list of SlashCommandData objects representing all commands.
	 * @pure
	 */
	public static List<SlashCommandData> getSlashCommandDataList(){
		List<SlashCommandData> commandData= new ArrayList<>();
		for (BotCommand cmd : BotCommand.getCommands()) {
			commandData.add(cmd.getSlashCommandData());
		}
		return commandData;
	}
	
	/**
	 * Returns a set of all commands that belong to the specified category.
	 * If the category is not found, it returns an empty set.
	 * @param cat The category for which to retrieve commands.
	 * @ensures \result != null
	 * @ensures \result.size() <= BotCommand.ALL_COMMANDS.size()
	 * @return A set of BotCommand objects belonging to the specified category.
	 */
	public static Set<BotCommand> getCommandsByCategory(BotCommand.CommandCategory cat){
		if(commandByCategory.isEmpty()){
			for (BotCommand.CommandCategory c : BotCommand.CommandCategory.getCategories()){
				commandByCategory.put(c, new HashSet<>());
			}
			for (BotCommand cmd : BotCommand.ALL_COMMANDS){
				commandByCategory.get(cmd.getCategory()).add(cmd);
			}
		}
		return commandByCategory.getOrDefault(cat, Collections.emptySet());
	}
	
	/**
	 * Returns a list of OptionData objects that are required for the command.
	 * It filters the options to include only those that are marked as required.
	 * @return A list of OptionData objects that are required for the command.
	 * @pure
	 */
	public List<OptionData> getRequiredOptionData(){
		ArrayList<OptionData> res = new ArrayList<>();
		for (OptionData opt:getOptionData()){
			if (opt.isRequired()){
				res.add(opt);
			}
		}
		return res;
	}
	
	/**
	 * Returns a BotCommand object based on the provided emoji formattedEmoji.
	 * It checks the formattedEmoji against predefined emojis and returns the corresponding command.
	 * @param formattedEmoji The emoji formattedEmoji to check.
	 * @return The BotCommand associated with the emoji, or null if no match is found.
	 * @pure
	 */
	public static BotCommand getCommandFromEmoji(String formattedEmoji){
		if (formattedEmoji.equals(Constants.EMOJIMORETIME)){
			return BotCommand.getCommandByName(UseAutoNextCommand.CMDNAME);
		}
		if (formattedEmoji.equals(Constants.EMOJISTOP)){
			return BotCommand.getCommandByName(EndCommand.CMDNAME);
		}
		if(formattedEmoji.equals(Constants.EMOJINEXTQUESTION)){
			return BotCommand.getCommandByName(NextCommand.CMDNAME);
		}
		if(formattedEmoji.equals(Constants.EMOJIPREVQUESTION)){
			return BotCommand.getCommandByName(PreviousCommand.CMDNAME);
		}
		if(formattedEmoji.equals(Constants.EMOJIRELOAD)){
			return BotCommand.getCommandByName(CurrentCommand.CMDNAME);
		}
		if(formattedEmoji.equals(Constants.EMOJIEXPLICATION)){
			return BotCommand.getCommandByName(ExplainCommand.CMDNAME);
		}
		if(formattedEmoji.equals(Constants.EMOJIDEL)){
			return BotCommand.getCommandByName(RemoveListCommand.CMDNAME);
		}
		return null;
	}
	
	/**
	 * Returns a string representation of the command in JSON format.
	 * It includes the command's name, description, and category.
	 * @return A JSON string representation of the command.
	 * @pure
	 */
	@Override
	public int hashCode() {
		return getName().hashCode()*7 + getDescription().hashCode()*2 + getCategory().hashCode();
	}
	
	/**
	 * Checks if this command is equal to another object.
	 * Two commands are considered equal if they have the same name, description, and category.
	 * @param o The object to compare with this command.
	 * @return true if the commands are equal, false otherwise.
	 * @pure
	 */
	@Override
	public boolean equals(Object o) {
		if (this == o)	return true;
		if(!(o instanceof BotCommand)) {
			return false;
		}
		BotCommand cmd = (BotCommand) o;
		return getName().equals(cmd.getName())
			&& getDescription().equals(cmd.getDescription())
			&& getCategory().equals(cmd.getCategory());
	}
}
