package com.linked.quizbot.commands.list;

import java.util.List;
import java.util.ArrayList;

import com.linked.quizbot.commands.BotCommand;
import com.linked.quizbot.commands.Output;
import com.linked.quizbot.commands.CommandOutput;
import com.linked.quizbot.utils.Users;

import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;

/**
 * The {@code CreateTagCommand} class allows users to create a tag that can be used to sort question lists.
 * It extends {@link BotCommand} and provides functionality to create a new tag with an associated emoji.
 * <p>
 * This command is part of a Discord bot that manages quiz questions and user interactions.
 * It provides options for specifying the tag name and an emoji for visual representation.
 * </p>
 * @author alinked0
 * @version 1.0
 * @since 2025-02-01
 * @see BotCommand
 * @see Users
 */
public class CreateTagCommand extends BotCommand{
	public static final String CMDNAME = "createtag";
	private String cmdDesrciption = "creating a tag that can be use to sort question lists";
	private List<String> abbrevs = List.of("ct");
	
	@Override
	public List<String> getAbbreviations(){ return abbrevs;}
	@Override
	public BotCommand.CommandCategory getCategory(){ return BotCommand.CommandCategory.EDITING;}
	@Override
	public String getName(){ return CMDNAME;}
	@Override
	public String getDescription(){ return cmdDesrciption;}
	@Override
	public List<OptionData> getOptionData(){
		List<OptionData> res = new ArrayList<>();
		res.add(new OptionData(OptionType.STRING, "tag-name", "the name associated with your tag", true));
		res.add(new OptionData(OptionType.STRING, "shorthand", "a visual aide", true));
		return res;
	}
	@Override
	public CommandOutput execute(String userId,  List<String> args){
		if (args.size() < getRequiredOptionData().size()){
			return BotCommand.getCommandByName(HelpCommand.CMDNAME).execute(userId, List.of(getName()));
		}
		String tagNameInput=args.get(0), tagEmojiInput=args.get(1);
		String s;
		
		
		if (Users.createTag(userId, tagNameInput, tagEmojiInput)){
			s = "Tag has been created.";
		} else {
			s = "Tag already exists.";
		}
		
		CommandOutput out;
		out = new CommandOutput(List.of(new Output.Builder()
				.add(s)
				.build()));
		return  out;
	}

}
