package com.linked.quizbot.commands;
import java.util.List;
import java.util.Random;
import java.util.ArrayList;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.HashSet;
import java.util.HashMap;
import java.util.Map;
import java.util.Iterator;
import java.util.Arrays;

import com.linked.quizbot.Constants;
import com.linked.quizbot.commands.BotCommand;
import com.linked.quizbot.commands.CommandCategory;
import com.linked.quizbot.commands.list.CollectionCommand;
import com.linked.quizbot.commands.list.EndCommand;
import com.linked.quizbot.commands.list.ExplainCommand;
import com.linked.quizbot.commands.list.LeaderBoardCommand;
import com.linked.quizbot.commands.list.StartCommand;
import com.linked.quizbot.commands.list.NextCommand;
import com.linked.quizbot.commands.list.PreviousCommand;
import com.linked.quizbot.commands.list.MoreTimeCommand;
import com.linked.quizbot.commands.list.ViewCommand;
import com.linked.quizbot.commands.list.DeleteCommand;
import com.linked.quizbot.commands.list.HelpCommand;
import com.linked.quizbot.commands.list.InviteCommand;
import com.linked.quizbot.commands.list.CreateListCommand;
import com.linked.quizbot.commands.list.AddListCommand;
import com.linked.quizbot.commands.list.EmbedCommand;
import com.linked.quizbot.commands.list.PingCommand;
import com.linked.quizbot.core.BotCore;
import com.linked.quizbot.utils.QuestionList;
import com.linked.quizbot.utils.UserLists;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.channel.middleman.MessageChannel;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.GenericEvent;
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
	
	public abstract void execute(User sender, Message message, MessageChannel channel, String[] args);

	public abstract String getName();

	public CommandCategory getCategory(){
		return CommandCategory.OTHER;
	}

	public String[] getAbbreviations(){
		return new String[0];
	}

    public List<OptionData> getOptionData(){
		List<OptionData> res = new ArrayList<>();
        return res;
	}

	public abstract String getDescription();

	public String getDetailedExamples(){
		return getDescription();
	}

	public static Set<BotCommand> getCommands() { 
		Set<BotCommand> res = new HashSet<>();
		res.addAll(Arrays.asList(
			new CreateListCommand(),
			new CollectionCommand(),
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
			new StartCommand(),
			new AddListCommand(),
			new ViewCommand()
		));
		return res;
	}

	public static BotCommand getCommandByName(String name) {
		for (BotCommand cmd : BotCommand.getCommands()) {
			if(cmd.getName().equals(name)) {
				return cmd;
			}
		}
		for (BotCommand cmd : BotCommand.getCommands()) {
			String[] abbrev = cmd.getAbbreviations();
			for(int i=0; i<abbrev.length; i++) {
				if(abbrev[i].equals(name)) {
					return cmd;
				}
			}
		}
		return null;
	}

	public static String getUserIdFromArg(String arg, JDA jda) {
        long start = System.nanoTime();
		if (arg.startsWith("<@")) {
			return arg.substring(2, arg.length());
		}
		String approxiUserId = null;
		int minDistTo0 = 10;
		Set<User> allUsers = new HashSet<>();
		allUsers.addAll(jda.getUsers());
		allUsers.addAll(BotCore.getAllUsers());
		for (User u : allUsers){
			if (!u.isBot()) {
				ArrayList<String> l = new ArrayList<>();
				String userId = u.getId().toLowerCase();
				String userName = u.getName().toLowerCase();
				String userEffectiveName = u.getEffectiveName().toLowerCase();
				String userTag = u.getAsTag().toLowerCase();
				l.add(userId);l.add(userName);l.add(userEffectiveName);l.add(userTag);
				System.out.printf("   $> %s %s %s %s;\n", userId, userName, userEffectiveName,userTag);
				for (String s : l){
					int v = arg.compareTo(s);
					if(v==0){
						return userId;
					}
					if(Math.abs(v)<minDistTo0){
						approxiUserId = userId;
						minDistTo0 = Math.abs(v);
					}
				}
			}
		}
		System.out.printf("    $> approxiUserId %s;\n", approxiUserId);
		System.out.printf("   $> time getUserIdFromArg = %.3f ms", (System.nanoTime() - start) / 1000000.00);
		if (arg.length()==18){
			return arg;
		}
		return approxiUserId;
	}

	public static QuestionList getSelectedQuestionList(String senderId, JDA jda, String[] args) {
        long start = System.nanoTime();
		UserLists userList;
        int n = args.length;
        int themeIndex = -1, listIndex = -1;
        String userId, theme;
        List<QuestionList> lists;
        if (n>=3) {
            userId = getUserIdFromArg(args[0], jda);
            themeIndex = Integer.parseInt(args[1])-1;
            listIndex = Integer.parseInt(args[2])-1;
        }else if(n==2) {
            userId = senderId;
            themeIndex = Integer.parseInt(args[0])-1;
            listIndex = Integer.parseInt(args[1])-1;
        } else if (n==1 && args[0].length()>1) {
            //making sure the string is long enough to be a user id, 1 is good enough
            userId = getUserIdFromArg(args[0], jda);
        }else if (n==0){
			userId = senderId;
		}else{
			return null;
        }
		if (userId == null){
			return null;
		}
		userList= new UserLists(userId);
		int themesSize = userList.getAllThemes().size();
		if (themeIndex == -1 && listIndex==-1) {
			themeIndex = rand.nextInt(0, themesSize);
            listIndex = rand.nextInt(0, userList.getListsByTheme(userList.getAllThemes().get(themeIndex)).size());
		}
		QuestionList res;
		if (themeIndex==themesSize && listIndex==0) {
			res = QuestionList.getExampleQuestionList();
			res.setAuthorId(senderId);
		} else {
			theme = userList.getAllThemes().get(themeIndex);
			lists = userList.getListsByTheme(theme);
			res = lists.get(listIndex);
		}
		System.out.printf("  $> time getSelectedQuestionList = %.3f ms\n", (System.nanoTime() - start) / 1000000.00);
		return res;
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
		if(!commandByCategory.isEmpty()){
			return commandByCategory.get(cat);
		}
		for (CommandCategory c : CommandCategory.getCategories()){
			commandByCategory.put(c, new HashSet<>());
		}
		for (BotCommand cmd : getCommands()){
			commandByCategory.get(cmd.getCategory()).add(cmd);
		}
		return commandByCategory.get(cat);
	}
    public static void recursive_send(Iterator<String> iter, Message message, MessageChannel channel){
		if (iter.hasNext()){
			if (message==null) {
				MessageCreateAction send = channel.sendMessage(iter.next());
				if (message != null){send.setMessageReference(message);}
				send.queue(msg -> {
					recursive_send(iter, msg, channel);
				});
			} else {
				MessageCreateAction send = message.getChannel().sendMessage(iter.next());
				send.setMessageReference(message);
				send.queue(
					msg -> { 
						recursive_send(iter, msg, channel);
						msg.delete().queueAfter(Constants.READTIMELONGMIN, TimeUnit.MINUTES);
					}
				);
			}
		}
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