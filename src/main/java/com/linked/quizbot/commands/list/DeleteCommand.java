package com.linked.quizbot.commands.list;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import com.linked.quizbot.Constants;
import com.linked.quizbot.commands.BotCommand;
import com.linked.quizbot.core.BotCore;
import com.linked.quizbot.core.QuizBot;
import com.linked.quizbot.commands.CommandCategory;
import com.linked.quizbot.utils.QuestionList;
import com.linked.quizbot.utils.UserLists;

import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.channel.middleman.MessageChannel;
import net.dv8tion.jda.api.entities.emoji.Emoji;
import net.dv8tion.jda.api.interactions.commands.OptionType;
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
 * @see UserLists
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
        List<OptionData> res = new ArrayList<>();
        res.add(new OptionData(OptionType.INTEGER, "index-of-theme", "the theme index given by !c"));
        res.add(new OptionData(OptionType.INTEGER, "index-of-list", "the list index given by !c"));
        return res;
    }
	@Override
    public void execute(User sender, Message message, MessageChannel channel, String[] args){
        
        String res; 
        int n = args.length;
        if (n<2){
            BotCommand.getCommandByName(HelpCommand.CMDNAME).execute(sender, message, channel, new String[]{getName()});
            return;
        }
        int fst = Integer.parseInt(args[0]);
        int snd = Integer.parseInt(args[1]);
        UserLists userLists = new UserLists(sender.getId());
		String theme = userLists.getAllThemes().get(fst>0?fst-1:0);
        QuestionList l = userLists.getListsByTheme(theme).get(snd>0?snd-1:0);
        l.setAuthorId(userLists.getUserId());
        res = "Are you sure you want to delete :\n\""+l.getName()+"\"?";
        MessageCreateAction send;
        send = channel.sendMessage(res);
        if(message!=null){send.setMessageReference(message);}
        send.queue(msg ->{
            msg.addReaction(Constants.EMOJIDEL).queue();
            BotCore.comfirmDeletion(msg, l);
        }
        );
    }

}
