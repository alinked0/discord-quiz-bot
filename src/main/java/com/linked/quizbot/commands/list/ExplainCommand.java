package com.linked.quizbot.commands.list;

import java.util.List;
import java.util.ArrayList;

import com.linked.quizbot.commands.CommandOutput;
import com.linked.quizbot.commands.BotCommand;
import com.linked.quizbot.core.BotCore;
import com.linked.quizbot.core.viewers.Explain;
import com.linked.quizbot.core.viewers.QuizBot;
import com.linked.quizbot.utils.Users;

import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;

/**
 * The {@code ExplainCommand} class provides functionality to explain the scoring for the current question
 * in an ongoing game or all questions if no game is active. It extends {@link BotCommand} and is part of
 * a Discord bot that manages quiz games.
 * <p>
 * This command allows users to understand how their scores are calculated based on their answers.
 * It can be used during an active game or to review past questions when no game is currently running.
 * </p>
 * @author alinked0
 * @version 1.0
 * @since 2025-02-01
 * @see BotCommand
 */
public class ExplainCommand extends BotCommand {
	public static final String CMDNAME = "explain";
	private String cmdDesrciption = "explaining the scoring for, the current question if a game is ongoing, or all question if not.";
	private List<String> abbrevs = List.of("expl");
	
	@Override
	public List<String> getAbbreviations(){ return abbrevs;}
	@Override
	public BotCommand.CommandCategory getCategory(){
		return BotCommand.CommandCategory.GAME;
	}
	@Override
	public String getName(){ return CMDNAME;}
	@Override
	public String getDescription(){ return cmdDesrciption;}
	@Override
	public List<OptionData> getOptionData(){
		List<OptionData> res = new ArrayList<>();
		res.add (new OptionData(OptionType.STRING, "messageid", "id of the message to explain", false));
		return res;
	}
	@Override
	public CommandOutput execute(String userId,  List<String> args){
		if (args.size() < getRequiredOptionData().size()){
			return BotCommand.getCommandByName(HelpCommand.CMDNAME).execute(userId, List.of(getName()));
		}
		String messageId = args.get(0);
		QuizBot q =(QuizBot) BotCore.getViewer(messageId);
		if (q == null) {
			return BotCommand.getCommandByName(HelpCommand.CMDNAME).execute(userId, List.of(getName()));
		}
		q.addPlayer(userId);
		q.isExplaining(true);
		CommandOutput.Builder output = new CommandOutput.Builder();
		CommandOutput expl;
		int nbPlayers = q.getPlayers().size();
		Explain ex;
		if (q.isActive()) {
			ex = new Explain(q, userId, q.getCurrentIndex());
			expl = ex.current();
		}else {
			ex = new Explain(q, userId);
			expl = ex.start();
		}
		output.add(expl).add(q.getLastTimestamp().toString());
		if (nbPlayers>1){
			output.sendAsPrivateMessage(userId).sendInOriginalMessage(false);
		}
		return  output.build();
	}
}
