package com.linked.quizbot.commands.list;

import java.util.List;

import com.linked.quizbot.commands.BotCommand;
import com.linked.quizbot.commands.CommandOutput;
import com.linked.quizbot.core.BotCore;
import com.linked.quizbot.core.viewers.QuizBot;
import com.linked.quizbot.core.viewers.Viewer;

import net.dv8tion.jda.api.interactions.commands.build.OptionData;

/**
 * TODO
 * @author alinked0
 * @version 1.0
 * @since 2025-02-01
 * @see BotCommand
 */
public class CurrentCommand extends BotCommand {
	public static final String CMDNAME = "current";
	private String cmdDesrciption = "triggering a reload of an ongoing view";
	
	@Override
	public BotCommand.CommandCategory getCategory(){
		return BotCommand.CommandCategory.NAVIGATION;
	}
	@Override
	public String getName(){ return CMDNAME;}
	@Override
	public String getDescription(){ return cmdDesrciption;}
	@Override
	public List<OptionData> getOptionData(){
		return BotCommand.getCommandByName(PreviousCommand.CMDNAME).getOptionData();
	}
	@Override
	public CommandOutput execute(String userId,  List<String> args){
		if (args.size() < getRequiredOptionData().size()){
			return BotCommand.getCommandByName(HelpCommand.CMDNAME).execute(userId, List.of(getName()));
		}
		String messageId = args.get(0);
		Viewer q = BotCore.getViewer(messageId);
		if (q != null && q.isActive()){
			if (q instanceof QuizBot){
				((QuizBot)q).addPlayer(userId);
			}
			return q.current();
		}
		if (messageId.equals(CollectionCommand.messageIdByUserId.get(userId))){
			return CollectionCommand.current(userId, CollectionCommand.CMDNAME);
		}
		if (messageId.equals(HistoryCommand.messageIdByUserId.get(userId))){
			return HistoryCommand.current(userId, HistoryCommand.CMDNAME);
		}
		return BotCommand.getCommandByName(HelpCommand.CMDNAME).execute(userId, List.of(getName()));
	}
}
