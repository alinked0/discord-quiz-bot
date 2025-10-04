package com.linked.quizbot.commands.list;

import java.util.List;

import java.util.LinkedList;

import com.linked.quizbot.commands.BotCommand;
import com.linked.quizbot.commands.CommandOutput;
import com.linked.quizbot.core.BotCore;

import net.dv8tion.jda.api.Permission;

/**
 * The {@code InviteCommand} class provides functionality to generate an installation link for the bot
 * with specific permissions. It extends {@link BotCommand} and is part of a Discord bot that manages quiz games.
 * <p>
 * This command allows users to create a link that can be used to invite the bot to their server with the necessary permissions.
 * </p>
 * @author alinked0
 * @version 1.0
 * @since 2025-02-01
 * @see BotCommand
 */
public class InviteCommand extends BotCommand {
    public static String CMDNAME = "invite";
    private String cmdDesrciption = "creating a bot instalation link";
	@Override
    public String getName(){ return CMDNAME;}
	@Override
    public String getDescription(){ return cmdDesrciption;}
    @Override
    public CommandOutput execute(String userId,  List<String> args){
        List<Permission> permissions = new LinkedList<>(
            List.of(
                Permission.CREATE_PUBLIC_THREADS, 
                Permission.MESSAGE_ADD_REACTION,
                Permission.MESSAGE_ATTACH_FILES, 
                Permission.MESSAGE_EXT_EMOJI, 
                Permission.MESSAGE_EMBED_LINKS, 
                Permission.MESSAGE_MANAGE, 
                Permission.MESSAGE_SEND, 
                Permission.MESSAGE_SEND_IN_THREADS, 
                Permission.USE_APPLICATION_COMMANDS, 
                Permission.VIEW_CHANNEL
            )
        );
        String res = String.format("`%s`", BotCore.getJDA().getInviteUrl(permissions));
		return new CommandOutput.Builder()
				.add(res)
				.build();
    }
}
