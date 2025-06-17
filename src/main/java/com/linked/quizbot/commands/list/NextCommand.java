package com.linked.quizbot.commands.list;

import java.util.List;

import java.util.concurrent.TimeUnit;

import com.linked.quizbot.commands.BotCommand;
import com.linked.quizbot.commands.CommandCategory;
import com.linked.quizbot.core.BotCore;
import com.linked.quizbot.core.QuizBot;

import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.channel.middleman.MessageChannel;

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
	public CommandCategory getCategory(){
		return CommandCategory.NAVIGATION;
	}
    @Override
    public String getName(){ return CMDNAME;}
	@Override
    public String getDescription(){ return cmdDesrciption;}
	@Override
    public void execute(User sender, Message message, MessageChannel channel, List<String> args){
        String channelId = channel.getId();
        QuizBot q = BotCore.getCurrQuizBot(channelId);
        if (q == null){
            BotCommand.getCommandByName(HelpCommand.CMDNAME).execute(sender, message, channel, List.of(getName()));
        }else {
            Message msg = q.getQuizMessage();
            q.nextQuestion();
            try {
                msg.delete().queueAfter(3, TimeUnit.SECONDS);
            } catch(Exception e) {
                e.printStackTrace();
            }
        }
    }
}
