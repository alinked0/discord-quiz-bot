package com.linked.quizbot.commands.list;

import java.util.List;

import com.linked.quizbot.commands.BotCommand;
import com.linked.quizbot.commands.CommandOutput;
import com.linked.quizbot.core.BotCore;
import com.linked.quizbot.core.viewers.QuizBot;
import com.linked.quizbot.core.viewers.Viewer;

import net.dv8tion.jda.api.interactions.commands.build.OptionData;

/**
 * The {@code NextCommand} class provides functionality to trigger the next question in an ongoing quiz.
 * It extends {@link BotCommand} and is part of a Discord bot that manages quiz games.
 * <p>
 * This command allows users to advance to the next question in a quiz, which is useful for keeping the game
 * moving smoothly without waiting for all players to respond.
 * </p>
 * @author alinked0
 * @version 1.0
 * @since 2025-02-01
 * @see BotCommand
 */
public class NextCommand extends BotCommand {
	public static final String CMDNAME = "next";
	private String cmdDesrciption = "triggering the next question to get sent if a quiz is ongoing";
	
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
		Viewer q = BotCore.getViewer(args.get(0));
        if (q != null && q.isActive()){
			if (q instanceof QuizBot){
				((QuizBot)q).addPlayer(userId);
			}
			return q.next();
		}
		return BotCommand.getCommandByName(HelpCommand.CMDNAME).execute(userId, List.of(getName()));
	}
}
