package com.linked.quizbot.commands.list;

import java.util.List;

import com.linked.quizbot.commands.BotCommand;
import com.linked.quizbot.commands.BotCommand.CommandCategory;
import com.linked.quizbot.commands.CommandOutput;

import net.dv8tion.jda.api.entities.Message;

import com.linked.quizbot.core.BotCore;
import com.linked.quizbot.core.QuizBot;

import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.channel.middleman.MessageChannel;

public class EndCommand extends BotCommand {
    public static final String CMDNAME = "end";
    private String cmdDesrciption = "ending an already ongoing quiz.";

	@Override
	public BotCommand.CommandCategory getCategory(){
        return BotCommand.CommandCategory.GAME;
	}
    @Override
    public String getName(){ return CMDNAME;}
    @Override
    public String getDescription(){ return cmdDesrciption;}
    @Override
    public CommandOutput execute(String userId, String channelId, List<String> args, boolean reply){
        QuizBot q = BotCore.getCurrQuizBot(channelId);
        if (q == null) {
            return BotCommand.getCommandByName(HelpCommand.CMDNAME).execute(userId,  channelId, List.of(getName()), reply);
        }
        BotCore.endQuizBot(q);
        return BotCommand.getCommandByName(LeaderBoardCommand.CMDNAME).execute(userId, channelId, List.of(), reply);
    }
}
