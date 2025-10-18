package com.linked.quizbot.commands.list;

import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

import com.linked.quizbot.Constants;
import com.linked.quizbot.commands.BotCommand;
import com.linked.quizbot.commands.CommandOutput;
import com.linked.quizbot.core.BotCore;
import com.linked.quizbot.core.viewers.QuizBot;

import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.emoji.Emoji;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;

/**
 * The {@code LeaderBoardCommand} class provides functionality to display the leaderboard of the last played game.
 * It extends {@link BotCommand} and is part of a Discord bot that manages quiz games.
 * <p>
 * This command allows users to view the leaderboard, which shows the scores of players from the most recent game.
 * </p>
 * @author alinked0
 * @version 1.0
 * @since 2025-02-01
 * @see BotCommand
 */
public class LeaderBoardCommand extends BotCommand {
	public static final String CMDNAME = "leaderboard";
	private String cmdDesrciption = "displaying the leaderboard of the last played game.";
	private List<String> abbrevs = List.of("lb");
	
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
		return BotCommand.getCommandByName(PreviousCommand.CMDNAME).getOptionData();
	}
	@Override
	public CommandOutput execute(String userId,  List<String> args){
		if (args.size() < getRequiredOptionData().size()){
			return BotCommand.getCommandByName(HelpCommand.CMDNAME).execute(userId, List.of(getName()));
		}
		String messageId = args.get(0);
		QuizBot q =(QuizBot) BotCore.getViewer(messageId);
		if(q == null) {
			return BotCommand.getCommandByName(HelpCommand.CMDNAME).execute(userId, List.of(getName()));
		}
		CommandOutput.Builder outputBuilder = new CommandOutput.Builder();
		List<String> lb = q.leaderBoard();
		outputBuilder.addAll(lb);
		Consumer<Message> leaderboardPostSendAction = sentMessage -> {
			BotCore.explicationRequest.add(sentMessage.getId());
			// Schedule clearing of reactions after a delay
			if (sentMessage.isFromGuild())
			sentMessage.clearReactions().queueAfter(Constants.READTIMEMIN, TimeUnit.MINUTES,
				clearSuccess -> {
					// Remove message ID from the tracking set after reactions are cleared
					BotCore.explicationRequest.remove(sentMessage.getId());
				},
				clearFailure -> {
					System.err.println(Constants.ERROR + "Failed to clear reactions for message " + sentMessage.getId() + ": " + clearFailure.getMessage());
					BotCore.explicationRequest.remove(sentMessage.getId()); // Still try to remove from tracking
				}
			);
		};
		
		// Build and return the CommandOutput
		return outputBuilder
			.clearReactions(true)
			.addReaction(Emoji.fromFormatted(Constants.EMOJIEXPLICATION))
			.addPostSendAction(leaderboardPostSendAction)
			.sendInOriginalMessage(true)
			.useButtons(q.useButtons())
		.build();
	}
}
