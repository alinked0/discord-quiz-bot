package com.linked.quizbot.core.viewers;

import java.util.List;
import java.util.ArrayList;
import java.util.NoSuchElementException;
import java.util.function.Consumer;

import com.linked.quizbot.Constants;
import com.linked.quizbot.commands.CommandOutput;
import com.linked.quizbot.core.BotCore;
import com.linked.quizbot.utils.Question;
import com.linked.quizbot.utils.QuestionList;

import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.channel.middleman.MessageChannel;
import net.dv8tion.jda.api.entities.emoji.Emoji;
import net.dv8tion.jda.api.utils.TimeFormat;

/**
 * A stateful component responsible for displaying and navigating through a
 * {@link com.linked.quizbot.utils.QuestionList} for a single user.
 * <p>
 * The {@code Viewer} class manages the current state of a viewing session, including the active question,
 * and handles the logic for moving between questions. It is designed to work with both reaction-based
 * and button-based navigation, adapting its behavior based on the `useButtons` flag.
 * It also handles the updating of messages in a Discord channel to create a
 * dynamic, single-message quiz viewing experience.
 * </p>
 * @author alinked0
 * @version 1.0
 * @since 2025-02-01
 */
public class Viewer {
	private final QuestionList questions;
	private boolean active= false;
	private boolean sendInOriginalMessage= true;
	private final boolean useButtons;
	private int currIndex;
	private Message message = null;

	/**
     * Constructs a Viewer with a specified question list and button preference.
     *
     * @param l The QuestionList to be viewed.
     * @param useButtons Whether to use Discord buttons for navigation.
     */
	public Viewer(QuestionList l, boolean useButtons){
		this.questions = l;
		this.useButtons = useButtons;
	}

	/**
     * Constructs a Viewer with a specified question list, defaulting to using buttons.
     *
     * @param l The QuestionList to be viewed.
     */
	public Viewer(QuestionList l){this(l, true);}

	/**
     * Sets the JDA message associated with this viewer.
     *
     * @param m The message to associate.
     */
	public void setMessage(Message m) { message = m;}

	/**
     * Retrieves the current JDA message.
     *
     * @return The message object.
     */
	public Message getMessage() { return message;}

	/**
     * Checks if the viewer is configured to use buttons.
     *
     * @return true if buttons are enabled, false otherwise.
     */
	public boolean useButtons(){return useButtons;}

	 /**
     * Gets the ID of the current message.
     *
     * @return The message ID, or null if no message is set.
     */
	public String getMessageId() { return getMessage()!=null?getMessage().getId(): null;}

	/**
     * Gets the channel of the current message.
     *
     * @return The message channel, or null if no message is set.
     */
	public MessageChannel getChannel() { return getMessage()!=null?getMessage().getChannel(): null;}

	/**
     * Gets the ID of the channel of the current message.
     *
     * @return The channel ID, or null if no channel is set.
     */
	public String getChannelId() { return getChannel()!=null?getChannel().getId(): null;}

	/**
     * Checks if the viewer session is active.
     *
     * @return true if the session is active, false otherwise.
     */
	public Boolean isActive() { return active;}

	/**
     * A placeholder method for adding a reaction (not fully implemented in this class).
     *
     * @param userId The ID of the user who reacted.
     * @param emoji The emoji added.
     */
	public void addReaction(String userId, Emoji emoji){};

	/**
     * A placeholder method for removing a reaction (not fully implemented in this class).
     *
     * @param userId The ID of the user who removed the reaction.
     * @param emoji The emoji removed.
     */
	public void removeReaction(String userId, Emoji emoji){};

	/**
     * Returns a Consumer to be executed after the initial message is sent.
     * This consumer updates the bot's internal state with the new message ID.
     *
     * @return a Consumer for message post-processing.
     */
	public Consumer<Message> postSendActionStart(){
		return msg ->{
			String oldId = msg.getId();
			this.setMessage(msg);
			BotCore.viewerByMessageId.put(oldId, this);
		};
	}

	/**
     * Returns a Consumer to be executed after a message update.
     * This consumer replaces the old message ID with the new one in the bot's state.
     *
     * @return a Consumer for message post-processing.
     */
	public Consumer<Message> postSendActionCurrent(){
		return msg ->{
			String oldId = msg.getId();
			this.setMessage(msg);
			BotCore.viewerByMessageId.remove(oldId);
			BotCore.viewerByMessageId.put(msg.getId(), this);
		};
	}

	/** Placeholder method for additional processing on start. */
	public void inBetweenProccessorStart(){}

	/**
     * Starts the viewer session.
     * <p>
     * Initializes the viewer state and returns the first {@link CommandOutput} containing the list's header.
     *
     * @return A CommandOutput containing the initial message.
     */
	public CommandOutput start() {
		active = true;
		currIndex = -1;
		inBetweenProccessorStart();
		CommandOutput.Builder output = new CommandOutput.Builder();
		return output.sendInOriginalMessage(false)
			.add(getHeader())
			.useButtons(useButtons())
			.addReactions(getReactions())
			.addPostSendAction(postSendActionStart())
			.build();
	}

	/**
     * Gets the current question object.
     *
     * @return The current {@link Question} or null if the viewer is inactive or at the end of the list.
     */
	public Question getCurrQuestion() {
		if (!isActive() || getCurrentIndex()>=questions.size()) { return null;}
		return questions.get(getCurrentIndex());
	}

	/**
     * Gets the entire question list.
     *
     * @return The {@link QuestionList} object.
     */
	public QuestionList getQuestionList(){return questions;}

	/**
     * Gets the index of the currently displayed question.
     *
     * @return The current index.
     */
	public int getCurrentIndex() { return currIndex;}

	/**
     * Returns a list of emojis to be used as reactions for navigation.
     *
     * @return A list of {@link Emoji}s.
     */
	public List<Emoji> getReactions(){
		List<Emoji> emojis = new ArrayList<>();
		if (hasPrevious())emojis.add(Emoji.fromFormatted(Constants.EMOJIPREVQUESTION));
		emojis.add(Emoji.fromFormatted(hasNext()?Constants.EMOJINEXTQUESTION:Constants.EMOJISTOP));
		return emojis;
	}

	/**
     * Navigates to the next question in the list.
     *
     * @return A CommandOutput for the next question.
     * @throws NoSuchElementException if there is no next question.
     */
	public CommandOutput next() {
		if (!hasNext()) {
			throw new NoSuchElementException();
		}
		currIndex++;
		return current();
	}

	/**
     * Navigates to the previous question in the list.
     *
     * @return A CommandOutput for the previous question.
     * @throws NoSuchElementException if there is no previous question.
     */
	public CommandOutput previous(){
		if (!hasPrevious()) {
			throw new NoSuchElementException();
		}
		currIndex--;
		if (getCurrentIndex() == -1){
			return start();
		}
		return current();
	}

	/**
     * Gets the header text for the question list.
     *
     * @return The header string.
     */
	public String getHeader(){
		return String.format("%s**Author:** <@%s>\n**Date created:** %s\n", questions.header(),
			questions.getAuthorId(), TimeFormat.DATE_TIME_LONG.atTimestamp(questions.getTimeCreatedMillis()));
	}

	/**
     * Gets the formatted text for the current question.
     *
     * @return The formatted question string.
     */
	public String getFormatedQuestion(){
		return questions.getFormatedCorrection(currIndex);
	}

	/** Placeholder method for additional processing on moving to the current question. */
	public void inBetweenProccessorCurrent(){}

	/**
     * Sets whether to send updates in the original message.
     *
     * @param b The boolean value to set.
     */
	public void setSendInOriginalMessage(boolean b){ sendInOriginalMessage=b;}

	/**
     * Generates a CommandOutput for the current state of the viewer.
     * <p>
     * This method handles both the initial header display and subsequent question displays,
     * including reaction and button updates.
     *
     * @return A CommandOutput containing the formatted message for the current state.
     * @throws NoSuchElementException if the current index is out of bounds.
     */
	public CommandOutput current(){
		if (getCurrentIndex() >= questions.size()) {
			throw new NoSuchElementException();
		}
		CommandOutput.Builder output = new CommandOutput.Builder();
		if (!isActive()) { return output.build();}
		if (currIndex == -1){
			output.add(getHeader());
		} else {
			output.add(getFormatedQuestion());
			if (!BotCore.isBugFree())output.add(String.format("```txt\n%s\n```\n",getFormatedQuestion()));
		}
		inBetweenProccessorCurrent();
		return output.sendInOriginalMessage(sendInOriginalMessage)
			.clearReactions(true)
			.setMessage(message)
			.useButtons(useButtons())
			.addReactions(getReactions())
			.addPostSendAction(postSendActionCurrent())
			.build();
	}

	/**
     * Checks if there is a next question to view.
     *
     * @return true if a next question exists, false otherwise.
     */
	public boolean hasNext(){ return getCurrentIndex()+1 < questions.size();}

	/**
     * Checks if there is a previous question to view.
     *
     * @return true if a previous question exists, false otherwise.
     */
	public boolean hasPrevious(){ return -1 <= getCurrentIndex()-1;}

	/** Ends the viewer session by setting the active flag to false. */
	public void end() {active = false;}
}
