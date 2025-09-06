package com.linked.quizbot.commands.list;

import java.util.List;

import java.util.ArrayList;
import java.util.function.Consumer;

import com.linked.quizbot.Constants;
import com.linked.quizbot.commands.BotCommand;
import com.linked.quizbot.core.BotCore;
import com.linked.quizbot.commands.CommandOutput;
import com.linked.quizbot.utils.QuestionList;
import com.linked.quizbot.utils.Users;

import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.emoji.Emoji;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import net.dv8tion.jda.api.requests.restaction.MessageCreateAction;

/**
 * The {@code DeleteListCommand} class allows users to delete a question list they own.
 * It extends {@link BotCommand} and provides functionality to confirm deletion of a specified list.
 * <p>
 * This command is part of a Discord bot that manages quiz questions and user interactions.
 * It prompts the user for confirmation before proceeding with the deletion.
 * </p>
 * @author alinked0
 * @version 1.0
 * @since 2025-02-01
 * @see BotCommand
 * @see Users
 */
public class DeleteListCommand extends BotCommand{
	public static final String CMDNAME = "deletelist";
	private String cmdDesrciption = "deleting a list of questions";
	private List<String> abbrevs = List.of("dl");
	
	@Override
	public List<String> getAbbreviations(){ return abbrevs;}
	@Override
	public BotCommand.CommandCategory getCategory(){
		return BotCommand.CommandCategory.EDITING;
	}
	@Override
	public String getName(){ return CMDNAME;}
	@Override
	public String getDescription(){ return cmdDesrciption;}
	@Override
	public List<OptionData> getOptionData(){
		List<OptionData> res = new ArrayList<OptionData>();
		res.add(BotCommand.getCommandByName(StartCommand.CMDNAME).getOptionData().get(0).setRequired(true));
		return res;
	}
	@Override
	public CommandOutput execute(String userId,  List<String> args){
		String ownerId;
		QuestionList l;
		if (args.size()<getOptionData().size()){
			return BotCommand.getCommandByName(HelpCommand.CMDNAME).execute(userId, List.of(getName()));
		}
		CommandOutput.Builder output = new CommandOutput.Builder();
		l = Users.getById(args.get(0));
		ownerId = l.getAuthorId(); 
		if (ownerId.equals(userId)){
			output.addTextMessage(String.format("Are you sure you want to delete :`%s`**%s**?",l.getId(), l.getName()))
				.addReaction(Emoji.fromFormatted(Constants.EMOJIDEL))
			.addPostSendAction(
				message ->{
				String messageId = message.getId();
				BotCore.toBeDeleted.put(messageId, l);
				BotCore.deletionMessages.put(messageId, message);
			});
		}else {
			output.addTextMessage(String.format("You do not own the list:`%s`**%s**",l.getId(), l.getName()));
		}
		return output.build();
	}

}
