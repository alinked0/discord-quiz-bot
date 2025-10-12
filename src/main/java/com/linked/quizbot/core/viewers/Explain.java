package com.linked.quizbot.core.viewers;

import java.util.List;
import java.util.ArrayList;
import java.util.Set;
import java.util.function.Consumer;

import com.linked.quizbot.Constants;
import com.linked.quizbot.core.BotCore;
import com.linked.quizbot.utils.Option;
import com.linked.quizbot.utils.QuestionList;

import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.emoji.Emoji;

/**
 * A specialized extension of {@link Viewer} that provides a detailed walkthrough of a quiz.
 * <p>
 * This class is designed to show a specific user their answers, the correct answers,
 * and a summary of their score for a completed or active quiz. Unlike the base {@code Viewer},
 * it does not allow for new answers to be submitted. It focuses purely on correction and review.
 * This component can be initiated after a quiz is finished or can be started from an ongoing
 * {@link QuizBot} session to provide immediate feedback on past questions.
 * </p>
 *
 * @author alinked0
 * @version 1.0
 * @since 2025-02-01
 * @see Viewer
 * @see QuizBot
 */
public class Explain extends Viewer{
	private List<Set<Option>> userAwnsersByQuestionIndex;
	private double points;
	private String userId;
	private boolean fromActiveQuiz = false;
	
	/**
	 * Constructs an Explain viewer for a given quiz with the user's recorded answers and score.
	 * @param l The {@link QuestionList} for the quiz.
	 * @param userAwnsersByQuestionIndex A list containing the user's answers for each question.
	 * @param userId The ID of the user whose answers are being viewed.
	 * @param points The user's score.
	 * @param useButtons If true, uses buttons for navigation; otherwise, uses reactions.
	 */
	public Explain(QuestionList l, List<Set<Option>> userAwnsersByQuestionIndex, String userId, double points, boolean useButtons){
		super(l, useButtons);
		this.userAwnsersByQuestionIndex = userAwnsersByQuestionIndex;
		this.points = points;
		this.userId = userId;
	}
	
	/**
	 * Constructs an Explain viewer directly from a live {@link QuizBot} session.
	 * @param view The active QuizBot instance.
	 * @param userId The ID of the user to explain the quiz to.
	 */
	public Explain(QuizBot view, String userId){
		this(view.getQuestionList(), view.getAwsersByQuestion(userId), userId, view.getUserScore(userId), view.useButtons());
	}

	/**
	 * Constructs an Explain viewer from an active {@link QuizBot} and starts it at a specific index.
	 * @param view The active QuizBot instance.
	 * @param userId The ID of the user to explain the quiz to.
	 * @param currIndex The question index to start the explanation from.
	 */
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
			String uName = BotCore.getEffectiveNameFromId(userId);
			if (uName == userId){
				uName = String.format("<@%s>", uName);
			}
			header += String.format("For %s **`%s/%d`**\n",uName, points, getQuestionList().size());
		}
		header+= super.getHeader();
		return header;
	}
	@Override
	public String getFormatedQuestion(){
		return getQuestionList().getFormatedCorrection(getCurrentIndex(), userAwnsersByQuestionIndex.get(getCurrentIndex()));
	}
	@Override
	public List<Emoji> getReactions(){
		List<Emoji> emojis = new ArrayList<>();
		if (hasPrevious()) emojis.add(Emoji.fromFormatted(Constants.EMOJIPREVQUESTION));
		if (hasNext()) emojis.add(Emoji.fromFormatted(Constants.EMOJINEXTQUESTION));
		return emojis;
	}
	@Override
	public Consumer<Message> postSendActionCurrent(){
		if (!fromActiveQuiz){
			return msg ->{
				String oldId = this.getMessageId();
				this.setMessage(msg);
				BotCore.viewerByMessageId.remove(oldId);
				BotCore.viewerByMessageId.put(msg.getId(), this);
			};
		}
		return  msg ->{};
	}
}
