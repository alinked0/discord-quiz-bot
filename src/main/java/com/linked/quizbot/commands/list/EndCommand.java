package com.linked.quizbot.commands.list;

import java.util.List;

import com.linked.quizbot.commands.BotCommand;
import com.linked.quizbot.commands.CommandCategory;
import net.dv8tion.jda.api.entities.Message;

import com.linked.quizbot.core.BotCore;
import com.linked.quizbot.core.QuizBot;

import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.channel.middleman.MessageChannel;

public class EndCommand extends BotCommand {
    public static final String CMDNAME = "end";
    private String cmdDesrciption = "ending an already ongoing quiz.";

	@Override
	public CommandCategory getCategory(){
        return CommandCategory.GAME;
	}
    @Override
    public String getName(){ return CMDNAME;}
    @Override
    public String getDescription(){ return cmdDesrciption;}
    @Override
    public void execute(User sender, Message message, MessageChannel channel, List<String> args){
        QuizBot q = BotCore.getCurrQuizBot(channel);
        if (q == null) {
            BotCommand.getCommandByName(HelpCommand.CMDNAME)
            .execute(sender, message, channel, List.of(getName()));
        } else{
            BotCore.endQuizBot(q);
            BotCommand.getCommandByName(LeaderBoardCommand.CMDNAME)
            .execute(sender, message, channel, List.of());
        }
    }
}
