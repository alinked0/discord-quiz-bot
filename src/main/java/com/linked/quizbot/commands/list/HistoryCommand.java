package com.linked.quizbot.commands.list;

import java.util.List;
import java.util.Map;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

import com.linked.quizbot.Constants;
import com.linked.quizbot.commands.BotCommand;
import com.linked.quizbot.commands.CommandOutput;
import com.linked.quizbot.utils.Attempt;
import com.linked.quizbot.utils.Displayable;
import com.linked.quizbot.utils.QuestionList;
import com.linked.quizbot.utils.User;
import com.linked.quizbot.utils.Users;

import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;

/**
 * The {@code HistoryCommand} class retrieves and displays the last games played on a specified list of list,
 * TODO
 * <p>
 * This command is part of a Discord bot that manages quiz list and user interactions.
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
public class HistoryCommand extends BotCommand {
	public static final String CMDNAME = "history";
	private String cmdDesrciption = "listing all list";
	private List<String> abbrevs = List.of("hi");
	public static final Map<String,String> messageIdByUserId = new HashMap<>();
	public static final Map<String, Map<Integer, List<List<Attempt>>>> listsByLastIndexByUserId = new HashMap<>();
	
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
		q!hi abcdefg
		```
		""";
	}
	@Override
	public List<OptionData> getOptionData(){
		List<OptionData> res = new ArrayList<>();
		res.add(new OptionData(OptionType.STRING, "listid", "listid given by "+CollectionCommand.CMDNAME, false)
		.setRequiredLength(QuestionList.Hasher.DEFAULT_LENGTH, QuestionList.Hasher.DEFAULT_LENGTH));
		return res;
	}
	
	@Override
	public CommandOutput execute(String userId,  List<String> args){	
		User user = Users.get(userId);
		List<Attempt> lists;
		QuestionList list;
		
		if (args.size()>0){
			list = args.size()>0?user.getById(args.get(0)): null;
			if (list==null){
				return BotCommand.getCommandByName(HelpCommand.CMDNAME).execute(userId, List.of(getName()));
			}
			lists =  user.getAttempts(list.getId());
		} else {
			lists = user.getAttemptsByListId().values().stream()
				.flatMap(l->l.stream()).sorted(Attempt.comparatorStart().reversed())
				.toList();
		}
		
		return CollectionCommand.execute(userId, args, lists, listsByLastIndexByUserId, messageIdByUserId,  displayableAttempt(), getName());
	}
	
	public static CommandOutput next(String userId, String commandName){
		return CollectionCommand.get(userId, 1, listsByLastIndexByUserId, displayableAttempt(), commandName);
	}
	
	public static CommandOutput previous(String userId, String commandName){
		return CollectionCommand.get(userId, -1, listsByLastIndexByUserId, displayableAttempt(), commandName);
	}
	
	public static CommandOutput current(String userId, String commandName){
		return CollectionCommand.get(userId, 0, listsByLastIndexByUserId, displayableAttempt(), commandName);
	}
	
	public static Displayable<Attempt> displayableAttempt(){
		Displayable<Attempt> res = (att) -> {
			User user;
			String minEmoji, tagName, emoji;
			Iterator<String> tagNames;
			QuestionList l;
			
			l = att.getQuestionList();
			user= Users.get(att.getUserId());
			minEmoji = Constants.EMOJIBLACKSQUARE; // TODO allow the user to choose the default
			if (!l.tagNames().isEmpty()) {
				tagName = (String)l.tagNames().iterator().next();
				int min = user.getListsByTag(tagName).size();
				minEmoji = l.getEmoji(tagName);
				tagNames = l.tagNames().iterator();
				
				while(tagNames.hasNext()) {
					emoji = (String)tagNames.next();
					int curr = user.getListsByTag(emoji).size();
					if (curr < min) {
						min = curr;
						minEmoji = l.getEmoji(emoji);
					}
				}
			}
			return String.format("`%s` %s `%2s` %s %s\n", l.getId(), minEmoji, l.size(), att.getTextPoints(), l.getName());
		};
		return res;
	}
}
