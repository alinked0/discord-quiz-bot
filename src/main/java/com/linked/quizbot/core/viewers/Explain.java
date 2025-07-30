package com.linked.quizbot.core.viewers;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;

import com.linked.quizbot.Constants;
import com.linked.quizbot.core.BotCore;
import com.linked.quizbot.utils.Option;
import com.linked.quizbot.utils.QuestionList;

import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.emoji.Emoji;

public class Explain extends Viewer{
    private List<Set<Option>> userAwnsersByQuestionIndex;
    private double points;
    private String userId;
    private boolean fromActiveQuiz = false;
    
    public Explain(QuestionList l, List<Set<Option>> userAwnsersByQuestionIndex, String userId, double points, boolean useButtons){
        super(l.clone(), useButtons);
        this.userAwnsersByQuestionIndex = userAwnsersByQuestionIndex;
        this.points = points;
        this.userId = userId;
    }
    public Explain(QuizBot view, String userId){
        this(view.getQuestionList(), view.getAwsersByQuestion(userId), userId, view.getUserScore(userId), view.useButtons());
    }
    public Explain(QuizBot view, String userId, int currIndex){
        this(view.getQuestionList(), view.getAwsersByQuestion(userId), userId, view.getUserScore(userId), view.useButtons());
        this.fromActiveQuiz = true;
        this.start();
        int index = view.useAutoNext()?currIndex-1:currIndex;
        while (getCurrentIndex()<index) {
            this.next();
        }
    }
    @Override
    public String getHeader(){
        String header = "";
        if (userAwnsersByQuestionIndex!=null){
            header += String.format("For %s **`%s/%d`**\n", BotCore.getEffectiveNameFromId(userId), points, getQuestionList().size());
        }
        header+= getQuestionList().header();
        return header;
    }
    @Override
    public String getFormatedQuestion(){
        return getQuestionList().getFormatedCorrection(getCurrentIndex(), userAwnsersByQuestionIndex.get(getCurrentIndex())).getSecond();
    }
    @Override
	public List<Emoji> getReactions(){
		List<Emoji> emojis = new ArrayList<>();
        if (hasPrevious()) emojis.add(Constants.EMOJIPREVQUESTION);
        if (hasNext()) emojis.add(Constants.EMOJINEXTQUESTION);
		return emojis;
	}
    @Override
	public Consumer<Message> postSendActionCurrent(){
        if (!fromActiveQuiz){
            return msg ->{
                BotCore.viewerByMessageId.remove(getMessageId());
                BotCore.viewerByMessageId.put(msg.getId(), this);
                BotCore.viewerByMessageId.get(msg.getId()).setMessage(msg);
            };
        }
        return  msg ->{};
	}
}
