package com.linked.quizbot.events;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

import com.linked.quizbot.Constants;
import com.linked.quizbot.core.BotCore;
import com.linked.quizbot.core.MessageSender;
import com.linked.quizbot.core.viewers.QuizBot;
import com.linked.quizbot.utils.Question;
import com.linked.quizbot.commands.BotCommand;
import com.linked.quizbot.commands.CommandOutput;
import com.linked.quizbot.commands.list.EndCommand;
import com.linked.quizbot.commands.list.NextCommand;

import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.channel.middleman.MessageChannel;
import net.dv8tion.jda.api.entities.emoji.Emoji;
import net.dv8tion.jda.api.entities.channel.ChannelType;
import net.dv8tion.jda.api.events.message.react.GenericMessageReactionEvent;
import net.dv8tion.jda.api.events.message.react.MessageReactionAddEvent;
import net.dv8tion.jda.api.events.message.react.MessageReactionRemoveEvent;

/**
 * The ReactionListener class is a specialized {@link ListenerAdapter} class, 
 * designed to keep track of reactions that are added or removed on any message that was sent by the bot. 
 * The messages are tracked through a list of {@link QuizBot} found in the {@link BotCore} class; 
 * every {@link QuizBot} class contains the message on which the current game is being played.
 */
public class ReactionListener extends ListenerAdapter {

	@Override
	public void onMessageReactionAdd(MessageReactionAddEvent event){
		long start = System.nanoTime();
		User sender = event.getUser();
		GenericMessageReactionEvent f = (GenericMessageReactionEvent)event;
		if (sender == null || sender.isBot()) {
			return;
		}
		
		if (event.isFromGuild() || event.isFromThread()){
			if (!Constants.canIRunThisHere(event.getGuild().getId())){
				return;
			}
		} else if (event.isFromType(ChannelType.PRIVATE)){
			if (Constants.AREWETESTING && !Constants.AUTHORID.equals(sender.getId())){
				return;
			}
		}
		
		BotCore.addUser(sender);
		String userId = sender.getId();
		MessageChannel channel = event.getChannel();
		Emoji reaction = f.getEmoji();
		String messageId = event.getMessageId();
		event.getChannel().retrieveMessageById(messageId).queue(message -> {
			BotCommand cmd = BotCommand.getCommandFromEmoji(reaction);
			if(cmd!=null){
				if (!Constants.isBugFree()) System.out.printf("  $> "+cmd.getName());
				MessageSender.sendCommandOutput(
					cmd.execute(userId, List.of(messageId)),
					channel,
					message 
				);
				if (!Constants.isBugFree()) System.out.printf("   $> time = `%.3f ms`\n", (System.nanoTime() - start) / 1000000.00);
				return;
			}
			QuizBot quizBot = (QuizBot)BotCore.getViewer(messageId);
			if (quizBot!=null && quizBot.isActive() && quizBot.getReactions().contains(reaction)){
				quizBot.addReaction(userId, reaction);
				if (quizBot.getCurrentIndex()>=0&& quizBot.awnsersByUserIdByQuestionIndex.get(quizBot.getCurrentIndex()).size()==1){
					MessageSender.sendCommandOutput(
						new CommandOutput.Builder().add(quizBot.current()).sendInOriginalMessage(true).build(),
						channel,
						message 
					);
					ReactionListener.autoNext(userId, message, quizBot);
					return;
				}else{
					quizBot.isExplaining(false);
				}
			}
		});
	}

	public static void autoNext(String userId, Message message, QuizBot quizBot){
		if(!quizBot.useAutoNext()){
			return;
		}
		Question oldQ = quizBot.getCurrQuestion();
		MessageChannel channel = message.getChannel();
		String messageId = message.getId();
		if(oldQ.equals(quizBot.getCurrQuestion()) && !quizBot.isExplaining()){
			BotCommand cmd = BotCommand.getCommandByName(quizBot.getCurrentIndex()<quizBot.getQuestionList().size()-1?NextCommand.CMDNAME:EndCommand.CMDNAME);
			MessageSender.sendCommandOutput(
				cmd.execute(userId, List.of(messageId)),
				channel,
				message 
			);
		}
	}
	@Override
	public void onMessageReactionRemove(MessageReactionRemoveEvent event){
		User sender = event.getUser();
		GenericMessageReactionEvent f = (GenericMessageReactionEvent)event;
		if (sender == null || sender.isBot()) {
			return;
		}
		
		if (event.isFromGuild() || event.isFromThread()){
			if (!Constants.canIRunThisHere(event.getGuild().getId())){
				return;
			}
		} else if (event.isFromType(ChannelType.PRIVATE)){
			if (Constants.AREWETESTING && !Constants.AUTHORID.equals(sender.getId())){
				return;
			}
		}
		// TODO 
	}
}