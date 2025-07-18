package com.linked.quizbot.core;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import com.linked.quizbot.Constants;
import com.linked.quizbot.utils.Option;
import com.linked.quizbot.utils.QuestionList;

import net.dv8tion.jda.api.entities.emoji.Emoji;

public class Explain extends Viewer{
    private List<Set<Option>> userAwnsersByQuestionIndex;
    private double points;
    private String userId;
    
    public Explain(QuestionList l, List<Set<Option>> userAwnsersByQuestionIndex, String userId, double points, boolean useButtons, boolean replyToSender){
        super(l.clone(), useButtons, replyToSender);
        this.userAwnsersByQuestionIndex = userAwnsersByQuestionIndex;
        this.points = points;
        this.userId = userId;
    }
    public Explain(QuizBot view, String userId){
        this(view.getQuestionList(), view.getAwsersByQuestion(userId), userId, view.getUserScore(userId), view.useButtons(), true);
    }
    public Explain(QuizBot view, String userId, int currIndex){
        this(view.getQuestionList(), view.getAwsersByQuestion(userId), userId, view.getUserScore(userId), view.useButtons(), view.replyToSender());
        this.start();
        int index = view.userAnswersForCurrQuestion.size()>0?currIndex:currIndex-1;
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
	public List<Emoji> getButtons(){
		List<Emoji> emojis = new ArrayList<>();
        if (hasPrevious()) emojis.add(Constants.EMOJIPREVQUESTION);
        if (hasNext()) emojis.add(Constants.EMOJINEXTQUESTION);
		return emojis;
	}
}
