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
		String res,ownerId;
		QuestionList l;
		Consumer<Message> success;
		if (args.size()<getOptionData().size()){
			return BotCommand.getCommandByName(HelpCommand.CMDNAME).execute(userId, List.of(getName()));
		}
		l = getSelectedQuestionList(args.get(0));
		ownerId = l.getAuthorId(); 
		if (ownerId.equals(userId)){
			res = String.format("Are you sure you want to delete :\n**%s**?", l.getName());
			success = message ->{
				String messageId = message.getId();
				BotCore.toBeDeleted.put(messageId, l);
				BotCore.deletionMessages.put(messageId, message);
			};
		}else {
			res = String.format("You are not the owner of the list:\n**%s**?", l.getName());
			success = null;
		}
		return new CommandOutput.Builder()
				.addTextMessage(res)
				.addReaction(Constants.EMOJIDEL)
				.addPostSendAction(success)
				.build();
	}

}
