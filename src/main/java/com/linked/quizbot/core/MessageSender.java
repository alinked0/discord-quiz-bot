package com.linked.quizbot.core;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

import com.linked.quizbot.Constants;
import com.linked.quizbot.commands.CommandOutput;

import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.channel.middleman.MessageChannel;
import net.dv8tion.jda.api.requests.restaction.MessageCreateAction;
import net.dv8tion.jda.api.utils.AttachedFile;
import net.dv8tion.jda.api.utils.FileUpload;
import net.dv8tion.jda.api.utils.messages.MessageEditBuilder;

public class MessageSender {
	// A ScheduledExecutorService for handling delayed messages without blocking the main thread.
	private static final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
	public static void sendCommandOutput(CommandOutput output, MessageChannel channel, Message originalMessage) {
		if (output == null) {
			// No output to send, or command returned null
			System.out.println("Warning: Command output was null, nothing to send.");
			return;
		}
		// output will be limite by the dicord char limit
		if (output.editOriginalMessage()){
			String content = "";
			for (String s : output.getTextMessages()){
				if (!s.isEmpty()){
					content += s+"\n";
				}
			}
			originalMessage.editMessage(
				new MessageEditBuilder()
				.setContent(content)
				.setAttachments(output.getFiles().stream().map(f->AttachedFile.fromData(f)).toList())
				.setEmbeds(output.getEmbeds())
				.build()).queue();
		}
		// Schedule the message sending if a delay is specified.
		else if (output.getDelayMillis() > 0) {
			scheduler.schedule(() -> sendActualOutput(output, channel, originalMessage),
							   output.getDelayMillis(), TimeUnit.MILLISECONDS);
		} else {
			sendActualOutput(output, channel, originalMessage);
		}
	}
	private static void sendActualOutput(CommandOutput output, MessageChannel channel, Message originalMessage) {
		// Send text messages first
		if (!output.getTextMessages().isEmpty()) {
			List<String> messagesToSend = new ArrayList<>();
			// Iterate through all text messages provided by the command and trim/split them
			for (String text : output.getTextMessages()) {
				messagesToSend.addAll(trimMessage(text));
			}
			// Pass the postSendActions to the recursive helper
			recursive_send_text(messagesToSend.iterator(), originalMessage, channel, output.shouldReplyToSender(), output.getPostSendActions());
		}

		// Then send embeds
		if (!output.getEmbeds().isEmpty()) {
			for (MessageEmbed embed : output.getEmbeds()) {
				MessageCreateAction sendAction = channel.sendMessageEmbeds(embed);
				if (output.shouldReplyToSender() && originalMessage != null) {
					sendAction.setMessageReference(originalMessage);
				}
				// Handle ephemeral if this was a slash command interaction and output.isEphemeral() is true
				// Note: For actual slash commands, you'd use event.deferReply().setEphemeral(true).queue()
				// or event.getHook().sendMessage(...).setEphemeral(true).queue()
				// This example focuses on MessageChannel.sendMessage for simplicity.
				sendAction.queue(
					sentMessage -> { // Execute post-send actions for embeds too
						for (Consumer<Message> action : output.getPostSendActions()) {
							action.accept(sentMessage);
						}
					},
					failure -> System.err.println("Failed to send embed: " + failure.getMessage()) // Log failure
				);
			}
		}
		// TODO: Add logic for sending files, components, etc., if needed in CommandOutput
		if (!output.getFiles().isEmpty()){
			MessageCreateAction sendAction = channel.sendFiles(output.getFiles().stream().map(f -> FileUpload.fromData(f)).toList());
			if (output.shouldReplyToSender() && originalMessage != null) {
				sendAction.setMessageReference(originalMessage);
			}
			// Handle ephemeral if this was a slash command interaction and output.isEphemeral() is true
			// Note: For actual slash commands, you'd use event.deferReply().setEphemeral(true).queue()
			// or event.getHook().sendMessage(...).setEphemeral(true).queue()
			// This example focuses on MessageChannel.sendMessage for simplicity.
			sendAction.queue(
				sentMessage -> { // Execute post-send actions for embeds too
					for (Consumer<Message> action : output.getPostSendActions()) {
						action.accept(sentMessage);
					}
				},
				failure -> System.err.println("Failed to send embed: " + failure.getMessage()) // Log failure
			);
		}
	}
	private static void recursive_send_text(Iterator<String> iter, Message originalMessage, MessageChannel channel, boolean shouldReply, List<Consumer<Message>> postSendActions) {
		if (iter.hasNext()){
			String part = iter.next();
			MessageCreateAction sendAction = channel.sendMessage(part);

			if (shouldReply && originalMessage != null) {
				sendAction.setMessageReference(originalMessage);
			}

			sendAction.queue(
				sentMessage -> {
					// Execute post-send actions for *this* successfully sent message part
					for (Consumer<Message> action : postSendActions) {
						action.accept(sentMessage);
					}
					// Recursively send the next part
					recursive_send_text(iter, originalMessage, channel, shouldReply, postSendActions);
				},
				failure -> {
					System.err.println("Failed to send message part: " + failure.getMessage());
					// Decide whether to continue sending remaining parts on failure of one.
					// For now, it will stop sending if one part fails.
				}
			);
		}
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
