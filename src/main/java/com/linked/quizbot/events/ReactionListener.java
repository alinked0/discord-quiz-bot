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
public class ReactionListener extends ListenerAdapter {

    @Override
    public void onMessageReactionAdd(MessageReactionAddEvent event){
		long start = System.nanoTime();
        User sender = event.getUser();
        GenericMessageReactionEvent f = (GenericMessageReactionEvent)event;
        if (sender == null || sender.isBot()) {
            return;
        }
        
        if (event.isFromGuild() || event.isFromThread()){
            if (!Constants.canIRunThisHere(event.getGuild().getId())){
                return;
            }
        } else if (event.isFromType(ChannelType.PRIVATE)){
            if (Constants.AREWETESTING && !Constants.AUTHORID.equals(sender.getId())){
                return;
            }
        }
        
        BotCore.addUser(sender);
        String userId = sender.getId();
        MessageChannel channel = event.getChannel();
        String channelId = channel.getId();
        Emoji reaction = f.getEmoji();
        String messageId = event.getMessageId();
        event.getChannel().retrieveMessageById(messageId).queue(message -> {
            BotCommand cmd = getCommandFromEmoji(reaction);
            if(cmd!=null){
                if (!Constants.isBugFree()) System.out.printf("  $> "+cmd.getName());
                MessageSender.sendCommandOutput(
                    cmd.execute(userId, List.of(messageId)),
                    channel,
                    message 
                );
		        if (!Constants.isBugFree()) System.out.printf("   $> time = `%.3f ms`\n", (System.nanoTime() - start) / 1000000.00);
                return;
            }
            QuestionList l = BotCore.toBeDeleted.get(messageId);
            if (l!=null && reaction.equals(Constants.EMOJIDEL)) {
                BotCore.deleteList(l, messageId);
                return;
            }
            handleExplain(userId, message, reaction);
        });
    }
    private void handleExplain(String userId, Message message, Emoji reaction){
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
                            channel,
                            message 
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
                    }
                }
            }
        }
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
    @Override
    public void onMessageReactionRemove(MessageReactionRemoveEvent event){
        User sender = event.getUser();
        GenericMessageReactionEvent f = (GenericMessageReactionEvent)event;
        if (sender == null || sender.isBot()) {
            return;
        }
        
        if (event.isFromGuild() || event.isFromThread()){
            if (!Constants.canIRunThisHere(event.getGuild().getId())){
                return;
            }
        } else if (event.isFromType(ChannelType.PRIVATE)){
            if (Constants.AREWETESTING && !Constants.AUTHORID.equals(sender.getId())){
                return;
            }
        }
        // TODO 
    }
}