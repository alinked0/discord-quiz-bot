package com.linked.quizbot.commands.list;

import java.util.List;

import java.util.concurrent.TimeUnit;

import com.linked.quizbot.Constants;
import com.linked.quizbot.commands.BotCommand;
import com.linked.quizbot.commands.BotCommand.CommandCategory;
import com.linked.quizbot.commands.CommandOutput;
import com.linked.quizbot.core.BotCore;
import com.linked.quizbot.core.QuizBot;

import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.channel.middleman.MessageChannel;

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
	public CommandOutput execute(String userId, String channelId, List<String> args, boolean reply){
		QuizBot q = BotCore.getCurrQuizBot(channelId);
		int n = args.size();
		if (q == null){
			return BotCommand.getCommandByName(HelpCommand.CMDNAME).execute(userId, channelId, List.of(getName()), reply);
		}
		if(n>0) {
			int sec = Integer.parseInt(args.get(0));
			q.setDelay(sec);
		}
		Message msg = q.getQuizMessage();
		try {
			msg.delete().queueAfter(3, TimeUnit.SECONDS);
		} catch(Exception e) {
			e.printStackTrace();
		}
		return q.currQuestion();
	}
}
