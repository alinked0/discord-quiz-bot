package com.linked.quizbot.events;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import com.linked.quizbot.Constants;
import com.linked.quizbot.core.BotCore;
import com.linked.quizbot.core.MessageSender;
import com.linked.quizbot.core.QuizBot;
import com.linked.quizbot.core.Viewer;
import com.linked.quizbot.utils.Question;
import com.linked.quizbot.utils.QuestionList;
import com.linked.quizbot.commands.BotCommand;
import com.linked.quizbot.commands.CommandOutput;
import com.linked.quizbot.commands.list.EndCommand;
import com.linked.quizbot.commands.list.ExplainCommand;
import com.linked.quizbot.commands.list.MoreTimeCommand;
import com.linked.quizbot.commands.list.NextCommand;
import com.linked.quizbot.commands.list.PreviousCommand;

import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.components.ActionRow;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import net.dv8tion.jda.api.interactions.components.buttons.ButtonStyle;
import net.dv8tion.jda.api.utils.Timestamp;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.channel.middleman.MessageChannel;
import net.dv8tion.jda.api.entities.emoji.Emoji;
import net.dv8tion.jda.api.entities.channel.ChannelType;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.events.message.react.GenericMessageReactionEvent;
import net.dv8tion.jda.api.events.message.react.MessageReactionAddEvent;
import net.dv8tion.jda.api.events.message.react.MessageReactionRemoveEvent;

/**
 * The ReactionListener class is a specialized {@link ListenerAdapter} class, 
 * designed to keep track of reactions that are added or removed on any message that was sent by the bot. 
 * The messages are tracked through a list of {@link QuizBot} found in the {@link BotCore} class; 
 * every {@link QuizBot} class contains the message on which the current game is being played.
 */
public class ButtonListener extends ListenerAdapter {
    private void handleExplain(String userId, Message message, Emoji reaction, ButtonInteractionEvent event)
    {
        String messageId = message.getId();
        MessageChannel channel = message.getChannel();
        QuizBot currQuizBot = (QuizBot)BotCore.getViewer(messageId);
        if (currQuizBot!=null) {
            if (currQuizBot.isActive() && message.getIdLong() == currQuizBot.getMessage().getIdLong()) {
                if (currQuizBot.getButtons().contains(reaction)){
                    currQuizBot.addReaction(userId, reaction);
                    if (currQuizBot.getDelaySec()>0 && currQuizBot.awnsersByUserIdByQuestionIndex.get(currQuizBot.getCurrentIndex()).size()==1){
                        CommandOutput out = currQuizBot.current();
                        MessageSender.sendCommandOutput(
                            new CommandOutput.Builder().addCommandOutput(out).sendInOriginalMessage(true).build(),
                            event
                        );
                        Question oldQ = currQuizBot.getCurrQuestion();
                        Timestamp oldT = currQuizBot.getLastTimestamp();
                        try{
                            TimeUnit.SECONDS.sleep(currQuizBot.getDelaySec());
                        }catch (Exception e){
                            e.printStackTrace();
                        }
                        BotCommand cmd;
                        if(!currQuizBot.explainWasTrigerred() && oldQ.equals(currQuizBot.getCurrQuestion()) && oldT.equals(oldT)){
                            cmd = BotCommand.getCommandByName(NextCommand.CMDNAME);
                            MessageSender.sendCommandOutput(
                                cmd.execute(userId, List.of(messageId)),
                                channel,
                                message 
                            );
                        }
                        return;
                    }else{
                        currQuizBot.setExplainTriger(false);
                        event.editButton(event.getButton()).queue();
                    }
                }
            }
        }
    }

    @Override
    public void onButtonInteraction(ButtonInteractionEvent event) {
		long start = System.nanoTime();
        User sender = event.getUser();
        if (sender == null || sender.isBot()) {
            return;
        }
        MessageChannel channel = event.getChannel();
        if (channel.getType().isGuild() || channel.getType().isThread()){
            if (!Constants.canIRunThisHere(event.getGuild().getId())){
                return;
            }
        } else if (!channel.getType().isGuild()){
            if (Constants.AREWETESTING && !Constants.AUTHORID.equals(sender.getId())){
                return;
            }
        }
        String componentId = event.getComponentId(); // The ID you assigned to the buttonBotCore.addUser(sender);
        String userId = sender.getId();
        String channelId = channel.getId();
        String messageId = event.getMessageId();
        Message message = event.getMessage();
        BotCommand cmd = BotCommand.getCommandByName(componentId);
        if(cmd!=null){
            if (!Constants.isBugFree()) System.out.printf("  $> "+cmd.getName());
            CommandOutput.Builder output = new CommandOutput.Builder()
            .addCommandOutput(cmd.execute(userId, List.of(messageId)));
            MessageSender.sendCommandOutput(
                output.build(),
                event
            );
            if (!Constants.isBugFree()) System.out.printf("   $> time = `%.3f ms`\n", (System.nanoTime() - start) / 1000000.00);
            return;
        }
        handleExplain(userId, message, Emoji.fromFormatted(event.getButton().getLabel()), event);
    }
    public static BotCommand getCommandFromEmoji(Emoji reaction){
        if (reaction.equals(Constants.EMOJIMORETIME)){
            return BotCommand.getCommandByName(MoreTimeCommand.CMDNAME);
        }
        if (reaction.equals(Constants.EMOJISTOP)){
            return BotCommand.getCommandByName(EndCommand.CMDNAME);
        }
        if(reaction.equals(Constants.EMOJINEXTQUESTION)){
            return BotCommand.getCommandByName(NextCommand.CMDNAME);
        }
        if(reaction.equals(Constants.EMOJIPREVQUESTION)){
            return BotCommand.getCommandByName(PreviousCommand.CMDNAME);
        }
        if(reaction.equals(Constants.EMOJIEXPLICATION)){
            return BotCommand.getCommandByName(ExplainCommand.CMDNAME);
        }
        return null;
    }
}