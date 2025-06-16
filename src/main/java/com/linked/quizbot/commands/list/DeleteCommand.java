package com.linked.quizbot.commands.list;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import com.linked.quizbot.Constants;
import com.linked.quizbot.commands.BotCommand;
import com.linked.quizbot.core.BotCore;
import com.linked.quizbot.commands.CommandCategory;
import com.linked.quizbot.utils.QuestionList;
import com.linked.quizbot.utils.Users;

import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.channel.middleman.MessageChannel;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import net.dv8tion.jda.api.requests.restaction.MessageCreateAction;

/**
 * The {@code DeleteCommand} class allows users to delete a specific list of questions 
 * from their collection. It extends {@link BotCommand} and requires the user to confirm 
 * the deletion by reacting with a designated emoji.
 *
 * <h2>Usage:</h2>
 * <ul>
 *     <li>Typing {@code /delete [index-of-theme] [index-of-list]} initiates the deletion process.</li>
 *     <li>The bot prompts the user for confirmation via an emoji reaction.</li>
 *     <li>Deletion is only finalized after confirmation.</li>
 * </ul>
 *
 * <h2>Features:</h2>
 * <ul>
 *     <li>Deletes a list of questions from a user’s collection.</li>
 *     <li>Uses indexes from the {@code !c} (collection) command to identify the list.</li>
 *     <li>Requires confirmation before deletion to prevent accidental loss.</li>
 *     <li>Utilizes {@link BotCore#comfirmDeletion} to handle the confirmation process.</li>
 * </ul>
 *
 * <h2>Example:</h2>
 * <pre>
 * // Request to delete a specific question list
 * /delete 2 3
 * 
 * // The bot will prompt: "Are you sure you want to delete: [List Name]?"
 * // User reacts with the delete emoji to confirm.
 * </pre>
 *
 * @author alinked
 * @version 1.0
 * @see BotCommand
 * @see Users
 * @see QuestionList
 * @see BotCore#comfirmDeletion
 */
public class DeleteCommand extends BotCommand{
    public static final String CMDNAME = "delete";
    private String cmdDesrciption = "deleting a list of questions";
	private String[] abbrevs = new String[]{"del"};
    
	@Override
	public String[] getAbbreviations(){ return abbrevs;}
	@Override
	public CommandCategory getCategory(){
		return CommandCategory.EDITING;
	}
	@Override
    public String getName(){ return CMDNAME;}
	@Override
    public String getDescription(){ return cmdDesrciption;}
    
	@Override
    public List<OptionData> getOptionData(){
        List<OptionData> res = new ArrayList<OptionData>();
        res.add(BotCommand.getCommandByName(StartCommand.CMDNAME).getOptionData().get(0).setRequired(true));
        return res;
    }
	@Override
    public void execute(User sender, Message message, MessageChannel channel, String[] args){
        
        String res,ownerId; 
        MessageCreateAction send;
        QuestionList l;
        Consumer<Message> success;
        if (args.length<getOptionData().size()){
            BotCommand.getCommandByName(HelpCommand.CMDNAME).execute(sender, message, channel, new String[]{getName()});
            return;
        }
        
        l = getSelectedQuestionList(args[0]);
        ownerId = l.getAuthorId(); 
        if (ownerId.equals(sender.getId())){
            res = "Are you sure you want to delete :\n\""+l.getName()+"\"?";
            success = msg ->{
                msg.addReaction(Constants.EMOJIDEL).queue();
                BotCore.comfirmDeletion(msg, l);
            };
        }else {
            res = "You are not the owner of the list:\n\""+l.getName()+"\"";
            success = null;
        }
        send = channel.sendMessage(res);
        if(message!=null){send.setMessageReference(message);}
        send.queue(success);
    }

}
