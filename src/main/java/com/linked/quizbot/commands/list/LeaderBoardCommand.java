package com.linked.quizbot.commands.list;

import java.util.ArrayList;
import java.util.List;
import java.util.HashSet;
import java.util.concurrent.TimeUnit;

import com.linked.quizbot.Constants;
import com.linked.quizbot.commands.BotCommand;
import com.linked.quizbot.commands.CommandCategory;
import com.linked.quizbot.core.BotCore;
import com.linked.quizbot.core.QuizBot;

import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.channel.middleman.MessageChannel;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import net.dv8tion.jda.api.requests.restaction.MessageCreateAction;

public class LeaderBoardCommand extends BotCommand {
    public static final String cmdName = "leaderboard";
    private String cmdDesrciption = "displays the leaderboard of the last played game.";
	private String[] abbrevs = new String[]{"lb"};
    
	@Override
	public String[] getAbbreviations(){ return abbrevs;}
	@Override
	public CommandCategory getCategory(){
        return CommandCategory.GAME;
	}
    @Override
    public String getName(){ return cmdName;}
    @Override
    public String getDescription(){ return cmdDesrciption;}
    @Override
    public void execute(User sender, Message message, MessageChannel channel, String[] args){
        String channelId = channel.getId();
        QuizBot q = BotCore.getCurrQuizBot(channelId);
        if (q == null){
            q = BotCore.getPrevQuizBot(channelId);
        }
        BotCore.explicationRequestByChannel.putIfAbsent(channelId, new HashSet<>());
        if(q!=null) {
            List<String> lb = q.leaderBoard();
            MessageCreateAction send;
            for (String s : lb){
                send = channel.sendMessage(s);
                if (message != null){send.setMessageReference(message);}
                send.queue(msg -> {
                    BotCore.explicationRequestByChannel.get(channelId).add(msg.getId());
                    msg.addReaction(Constants.EMOJIEXPLICATION).queue();
                    msg.delete().queueAfter(Constants.READTIMEMIN, TimeUnit.MINUTES, tmp -> {
                        BotCore.explicationRequestByChannel.get(channelId).remove(msg.getId());
                    });
                });
            }
        } else {
            BotCommand.getCommandByName(HelpCommand.cmdName).execute(sender, message, channel, new String[]{getName()});
        }
    }
}
