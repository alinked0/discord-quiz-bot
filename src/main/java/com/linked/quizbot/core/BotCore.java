package com.linked.quizbot.core;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import com.linked.quizbot.Constants;
import com.linked.quizbot.commands.BotCommand;
import com.linked.quizbot.commands.CommandOutput;
import com.linked.quizbot.commands.list.ExplainCommand;
import com.linked.quizbot.commands.list.HelpCommand;
import com.linked.quizbot.events.ButtonListener;
import com.linked.quizbot.events.CommandLineListener;
import com.linked.quizbot.events.ReactionListener;
import com.linked.quizbot.events.SlashCommandListener;
import com.linked.quizbot.events.readyEvent;
import com.linked.quizbot.utils.Option;
import com.linked.quizbot.utils.Question;
import com.linked.quizbot.utils.QuestionList;
import com.linked.quizbot.utils.Users;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.channel.middleman.MessageChannel;
import net.dv8tion.jda.api.entities.emoji.Emoji;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.utils.AttachedFile;
/**
 * Te class BotCore stores bot prefrences. 
 * this class takes insparation from net.askigh.quizz.core.BotCore
 */

public class BotCore {
	public static boolean SHUTINGDOWN = false;
	public static JDA jda = null;
	public static Random rand = new Random();
	public static  String cmdPrefixe = Constants.CMDPREFIXE;
	public static Map<String, Viewer> viewerByMessageId = new HashMap<>();
	public static Set<BotCommand> commands = new HashSet<>();
	public static List<Viewer> listOfGames = new ArrayList<>();
	public static Set<User> allUsers = new HashSet<>();
	public static Map<String, QuestionList> toBeDeleted = new HashMap<>();
	public static Map<String, Message> deletionMessages = new HashMap<>();
	public static Set<String> explicationRequest = new HashSet<>();

	public static Random getRandom(){ return rand;}
	public static String getPrefixe() { return cmdPrefixe;}
	public static List<User> getAllUsers() { return new ArrayList<>(allUsers);}
	public static void addUser(User u) { allUsers.add(u);}
	public static List<Viewer> getListOfViewers() {
		return listOfGames;
	}
	public static JDA getJDA() {
		return jda;
	}
	public static JDA startJDA(){
		if (getJDA()!=null){
			return getJDA();
		}
		JDA jda = JDABuilder.createDefault(Constants.TOKEN,
			GatewayIntent.GUILD_MESSAGES, 
			GatewayIntent.MESSAGE_CONTENT,
			GatewayIntent.GUILD_MEMBERS, 
			GatewayIntent.DIRECT_MESSAGES, 
			GatewayIntent.DIRECT_MESSAGE_REACTIONS,
			GatewayIntent.GUILD_MESSAGE_REACTIONS
		).setActivity(Activity.playing(Constants.CMDPREFIXE+HelpCommand.CMDNAME)).build();
		BotCore.jda = jda;
		jda.addEventListener(
			new SlashCommandListener(), 
			new ReactionListener(), 
			new ButtonListener(),
			new CommandLineListener(),
			new readyEvent()
		);
		try {
			jda.awaitReady();
		}catch (InterruptedException e){
			e.printStackTrace();
		}catch(IllegalStateException e){
			e.printStackTrace();
		}
		return jda;
	}
	public static void shutDown(){
		if (getJDA()==null){
			return;
		}
		BotCore.SHUTINGDOWN = true;
		Users.exportAllUserLists();
		Users.exportAllUserData();
		getJDA().shutdownNow();
		BotCore.jda = null;
	}
	public static boolean isShutingDown(){
		return BotCore.SHUTINGDOWN;
	}
	public static Viewer getViewer(String messageId) {
		Viewer q =viewerByMessageId.getOrDefault(messageId, null);
		return q;
	}
	public static void comfirmDeletion(Message message, QuestionList l) {
		//System.out.println("  $> list="+l);
		String messageId = message.getId();
		toBeDeleted.put(messageId, l);
		deletionMessages.put(messageId, message);
	}
	public static Viewer getCurrViewer(String messageId) {
		return viewerByMessageId.getOrDefault(messageId, null);
	}
	public static void endViewer (Viewer q) {
		String messageId = q.getMessageId();
		viewerByMessageId.remove(messageId);
		q.end();
	}
	public static void deleteList(QuestionList l, String messageId){
		Users.deleteList(l);
		Message message = BotCore.deletionMessages.get(messageId);
		int n = l.toString().length();
		if (n>Constants.CHARSENDLIM) {
			message.editMessageAttachments(AttachedFile.fromData(new File(l.getPathToList()))).queue();
		} else{
			message.editMessage("```js\n"+l.toString()+"\n``` is deleted form your collection\n").queue();
		}
		BotCore.toBeDeleted.remove(messageId);
		BotCore.deletionMessages.remove(messageId);
	}
	public void setPrefixe(String prefixe) { cmdPrefixe = prefixe;}
	public static String getEffectiveNameFromId(String userId){
		String res;User user;
		if (getJDA()!=null){
			user = getJDA().getUserById(userId);
			if (user!=null){
				res = user.getEffectiveName();
				if (res!=null){
					return res;
				}
			}
		}
		List<User> l= BotCore.allUsers.stream().filter(u -> u.getId().equals(userId)).toList();
		if(!l.isEmpty()){
			user=l.getFirst();
			user = getJDA().getUserById(userId);
			if (user!=null){
				res = user.getEffectiveName();
				if (res!=null){
					return res;
				}
			}
		}
		return userId;
	}
	public static User getUser(String userId){
		Set<User> allUsers = new HashSet<>();
		allUsers.addAll(BotCore.getAllUsers());
		allUsers.addAll(jda.getUsers());
		for (User u : allUsers){
			if (u.getId().equals(userId)){
				return u;
			}
		}
		return null;
	}
}
