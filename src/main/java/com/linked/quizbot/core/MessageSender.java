package com.linked.quizbot.core;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.linked.quizbot.Constants;
import com.linked.quizbot.commands.BotCommand;
import com.linked.quizbot.commands.Output;
import com.linked.quizbot.commands.CommandOutput;

import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.channel.middleman.MessageChannel;
import net.dv8tion.jda.api.entities.channel.unions.MessageChannelUnion;
import net.dv8tion.jda.api.entities.emoji.Emoji;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
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
 * @see Output
 * @see net.dv8tion.jda.api.entities.Message
 * @see net.dv8tion.jda.api.entities.MessageEmbed
 * @see net.dv8tion.jda.api.entities.channel.middleman.MessageChannel
 * @see net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent
 */	
public class MessageSender {
	// A ScheduledExecutorService for handling delayed messages without blocking the main thread.
	private static final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
	
	public static void send(CommandOutput output) {
		if (output == null) {
			// No output to send, or command returned null
			System.out.println(Constants.INFO + "Warning: Command output was null, nothing to send.");
			return;
		}
		if (output.getFirst().clearReactions()){
			output.getFirst().getMessage().clearReactions().queue(none -> treatDelay(output));
		} else {
			// Schedule the message sending if a delay is specified.
			treatDelay(output);
		}
	}

	public static void send(Output output) {
		send(new CommandOutput(output));
	}

	public static void send(CommandOutput output, Message message) {
		CommandOutput res = new CommandOutput();
		for (Output out : output){
			res.add(new Output.Builder(out).setMessage(message).build());
		}
		send(res);
	}

	public static void send(CommandOutput output, MessageChannel channel) {
		CommandOutput res = new CommandOutput();
		for (Output out : output){
			res.add(new Output.Builder(out).channel(channel).build());
		}
		send(res);
	}

	public static void send(CommandOutput output, ButtonInteractionEvent event) {
		CommandOutput res = new CommandOutput();
		MessageChannel channel = event.getChannel();
		Message message = event.getMessage();
		for (Output out : output){
			res.add(
				new Output.Builder(out).channel(channel).setMessage(message).build()
			);
		}
		send(res);
	}

	public static void send(Output output, Message message) {
		send(new Output.Builder(output).setMessage(message).build());
	}

	public static void send(Output output, MessageChannel channel) {
		send(new Output.Builder(output).channel(channel).build());
	}
	
	public static void send(@NotNull Output output, @NotNull ButtonInteractionEvent event) {
		MessageChannel channel = event.getChannel();
		Message message = event.getMessage();
		send(
			new Output.Builder(output).channel(channel).setMessage(message).build()
		);
	}
	
	private static void treatDelay(CommandOutput output){
		 if (output.getFirst().getDelayMillis() > 0) {
			scheduler.schedule(
				() -> destination(output),
				output.getFirst().getDelayMillis(), TimeUnit.MILLISECONDS
			);
		} else {
			destination(output);
		}
	}
	
	private static void destination(CommandOutput output) {
		MessageChannel channel;
		User user;
		Output.Builder build ;
		final String title;

		channel = output.getFirst().getChannel();
		build = new Output.Builder(output.getFirst());
		// output will be limite by the dicord char limit
		if (output.getFirst().sendInThread()  && channel.getType().isGuild() && !channel.getType().isThread()){
			title = java.time.Instant.now().toString();
			if (output.getFirst().getMessage()!=null){
				output.getFirst().getMessage().createThreadChannel(title).queue(
					chaine -> {
						for (Output out : output){
							prepPackages(
								new Output.Builder(out).channel(chaine).build()
							);
						}
					}
				);
			} else {
				channel.sendMessage(title).queue(
					msg -> msg.createThreadChannel(title).queue(
						chaine -> prepPackages(
							build.channel(chaine).setMessage(msg).build()
						)
					)
				);
			}
			return;
		}
		if (output.getFirst().sendAsPrivateMessage() && channel.getType().isGuild()){
			user = BotCore.getUser(output.getFirst().getRequesterId());
			if (user!=null){
				user.openPrivateChannel().queue(
					chaine -> {
						for (Output out : output){
							prepPackages(new Output.Builder(out).channel(chaine).build());
						}
					}
				);
				return;
			}
			if (BotCore.getJDA()!=null && output.getFirst().getRequesterId()!=null){
				BotCore.getJDA().retrieveUserById(output.getFirst().getRequesterId()).queue(
					u -> u.openPrivateChannel().queue(
						chaine -> {
							for (Output out : output){
								prepPackages(new Output.Builder(out).channel(chaine).build());
							}
						}
					)
				);
				return;
			}
		}
		for (Output out : output){
			prepPackages(out);
		}
	}

	private static void prepPackages(Output output) {
		Message originalMessage;
		List<MessageCreateAction> sendActions ;
		MessageChannel channel;

		originalMessage = output.getMessage();
		sendActions = new ArrayList<>();
		channel = output.getChannel();

		// Send text messages first
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
				sendActions.add(sendAction);
			}
		}
		if (!output.getFiles().isEmpty()){
			MessageCreateAction sendAction;
			if (!sendActions.isEmpty()){
				sendAction = sendActions.getLast().addFiles(output.getFiles().stream().map(f -> FileUpload.fromData(f)).toList());
				sendActions.set(sendActions.size()-1, sendAction);
			}else{
				 sendAction = channel.sendFiles(output.getFiles().stream().map(f -> FileUpload.fromData(f)).toList());
				sendActions.add(sendAction);
			}
		}
		if (output.useButtons()  || !channel.getType().isGuild()){
			if (!sendActions.isEmpty()){
				sendActions.getLast().setComponents(output.getActionRows());
			}
		}
		send(output, sendActions, channel);
	}

	private static void send(Output output, List<MessageCreateAction> sendActions, MessageChannel channel){
		Message originalMessage;

		originalMessage = output.getMessage();

		if (output.sendInOriginalMessage() && sendActions.size()==1){
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
				failure -> System.err.println(Constants.ERROR + "Failed to edit Message : " + failure.getMessage()) // Log failure
			);
			return;
		}
		for (MessageCreateAction sendAction : sendActions){
			sendAction.queue(
				sentMessage -> { // Execute post-send actions for embeds too
					for (Consumer<Message> action : output.getPostSendActions()) {
						action.accept(sentMessage);
					}
					if (!output.useButtons()  && channel.getType().isGuild()) addReactions(sentMessage, output.getReactions().iterator());
				},
				failure -> System.err.println(Constants.ERROR + "Failed to send embed: " + failure.getMessage()) // Log failure
			);
		}
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
		int nbOptions, i;
		List<Button> row;
		List<ActionRow> newActionRows;
		Emoji e;
		BotCommand cmd ;
		String cmdName;

		row= new ArrayList<>();
		newActionRows = new ArrayList<>();
		for (nbOptions = 0; nbOptions<l.size(); ++nbOptions){
			e = Emoji.fromUnicode("U+3"+(nbOptions+1)+"U+fe0fU+20e3");
			if (l.get(nbOptions).equals(e)){
				row.add(
					Button.of(
						ButtonStyle.PRIMARY, 
						String.format("%s", (nbOptions+1)), e.getFormatted()
					)
				);
				if(row.size()==5){
					newActionRows.add(ActionRow.of(row));
					row = new ArrayList<>();
				}
			} else {
				break;
			}
		}
		if (!row.isEmpty()){
			newActionRows.add(ActionRow.of(row));
			row = new ArrayList<>();
		}

		for (i=nbOptions; i<l.size(); ++i){
			e = l.get(i);
			cmd = BotCommand.getCommandFromEmoji(e.getFormatted());
			if (cmd!=null){
				cmdName = cmd.getName();
			} else {
				cmdName = e.getFormatted();
			}
			row.add(
				Button.of(ButtonStyle.PRIMARY, cmdName, e.getFormatted())
			);
			if(row.size()==5){
				newActionRows.add(ActionRow.of(row));
				row = new ArrayList<>();
			}
		}
		if (!row.isEmpty()){
			newActionRows.add(ActionRow.of(row));
		}
		if (newActionRows.size() > 5) {
			return newActionRows.subList(0, 5);
		}
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
	// TODO this is technical dept
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
