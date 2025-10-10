package com.linked.quizbot.events;

import com.linked.quizbot.Constants;
import com.linked.quizbot.commands.CommandOutput;
import com.linked.quizbot.core.BotCore;
import com.linked.quizbot.core.CommandLineInterface;
import com.linked.quizbot.core.MessageSender;

import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.channel.middleman.MessageChannel;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

/**
 * The class MessageReceivedListener will serve as the first layer to any text command
 * that means message commands like q!help
 */
public class MessageListener extends ListenerAdapter {
	
	@Override
	public void onMessageReceived(MessageReceivedEvent event) {
		long start = System.nanoTime();
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
		
		List<String> tmp = CommandLineInterface.parsePrefixe(userId, content);
		String cmndLineArgs = "";
		if (tmp.isEmpty()){
			return;
		}
		content = tmp.getLast();
		tmp = CommandLineInterface.parseBotCommand(content);
		if (tmp.isEmpty()){
			return;
		}
		cmndLineArgs = tmp.getLast();
		BotCommand cmd = BotCommand.getCommandByName(tmp.get(0));
		
		if(BotCore.isShutingDown()){
			BotCommand.CommandCategory category = cmd.getCategory();
			if(category.equals(BotCommand.CommandCategory.EDITING) || category.equals(BotCommand.CommandCategory.GAME)){
				MessageSender.sendCommandOutput(
					new CommandOutput.Builder().add(Constants.UPDATEEXPLANATION).build(),
					channel,
					null 
					);
				return;
			}
		}
		List<String> arguments=new ArrayList<>();
		arguments.addAll(cmd.parseArguments(cmndLineArgs));
		switch (cmd.getName()) {
			case CreateListCommand.CMDNAME, AddListCommand.CMDNAME -> {
				arguments.addAll(BotCommand.getArgFromAttachments(userId, message.getAttachments()));
			}
		}
		MessageSender.sendCommandOutput(
			cmd.execute(userId, arguments),
			channel,
			message
		);

		System.out.printf("[INFO] cmd=%s, Time_elapsed=`%.3f ms`, Argc-1=%d;\n",cmd.getName(), (System.nanoTime() - start) / 1000000.00, arguments.size());
	}
}



