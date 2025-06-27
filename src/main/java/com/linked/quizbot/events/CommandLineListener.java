package com.linked.quizbot.events;

import java.util.ArrayList;
import java.util.List;

import com.linked.quizbot.Constants;
import com.linked.quizbot.commands.BotCommand;
import com.linked.quizbot.commands.BotCommand.CommandCategory;
import com.linked.quizbot.commands.CommandOutput;
import com.linked.quizbot.commands.list.CreateListCommand;
import com.linked.quizbot.commands.list.HelpCommand;
import com.linked.quizbot.commands.list.AddListCommand;
import com.linked.quizbot.commands.list.ViewCommand;
import com.linked.quizbot.core.BotCore;
import com.linked.quizbot.core.CommandLineInterface;
import com.linked.quizbot.core.MessageSender;
import com.linked.quizbot.utils.Users;

import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.Message.Attachment;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.channel.ChannelType;
import net.dv8tion.jda.api.entities.channel.middleman.MessageChannel;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

/**
 * The class MessageReceivedListener will serve as the first layer to any text command
 * that means message commands like !help
 */
public class CommandLineListener extends ListenerAdapter {
	
	@Override
	public void onMessageReceived(MessageReceivedEvent event) {
		long start = System.nanoTime();
		// Ignore bots
		User sender = event.getAuthor();
		if (sender.isBot()) return;
		// Stop a buggy bot from being used on all of discord
		if (event.isFromGuild() || event.isFromThread()){
			if (!Constants.canIRunThisHere(event.getGuild().getId())){
				return;
			}
		} else if (event.isFromType(ChannelType.PRIVATE)){
			if (Constants.AREWETESTING && !Constants.AUTHORID.equals(sender.getId())){
				return;
			}
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
					new CommandOutput.Builder().addTextMessage(Constants.UPDATEEXPLANATION).build(),
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
		if (!Constants.isBugFree()) {
			System.out.print("  $> "+content.replace("\n", "").replace("\t", ""));
			System.out.print(" ; arguments :");
			for (int i=0; i<arguments.size(); i++) { 
				System.out.print(arguments.get(i).replace("\n", "").replace("\t", "")+":");
			}
		}
		MessageSender.sendCommandOutput(
			cmd.execute(userId, channelId, arguments, true),
			channel,
			null 
		);
		if (!Constants.isBugFree()) System.out.printf("   $> time of "+cmd.getName()+" = `%.3f ms`\n", (System.nanoTime() - start) / 1000000.00);
	}
}


