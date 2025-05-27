package com.linked.quizbot.commands.list;

import java.util.concurrent.TimeUnit;

import com.linked.quizbot.Constants;
import com.linked.quizbot.commands.BotCommand;
import com.linked.quizbot.commands.CommandCategory;
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
    private String cmdDesrciption = "if a quiz is ongoing, it adds a few more secs on the awnser time.";
	private String[] abbrevs = new String[]{"mt"};
    
	@Override
	public String[] getAbbreviations(){ return abbrevs;}
	@Override
	public CommandCategory getCategory(){
		return CommandCategory.GAME;
	}
	@Override
    public String getName(){ return CMDNAME;}
	@Override
    public String getDescription(){ return cmdDesrciption;}
	@Override
    public void execute(User sender, Message message, MessageChannel channel, String[] args){
        String channelId = channel.getId();
        QuizBot q = BotCore.getCurrQuizBot(channelId);
        int n = args.length;
        if (q == null){
            BotCommand.getCommandByName(HelpCommand.CMDNAME).execute(sender, message, channel, new String[]{getName()});
            return;
        }
        if(n>0) {
            int sec = Integer.parseInt(args[0]);
            q.setDelay(sec);
        }
        Message msg = q.getQuizMessage();
        q.currQuestion();
        try {
            msg.delete().queueAfter(3, TimeUnit.SECONDS);
        } catch(Exception e) {
            e.printStackTrace();
        }
    }
}
