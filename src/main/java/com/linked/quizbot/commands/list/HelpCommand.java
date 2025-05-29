package com.linked.quizbot.commands.list;

import java.util.ArrayList;
import java.util.List;
import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import com.linked.quizbot.Constants;
import com.linked.quizbot.commands.BotCommand;
import com.linked.quizbot.commands.CommandCategory;
import com.linked.quizbot.core.BotCore;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.channel.middleman.MessageChannel;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import net.dv8tion.jda.api.requests.restaction.MessageCreateAction;

/**
 * The {@code HelpCommand} class provides a help command for users to understand the available bot commands.
 * It extends {@link BotCommand} and allows users to retrieve either a list of all available commands
 * or detailed information about a specific command.
 *
 * <p>The command supports an optional argument that specifies a command name. If no argument is provided,
 * it returns a list of all commands. If an argument is provided, it returns details about that specific command.</p>
 *
 * <h2>Usage:</h2>
 * <ul>
 *     <li>Typing {@code /help} displays a list of all available commands.</li>
 *     <li>Typing {@code /help [command_name]} provides details about the specified command.</li>
 * </ul>
 *
 * <h2>Features:</h2>
 * <ul>
 *     <li>Supports abbreviations for easier command access (e.g., {@code h} for help).</li>
 *     <li>Uses Discord's EmbedBuilder for a structured and visually appealing message format.</li>
 *     <li>Automatically deletes the response after a predefined time to keep channels clean.</li>
 * </ul>
 *
 * <h2>Example:</h2>
 * <pre>
 * // Display all commands
 * /help
 *
 * // Get detailed information about a specific command
 * /help start
 * </pre>
 *
 * @author alinked0
 * @version 1.0
 * @see BotCommand
 * @see BotCore
 * @see EmbedBuilder
 */
public class HelpCommand extends BotCommand {
    public static final String CMDNAME = "help";
    private String cmdDesrciption = "helping people understands the commands.";
	private String[] abbrevs = new String[]{"h"};
	
    @Override
	public String[] getAbbreviations(){ return abbrevs;}
    @Override
    public String getName(){ return CMDNAME;}
    @Override
    public String getDescription(){ return cmdDesrciption;}
    @Override
    public List<OptionData> getOptionData(){
		List<OptionData> res = new ArrayList<>();
        res.add (new OptionData(OptionType.STRING, "command", "Command name for detailed description", true));
        return res;
    }
	@Override
    public void execute(User sender, Message message, MessageChannel channel, String[] args){
		ArrayList<EmbedBuilder> res = new ArrayList<>();
		boolean allAtOnce = args.length < 1;
		BotCommand cmd;
		Set<BotCommand> commands = BotCommand.getCommands();
		if (allAtOnce) {
			EmbedBuilder embed = new EmbedBuilder();
			embed.setTitle("All Commands");
			embed.setDescription("Type `"+Constants.CMDPREFIXE+getName()+"` followed by a command name to see more details about that particular command.")
			.setTimestamp(java.time.Instant.now());
			for (CommandCategory c : CommandCategory.getCategories()){
				String s = "";
				Set<BotCommand> cmds = BotCommand.getCommandsByCategory(c);
				if(cmds!=null && !cmds.isEmpty()){
					Iterator<BotCommand> iter = cmds.iterator();
					while (iter.hasNext()){
						cmd=iter.next();
						s += "`"+cmd.getName()+"`";
						if (iter.hasNext()) {
							s+= ", ";
						}
					}
				}
				embed.addField(c.toString(), s, true);
			}
			res.add(embed);
		} else {
			cmd = null;
			for (int i = 0; i<args.length; i++) {
				cmd = null;
				for (BotCommand c: commands){
					if (c.getName().equals(args[i])) {
						cmd = c;
						break;
					} 
					for (int j=0; j<c.getAbbreviations().length; j++){ 
						if (c.getAbbreviations()[j].equals(args[i])){
							cmd = c;
							break;
						}
					}
					if (cmd!=null) {break;}
				}	
				if (cmd != null) {
					EmbedBuilder embed = new EmbedBuilder();
					embed.setTitle("Command Info: `"+cmd.getName()+"`");
					int n = cmd.getAbbreviations().length;
					String s = "";
					for (int j=0; j<n; j++){ 
						s+="`"+cmd.getAbbreviations()[j]+"`";
						if (j+1<n) {
							s+= ", ";
						}
					}
					embed.addField("Shortcuts: ", s, false);
					s = "`"+Constants.CMDPREFIXE+cmd.getName()+"`";
					for (OptionData opt : cmd.getOptionData()) {
						s += " `<"+opt.getName()+">`";
					}
					s+="\n";
					embed.addField("Usage", s+cmd.getDescription(), false);
					embed.addField("examples", cmd.getDetailedExamples(), false);
					res.add(embed);
				}
			}
		}
		for (EmbedBuilder k : res) {
			MessageCreateAction send;
			send = channel.sendMessageEmbeds(k.build());
			if(message!=null){send.setMessageReference(message);}
			send.queue(msg -> msg.delete().queueAfter(Constants.READTIMEMIN, TimeUnit.MINUTES));
		}
    }
}    
