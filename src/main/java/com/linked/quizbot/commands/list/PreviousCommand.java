package com.linked.quizbot.commands.list;

import java.util.concurrent.TimeUnit;

import com.linked.quizbot.commands.BotCommand;
import com.linked.quizbot.commands.CommandCategory;
import com.linked.quizbot.core.BotCore;
import com.linked.quizbot.core.QuizBot;

import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.channel.middleman.MessageChannel;

/**
 * The {@code PreviousCommand} class the previous question to get sent.
 *
 * @author alinked0
 * @version 1.0
 * @see BotCommand
 * @see QuizBot
 * @see BotCore
 */
public class PreviousCommand extends BotCommand {
    public static final String CMDNAME = "previous";
    private String cmdDesrciption = "triggering the previous question to get sent if a quiz is ongoing";
	private String[] abbrevs = new String[]{"prev"};

	public String[] getAbbreviations(){ return abbrevs;}
	@Override
	public CommandCategory getCategory(){
		return CommandCategory.NAVIGATION;
	}
	@Override
    public String getName(){ return CMDNAME;}
	@Override
    public String getDescription(){ return cmdDesrciption;}
	@Override
    public void execute(User sender, Message message, MessageChannel channel, String[] args){
        String channelId = channel.getId();
        QuizBot q = BotCore.getCurrQuizBot(channelId);
        if (q == null){
            BotCommand.getCommandByName(HelpCommand.CMDNAME).execute(sender, message, channel, new String[]{getName()});
        }else {
            Message msg = q.getQuizMessage();
            q.prevQuestion();
            try {
                msg.delete().queueAfter(3, TimeUnit.SECONDS);
            } catch(Exception e) {
                e.printStackTrace();
            }
        }
    }
}
