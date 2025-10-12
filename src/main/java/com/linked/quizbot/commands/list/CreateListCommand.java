package com.linked.quizbot.commands.list;

import java.util.List;

import java.io.IOException;
import java.util.ArrayList;

import com.linked.quizbot.Constants;
import com.linked.quizbot.commands.BotCommand;
import com.linked.quizbot.commands.CommandOutput;
import com.linked.quizbot.utils.QuestionList;
import com.linked.quizbot.utils.Users;

import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;

/**
 * The {@code CreateListCommand} class allows users to create a list of questions to their personal collection.
 * It extends {@link BotCommand} and takes a JSON-formatted list as an argument.
 *
 * @author alinked0
 * @version 1.0
 * @since 2025-02-01
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
	@Override
	public List<String> parseArguments(String cmndLineArgs){
		List<String> res = new ArrayList<>();
		res.addAll(splitJson(cmndLineArgs));
		return res;
	}
	public CommandOutput execute(String userId,  List<String> args){
		if (args.size()<getRequiredOptionData().size()){
			return BotCommand.getCommandByName(HelpCommand.CMDNAME).execute(userId, List.of(getName()));
		}
		int n = args.size();
		List<String> res = new ArrayList<>();
		for (int i = 0; i<n; i++) {
			try {
				QuestionList l = QuestionList.Parser.fromString(args.get(i)).authorId(userId).build();
				if (l.getName()!=null) {
					if(Users.get(userId).getByName(l.getName())!=null) {
						res.add("Failed, list of name : "+Constants.MAPPER.writeValueAsString(""+l.getName()+"")+" already exists in your collection.\n");
					} else {
						Users.addListToUser(l.getAuthorId(), l);
						res.add("Success, list of name : "+Constants.MAPPER.writeValueAsString(""+l.getName()+"")+", has been created, \nuse `"+Constants.CMDPREFIXE+ViewCommand.CMDNAME+" "+l.getId()+"` command to verife.\n");
					}
				}else {
					res.add("Failed to import ```"+args.get(i)+"```, no "+Constants.MAPPER.writeValueAsString("name")+" found\n");
				}
			}catch (IOException e){
				res.add(String.format("**Failed to import** ```js\n%s```\n",args.get(i) ));
				res.add(String.format("**Reasons:** %s\n", e.getMessage()));
				e.printStackTrace();
			}
		}
		return new CommandOutput.Builder()
				.addAll(res)
				.build();
	}
}
