package com.linked.quizbot.core.viewers;

import java.util.List;
import java.util.Map;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.NoSuchElementException;
import java.util.function.Consumer;

import org.apache.commons.collections4.map.HashedMap;

import com.linked.quizbot.Constants;
import com.linked.quizbot.commands.Output;
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
	public static Map<String, Map<String, Integer>> questionIndexByMessageId = new HashMap<>();
	private boolean active= false;
	private boolean sendInOriginalMessage= true;
	private boolean allAtOnce= false;
	private final boolean useButtons;
	private int currIndex;
	private Message message = null;
	private String id;
	
	/**
	 * Constructs a Viewer with a specified question list and button preference.
	 *
	 * @param l The QuestionList to be viewed.
	 * @param useButtons Whether to use Discord buttons for navigation.
	 */
	public Viewer(QuestionList l, boolean useButtons, boolean allAtOnce){
		this.questions = l;
		this.useButtons = useButtons;
		this.allAtOnce = allAtOnce;
		this.sendInOriginalMessage=!allAtOnce && sendInOriginalMessage;
		this.id = ""+Instant.now().toEpochMilli();
	}
		
	/**
	 * Constructs a Viewer with a specified question list, defaulting to using buttons.
	 *
	 * @param l The QuestionList to be viewed.
	 */
	public Viewer(QuestionList l){this(l, true, false);}
	
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

	public String getId() { return id;}

	
	/**
	 * Checks if the viewer is configured to use buttons.
	 *
	 * @return true if buttons are enabled, false otherwise.
	 */
	public boolean useButtons(){return useButtons;}

	public void allAtOnce(boolean b){allAtOnce=b;}
	public boolean allAtOnce(){return allAtOnce;}
	
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

	public Integer getIndexFromMessage(Message message){
		if (Viewer.questionIndexByMessageId.get(getId())==null){
			return null;
		}
		return Viewer.questionIndexByMessageId.get(getId()).get(message.getId());
	}

	public Question get(int index) {
		return getQuestionList().get(index);
	}
	
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
	public void addReaction(String userId, Emoji emoji, Message origin){};
	
	/**
	 * A placeholder method for removing a reaction (not fully implemented in this class).
	 *
	 * @param userId The ID of the user who removed the reaction.
	 * @param emoji The emoji removed.
	 */
	public void removeReaction(String userId, Emoji emoji){};
	public void removeReaction(String userId, Emoji emoji, Message origin){};
	
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
			Map<String, Integer> n = new HashMap<>();
			n.put(msg.getId(), -1);
			Viewer.questionIndexByMessageId.put(getId(), n);
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
		final int index = this.getCurrentIndex();
		return msg ->{
			this.setMessage(msg);
			Map<String, Integer> n = Viewer.questionIndexByMessageId.getOrDefault(getId(), new HashMap<>());
			n.put(msg.getId(), index);
			Viewer.questionIndexByMessageId.put(getId(), n);
			BotCore.viewerByMessageId.put(msg.getId(), this);
		};
	}
	
	/** Placeholder method for additional processing on start. */
	public void inBetweenProccessorStart(){}

	public Output getOutput(Message message){
		int index = getIndexFromMessage(message);
		return getOutput(index);
	}

	public Output getOutput(int index){
		if (index >= getQuestionList().size()) {
			throw new NoSuchElementException();
		}
		Output.Builder output = new Output.Builder();
		if (!isActive()) { return output.build();}
		String content;
		if (currIndex == -1){
			content = getHeader();
		} else {
			content = getFormatedQuestion(index);
		}
		output.add(content);
		return output.sendInOriginalMessage(sendInOriginalMessage)
			.clearReactions(true)
			.useButtons(useButtons())
			.addReactions(getReactions(index))
			.build();
	}
	
	/**
	 * Starts the viewer session.
	 * <p>
	 * Initializes the viewer state and returns the first {@link Output} containing the list's header.
	 *
	 * @return A Output containing the initial message.
	 */
	public Output start() {
		active = true;
		currIndex = -1;
		inBetweenProccessorStart();
		Output.Builder output = new Output.Builder();
		return output.sendInOriginalMessage(false)
			.add(getHeader())
			.useButtons(useButtons())
			.addReactions(getReactions())
			.addPostSendAction(postSendActionStart())
			.sendInThread(allAtOnce())
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
		return getReactions(getCurrentIndex());
	}
	public List<Emoji> getReactions(int index){
		List<Emoji> emojis = new ArrayList<>();
		if (allAtOnce && !hasNext(index)){
			emojis.add(Emoji.fromFormatted(Constants.EMOJISTOP));
			return emojis;
		}else {
			if (hasPrevious(index)){
				emojis.add(Emoji.fromFormatted(Constants.EMOJIPREVQUESTION));
			}
			if (hasNext(index)) {
				emojis.add(Emoji.fromFormatted(Constants.EMOJINEXTQUESTION));
			} else {
				emojis.add(Emoji.fromFormatted(Constants.EMOJISTOP));
			}
		}
		if (index<0) {
			emojis.add(Emoji.fromFormatted(Constants.EMOJIFASTDOWN));
		}
		return emojis;
	}
	
	/**
	 * Navigates to the next question in the list.
	 *
	 * @return A Output for the next question.
	 * @throws NoSuchElementException if there is no next question.
	 */
	public Output next() {
		if (!hasNext()) {
			throw new NoSuchElementException();
		}
		currIndex++;
		return current();
	}
	
	/**
	 * Navigates to the previous question in the list.
	 *
	 * @return A Output for the previous question.
	 * @throws NoSuchElementException if there is no previous question.
	 */
	public Output previous(){
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
		return String.format("%s\n**Author:** <@%s>\n**Date created:** %s\n", questions.header(),
			questions.getOwnerId(), TimeFormat.DATE_TIME_LONG.atTimestamp(questions.getTimeCreatedMillis()));
	}
	
	/**
	 * Gets the formatted text for the current question.
	 *
	 * @return The formatted question string.
	 */
	public String getFormatedQuestion(){
		return getFormatedQuestion(getCurrentIndex());
	}
	public String getFormatedQuestion(int index){
		return questions.getFormatedCorrection(index);
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
	 * Generates a Output for the current state of the viewer.
	 * <p>
	 * This method handles both the initial header display and subsequent question displays,
	 * including reaction and button updates.
	 *
	 * @return A Output containing the formatted message for the current state.
	 * @throws NoSuchElementException if the current index is out of bounds.
	 */
	public Output current(){
		if (getCurrentIndex() >= questions.size()) {
			throw new NoSuchElementException();
		}
		Output.Builder output = new Output.Builder();
		if (!isActive()) { return output.build();}
		String content;
		if (currIndex == -1){
			content = getHeader();
		} else {
			content = getFormatedQuestion();
		}
		output.add(content);
		inBetweenProccessorCurrent();
		return output.sendInOriginalMessage(sendInOriginalMessage)
			.clearReactions(true)
			.setMessage(message)
			.useButtons(useButtons())
			.addReactions(getReactions())
			.addPostSendAction(postSendActionCurrent())
			.sendInThread(allAtOnce())
			.build();
	}
	
	/**
	 * Checks if there is a next question to view.
	 *
	 * @return true if a next question exists, false otherwise.
	 */
	public boolean hasNext(){ return hasNext(getCurrentIndex());}
	public boolean hasNext(int index){ return index+1 < questions.size();}
	
	/**
	 * Checks if there is a previous question to view.
	 *
	 * @return true if a previous question exists, false otherwise.
	 */
	public boolean hasPrevious(){ return hasPrevious(getCurrentIndex());}
	public boolean hasPrevious(int index){ return -1 < index-1;}
	
	/** Ends the viewer session by setting the active flag to false. */
	public void end() {active = false;}
}
