package com.linked.quizbot.core;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.linked.quizbot.Constants;
import com.linked.quizbot.commands.BotCommand;
import com.linked.quizbot.commands.list.ExplainCommand;
import com.linked.quizbot.utils.Option;
import com.linked.quizbot.utils.Question;
import com.linked.quizbot.utils.QuestionList;
import com.linked.quizbot.utils.Users;

import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.channel.middleman.MessageChannel;
import net.dv8tion.jda.api.entities.emoji.Emoji;
import net.dv8tion.jda.api.utils.AttachedFile;
/**
 * Te class BotCore stores bot prefrences. 
 * this class takes insparation from net.askigh.quizz.core.BotCore
 */

public class BotCore {
	public static boolean SHUTINGDOWN = false;
	public static  String cmdPrefixe = Constants.CMDPREFIXE;
	public static Map<String, QuizBot> channelByQuizBot = new HashMap<>();
	public static Map<String, QuizBot> channelByLastQuizBot = new HashMap<>();
	public static Set<BotCommand> commands = new HashSet<>();
	public static List<QuizBot> listOfGames = new ArrayList<>();
	public static Set<User> allUsers = new HashSet<>();
	public static Map<String, QuestionList> toBeDeleted = new HashMap<>();
	public static Map<String, Message> deletionMessages = new HashMap<>();
	public static Map<String, Set<String>> explicationRequestByChannel = new HashMap<>();

	public static String getPrefixe() { return cmdPrefixe;}
	public static List<User> getAllUsers() { return new ArrayList<>(allUsers);}
	public static void addUser(User u) { allUsers.add(u);}
	public static List<QuizBot> getListOfQuizBots() {
		return listOfGames;
	}
	public static boolean isShutingDown(){
		return BotCore.SHUTINGDOWN;
	}
	public static void addQuizBot(QuizBot q) {
		String currChannel = q.getChannel().getId();
		QuizBot oldQuiz = channelByQuizBot.put(currChannel, q);//channelByQuizBot.getOrDefault(currChannel, null);
		if (null != oldQuiz){
			oldQuiz.end();
			channelByLastQuizBot.put(currChannel, oldQuiz);
			channelByQuizBot.remove(currChannel);
			listOfGames.remove(oldQuiz);
		}
		listOfGames.add(q);
		channelByQuizBot.get(currChannel).start();
	}

	public static void explicationRequest(User sender, MessageChannel channel, String messageId){
		Set<String> msgIds = BotCore.explicationRequestByChannel.get(channel.getId());
		if (msgIds != null) {
			if (msgIds.contains(messageId)){
				BotCommand.getCommandByName(ExplainCommand.CMDNAME)
				.execute(sender, null, channel, new String[0]);
			}
		}
	}
	public static void comfirmDeletion(Message message, QuestionList l) {
		//System.out.println("  $> list="+l);
		String messageId = message.getId();
		toBeDeleted.put(messageId, l);
		deletionMessages.put(messageId, message);
	}
	public static QuizBot getPrevQuizBot(String channelId){ 
		return channelByLastQuizBot.get(channelId);
	}
	public static QuizBot getPrevQuizBot(MessageChannel channel){ 
		return BotCore.getPrevQuizBot(channel.getId());
	}
	public static QuizBot getCurrQuizBot(MessageChannel channel) {
		return BotCore.getCurrQuizBot(channel.getId());
	}
	public static QuizBot getCurrQuizBot(String channelId) {
		return channelByQuizBot.getOrDefault(channelId, null);
	}

	public static void addQuiz(QuizBot q) {
		addQuizBot(q);
	}
	public static void endQuizBot (QuizBot q) {
		String currChannelId = q.getChannel().getId();
		channelByLastQuizBot.put(currChannelId, q);
		channelByQuizBot.remove(currChannelId);
		listOfGames.remove(q);
		q.end();
	}
	public static void updateUserScoreAddReaction(User user, QuizBot currQuizBot, Emoji reaction) {
		Question currQuestion = currQuizBot.getCurrQuestion();

		if (!currQuizBot.userAnswers.containsKey(user)) {
			currQuizBot.userAnswers.put(user, new HashSet<>());
		}
		Option userAwnser = null;
		// Record user's answer (reaction)
		for (int i = 1; i<=currQuestion.size(); i++) {
			if (reaction.equals(currQuizBot.getReactionForAnswer(i))) {
				userAwnser = currQuestion.get(i-1);
				if (userAwnser != null) {
					//System.out.printf("  $> awnser %s, %s ;\n", userAwnser.isCorrect(), userAwnser.getText());
					currQuizBot.userAnswers.get(user).add(userAwnser);
				}
				break;
			}
		}
		Map<User, Set<Option>> tmpUserAnswers = new HashMap<>(currQuizBot.userAnswers);
		currQuizBot.awnsersByUserByQuestion.put(currQuestion, tmpUserAnswers);
		// If it's the correct answer, increase their score
		if (userAwnser != null) {
			double point = (userAwnser.isCorrect()?currQuizBot.pointsForCorrect:currQuizBot.pointsForIncorrect)/currQuestion.getTrueOptions().size();
			currQuizBot.userScoreApproxi.put(user, currQuizBot.getUserScore(user) +point);
		}
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
		toBeDeleted.remove(messageId);
		deletionMessages.remove(messageId);
	}
	public void setPrefixe(String prefixe) { cmdPrefixe = prefixe;}
	
}
