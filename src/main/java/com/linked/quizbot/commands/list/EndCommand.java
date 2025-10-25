package com.linked.quizbot.commands.list;

import java.util.List;

import com.linked.quizbot.commands.BotCommand;
import com.linked.quizbot.commands.CommandOutput;

import com.linked.quizbot.core.BotCore;
import com.linked.quizbot.core.viewers.QuizBot;
import com.linked.quizbot.core.viewers.Viewer;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;

/**
 * The {@code EndCommand} class provides functionality to end an ongoing {@link Viewer}.
 * It extends {@link BotCommand} and is part of a Discord bot that manages quiz games.
 * <p>
 * This command allows users to terminate a currently active quiz, which can be useful
 * if the game needs to be stopped prematurely for any reason.
 * </p>
 * @author alinked0
 * @version 1.0
 * @since 2025-02-01
 * @see BotCommand
 */
public class EndCommand extends BotCommand {
	public static final String CMDNAME = "end";
	private String cmdDesrciption = "ending an already ongoing viewer, quiz, or explanation.";
	
	@Override
	public BotCommand.CommandCategory getCategory(){
		return BotCommand.CommandCategory.GAME;
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
		if (q == null) {
			return BotCommand.getCommandByName(HelpCommand.CMDNAME).execute(userId, List.of(getName()));
		}
		CommandOutput output;
		if (q instanceof QuizBot){
			q.end();
			output = BotCommand.getCommandByName(LeaderBoardCommand.CMDNAME).execute(userId, args);
		} else {
			output = q.start();
		}
		return output;
	}
}
