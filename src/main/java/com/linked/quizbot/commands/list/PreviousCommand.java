package com.linked.quizbot.commands.list;

import java.util.List;

import java.util.concurrent.TimeUnit;

import com.linked.quizbot.commands.BotCommand;
import com.linked.quizbot.commands.BotCommand.CommandCategory;
import com.linked.quizbot.commands.CommandOutput;
import com.linked.quizbot.core.BotCore;
import com.linked.quizbot.core.QuizBot;
import com.linked.quizbot.core.Viewer;

import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.channel.middleman.MessageChannel;

/**
 * The {@code PreviousCommand} class the previous question to get sent.
 *
 * @author alinked0
 * @version 1.0
 * @see BotCommand
 * @see QuizBot
 * @see BotCore
 */
public class PreviousCommand extends BotCommand {
    public static final String CMDNAME = "previous";
    private String cmdDesrciption = "triggering the previous question to get sent if a quiz is ongoing";
	private List<String> abbrevs = List.of("prev");

	public List<String> getAbbreviations(){ return abbrevs;}
	@Override
	public BotCommand.CommandCategory getCategory(){
		return BotCommand.CommandCategory.NAVIGATION;
	}
	@Override
    public String getName(){ return CMDNAME;}
	@Override
    public String getDescription(){ return cmdDesrciption;}
	@Override
    public CommandOutput execute(String userId, String channelId, List<String> args, boolean reply){
        if (args.size()>=1){
            Viewer viewer = BotCore.messageIdByViewer.get(args.get(0));
            if (viewer != null && viewer.hasPrevious()){
                return viewer.previous();
            }
        }
        QuizBot q = BotCore.getCurrQuizBot(channelId);
        if (q != null && q.isActive()){
            Message msg = q.getMessage();
			if (msg!=null){
				try {
					msg.delete().queueAfter(3, TimeUnit.SECONDS);
				} catch(Exception e) {
					e.printStackTrace();
				}
			}
            return q.previous();
        }
        return BotCommand.getCommandByName(HelpCommand.CMDNAME).execute(userId, channelId, List.of(getName()), reply);
    }
}
