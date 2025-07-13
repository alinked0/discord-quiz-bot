package com.linked.quizbot.commands.list;

import java.util.List;

import com.linked.quizbot.commands.BotCommand;
import com.linked.quizbot.commands.CommandOutput;
import com.linked.quizbot.core.BotCore;
import com.linked.quizbot.core.QuizBot;
import com.linked.quizbot.core.Viewer;

import net.dv8tion.jda.api.interactions.commands.build.OptionData;

/**
 * The {@code NextCommand} class triggers the next question to get sent, creating a new quiz question.
 *
 * @author alinked0
 * @version 1.0
 * @see BotCommand
 * @see QuizBot
 * @see BotCore
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
			return q.next();
		}
		return BotCommand.getCommandByName(HelpCommand.CMDNAME).execute(userId, List.of(getName()));
	}
}
