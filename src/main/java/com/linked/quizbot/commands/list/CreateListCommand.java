package com.linked.quizbot.commands.list;

import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import com.linked.quizbot.Constants;
import com.linked.quizbot.commands.BotCommand;
import com.linked.quizbot.commands.CommandCategory;
import com.linked.quizbot.utils.QuestionList;
import com.linked.quizbot.utils.QuestionListParser;
import com.linked.quizbot.utils.UserLists;

import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.channel.middleman.MessageChannel;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import net.dv8tion.jda.api.requests.restaction.MessageCreateAction;

/**
 * The {@code CreateListCommand} class allows users to create a list of questions to their personal lists.
 * It extends {@link BotCommand} and takes a JSON-formatted list as an argument.
 *
 * <p>Once executed, the command processes the provided JSON string, stores it temporarily,
 * and attempts to create a {@link QuestionList}. If successful, the list is added to the user's stored lists.</p>
 *
 * <h2>Usage:</h2>
 * <ul>
 *     <li>Typing {@code /createlist [json]} adds a list of questions from a JSON-formatted input.</li>
 * </ul>
 *
 * <h2>Features:</h2>
 * <ul>
 *     <li>Validates the provided JSON structure to ensure it contains a "name" and "theme".</li>
 *     <li>Assigns an author ID to the ID of the sender.</li>
 *     <li>Stores the list persistently using {@link UserLists}.</li>
 *     <li>Removes temporary files after processing.</li>
 *     <li>Provides feedback to the user on success or failure.</li>
 * </ul>
 *
 * <h2>Example:</h2>
 * <pre>
 * // creating a list of questions (JSON format required)
 * /createlist {"name": "Math Quiz", "theme": "Mathematics", "questions": [...]}
 * </pre>
 *
 * @author alinked0
 * @version 1.0
 * @see BotCommand
 * @see QuestionList
 * @see UserLists
 */
public class CreateListCommand extends BotCommand {
    public static final String CMDNAME = "createlist";
    private String cmdDesrciption = "adding a list of questions to a user's lists";
	private String[] abbrevs = new String[]{"create","cl"};
    
	public String[] getAbbreviations(){ return abbrevs;}
	@Override
	public CommandCategory getCategory(){
		return CommandCategory.EDITING;
	}
    public String getName(){ return CMDNAME;}
    public String getDescription(){ return cmdDesrciption;}
    public String getDetailedExamples(){ 
        String s = "```js \n"+Constants.CMDPREFIXE+getName()+" "+QuestionList.getExampleQuestionList()+"\n```";
        return s;
    }
	@Override
    public List<OptionData> getOptionData(){
        List<OptionData> res = new ArrayList<>();
        res.add(new OptionData(OptionType.STRING, "json", "question list written as a json"));
        res.add(new OptionData(OptionType.ATTACHMENT, "jsonfile", "question list written as a json"));
        return res;
    }
    public void execute(User sender, Message message, MessageChannel channel, String[] args){
		// Répondre avec le temps de latence
        int n = args.length;
        String res = "";
        String userId = sender.getId().replace("[a-zA-Z]", "");
        if (n<=0) {
            BotCommand.getCommandByName(HelpCommand.CMDNAME).execute(sender, message, channel, new String[]{getName()});
            return;
        }
        for (int i = 0; i<n; i++) {
            QuestionList l = QuestionListParser.stringToQuestionList(args[i]);
            if (l!=null) {
                if (l.getAuthorId()==null) {
                    l.setAuthorId(userId);
                }
                if (l.getName()!=null && l.getTheme()!=null) {
                    if(UserLists.getUserListQuestions(userId).contains(l)) {
                        res = "Failed, list of name : \""+l.getName()+"\" and theme : \""+l.getTheme()+"\" already exists.";
                    } else {
                        UserLists.addListToUser(l.getAuthorId(), l);
                        String index = UserLists.getCodeForIndexQuestionList(l);
                        res = "Success, list has been created, use ```"+Constants.CMDPREFIXE+ViewCommand.CMDNAME+" "+l.getAuthorId()+" "+index+"``` command to verife.\n" +res;
                    }
                }else {
                    res = "Failed, no \"name\" or \"theme\" found\n" + res;
                }
            }
        }
        MessageCreateAction send;
        send = channel.sendMessage(res);
        if(message!=null){send.setMessageReference(message);}
        send.queue(msg -> msg.delete().queueAfter(Constants.READTIMEMIN, TimeUnit.MINUTES));
    }
}
