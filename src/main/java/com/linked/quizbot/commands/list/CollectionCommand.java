package com.linked.quizbot.commands.list;

import java.util.List;

import java.util.ArrayList;
import java.util.Iterator;

import com.linked.quizbot.Constants;
import com.linked.quizbot.commands.BotCommand;
import com.linked.quizbot.core.BotCore;
import com.linked.quizbot.commands.CommandOutput;
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
	public List<OptionData> getOptionData(){
		List<OptionData> tmp = new ArrayList<>();
		tmp.add(new OptionData(OptionType.STRING, "user-id", "id of the user who's questions will be listed"));
		return tmp;
	}
	@Override
	public List<String> parseArguments(String cmndLineArgs) {
		List<String> res = new ArrayList<>();
		String order = "";
		String[] tmp = cmndLineArgs.toLowerCase().trim().replaceAll("sort( by|[\\s:=<>!]+)\\s*", "o=").replaceAll("\\s*([:=<>!]+)\\s*", "$1").split("\\s+");
		String prev = "";

		for(int k = 0; k < tmp.length; ++k) {
			if (tmp[k].matches(".*[!<>=:]+.*")) {
			if (!prev.isEmpty()) {
				res.add(prev);
			}
			prev = tmp[k];
			} else if (tmp[k].matches("a|asc|ascending")) {
			order = "asc";
			} else if (tmp[k].matches("d|desc|descending")) {
			order = "desc";
			} else {
			prev = prev + " " + tmp[k];
			}
		}

		if (!prev.isEmpty()) {
			res.add(prev);
		}
		
		if (!order.isEmpty()) {
			res.add(order);
		}

		return res;
	}
	@Override
	public CommandOutput execute(String userId,  List<String> args){
		User user = null;
		String minEmoji;
		if (this.getOptionData().size() <= args.size()) {
			minEmoji = BotCommand.getIdFromArg((String)args.get(0), BotCore.getJDA());
			if (minEmoji == null) {return BotCommand.getCommandByName("help").execute(userId, List.of(this.getName()));}
			user = Users.get(minEmoji);
			if (user == null) {return BotCommand.getCommandByName("help").execute(userId, List.of(this.getName()));}
			userId = minEmoji;
		}

		List<String> res = new ArrayList<>();
		if (user == null) {
			user = Users.get(userId);
		}

		String tmp = String.format("Collection of <@%s>\n", userId);
		List<QuestionList> list = user.getLists();
		list.sort(QuestionList.comparatorByDate().reversed());
		list.add(QuestionList.getExampleQuestionList());
		Iterator<QuestionList> lists = list.iterator();

		while(lists.hasNext()) {
			QuestionList l = (QuestionList)lists.next();
			minEmoji = "";
			if (!l.getTagNames().isEmpty()) {
			String tagName = (String)l.getTagNames().iterator().next();
			int min = user.getListsByTag(tagName).size();
			minEmoji = l.getEmoji(tagName);
			Iterator<String> tagNames = l.getTagNames().iterator();

			while(tagNames.hasNext()) {
				String emoji = (String)tagNames.next();
				int curr = user.getListsByTag(emoji).size();
				if (curr < min) {
					min = curr;
					minEmoji = l.getEmoji(emoji);
				}
			}
			}

			tmp = tmp + String.format("`%s` %s %s\n", l.getId(), minEmoji, l.getName());
			if (tmp.length() > 1600) {
			res.add(tmp);
			tmp = "";
			}
		}
		if (tmp.length() > 0) {res.add(tmp);}
		return (new CommandOutput.Builder()).addAll(res).build();
	}
}
