package com.linked.quizbot.events;

import java.util.List;

import com.linked.quizbot.Constants;
import com.linked.quizbot.core.BotCore;
import com.linked.quizbot.core.MessageSender;
import com.linked.quizbot.core.QuizBot;
import com.linked.quizbot.commands.BotCommand;
import com.linked.quizbot.commands.CommandOutput;

import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.channel.middleman.MessageChannel;
import net.dv8tion.jda.api.entities.emoji.Emoji;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;

/**
 * The ReactionListener class is a specialized {@link ListenerAdapter} class, 
 * designed to keep track of reactions that are added or removed on any message that was sent by the bot. 
 * The messages are tracked through a list of {@link QuizBot} found in the {@link BotCore} class; 
 * every {@link QuizBot} class contains the message on which the current game is being played.
 */
public class ButtonListener extends ListenerAdapter {
	@Override
	public void onButtonInteraction(ButtonInteractionEvent event) {
		long start = System.nanoTime();
		User sender = event.getUser();
		if (sender == null || sender.isBot()) {
			return;
		}
		MessageChannel channel = event.getChannel();
		if (channel.getType().isGuild() || channel.getType().isThread()){
			if (!Constants.canIRunThisHere(event.getGuild().getId())){
				return;
			}
		} else if (!channel.getType().isGuild()){
			if (Constants.AREWETESTING && !Constants.AUTHORID.equals(sender.getId())){
				return;
			}
		}
		String componentId = event.getComponentId(); // The ID you assigned to the buttonBotCore.addUser(sender);
		String userId = sender.getId();
		String messageId = event.getMessageId();
		Message message = event.getMessage();
		BotCommand cmd = BotCommand.getCommandByName(componentId);
		if(cmd!=null){
			if (!Constants.isBugFree()) System.out.printf("  $> "+cmd.getName());
			CommandOutput.Builder output = new CommandOutput.Builder()
			.addCommandOutput(cmd.execute(userId, List.of(messageId)));
			MessageSender.sendCommandOutput(
				output.build(),
				event
			);
			if (!Constants.isBugFree()) System.out.printf("   $> time = `%.3f ms`\n", (System.nanoTime() - start) / 1000000.00);
			return;
		}
		Emoji reaction = Emoji.fromFormatted(event.getButton().getLabel());
		QuizBot quizBot = (QuizBot)BotCore.getViewer(messageId);
		if (quizBot!=null && quizBot.isActive() && quizBot.getButtons().contains(reaction)){
			// If the reaction is a number, the viewer will handle it.
			quizBot.addReaction(userId, reaction);
			if (quizBot.getCurrentIndex()>=0&& quizBot.awnsersByUserIdByQuestionIndex.get(quizBot.getCurrentIndex()).size()==1){
				MessageSender.sendCommandOutput(
					new CommandOutput.Builder().addCommandOutput(quizBot.current()).sendInOriginalMessage(true).build(),
					event
				);
				ReactionListener.treatDelay(userId, message, quizBot);
				return;
			}else{
				quizBot.setExplainTriger(false);
				event.editButton(event.getButton()).queue();
			}
		}
	}
}

