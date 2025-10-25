package com.linked.quizbot.commands.list;

import java.util.List;
import java.util.ArrayList;

import com.linked.quizbot.commands.BotCommand;
import com.linked.quizbot.commands.CommandOutput;
import com.linked.quizbot.utils.QuestionList;
import com.linked.quizbot.utils.User;
import com.linked.quizbot.utils.Users;

import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;

/**
 * The {@code RenameListCommand} class allows users to rename a question list they own.
 * It extends {@link BotCommand} and provides functionality to change the name of a specified list.
 * <p>
 * This command is part of a Discord bot that manages quiz questions and user interactions.
 * It requires the user to specify the list ID and the new name for the list.
 * </p>
 * @author alinked0
 * @version 1.0
 * @since 2025-02-01
 * @see BotCommand
 * @see Users
 */
public class RenameListCommand extends BotCommand{
	public static final String CMDNAME = "renamelist";
	private String cmdDesrciption = "renaming a question list";
	private List<String> abbrevs = List.of("mv", "rename");
	
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
		res.add(new OptionData(OptionType.STRING, "list-id", "the list-id of the question list you wish to rename", true));
		res.add(new OptionData(OptionType.STRING, "new-name", "the new name for the old question list", true));
		return res;
	}
	@Override
	public List<String> parseArguments(String cmndLineArgs){
		int k = 0;
		List<String> res = new ArrayList<>();
		String [] tmp = cmndLineArgs.split("\\s+");
		if(tmp.length < 2){ return res;}
		
		k = cmndLineArgs.indexOf(" ");
		if(k < 0){ return res;}
		String a = cmndLineArgs.substring(0, k).trim();
		String b = cmndLineArgs.substring(k).trim();
		res.add(a);
		res.add(b);
		return res;
	}
	@Override
	public CommandOutput execute(String userId,  List<String> args){
		if (args.size() < getRequiredOptionData().size()){
			return BotCommand.getCommandByName(HelpCommand.CMDNAME).execute(userId, List.of(getName()));
		}
		String id=args.get(0);
		String newName=args.get(1);
		User u = Users.get(userId);
		QuestionList l = u.getById(id);
		CommandOutput.Builder output = new CommandOutput.Builder();
		if (l==null){
			output.add(String.format("Could not find `%s` in your collection.", id));
		}else {
			String oldName= l.getName();
			if(u.renameList(l, newName)){
				output.add(String.format("`%s` **%s** was renamed to `%s` **%s** .", id, oldName, id, newName));
			}else {
				QuestionList k = u.getByName(newName);
				output.add(String.format("There is already a list of this name, `%s` **%s** .", k.getId(), k.getName()));
			}
		}
		return output.build();
	}

}
