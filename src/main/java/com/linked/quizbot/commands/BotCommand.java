package com.linked.quizbot.commands;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import com.linked.quizbot.Constants;
import com.linked.quizbot.commands.list.AddListCommand;
import com.linked.quizbot.commands.list.CollectionCommand;
import com.linked.quizbot.commands.list.CreateListCommand;
import com.linked.quizbot.commands.list.CreateTagCommand;
import com.linked.quizbot.commands.list.DeleteCommand;
import com.linked.quizbot.commands.list.EmbedCommand;
import com.linked.quizbot.commands.list.EndCommand;
import com.linked.quizbot.commands.list.ExplainCommand;
import com.linked.quizbot.commands.list.HelpCommand;
import com.linked.quizbot.commands.list.InviteCommand;
import com.linked.quizbot.commands.list.LeaderBoardCommand;
import com.linked.quizbot.commands.list.MoreTimeCommand;
import com.linked.quizbot.commands.list.NextCommand;
import com.linked.quizbot.commands.list.PingCommand;
import com.linked.quizbot.commands.list.PreviousCommand;
import com.linked.quizbot.commands.list.SetPrefixeCommand;
import com.linked.quizbot.commands.list.StartCommand;
import com.linked.quizbot.commands.list.TagListCommand;
import com.linked.quizbot.commands.list.UserInfoCommand;
import com.linked.quizbot.commands.list.ViewCommand;
import com.linked.quizbot.core.BotCore;
import com.linked.quizbot.utils.QuestionList;
import com.linked.quizbot.utils.Users;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.channel.middleman.MessageChannel;
import net.dv8tion.jda.api.entities.emoji.Emoji;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;
import net.dv8tion.jda.api.requests.restaction.MessageCreateAction;

/**
 * The  BotCommand is an abstract class that represents any command that can be ran by the bot.
 * 
 * This class takes insparation from {@link https://github.com/Tran-Antoine/Askigh-Bot/}
 */
public abstract class BotCommand {
	private final static Map<CommandCategory, Set<BotCommand>> commandByCategory = new HashMap<>();
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
            new DeleteCommand(),
            new EndCommand(),
            new ExplainCommand(),
            new EmbedCommand(),
            new HelpCommand(),
            new InviteCommand(),
            new LeaderBoardCommand(),
            new MoreTimeCommand(),
            new NextCommand(),
            new PingCommand(),
            new PreviousCommand(),
            new SetPrefixeCommand(),
            new StartCommand(),
            new TagListCommand(),
            new UserInfoCommand(),
            new ViewCommand()
        ));
    }
	
	public abstract void execute(User sender, Message message, MessageChannel channel,List<String> args);

	public abstract String getName();

	public CommandCategory getCategory(){
		return CommandCategory.OTHER;
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

	public static QuestionList getSelectedQuestionList(String listId) {
		return Users.getQuestionListByListId(listId);
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
	public static Set<BotCommand> getCommandsByCategory(CommandCategory cat){
		if(commandByCategory.isEmpty()){
			for (CommandCategory c : CommandCategory.getCategories()){
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
    public static void recursive_send(Iterator<String> iter, Message message, MessageChannel channel){
		if (iter.hasNext()){
			String s=iter.next();
			if (s.length()>Constants.CHARSENDLIM){
				recursive_send(trimMessage(s).iterator(), message, channel);
				recursive_send(iter, message, channel);
			} else {
				if (message==null) {
					MessageCreateAction send = channel.sendMessage(s);
					send.queue(msg -> {
						recursive_send(iter, msg, channel);
					});
				} else {
					MessageCreateAction send = message.getChannel().sendMessage(s);
					send.setMessageReference(message);
					send.queue(
						msg -> { 
							recursive_send(iter, msg, channel);
						}
					);
				}
			}
		}
    }
	public static List<String> trimMessage(String s){
        List<String> res = new ArrayList<>();
        if (s.length()>Constants.CHARSENDLIM){
            String l=s.substring(0, Constants.CHARSENDLIM), r = s.substring(Constants.CHARSENDLIM);
            int index = l.lastIndexOf("\n");
            if (index >0){
                res.add(l.substring(0, index+1));
                res.addAll(trimMessage(l.substring(index+1)));
                res.addAll(trimMessage(r));
            } else {
                res.add(l);
                res.addAll(trimMessage(r));
            }
        } else {
            res.add(s);
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