package com.linked.quizbot.commands.list;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;

import com.linked.quizbot.Constants;
import com.linked.quizbot.commands.BotCommand;
import com.linked.quizbot.commands.BotCommand.CommandCategory;
import com.linked.quizbot.core.BotCore;
import com.linked.quizbot.commands.CommandOutput;
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
public class TagsCommand extends BotCommand {
	public static final String CMDNAME = "tags";
    private String cmdDesrciption = "listing all questions";
	private List<String> abbrevs = List.of();
	
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
        
        List<AbstractMap.SimpleEntry<String, Integer>> list = user.getQuestionListPerTags().entrySet().stream().map(m -> new AbstractMap.SimpleEntry<String, Integer>(m.getKey(), m.getValue().size())).sorted((e, f) -> e.getValue()-f.getValue()).toList();
        
        for (AbstractMap.SimpleEntry<String, Integer> m : list){
            response += String.format("`%d` %s %s\n", m.getValue(), user.getEmojiFomTagName(m.getKey()), m.getKey());
        }
        return output.add(response).build();
    }
}
