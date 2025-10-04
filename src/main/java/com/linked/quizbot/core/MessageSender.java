package com.linked.quizbot.core;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

import org.jetbrains.annotations.Nullable;

import com.linked.quizbot.Constants;
import com.linked.quizbot.commands.BotCommand;
import com.linked.quizbot.commands.CommandOutput;

import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.channel.middleman.MessageChannel;
import net.dv8tion.jda.api.entities.emoji.Emoji;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.interactions.components.ActionRow;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import net.dv8tion.jda.api.interactions.components.buttons.ButtonStyle;
import net.dv8tion.jda.api.requests.restaction.MessageCreateAction;
import net.dv8tion.jda.api.utils.AttachedFile;
import net.dv8tion.jda.api.utils.FileUpload;
import net.dv8tion.jda.api.utils.messages.MessageEditBuilder;

/**
 * The {@code MessageSender} class is responsible for sending command outputs to Discord channels.
 * It handles both immediate and delayed message sending, including text messages, embeds, and file attachments.
 * This class is designed to work with Discord's JDA (Java Discord API) library.
 * <p>
 * It provides methods to send command outputs in response to button interactions or directly to message channels,
 * while respecting Discord's character limits and message formatting requirements.
 * </p>
 * <p>
 * This class also includes functionality to handle message reactions and buttons, allowing for interactive messages.
 * It can send messages in threads or private channels based on the command output configuration.
 * </p>
 * <p>
 * Note: This class uses a {@link ScheduledExecutorService} to manage delayed message sending without blocking the main thread.
 * It ensures that messages are sent asynchronously, improving responsiveness and user experience.
 * </p>
 * @author alinked0
 * @version 1.0
 * @since 2025-02-01
 * @see CommandOutput
 * @see net.dv8tion.jda.api.entities.Message
 * @see net.dv8tion.jda.api.entities.MessageEmbed
 * @see net.dv8tion.jda.api.entities.channel.middleman.MessageChannel
 * @see net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent
 */	
public class MessageSender {
	// A ScheduledExecutorService for handling delayed messages without blocking the main thread.
	private static final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
	
	public static void sendCommandOutput(CommandOutput output, MessageChannel channel, Message originalMessage) {
		if (output == null) {
			// No output to send, or command returned null
			System.out.println("[INFO] Warning: Command output was null, nothing to send.");
			return;
		}
		if (output.getMessage()!=null){
			originalMessage = output.getMessage();
		}
		if (originalMessage!=null && output.clearReactions()){
			final Message msg = originalMessage;
			originalMessage.clearReactions().queue(none -> treatDelay(output, channel, msg));
		} else {
			// Schedule the message sending if a delay is specified.
			treatDelay(output, channel, originalMessage);
		}
	}

	private static void treatDelay(CommandOutput output, MessageChannel channel, Message originalMessage){
		 if (output.getDelayMillis() > 0) {
			scheduler.schedule(() -> sendConditions(output, channel, originalMessage),
							   output.getDelayMillis(), TimeUnit.MILLISECONDS);
		} else {
			sendConditions(output, channel, originalMessage);
		}
	}

	private static void sendConditions(CommandOutput output, MessageChannel channel, Message originalMessage) {
		// output will be limite by the dicord char limit
		if (output.sendInOriginalMessage()){
			String content = "";
			for (String s : output.getTextMessages()){
				if (!s.isEmpty()){
					content += s+"\n";
				}
			}
			MessageEditBuilder newMessage = new MessageEditBuilder()
				.setContent(content)
				.setAttachments(output.getFiles().stream().map(f->AttachedFile.fromData(f)).toList())
			.setEmbeds(output.getEmbeds());
			if (output.useButtons()  || !channel.getType().isGuild() && !channel.getType().isThread()){
				newMessage.setComponents(output.getActionRows());
			}
			originalMessage.editMessage(newMessage.build()).queue(
				sentMessage -> { // Execute post-send actions for embeds too
					for (Consumer<Message> action : output.getPostSendActions()) {
						action.accept(sentMessage);
					}
					if (!output.useButtons()  && channel.getType().isGuild()) addReactions(sentMessage, output.getReactions().iterator());
				},
				failure -> System.err.println("[ERROR] Failed to edit Message : " + failure.getMessage()) // Log failure
			);
			return;
		} 
		if (output.sendInThread()  && channel.getType().isGuild() && !channel.getType().isThread()){
			String s = output.getTextMessages().get(0);
			
			int i = s.indexOf("\n");
			final String title;
			if (i<0){
				title = s;
			} else {
				i = s.indexOf("\n", i+1);
				if (i<0){
					i=s.length();
					title = s;
				} else {
					s = s.substring(0, i);
					title = s;
				}
			}
			channel.sendMessage(s).queue(msg -> msg.createThreadChannel(title).queue(chaine -> sendActualOutput(output, chaine, null)));
			return;
		}
		if (Constants.DEBUGCHANNELID == null && Constants.DEBUGGUILDID == null || output.sendAsPrivateMessage() && channel.getType().isGuild()){
			User u = BotCore.getUser(output.getRequesterId());
			if (u!=null){
				u.openPrivateChannel().queue(chaine -> sendActualOutput(output, chaine, null));
				return;
			}
			if (BotCore.getJDA()!=null && output.getRequesterId()!=null){
				BotCore.getJDA().retrieveUserById(output.getRequesterId()).queue(user -> user.openPrivateChannel().queue(chaine -> sendActualOutput(output, chaine, null)));
				return;
			}
		}
		sendActualOutput(output, channel, originalMessage);
	}
	private static void sendActualOutput(CommandOutput output, MessageChannel channel, Message originalMessage) {
		// Send text messages first
		List<MessageCreateAction> sendActions = new ArrayList<>();
		if (!output.getTextMessages().isEmpty()) {
			List<String> messagesToSend = new ArrayList<>();
			// Iterate through all text messages provided by the command and trim/split them
			for (String text : output.getTextMessages()) {
				messagesToSend.addAll(trimMessage(text));
			}
			// Pass the postSendActions to the recursive helper
			sendActions.addAll(recursive_send_text(messagesToSend.iterator(), originalMessage, channel, output.shouldReplyToSender(), List.of()));
		}
		// Then send embeds
		if (!output.getEmbeds().isEmpty()) {
			for (MessageEmbed embed : output.getEmbeds()) {
				MessageCreateAction sendAction = channel.sendMessageEmbeds(embed);
				// Handle ephemeral if this was a slash command interaction and output.isEphemeral() is true
				// Note: For actual slash commands, you'd use event.deferReply().setEphemeral(true).queue()
				// or event.getHook().sendMessage(...).setEphemeral(true).queue()
				// This example focuses on MessageChannel.sendMessage for simplicity.
				sendActions.add(sendAction);
			}
		}
		if (!output.getFiles().isEmpty()){
			if (!sendActions.isEmpty()){
				MessageCreateAction sendAction = sendActions.getLast().addFiles(output.getFiles().stream().map(f -> FileUpload.fromData(f)).toList());
				sendActions.set(sendActions.size()-1, sendAction);
			}else{
				MessageCreateAction  sendAction = channel.sendFiles(output.getFiles().stream().map(f -> FileUpload.fromData(f)).toList());
				sendActions.add(sendAction);
			}
		}
		if (output.useButtons()  || !channel.getType().isGuild()){
			if (!sendActions.isEmpty()){
				sendActions.getLast().setComponents(output.getActionRows());
			}
		}
		for (MessageCreateAction sendAction : sendActions){
			sendAction.queue(
				sentMessage -> { // Execute post-send actions for embeds too
					for (Consumer<Message> action : output.getPostSendActions()) {
						action.accept(sentMessage);
					}
					if (!output.useButtons()  && channel.getType().isGuild()) addReactions(sentMessage, output.getReactions().iterator());
				},
				failure -> System.err.println("[ERROR] Failed to send embed: " + failure.getMessage()) // Log failure
			);
		}
	}

	public static void sendCommandOutput(CommandOutput output, ButtonInteractionEvent event) {
		if (output == null) {
			// No output to send, or command returned null
			System.out.println("[INFO] Warning: Command output was null, nothing to send.");
			return;
		}

        MessageChannel channel = event.getChannel();
        Message message = event.getMessage();
		String content = "";
		for (String s : output.getTextMessages()){
			if (!s.isEmpty()){
				content += s+"\n";
			}
		}
		MessageEditBuilder newMessage = new MessageEditBuilder()
			.setContent(content)
			.setAttachments(output.getFiles().stream().map(f->AttachedFile.fromData(f)).toList())
		.setEmbeds(output.getEmbeds());
		if (output.useButtons() || !channel.getType().isGuild() && !channel.getType().isThread()){
			newMessage.setComponents(output.getActionRows());
		}
		event.editMessage(newMessage.build()).queue(
			sentMessage -> { // Execute post-send actions for embeds too
				for (Consumer<Message> action : output.getPostSendActions()) {
					action.accept(message);
				}
				if (!output.useButtons()  && channel.getType().isGuild()) addReactions(message, output.getReactions().iterator());
			},
			failure -> System.err.println("[ERROR] Failed to edit Message : " + failure.getMessage()) // Log failure
		);
	}

	public static void addReactions(Message message, Iterator<Emoji> iter, @Nullable Consumer<? super Message> success) {
		if(!iter.hasNext()){
			if (success!=null) success.accept(message);return;
		}
		message.addReaction(iter.next()).queue( v -> addReactions(message, iter, success));
    }

	public static void addReactions(Message message, Iterator<Emoji> iter) {
		message.addReaction(iter.next()).queue( v -> addReactions(message, iter, null));
    }
	public static List<ActionRow> actionRowsFromEmojis(List<Emoji> l) {
		int nbOptions = 0;
		List<Button> row= new ArrayList<>();
		List<ActionRow> newActionRows = new ArrayList<>();
		int i=0;
		Emoji e;
		for (; nbOptions<l.size(); ++nbOptions){
			e = Emoji.fromUnicode("U+3"+(nbOptions+1)+"U+fe0fU+20e3");
			if (l.get(nbOptions).equals(e)){
				row.add(Button.of(ButtonStyle.PRIMARY, String.format("%s", (nbOptions+1)), e.getFormatted())); //ReactionListener.getCommandFromEmoji(e).getName()
				if(row.size()==5){
					newActionRows.add(ActionRow.of(row));
					row = new ArrayList<>();
				}
			} else {
				break;
			}
		}
		if (!row.isEmpty())newActionRows.add(ActionRow.of(row));
		row = new ArrayList<>();
		for (i=nbOptions; i<l.size(); ++i){
			e = l.get(i);
			BotCommand cmd = BotCommand.getCommandFromEmoji(e.getFormatted());
			if (cmd!=null){
				String id = cmd.getName();
				row.add(Button.of(ButtonStyle.PRIMARY, id, e.getFormatted()));
				if(row.size()==5){
					newActionRows.add(ActionRow.of(row));
					row = new ArrayList<>();
				}
			}
		}
		if (!row.isEmpty())newActionRows.add(ActionRow.of(row));
		return newActionRows;
	}
	public static void addButtons(Message message, List<Emoji> l, @Nullable Consumer<? super Message> success) {
		message.editMessageComponents(MessageSender.actionRowsFromEmojis(l)).queue(success);;
    }
	public static void addButtons(Message message, List<Emoji> l) {
		MessageSender.addButtons(message, l, null);
    }
	private static List<MessageCreateAction> recursive_send_text(Iterator<String> iter, Message originalMessage, MessageChannel channel, boolean shouldReply, List<Consumer<Message>> postSendActions) {
		List<MessageCreateAction> sendActions = new ArrayList<>();
		while (iter.hasNext()){
			String part = iter.next();
			MessageCreateAction sendAction = channel.sendMessage(part);

			if (shouldReply && originalMessage != null) {
				sendAction.setMessageReference(originalMessage);
			}
			sendActions.add(sendAction);
		}
		return sendActions;
	}
	public static List<String> trimMessage(String s){
		List<String> resultParts = new ArrayList<>();
		if (s == null || s.isEmpty()) {
			return resultParts;
		}
		if (s.length() <= Constants.CHARSENDLIM){
			resultParts.add(s);
		} else {
			// The message is too long, split it.
			String currentPart = s.substring(0, Math.min(s.length(), Constants.CHARSENDLIM));
			String remainingPart = s.substring(Math.min(s.length(), Constants.CHARSENDLIM));

			// Try to find a natural break point (newline) within the current part.
			// Prioritize splitting at newline if it's not too close to the beginning of the limit.
			int lastNewlineIndex = currentPart.lastIndexOf("\n");
			// If a newline exists and it's not very early in the part, split there.
			// Using Constants.CHARSENDLIM / 2 as a heuristic threshold.
			if (lastNewlineIndex != -1 && lastNewlineIndex > Constants.CHARSENDLIM / 2) {
				resultParts.add(currentPart.substring(0, lastNewlineIndex));
				// Recurse with the remainder of the current part concatenated with the overall remaining part.
				resultParts.addAll(trimMessage(currentPart.substring(lastNewlineIndex + 1) + remainingPart));
			} else {
				// No good newline break, or newline is too early. Split at the character limit.
				resultParts.add(currentPart);
				// Recurse with the remaining part.
				resultParts.addAll(trimMessage(remainingPart));
			}
		}
		return resultParts;
	}
}
