package com.linked.quizbot.events;

import java.util.List;

import com.linked.quizbot.core.BotCore;
import com.linked.quizbot.core.MessageSender;
import com.linked.quizbot.core.viewers.QuizBot;
import com.linked.quizbot.core.viewers.Viewer;
import com.linked.quizbot.Constants;
import com.linked.quizbot.commands.BotCommand;
import com.linked.quizbot.commands.CommandOutput;

import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.channel.middleman.MessageChannel;
import net.dv8tion.jda.api.entities.emoji.Emoji;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;

/**
 * The {@code ButtonListener} class listens for button interactions in Discord.
 * It extends {@link ListenerAdapter} and processes button clicks to execute commands or handle quiz reactions.
 * <p>
 * This class is part of a Discord bot that manages quiz games and user interactions.
 * It checks if the user is allowed to interact based on the channel type and executes the corresponding command
 * or handles quiz reactions based on the button clicked.
 * </p>
 * @author alinked0
 * @version 1.0
 * @since 2025-02-01
 * @see ListenerAdapter
 * @see BotCommand
 * @see QuizBot
 */
public class ButtonListener extends ListenerAdapter {
	@Override
	public void onButtonInteraction(ButtonInteractionEvent event) {
		long start = System.nanoTime();
		User sender = event.getUser();
		if (sender == null || sender.isBot()) {
			return;
		}
		// Stop a buggy bot from being used on all of discord
		if (!BotCore.isBugFree() && !(BotCore.canIRunThisHere(event.getChannel().getId()))){
			return;
		}
		MessageChannel channel = event.getChannel();
		String componentId = event.getComponentId(); // The ID you assigned to the buttonBotCore.addUser(sender);
		String userId = sender.getId();
		String messageId = event.getMessageId();
		Message message = event.getMessage();
		BotCommand cmd = BotCommand.getCommandByName(componentId);
		if(cmd!=null){
			CommandOutput.Builder output = new CommandOutput.Builder()
			.add(cmd.execute(userId, List.of(messageId)));
			MessageSender.sendCommandOutput(
				output.build(),
				event
			);
			if (!BotCore.isBugFree()) System.out.printf(Constants.INFO + "%s, Time elapsed: `%.3f ms`\n",cmd.getName(), (System.nanoTime() - start) / 1000000.00);
			return;
		}
		Emoji reaction = Emoji.fromFormatted(event.getButton().getLabel());
		Viewer viewer = BotCore.getViewer(messageId);
		if (viewer!=null && viewer.isActive() && viewer.getReactions().contains(reaction)){
			// If the reaction is a number, the viewer will handle it.
			viewer.addReaction(userId, reaction);
			if (viewer instanceof QuizBot && viewer.getCurrentIndex()>=0){
				QuizBot quizBot = (QuizBot)viewer;
				MessageSender.sendCommandOutput(
					new CommandOutput.Builder().add(quizBot.current()).sendInOriginalMessage(true).build(),
					event
				);
				if (quizBot.getPlayers().size()==1){
					ReactionListener.autoNext(userId, message, quizBot);
					return;
				}
			}
		}
		event.editButton(event.getButton()).queue();
	}
}

