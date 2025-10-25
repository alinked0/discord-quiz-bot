package com.linked.quizbot.commands.list;

import java.util.List;
import java.util.ArrayList;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.linked.quizbot.Constants;
import com.linked.quizbot.commands.BotCommand;
import com.linked.quizbot.commands.CommandOutput;
import com.linked.quizbot.core.BotCore;
import com.linked.quizbot.utils.Attempt;
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
		res.add(new OptionData(OptionType.STRING, "listid", "listid given by "+CollectionCommand.CMDNAME, true)
		.setRequiredLength(QuestionList.Hasher.DEFAULT_LENGTH, QuestionList.Hasher.DEFAULT_LENGTH));
		return res;
	}
	
	@Override
	public CommandOutput execute(String userId,  List<String> args){
		User user = Users.get(userId);
		QuestionList list = args.size()>0?user.getById(args.get(0)): null;
		if (list==null){
			return BotCommand.getCommandByName(HelpCommand.CMDNAME).execute(userId, List.of(getName()));
		}
		
		if (user == null) {user = Users.addUser(userId);}
		
		String tmp = String.format("%sHistory\n", list.header());
		
		List<Attempt> lastscores = user.getAttempts(list.getId());
		
		List<String> res = new ArrayList<>();
		for(Attempt att : lastscores) {
			tmp = tmp + att.getTextPoints() + "\n";
			if (tmp.length() > 1600) {
				res.add(tmp);
				tmp = "";
			}
		}
		if (!tmp.isBlank()) {res.add(tmp);}
		return new CommandOutput.Builder().addAll(res).build();
	}
}
