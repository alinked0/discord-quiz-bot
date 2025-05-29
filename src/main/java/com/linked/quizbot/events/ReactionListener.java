package com.linked.quizbot.events;

import com.linked.quizbot.Constants;
import com.linked.quizbot.core.BotCore;
import com.linked.quizbot.core.QuizBot;
import com.linked.quizbot.utils.QuestionList;
import com.linked.quizbot.commands.BotCommand;

import net.dv8tion.jda.api.hooks.ListenerAdapter;
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
        Emoji reaction = f.getEmoji();
        MessageChannel channel = event.getChannel();
        event.getChannel().retrieveMessageById(event.getMessageId()).queue(message -> {
            BotCommand cmd ;
            if (reaction.equals(Constants.EMOJIMORETIME)){
                cmd = BotCommand.getCommandByName("moretime");
                cmd.execute(sender, message, channel, new String[0]);
                System.out.println("  $> "+cmd.getName());
                return;
            }
            if(reaction.equals(Constants.EMOJINEXTQUESTION)){
                cmd = BotCommand.getCommandByName("next");
                cmd.execute(sender, message, channel, new String[0]);
                System.out.println("  $> "+cmd.getName());
                return;
            }
            if(reaction.equals(Constants.EMOJIPREVQUESTION)){
                cmd = BotCommand.getCommandByName("previous");
                cmd.execute(sender, message, channel, new String[0]);
                System.out.println("  $> "+cmd.getName());
                return;
            }
            if(reaction.equals(Constants.EMOJIEXPLICATION)){
                BotCore.explicationRequest(sender, channel, message.getId());
                return;
            }
            QuizBot currQuizBot = BotCore.getCurrQuizBot(channel);
            if (currQuizBot!=null) {
                if (currQuizBot.isActive() && event.getMessageIdLong() == currQuizBot.getQuizMessage().getIdLong()) {
                    BotCore.updateUserScoreAddReaction(sender, currQuizBot, reaction);
                    return;
                }
            }
            String messageId = event.getMessageId();
            QuestionList l = BotCore.toBeDeleted.get(messageId);
            //System.out.println("  $> "+reaction+"?="+Constants.EMOJIDEL +" list="+ l);
            if (l!=null && reaction.equals(Constants.EMOJIDEL)) {
                BotCore.deleteCollection(l, messageId);
                return;
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