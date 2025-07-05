package com.linked.quizbot.events;

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
import com.linked.quizbot.commands.list.ExplainCommand;
import com.linked.quizbot.commands.list.MoreTimeCommand;
import com.linked.quizbot.commands.list.NextCommand;
import com.linked.quizbot.commands.list.PreviousCommand;

import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.utils.Timestamp;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.channel.middleman.MessageChannel;
import net.dv8tion.jda.api.entities.emoji.Emoji;
import net.dv8tion.jda.api.entities.channel.ChannelType;
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
        //System.out.println(" $> + "+event.getEmoji().getName()+" "+event.getEmoji().getAsReactionCode()+" "+event.getEmoji().asUnicode());
        //System.out.println("  $> Emoji added = :"+event.getEmoji().asUnicode()+":");
        //event.getChannel().sendMessage(":"+event.getEmoji().getAsReactionCode()+":");
        //System.out.println(" $> "+event.getEmoji());
        //event.getChannel().sendMessage("Test");
        //channel.sen
        // log User
        BotCore.addUser(sender);
        String userId = sender.getId();
        MessageChannel channel = event.getChannel();
        String channelId = channel.getId();
        Emoji reaction = f.getEmoji();
        String messageId = event.getMessageId();
        event.getChannel().retrieveMessageById(messageId).queue(message -> {
            BotCommand cmd ;
            if (reaction.equals(Constants.EMOJIMORETIME)){
                cmd = BotCommand.getCommandByName(MoreTimeCommand.CMDNAME);
                MessageSender.sendCommandOutput(
                    cmd.execute(userId, channelId, List.of(), false),
                    channel,
                    message 
                );
                System.out.println("  $> "+cmd.getName());
                return;
            }
            if(reaction.equals(Constants.EMOJINEXTQUESTION)){
                cmd = BotCommand.getCommandByName(NextCommand.CMDNAME);
                MessageSender.sendCommandOutput(
                    cmd.execute(userId, channelId, List.of(message.getId()), false),
                    channel,
                    message 
                );
                System.out.println("  $> "+cmd.getName());
                return;
            }
            if(reaction.equals(Constants.EMOJIPREVQUESTION)){
                cmd = BotCommand.getCommandByName(PreviousCommand.CMDNAME);
                MessageSender.sendCommandOutput(
                    cmd.execute(userId, channelId, List.of(message.getId()), false),
                    channel,
                    message 
                );
                System.out.println("  $> "+cmd.getName());
                return;
            }
            if(reaction.equals(Constants.EMOJIEXPLICATION)){
                cmd = BotCommand.getCommandByName(ExplainCommand.CMDNAME);
                MessageSender.sendCommandOutput(
                    cmd.execute(userId, channelId, List.of(message.getId()), false),
                    channel,
                    message 
                );
                System.out.println("  $> "+cmd.getName());
                return;
            }
            QuestionList l = BotCore.toBeDeleted.get(messageId);
            if (l!=null && reaction.equals(Constants.EMOJIDEL)) {
                BotCore.deleteList(l, messageId);
                return;
            }
            QuizBot currQuizBot = BotCore.getCurrQuizBot(channel);
            if (currQuizBot!=null) {
                if (currQuizBot.isActive() && event.getMessageIdLong() == currQuizBot.getMessage().getIdLong()) {
                    if (currQuizBot.getButtonsForOptions().contains(reaction)){
                        BotCore.updateUserScoreAddReaction(userId, currQuizBot, reaction);
                        if (currQuizBot.getDelaySec()>0 && currQuizBot.awnsersByUserByQuestion.get(currQuizBot.getCurrQuestion()).size()==1){
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
                            if(!currQuizBot.explainWasTrigerred() && oldQ.equals(currQuizBot.getCurrQuestion()) && oldT.equals(oldT)){
                                cmd = BotCommand.getCommandByName(NextCommand.CMDNAME);
                                MessageSender.sendCommandOutput(
                                    cmd.execute(userId, channelId, List.of(message.getId()), false),
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
        });
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
        ////System.out.println(" $> - "+event.getEmoji().getName()+" "+event.getEmoji().getAsReactionCode()+" "+event.getEmoji().asUnicode());
        //System.out.println("  $> Emoji removed = "+event.getEmoji().asUnicode());
    }
}