package com.linked.quizbot.core;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;

import com.linked.quizbot.Constants;
import com.linked.quizbot.commands.CommandOutput;
import com.linked.quizbot.utils.Question;
import com.linked.quizbot.utils.QuestionList;

import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.emoji.Emoji;

public class Viewer {
    public QuestionList questions;
    public boolean active= false;
    public int currIndex;
    public Message message = null;

    public Viewer(QuestionList l){this.questions = l;}
    public void setMessage(Message m) { message = m;}
    public Message getMessage() { return message;}
    public String getMessageId() { return getMessage().getId();}
    public Boolean isActive() { return active;}
	public CommandOutput start() {
		active = true;
		currIndex = -1;
		CommandOutput.Builder outputBuilder = new CommandOutput.Builder();
		return outputBuilder.sendInOriginalMessage(false)
			.addTextMessage(questions.header())
			.addPostSendAction(msg ->{
                message = msg;
                BotCore.messageIdByViewer.put(getMessageId(), this);
                addReactions(msg, getButtons().iterator());
			}).build();
	}
    public Question getCurrQuestion() {
        if (!isActive() || getCurrentIndex()>=questions.size()) { return null;}
        return questions.get(getCurrentIndex());
    }
    public QuestionList getQuestionList(){return questions;}
    public int getCurrentIndex() { return currIndex;}
	public List<Emoji> getButtons(){
		List<Emoji> emojis = new ArrayList<>();
        emojis.add(hasPrevious()?Constants.EMOJIPREVQUESTION:Constants.EMOJIWHITESQUARE);
        emojis.add(hasNext()?Constants.EMOJINEXTQUESTION:Constants.EMOJIWHITESQUARE);
		return emojis;
	}
	private static void addReactions(Message message, Iterator<Emoji> iter) {
		if(iter.hasNext()){
			message.addReaction(iter.next()).queue( msg -> addReactions(message, iter));
        }
    }
    public CommandOutput next() {
		if (!hasNext()) {
            throw new NoSuchElementException();
        }
		currIndex++;
        return current();
    }
    public CommandOutput previous(){
		if (!hasPrevious()) {
            throw new NoSuchElementException();
        }
        currIndex--;
        return current();
    }
    public CommandOutput current(){
		if (getCurrentIndex() >= questions.size()) {
            throw new NoSuchElementException();
        }
		CommandOutput.Builder outputBuilder = new CommandOutput.Builder();
        if (!isActive()) { return outputBuilder.build();}
        if (currIndex == -1){
			outputBuilder.addTextMessage(questions.header());
        } else {
            outputBuilder.addTextMessage(questions.getFormatedWithAwnsers(currIndex));
        }
		return outputBuilder.sendInOriginalMessage(true)
            .setMessage(message)
			.addPostSendAction(msg ->{
                msg.clearReactions().queue(none -> {
                    BotCore.messageIdByViewer.put(getMessageId(), this);
                    addReactions(msg, getButtons().iterator());
                });
			}).build();
    }
    public boolean hasNext(){ return getCurrentIndex()+1 < questions.size();}
    public boolean hasPrevious(){ return -1 <= getCurrentIndex()-1;}
    public void end() {active = false;}
}
