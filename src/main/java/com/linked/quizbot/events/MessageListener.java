package com.linked.quizbot.events;

import java.util.List;
import java.util.ArrayList;

import com.linked.quizbot.Constants;
import com.linked.quizbot.commands.BotCommand;
import com.linked.quizbot.commands.CommandOutput;
import com.linked.quizbot.commands.list.CreateListCommand;
import com.linked.quizbot.commands.list.AddListCommand;
import com.linked.quizbot.core.BotCore;
import com.linked.quizbot.core.CommandLineInterface;
import com.linked.quizbot.core.MessageSender;
import com.linked.quizbot.utils.Users;

import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.channel.middleman.MessageChannel;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

/**
 * The class MessageReceivedListener will serve as the first layer to any text command
 * that means message commands like !help
 */
public class MessageListener extends ListenerAdapter {
	
	@Override
	public void onMessageReceived(MessageReceivedEvent event) {
		// Ignore bots
		User sender = event.getAuthor();
		if (sender.isBot()) return;
		// Stop a buggy bot from being used on all of discord
		if (!BotCore.isBugFree() && !(BotCore.canIRunThisHere(event.getChannel().getId()))){
			return;
		}

		// log User
		BotCore.addUser(sender);
		String userId = sender.getId();
		
		MessageChannel channel = event.getChannel();
		String channelId = channel.getId();
		//vefifier si le message contient notre prefixe
		Message message = event.getMessage();
		String content = message.getContentRaw();
		String userPrefixe = Users.get(userId).getPrefix();
		if (!content.startsWith(Constants.CMDPREFIXE) && userPrefixe!=null && !content.startsWith(userPrefixe)){
			return;
		}
		
		CommandOutput output= CommandLineInterface.execute(content, userId);
		MessageSender.sendCommandOutput(
			output,
			channel,
			message
		);
	}
}


