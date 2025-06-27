package com.linked.quizbot.commands.list;

import java.util.List;
import java.util.HashSet;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

import com.linked.quizbot.Constants;
import com.linked.quizbot.commands.BotCommand;
import com.linked.quizbot.commands.BotCommand.CommandCategory;
import com.linked.quizbot.commands.CommandOutput;
import com.linked.quizbot.core.BotCore;
import com.linked.quizbot.core.QuizBot;

import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.channel.middleman.MessageChannel;
import net.dv8tion.jda.api.requests.restaction.MessageCreateAction;

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
	public CommandOutput execute(String userId, String channelId, List<String> args, boolean reply){
		QuizBot q = BotCore.getCurrQuizBot(channelId);
		if (q == null){
			q = BotCore.getPrevQuizBot(channelId);
		}
		if(q == null) {
			return BotCommand.getCommandByName(HelpCommand.CMDNAME).execute(userId, channelId, List.of(getName()), reply);
		}
		CommandOutput.Builder outputBuilder = new CommandOutput.Builder().reply(reply);

		List<String> lb = q.leaderBoard();
		outputBuilder.addAllTextMessage(lb);
		Consumer<Message> leaderboardPostSendAction = sentMessage -> {
			BotCore.explicationRequestByChannel.putIfAbsent(channelId, new HashSet<>());
			BotCore.explicationRequestByChannel.get(channelId).add(sentMessage.getId());

			// Add the reaction
			sentMessage.addReaction(Constants.EMOJIEXPLICATION).queue(
				reactionSuccess -> {
					// Schedule clearing of reactions after a delay
					sentMessage.clearReactions().queueAfter(Constants.READTIMEMIN, TimeUnit.MINUTES,
						clearSuccess -> {
							// Remove message ID from the tracking set after reactions are cleared
							BotCore.explicationRequestByChannel.get(channelId).remove(sentMessage.getId());
						},
						clearFailure -> {
							System.err.println("Failed to clear reactions for message " + sentMessage.getId() + ": " + clearFailure.getMessage());
							BotCore.explicationRequestByChannel.get(channelId).remove(sentMessage.getId()); // Still try to remove from tracking
						}
					);
				},
				reactionFailure -> {
					System.err.println("Failed to add reaction to message " + sentMessage.getId() + ": " + reactionFailure.getMessage());
					BotCore.explicationRequestByChannel.get(channelId).remove(sentMessage.getId()); // Clean up if reaction fails to add
				}
			);
		};
		outputBuilder.addPostSendAction(leaderboardPostSendAction);
		
		// Build and return the CommandOutput
		return outputBuilder.build();
	}
}
