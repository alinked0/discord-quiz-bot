package com.linked.quizbot.commands.list;

import java.util.List;

import java.util.concurrent.TimeUnit;

import com.linked.quizbot.Constants;
import com.linked.quizbot.commands.BotCommand;
import com.linked.quizbot.commands.BotCommand.CommandCategory;
import com.linked.quizbot.commands.CommandOutput;
import com.linked.quizbot.core.BotCore;
import com.linked.quizbot.core.QuizBot;
import com.linked.quizbot.core.Viewer;

import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.channel.middleman.MessageChannel;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;

/**
 * The {@code MoreTimeCommand} class adds a few more secs on the awnser time.
 *
 * @author alinked0
 * @version 1.0
 * @see BotCommand
 * @see QuizBot
 * @see BotCore
 */
public class MoreTimeCommand extends BotCommand {
	public static final String CMDNAME = "moretime";
	private String cmdDesrciption = "changing the time given to awnser a question.";
	private List<String> abbrevs = List.of("mt");
	
	@Override
	public List<String> getAbbreviations(){ return abbrevs;}
	@Override
	public BotCommand.CommandCategory getCategory(){
		return BotCommand.CommandCategory.GAME;
	}
	@Override
	public String getName(){ return CMDNAME;}
	@Override
	public String getDescription(){ return cmdDesrciption;}
	@Override
	public String getDetailedExamples(){
		return "`"+Constants.CMDPREFIXE+getName()+" 30` sending the current question with a time of 30s.\n`"+Constants.CMDPREFIXE+getName()+" ` re-sending the current question with the previous time.";
	}
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
		QuizBot q = (QuizBot) BotCore.getViewer(messageId);
		int n = args.size();
		if (q == null){
			return BotCommand.getCommandByName(HelpCommand.CMDNAME).execute(userId, List.of(getName()));
		}
		if(n>1) {
			int sec = Integer.parseInt(args.get(1));
			q.setDelay(sec);
		}
		return q.current();
	}
}
