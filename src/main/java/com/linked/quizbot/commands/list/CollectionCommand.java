package com.linked.quizbot.commands.list;

import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.function.Predicate;
import java.util.stream.Stream;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
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

import net.dv8tion.jda.api.entities.emoji.Emoji;
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
	public static final Map<String, Map<Integer, List<List<QuestionList>>>> listsByLastIndexByUserId = new HashMap<>();
	public static final Map<String,String> messageIdByUserId = new HashMap<>();
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
		q!c 1234567 size>=6 tag=trivia name=geo o=date asc
		q!c o=id a
		q!c o=name desc
		q!c o=score desc
		q!c o=start desc
		q!c tag=book
		q!c tag!=book
		q!c tag=.*book.*
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
		List<String> args = new ArrayList<>();
		String[] tmp = cmndLineArgs.toLowerCase().trim().replaceAll("sort( by|[\\s:=<>!]+)\\s*", "o=").replaceAll("\\s*([:]+)\\s*", "=").replaceAll("\\s*([=<>!]+)\\s*", "$1").split("\\s+");
		String prev = "", targetUserId = "", sortFieldToken = "", sortDirection = "";
		List<String> filterTokens = new ArrayList<>();
		
		for(int k = 0; k < tmp.length; ++k) {
			if (!tmp[k].isBlank()){
				if (tmp[k].matches(".*[!<>=]+.*")) {
					if (!prev.isBlank()) {
						args.add(prev);
					}
					prev = tmp[k].trim();
				} else if (tmp[k].matches("a|asc|ascending")) {
					sortDirection = "asc";
				} else if (tmp[k].matches("d|desc|descending")) {
					sortDirection = "desc";
				} else {
					prev = (!prev.isBlank()?( prev+ " "):"") + tmp[k].trim();
				}
			}
		}
		if (!prev.isBlank()) {args.add(prev);}		
		for (String arg : args) {
			if (arg.toLowerCase().matches("o[=:]+.*")) {
				sortFieldToken = arg;
			} else if (Users.get(arg) != null && targetUserId.isBlank()) {
				targetUserId = arg;
			} else {
				filterTokens.add(arg);
			}
		}
		if (sortDirection.isBlank()){sortDirection="desc";}
		if (sortFieldToken.isBlank()){
			sortFieldToken="o=date";
		}
		args.clear(); args.add(targetUserId); args.add(sortFieldToken); args.add(sortDirection); args.addAll(filterTokens);
		return args;
	}
	@Override
	public CommandOutput execute(String userId,  List<String> args){
		User user;
		Map<Integer, List<List<QuestionList>>> listsByLastIndex;
		List<String> filterTokens;
		String targetUserId, sortFieldToken, sortDirection;
		Iterator<QuestionList> iterList;
		List<QuestionList> collection;
		List<QuestionList> tmp;
		List<List<QuestionList>> listsPerPage;
		Comparator<QuestionList> comparator;
		int i, numberPerPage=10;

		targetUserId = args.get(0).isBlank()?userId:args.get(0); sortFieldToken = args.get(1); sortDirection = args.get(2);
		if (args.size()>3) filterTokens = args.subList(3, args.size()-1);
		else filterTokens = new ArrayList<>();
		user = Users.get(targetUserId);
		comparator = CollectionManager.parseComparator(user, sortFieldToken);

		collection = new ArrayList<>(user.getLists().values()); collection.add(QuestionList.getExampleQuestionList());
		collection =  collection.stream().filter( 
			filterTokens.stream().map(
				token -> CollectionManager.parseFilter(user, token)
			).reduce(list->true, Predicate::and)
		).sorted(
			sortDirection.equals("desc")?comparator.reversed():comparator
		).toList();
		
		listsPerPage = new ArrayList<>();
		iterList = collection.iterator();
		while(iterList.hasNext()) {
			i=0; tmp = new ArrayList<>();
			while(iterList.hasNext() && ++i<=numberPerPage) {
				tmp.add(iterList.next());
			}
			listsPerPage.add(tmp);
		}

		listsByLastIndex = new HashMap<>();
		listsByLastIndex.put(-1, listsPerPage);
		listsByLastIndexByUserId.put(userId, listsByLastIndex);

		return next(userId);
	}

	public static CommandOutput next(String userId){
		return get(userId, 1);
	}

	public static CommandOutput previous(String userId){
		return get(userId, -1);
	}
	private static CommandOutput get(String userId, int incr){
		User user;
		String outText;
		List<List<QuestionList>> listsPerPage;
		Map<Integer, List<List<QuestionList>>> listsByLastIndex;
		List<String> outList;
		List<Emoji> emojis;
		int lastIndex;

		user = Users.get(userId);
		listsByLastIndex = listsByLastIndexByUserId.get(userId);
		lastIndex = listsByLastIndex.keySet().iterator().next() + incr;
		listsPerPage = listsByLastIndex.values().iterator().next();

		outList = new ArrayList<>();
		outText = String.format("Collection of <@%s>\n", user.getId());
		if (listsPerPage.size()>lastIndex){
			for (QuestionList list : listsPerPage.get(lastIndex)) {
				outText = outText + getTextFromQuestionList(user, list);
				if (outText.length() > Constants.CHARSENDLIM-400) {
					outList.add(outText);
					outText = "";
				}
			}
		}
		outText += String.format("Page `%d` out of `%d`",lastIndex+1, listsPerPage.size()==0?1:listsPerPage.size());
		outList.add(outText);

		listsByLastIndexByUserId.get(userId).clear();

		listsByLastIndex.put(lastIndex, listsPerPage);
		listsByLastIndexByUserId.put(userId, listsByLastIndex);
		
		emojis = new ArrayList<>();
		if (lastIndex-1>=0) emojis.add(Emoji.fromFormatted(Constants.EMOJIPREVQUESTION));
		if (listsPerPage.size()>lastIndex+1) emojis.add(Emoji.fromFormatted(Constants.EMOJINEXTQUESTION));
		return new CommandOutput.Builder().addAll(outList).addReactions(emojis).addPostSendAction(m -> CollectionCommand.messageIdByUserId.put(userId, m.getId())).build();
	}
	private static String getTextFromQuestionList(User user, QuestionList l){
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
