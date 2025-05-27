package com.linked.quizbot.commands.list;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import com.linked.quizbot.Constants;
import com.linked.quizbot.commands.BotCommand;

import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.requests.restaction.MessageCreateAction;
import net.dv8tion.jda.api.entities.channel.middleman.MessageChannel;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;

public class InviteCommand extends BotCommand {
    public static String CMDNAME = "invite";
    private String cmdDesrciption = "create a bot instalation link";
	private String[] abbrevs = new String[0];
    
	@Override
	public String[] getAbbreviations(){ return abbrevs;}
	@Override
    public String getName(){ return CMDNAME;}
	@Override
    public String getDescription(){ return cmdDesrciption;}
	@Override
    public List<OptionData> getOptionData(){
        List<OptionData> res = new ArrayList<OptionData>();
        return res;
    }
    @Override
    public void execute(User sender, Message message, MessageChannel channel, String[] args){
        List<Permission> permissions = new LinkedList<>(
            List.of(Permission.CREATE_INSTANT_INVITE, Permission.CREATE_PUBLIC_THREADS, Permission.MESSAGE_ADD_REACTION, Permission.MESSAGE_ATTACH_FILES, Permission.MESSAGE_EXT_EMOJI, Permission.MESSAGE_EMBED_LINKS, Permission.MESSAGE_MANAGE, Permission.MESSAGE_SEND, Permission.MESSAGE_SEND_IN_THREADS, Permission.USE_APPLICATION_COMMANDS, Permission.VIEW_CHANNEL)
        );
        String res = "```\n"+channel.getJDA().getInviteUrl(permissions)+"\n```\n";
        MessageCreateAction send;
        send = channel.sendMessage(res);
        if(message!=null){send.setMessageReference(message);}
        send.queue(msg -> msg.delete().queueAfter(Constants.READTIMEMIN, TimeUnit.MINUTES));
    }
}
