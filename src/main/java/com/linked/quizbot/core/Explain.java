package com.linked.quizbot.core;

import java.util.List;
import java.util.Set;

import com.linked.quizbot.utils.Option;
import com.linked.quizbot.utils.QuestionList;

public class Explain extends Viewer{
    private List<Set<Option>> userAwnsersByQuestionIndex;
    private double points;
    private String userId;
    
    public Explain(QuestionList l, List<Set<Option>> userAwnsersByQuestionIndex, String userId, double points){
        super(l);
        this.userAwnsersByQuestionIndex = userAwnsersByQuestionIndex;
        this.points = points;
        this.userId = userId;
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
}
