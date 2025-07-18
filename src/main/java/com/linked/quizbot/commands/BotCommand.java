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
import com.linked.quizbot.commands.list.DeleteListCommand;
import com.linked.quizbot.commands.list.EmbedCommand;
import com.linked.quizbot.commands.list.EndCommand;
import com.linked.quizbot.commands.list.ExplainCommand;
import com.linked.quizbot.commands.list.HelpCommand;
import com.linked.quizbot.commands.list.InviteCommand;
import com.linked.quizbot.commands.list.LeaderBoardCommand;
import com.linked.quizbot.commands.list.AutoNextCommand;
import com.linked.quizbot.commands.list.NextCommand;
import com.linked.quizbot.commands.list.PingCommand;
import com.linked.quizbot.commands.list.PreviousCommand;
import com.linked.quizbot.commands.list.RawCommand;
import com.linked.quizbot.commands.list.RenameListCommand;
import com.linked.quizbot.commands.list.SetPrefixeCommand;
import com.linked.quizbot.commands.list.StartCommand;
import com.linked.quizbot.commands.list.TagListCommand;
import com.linked.quizbot.commands.list.UseButtonsCommand;
import com.linked.quizbot.commands.list.UserInfoCommand;
import com.linked.quizbot.commands.list.ViewCommand;
import com.linked.quizbot.core.BotCore;
import com.linked.quizbot.utils.QuestionList;
import com.linked.quizbot.utils.Users;

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
 */
public abstract class BotCommand {
	private final static Map<BotCommand.CommandCategory, Set<BotCommand>> commandByCategory = new HashMap<>();
	public static Random rand = new Random();

    // Static set to hold all command instances, initialized once.
    private static final Set<BotCommand> ALL_COMMANDS;

    static {
        ALL_COMMANDS = new HashSet<>();
        ALL_COMMANDS.addAll(List.of(
            new AddListCommand(),
            new CreateListCommand(),
            new CollectionCommand(),
            new CreateTagCommand(),
            new DeleteListCommand(),
            new EndCommand(),
            new ExplainCommand(),
            new EmbedCommand(),
            new HelpCommand(),
            new InviteCommand(),
            new LeaderBoardCommand(),
            new AutoNextCommand(),
            new NextCommand(),
            new PingCommand(),
            new PreviousCommand(),
			new RawCommand(),
			new RenameListCommand(),
            new SetPrefixeCommand(),
            new StartCommand(),
            new TagListCommand(),
            new UserInfoCommand(),
            new UseButtonsCommand(),
            new ViewCommand()
        ));
    }

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
	
	public abstract CommandOutput execute(String userId,  List<String> args);

	public abstract String getName();

	public BotCommand.CommandCategory getCategory(){
		return BotCommand.CommandCategory.OTHER;
	}

	public List<String> getAbbreviations(){
		return List.of();
	}

    public List<OptionData> getOptionData(){
		List<OptionData> res = new ArrayList<>();
        return res;
	}

	public abstract String getDescription();

	public String getDetailedExamples(){
		return "no examples found.";
	}

	public static Set<BotCommand> getCommands() {
		return ALL_COMMANDS;
	}

	public List<String> parseArguments(String cmndLineArgs){
		int k = 0;
		List<String> res = new ArrayList<>();
		String [] tmp = cmndLineArgs.split("\\s+");
		for (; k<tmp.length; ++k){
			if (tmp[k]!=null && !tmp[k].isEmpty() && tmp[k]!=""){
				res.add(tmp[k]);
			}
		}
		return res;
	}
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
    public static List<String> getArgFromAttachments(String userId, Attachment attachment){
        List<String> res = new ArrayList<>();
        if (attachment==null){
            return res;
        }
		String tmpStr = "";
		try {
			URL website = new URL(attachment.getUrl());
			String path = Constants.LISTSPATH+Constants.SEPARATOR+userId+Constants.SEPARATOR+"tmp";
			File f = new File(path);
			ReadableByteChannel rbc = Channels.newChannel(website.openStream());
			FileOutputStream fos = new FileOutputStream(f);
			fos.getChannel().transferFrom(rbc, 0, Long.MAX_VALUE);
			if(!f.getParentFile().exists()) {
				f.getParentFile().mkdirs();
			}
			BufferedReader fd = Files.newBufferedReader(f.toPath());

			String k = "";
			do {
				tmpStr+= k;
				k=fd.readLine();
			}while(k!=null);
			fd.close();
			f.delete();
			fos.close();
			if (!tmpStr.isEmpty()){
				res.addAll(BotCommand.splitJson(tmpStr));
			}
		} catch (IOException e) {
			System.err.println(" $> An error occurred while taking an attachment.");
			e.printStackTrace();
		}
        return res;
    }
    public static List<String> getArgFromAttachments(String userId, List<Attachment> c){
        List<String> res = new ArrayList<>();
        if (c==null){
            return res;
        }
        for (Attachment attachment : c){
            res.addAll(getArgFromAttachments( userId, attachment));
        }
        return res;
    }

	public static BotCommand getCommandByName(String name) {
		for (BotCommand cmd : BotCommand.getCommands()) {
			if(cmd.getName().equals(name)) {
				return cmd;
			}
		}
		for (BotCommand cmd : BotCommand.getCommands()) {
			for(int i=0; i<cmd.getAbbreviations().size(); i++) {
				if(cmd.getAbbreviations().get(i).equals(name)) {
					return cmd;
				}
			}
		}
		return null;
	}
    public static Emoji getEmojiFromArg(String arg){
        try {
            return Emoji.fromFormatted(arg);
        } catch (IllegalArgumentException e) {
            System.err.println("Failed to parse emoji from argument: " + arg + " - " + e.getMessage());
            return null;
        }
    }
	public static String getUserIdFromArg(String arg, JDA jda) {
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
				identifiers.add(userId);
				identifiers.add(userName);
				identifiers.add(userEffectiveName);
				identifiers.add(userTag);

				if (!Constants.isBugFree()) { // Debug logging
					System.out.printf("   $> %s %s %s %s;\n", userId, userName, userEffectiveName,userTag);
				}

				String lowerArg = arg.toLowerCase();
				for (String s : identifiers){
					// Exact match first
					if(lowerArg.equals(s)){
						return userId;
					}
					// Simple difference for approximate matching 
					// TODO improve with Levenshtein distance
					int v = lowerArg.compareTo(s);
					if(Math.abs(v) < minDistTo0){
						approxiUserId = userId;
						minDistTo0 = Math.abs(v);
					}
				}
			}
		}
		if (!Constants.isBugFree()) { // Debug logging
			System.out.printf("    $> approxiUserId %s;\n", approxiUserId);
			System.out.printf("   $> time getUserIdFromArg = %.3f ms%n", (System.nanoTime() - start) / 1000000.00);
		}

		// If the arg itself is a raw ID of the correct length
		if (Constants.DISCORDIDLENMIN<=arg.length() && arg.length()<=Constants.DISCORDIDLENMAX){
			return arg;
		}
		return approxiUserId;
	}

	public static QuestionList getSelectedQuestionList(String id) {
		return Users.getQuestionListById(id);
	}
	public SlashCommandData getSlashCommandData(){
		return Commands.slash(getName(), getDescription()).addOptions(getOptionData());
	}
	public static List<SlashCommandData> getSlashCommandDataList(){
		List<SlashCommandData> commandData= new ArrayList<>();
		for (BotCommand cmd : BotCommand.getCommands()) {
			commandData.add(cmd.getSlashCommandData());
		}
		return commandData;
	}
	public static Set<BotCommand> getCommandsByCategory(BotCommand.CommandCategory cat){
		if(commandByCategory.isEmpty()){
			for (BotCommand.CommandCategory c : BotCommand.CommandCategory.getCategories()){
				commandByCategory.put(c, new HashSet<>());
			}
			for (BotCommand cmd : getCommands()){
				commandByCategory.get(cmd.getCategory()).add(cmd);
			}
		}
		return commandByCategory.getOrDefault(cat, Collections.emptySet());
	}
	public List<OptionData> getRequiredOptionData(){
		ArrayList<OptionData> res = new ArrayList<>();
		for (OptionData opt:getOptionData()){
			if (opt.isRequired()){
				res.add(opt);
			}
		}
		return res;
	}
	@Override
	public int hashCode() {
		return getName().hashCode()*7 + getDescription().hashCode()*2 + getCategory().hashCode();
	}
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