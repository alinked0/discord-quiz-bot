package com.linked.quizbot.commands.list;

import java.util.List;

import java.util.LinkedList;
import java.util.List;
import com.linked.quizbot.commands.BotCommand;

import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.requests.restaction.MessageCreateAction;
import net.dv8tion.jda.api.entities.channel.middleman.MessageChannel;

public class InviteCommand extends BotCommand {
    public static String CMDNAME = "invite";
    private String cmdDesrciption = "creating a bot instalation link";
	@Override
    public String getName(){ return CMDNAME;}
	@Override
    public String getDescription(){ return cmdDesrciption;}
    @Override
    public void execute(User sender, Message message, MessageChannel channel, List<String> args){
        List<Permission> permissions = new LinkedList<>(
            List.of(Permission.CREATE_INSTANT_INVITE, Permission.CREATE_PUBLIC_THREADS, Permission.MESSAGE_ADD_REACTION, Permission.MESSAGE_ATTACH_FILES, Permission.MESSAGE_EXT_EMOJI, Permission.MESSAGE_EMBED_LINKS, Permission.MESSAGE_MANAGE, Permission.MESSAGE_SEND, Permission.MESSAGE_SEND_IN_THREADS, Permission.USE_APPLICATION_COMMANDS, Permission.VIEW_CHANNEL)
        );
        String res = "```\n"+channel.getJDA().getInviteUrl(permissions)+"\n```\n";
        MessageCreateAction send;
        send = channel.sendMessage(res);
        if(message!=null){send.setMessageReference(message);}
        send.queue();
    }
}
