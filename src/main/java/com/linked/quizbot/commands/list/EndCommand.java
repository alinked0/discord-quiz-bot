package com.linked.quizbot.commands.list;

import java.util.List;

import com.linked.quizbot.commands.BotCommand;
import com.linked.quizbot.commands.BotCommand.CommandCategory;
import com.linked.quizbot.commands.CommandOutput;

import net.dv8tion.jda.api.entities.Message;

import com.linked.quizbot.core.BotCore;
import com.linked.quizbot.core.QuizBot;
import com.linked.quizbot.core.Viewer;

import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.channel.middleman.MessageChannel;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;

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
    public List<OptionData> getOptionData(){
        return BotCommand.getCommandByName(PreviousCommand.CMDNAME).getOptionData();
    }
    @Override
    public CommandOutput execute(String userId,  List<String> args){
		if (args.size() < getRequiredOptionData().size()){
			return BotCommand.getCommandByName(HelpCommand.CMDNAME).execute(userId, List.of(getName()));
		}
        String messageId = args.get(0);
        Viewer q = BotCore.getCurrViewer(messageId);
        if (q == null) {
            return BotCommand.getCommandByName(HelpCommand.CMDNAME).execute(userId, List.of(getName()));
        }
        q.end();
        CommandOutput output = BotCommand.getCommandByName(LeaderBoardCommand.CMDNAME).execute(userId, args);
        BotCore.endViewer(q);
        return output;
    }
}
