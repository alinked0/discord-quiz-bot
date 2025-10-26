package com.linked.quizbot.events;

import java.util.List;

import com.linked.quizbot.Constants;
import com.linked.quizbot.core.BotCore;
import com.linked.quizbot.core.CommandLineInterface;
import com.linked.quizbot.core.MessageSender;
import com.linked.quizbot.core.viewers.QuizBot;
import com.linked.quizbot.core.viewers.Viewer;
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
		GenericMessageReactionEvent f = (GenericMessageReactionEvent)event;
		User sender = event.getUser();
		if (sender == null || sender.isBot()) {
			return;
		}
		if (!BotCore.isBugFree() && !(BotCore.canIRunThisHere(event.getChannel().getId()))){
			return;
		}
		
		BotCore.addUser(sender);
		String userId = sender.getId();
		MessageChannel channel = event.getChannel();
		Emoji reaction = f.getEmoji();
		String messageId = event.getMessageId();
		event.getChannel().retrieveMessageById(messageId).queue(message -> {
			BotCommand cmd = BotCommand.getCommandFromEmoji(reaction.getFormatted());
			if(cmd!=null){
				CommandOutput output= CommandLineInterface.execute(Constants.CMDPREFIXE+cmd.getName()+" "+messageId, userId, null);
				MessageSender.sendCommandOutput(
					output,
					channel,
					message 
				);				
				return;
			}			
			Viewer viewer = BotCore.getViewer(messageId);
			if (viewer!=null && viewer.isActive() && viewer.getReactions().contains(reaction)){
				viewer.addReaction(userId, reaction);
				if (viewer instanceof QuizBot && viewer.getCurrentIndex()>=0){
					QuizBot quizBot = (QuizBot)viewer;
					if (quizBot.useAutoNext() && quizBot.getPlayers().size()==1){
						ReactionListener.autoNext(userId, message, quizBot);
						return;
					}
				}
			}
			MessageSender.sendCommandOutput(
				new CommandOutput.Builder().add(viewer.current()).sendInOriginalMessage(true).build(),
				channel,
				message 
			);
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
			if (!BotCore.canIRunThisHere(event.getGuild().getId())){
				return;
			}
		} else if (event.isFromType(ChannelType.PRIVATE)){
			if (BotCore.areWeTesting && !Constants.ADMINID.equals(sender.getId())){
				return;
			}
		}
		// TODO impl the reaction removal
	}
}