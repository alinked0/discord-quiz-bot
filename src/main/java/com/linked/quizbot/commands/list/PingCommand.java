package com.linked.quizbot.commands.list;

import java.util.List;

import com.linked.quizbot.commands.BotCommand;

import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.requests.restaction.MessageCreateAction;
import net.dv8tion.jda.api.entities.channel.middleman.MessageChannel;

public class PingCommand extends BotCommand {
    public static final String CMDNAME = "ping";
    private String cmdDesrciption = "getting comfirmation that the bot is online.";
	private List<String> abbrevs = List.of("p");
    
	public List<String> getAbbreviations(){ return abbrevs;}
    public String getName(){ return CMDNAME;}
    public String getDescription(){ return cmdDesrciption;}
    public void execute(User sender, Message message, MessageChannel channel, List<String> args){
        long start = System.nanoTime();
		// Répondre avec le temps de latence
        String res = "pong " + ((System.nanoTime() - start) / 1000000.00) + "ms";
        MessageCreateAction send;
        send = channel.sendMessage(res);
        if(message!=null){send.setMessageReference(message);}
        send.queue();
    }
}
