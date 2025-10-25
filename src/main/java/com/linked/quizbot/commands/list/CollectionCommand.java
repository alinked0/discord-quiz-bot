package com.linked.quizbot.commands.list;

import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Stream;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Iterator;

import com.linked.quizbot.Constants;
import com.linked.quizbot.commands.BotCommand;
import com.linked.quizbot.core.BotCore;
import com.linked.quizbot.commands.CommandOutput;
import com.linked.quizbot.utils.Attempt;
import com.linked.quizbot.utils.CollectionManager;
import com.linked.quizbot.utils.QuestionList;
import com.linked.quizbot.utils.User;
import com.linked.quizbot.utils.Users;

import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;

/**
 * The {@code CollectionCommand} class retrieves and displays a user's collection of question lists,
 * categorized by tags. It extends {@link BotCommand} and allows users to view their own collections
 * or those of other users by specifying a user ID.
 * <p>
 * This command is part of a Discord bot that manages quiz questions and user interactions.
 * It provides functionality to list all question lists associated with a user, including an example list.
 * The command can be invoked with various abbreviations and includes options for specifying the user ID.
 * </p>
 * @author alinked0
 * @version 1.0
 * @since 2025-02-01
 * @see BotCommand
 * @see Users
 * @see QuestionList
 */
public class CollectionCommand extends BotCommand {
	public static final String CMDNAME = "collection";
	private String cmdDesrciption = "listing all questions";
	private List<String> abbrevs = List.of("c", "ls");
	
	@Override
	public String getName(){ return CMDNAME;}
	@Override
	public BotCommand.CommandCategory getCategory(){
		return BotCommand.CommandCategory.READING;
	}
	@Override
	public List<String> getAbbreviations(){ return abbrevs;}
	@Override
	public String getDescription(){ return cmdDesrciption;}
	@Override
	public String getDetailedExamples(){
		return 
		"""
		```py
		q!c 1234567 size>=6 tag=trivia name~=geo o=date asc
		q!c o=id a
		q!c o=name desc
		q!c tag=book
		q!c tag!=book
		q!c tag~=book
		q!c name=adolf
		q!c 1234567
		q!c size>=10
		```
		""";
	}
	@Override
	public List<OptionData> getOptionData(){
		List<OptionData> tmp = new ArrayList<>();
		tmp.add(new OptionData(OptionType.STRING, "user-id", "id of the user who's questions will be listed"));
		tmp.add(new OptionData(OptionType.STRING, "filtering", "add some options to filter the output"));
		return tmp;
	}
	@Override
	public List<String> parseArguments(String cmndLineArgs) {
		List<String> res = new ArrayList<>();
		String order = "";
		String[] tmp = cmndLineArgs.toLowerCase().trim().replaceAll("sort( by|[\\s:=<>!]+)\\s*", "o=").replaceAll("\\s*([:=<>!]+)\\s*", "$1").split("\\s+");
		String prev = "";
		
		for(int k = 0; k < tmp.length; ++k) {
			if (!tmp[k].isBlank()){
				if (tmp[k].matches(".*[!<>=:]+.*")) {
					if (!prev.isBlank()) {
						res.add(prev);
					}
					prev = tmp[k].trim();
				} else if (tmp[k].matches("a|asc|ascending")) {
					order = "asc";
				} else if (tmp[k].matches("d|desc|descending")) {
					order = "desc";
				} else {
					prev = (!prev.isBlank()?( prev+ " "):"") + tmp[k].trim();
				}
			}
		}
		
		if (!prev.isBlank()) {
			res.add(prev);
		}
		
		if (!order.isBlank()) {
			res.add(order);
		}
		
		return res;
	}
	@Override
	public CommandOutput execute(String userId,  List<String> args){
		String targetUserId = userId; String sortFieldToken = "";
		String sortDirection = "";
		List<String> filterTokens = new ArrayList<>();
		
		for (String arg : args) {
			if (arg.matches("o[=:]+.*")) {
				sortFieldToken = arg;
			} else if (arg.matches("asc|desc")) {
				sortDirection = arg;
			} else if (Users.get(arg) != null && targetUserId.equals(userId)) {
				targetUserId = arg;
			} else {
				filterTokens.add(arg);
			}
		}
		
		if (sortFieldToken.isBlank()){
			sortFieldToken="o=date";
			if (sortDirection.isBlank()){
				sortDirection="d";
			}
		}
		
		
		User user = Users.get(targetUserId);
		if (user == null) {user = Users.get(userId);}
		
		String tmp = String.format("Collection of <@%s>\n", targetUserId);
		Predicate<QuestionList> predicate = filterTokens.stream().map(a -> CollectionManager.parseFilter(a)).reduce(list->true, Predicate::and);
		List<QuestionList> collection = new ArrayList<>(user.getLists().values());
		collection.add(QuestionList.getExampleQuestionList());
		Stream<QuestionList> list = collection.stream().filter(predicate);
		
		if (!sortFieldToken.isBlank()) {
			Comparator<QuestionList> comparator = CollectionManager.parseComparator(sortFieldToken);
			if (sortDirection.matches("d|desc|descending")) {
				comparator = comparator.reversed();
			}			
			list = list.sorted(comparator);
		}		
		
		Iterator<QuestionList> iterLists = list.toList().iterator();
		List<String> res = new ArrayList<>();
		while(iterLists.hasNext()) {
			tmp = tmp + getTextFromQuestionList(user, iterLists.next());
			if (tmp.length() > 1600) {
				res.add(tmp);
				tmp = "";
			}
		}
		if (tmp.length() > 0) {res.add(tmp);}
		return new CommandOutput.Builder().addAll(res).build();
	}
	private String getTextFromQuestionList(User user, QuestionList l){
		String minEmoji = Constants.EMOJIBLACKSQUARE; // TODO allow the user to choose the default
		if (!l.tagNames().isEmpty()) {
			String tagName = (String)l.tagNames().iterator().next();
			int min = user.getListsByTag(tagName).size();
			minEmoji = l.getEmoji(tagName);
			Iterator<String> tagNames = l.tagNames().iterator();
			
			while(tagNames.hasNext()) {
				String emoji = (String)tagNames.next();
				int curr = user.getListsByTag(emoji).size();
				if (curr < min) {
					min = curr;
					minEmoji = l.getEmoji(emoji);
				}
			}
		}
		String score="";
		List<Attempt> tmp = user.getAttempts(l.getId()); 
		Attempt lastAttempt;
		if (tmp!=null && !tmp.isEmpty()) {
			lastAttempt = tmp.getFirst();
			score = lastAttempt.getTextPoints();
		}
		return String.format("`%s` %s `%2s` %s %s\n", l.getId(), minEmoji, l.size(), score, l.getName());
	}
}
