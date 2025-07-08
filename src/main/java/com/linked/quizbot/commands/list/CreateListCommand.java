package com.linked.quizbot.commands.list;

import java.util.List;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.linked.quizbot.Constants;
import com.linked.quizbot.commands.BotCommand;
import com.linked.quizbot.commands.BotCommand.CommandCategory;
import com.linked.quizbot.commands.CommandOutput;
import com.linked.quizbot.utils.QuestionList;
import com.linked.quizbot.utils.QuestionListHash;
import com.linked.quizbot.utils.QuestionListParser;
import com.linked.quizbot.utils.Users;

import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;

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
 *     <li>Stores the list persistently using {@link Users}.</li>
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
 * @see Users
 */
public class CreateListCommand extends BotCommand {
    public static final String CMDNAME = "createlist";
    private String cmdDesrciption = "adding a list of questions to a user's lists";
	private List<String> abbrevs = List.of("create","cl");
    
	public List<String> getAbbreviations(){ return abbrevs;}
	@Override
	public BotCommand.CommandCategory getCategory(){
		return BotCommand.CommandCategory.EDITING;
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
    public CommandOutput execute(String userId,  List<String> args){
        int n = args.size();
        List<String> res = new ArrayList<>();
        if (n<=0) {
            return BotCommand.getCommandByName(HelpCommand.CMDNAME).execute(userId, List.of(getName()));
        }
        String s;
        for (int i = 0; i<n; i++) {
            try {
                QuestionList l = QuestionListParser.fromString(args.get(i));
                if (l!=null) {
                    l.setAuthorId(userId);
                    if (l.getName()!=null) {
                        if(Users.getUserListQuestions(userId).contains(l)) {
                            res.add("Failed, list of name : \""+l.getName()+"\" already exists.\n");
                        } else {
                            String id = QuestionListHash.generate(l);
                            l.setId(id);
                            Users.addListToUser(l.getAuthorId(), l);
                            res.add("Success, list of name : \""+l.getName()+"\", has been created, \nuse `"+Constants.CMDPREFIXE+ViewCommand.CMDNAME+" "+id+"` command to verife.\n");
                        }
                    }else {
                        res.add("Failed to import ```"+args.get(i)+"```, no \"name\" found\n");
                    }
                }
            }catch (IOException e){
                res.add("Failed to import ```js\n"+args.get(i)+"```, reason unknown\n");
            }
        }
		return new CommandOutput.Builder()
				.addAllTextMessage(res)
				
				.build();
    }
    @Override
	public List<String> parseArguments(String cmndLineArgs){
		List<String> res = new ArrayList<>();
        res.addAll(splitJson(cmndLineArgs));
		return res;
	}
}
