package com.linked.quizbot.commands.list;

import java.util.List;
import java.util.AbstractMap;
import java.util.ArrayList;

import com.linked.quizbot.commands.BotCommand;
import com.linked.quizbot.commands.CommandOutput;
import com.linked.quizbot.utils.QuestionList;
import com.linked.quizbot.utils.User;
import com.linked.quizbot.utils.Users;

import net.dv8tion.jda.api.interactions.commands.build.OptionData;

/**
 * The {@code TagsCommand} class is a command that lists all tags associated with the user's questions,
 * sorted in ascending order by number of taged QuestionLists.
 * 
 * Each tag is displayed with an associated txt visual(often an emoji) and the count of questions tagged with it.
 * <p>
 * This command is part of a Discord bot that manages quiz games and user interactions.
 * It allows users to label their quizzes with custum tags, making it easier to organize and retrieve them.
 * </p>
 * @author alinked0
 * @version 1.0
 * @since 2025-02-01
 * @see BotCommand
 * @see Users
 * @see QuestionList
 */
public class TagsCommand extends BotCommand {
	public static final String CMDNAME = "tags";
	private String cmdDesrciption = "listing all questions";
	private List<String> abbrevs = List.of("lt", "listtags");
	
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
		return tmp;
	}
	@Override
	public CommandOutput execute(String userId,  List<String> args){
		CommandOutput.Builder output = new CommandOutput.Builder();
		String response = "Tags:\n";
		User user = Users.get(userId);
		
		List<AbstractMap.SimpleEntry<String, Integer>> list = user.getQuestionListPerTags().entrySet().stream().map(m -> new AbstractMap.SimpleEntry<String, Integer>(m.getKey(), m.getValue().size())).sorted((e, f) -> f.getValue()-e.getValue()).toList();
		
		for (AbstractMap.SimpleEntry<String, Integer> m : list){
			response += String.format("`%2d` %s %s\n", m.getValue(), user.getEmojiFomTagName(m.getKey()), m.getKey());
		}
		return output.add(response).build();
	}
}
