package com.linked.quizbot.commands.list;

import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.ArrayList;

import com.linked.quizbot.Constants;
import com.linked.quizbot.commands.BotCommand;
import com.linked.quizbot.commands.CommandCategory;
import com.linked.quizbot.core.BotCore;

import net.dv8tion.jda.api.events.Event;
import net.dv8tion.jda.api.events.GenericEvent;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.requests.restaction.MessageCreateAction;
import net.dv8tion.jda.api.entities.channel.middleman.MessageChannel;
// import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
// import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
// import net.dv8tion.jda.api.hooks.ListenerAdapter;

public class PingCommand extends BotCommand {
    public static final String cmdName = "ping";
    private String cmdDesrciption = "get comfirmation that the bot is online.";
	private String[] abbrevs = new String[]{"p"};
    
	public String[] getAbbreviations(){ return abbrevs;}
    public String getName(){ return cmdName;}
    public String getDescription(){ return cmdDesrciption;}
	@Override
    public List<OptionData> getOptionData(){
        List<OptionData> res = new ArrayList<OptionData>();
        return res;
    }
    public void execute(User sender, Message message, MessageChannel channel, String[] args){
        long start = System.nanoTime();
		// Répondre avec le temps de latence
        String res = "pong " + (System.nanoTime() - start) / 1000000.00 + "ms";
        MessageCreateAction send;
        send = channel.sendMessage(res);
        if(message!=null){send.setMessageReference(message);}
        send.queue(msg -> msg.delete().queueAfter(Constants.READTIMEMIN, TimeUnit.MINUTES));
    }
}
