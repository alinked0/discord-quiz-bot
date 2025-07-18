package com.linked.quizbot.core;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.function.Consumer;

import com.linked.quizbot.Constants;
import com.linked.quizbot.commands.CommandOutput;
import com.linked.quizbot.utils.Question;
import com.linked.quizbot.utils.QuestionList;

import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.channel.middleman.MessageChannel;
import net.dv8tion.jda.api.entities.emoji.Emoji;

public class Viewer {
    private final QuestionList questions;
    private boolean active= false;
    private boolean sendInOriginalMessage= true;
    private int currIndex;
    private Message message = null;

    public Viewer(QuestionList l){this.questions = l;}
    public void setMessage(Message m) { message = m;}
    public Message getMessage() { return message;}
    public String getMessageId() { return getMessage()!=null?getMessage().getId(): null;}
    public MessageChannel getChannel() { return getMessage()!=null?getMessage().getChannel(): null;}
    public String getChannelId() { return getChannel()!=null?getChannel().getId(): null;}
    public Boolean isActive() { return active;}
    public void addReaction(String userId, Emoji emoji){};
    public void removeReaction(String userId, Emoji emoji){};
    public Consumer<Message> postSendActionStart(){
        return msg ->{
            BotCore.viewerByMessageId.put(msg.getId(), this);
            BotCore.viewerByMessageId.get(msg.getId()).setMessage(msg);
        };
    }
    public Consumer<Message> postSendActionCurrent(){
        return msg ->{
            BotCore.viewerByMessageId.remove(getMessageId());
            BotCore.viewerByMessageId.put(msg.getId(), this);
            BotCore.viewerByMessageId.get(msg.getId()).setMessage(msg);
        };
    }
    public void inBetweenProccessorStart(){}
    public CommandOutput start() {
		active = true;
		currIndex = -1;
        inBetweenProccessorStart();
		CommandOutput.Builder outputBuilder = new CommandOutput.Builder();
		return outputBuilder.sendInOriginalMessage(false)
			.addTextMessage(questions.header())
            .addReactions(getButtons())
			.addPostSendAction(postSendActionStart())
            .build();
	}
    public Question getCurrQuestion() {
        if (!isActive() || getCurrentIndex()>=questions.size()) { return null;}
        return questions.get(getCurrentIndex());
    }
    public QuestionList getQuestionList(){return questions;}
    public int getCurrentIndex() { return currIndex;}
	public List<Emoji> getButtons(){
		List<Emoji> emojis = new ArrayList<>();
        if (hasPrevious())emojis.add(Constants.EMOJIPREVQUESTION);
        emojis.add(hasNext()?Constants.EMOJINEXTQUESTION:Constants.EMOJISTOP);
		return emojis;
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
    public String getHeader(){
        return questions.header();
    }
    public String getFormatedQuestion(){
        return questions.getFormatedCorrection(currIndex);
    }
    public void inBetweenProccessorCurrent(){}
    public void setSendInOriginalMessage(boolean b){ sendInOriginalMessage=b;}
    public CommandOutput current(){
		if (getCurrentIndex() >= questions.size()) {
            throw new NoSuchElementException();
        }
		CommandOutput.Builder outputBuilder = new CommandOutput.Builder();
        if (!isActive()) { return outputBuilder.build();}
        if (currIndex == -1){
			outputBuilder.addTextMessage(getHeader());
        } else {
            outputBuilder.addTextMessage(getFormatedQuestion());
        }
        inBetweenProccessorCurrent();
		return outputBuilder.sendInOriginalMessage(sendInOriginalMessage)
            .clearReactions(true)
            .setMessage(message)
            .addReactions(getButtons())
			.addPostSendAction(postSendActionCurrent())
            .build();
    }
    public boolean hasNext(){ return getCurrentIndex()+1 < questions.size();}
    public boolean hasPrevious(){ return -1 <= getCurrentIndex()-1;}
    public void end() {active = false;}
}
