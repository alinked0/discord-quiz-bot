package com.linked.quizbot.commands.list;

import java.util.List;

import java.util.ArrayList;
import java.util.List;
import java.util.Iterator;
import java.util.Set;

import com.linked.quizbot.Constants;
import com.linked.quizbot.commands.BotCommand;
import com.linked.quizbot.commands.BotCommand.CommandCategory;
import com.linked.quizbot.commands.CommandOutput;
import com.linked.quizbot.core.BotCore;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;

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
	private List<String> abbrevs = List.of("h");
	
    @Override
	public List<String> getAbbreviations(){ return abbrevs;}
    @Override
    public String getName(){ return CMDNAME;}
    @Override
    public String getDescription(){ return cmdDesrciption;}
    @Override
    public List<OptionData> getOptionData(){
		List<OptionData> res = new ArrayList<>();
        res.add (new OptionData(OptionType.STRING, "command", "Command name for detailed description", false));
        return res;
    }
	@Override
    public CommandOutput execute(String userId, String channelId, List<String> args, boolean reply){
		boolean newbie = args.size() < 1;
		BotCommand cmd;
		CommandOutput.Builder outBuilder = new CommandOutput.Builder();
		if (newbie) {
			outBuilder.sendAsPrivateMessage(userId);
			outBuilder.addEmbed(getEmbed());
		} else {
			for (String s : args) {
				cmd = getCommandByName(s);
				if (cmd != null) {
					outBuilder.addEmbed(getEmbed( cmd));
				}
			}
		}
		return outBuilder.build();
    }
	private static EmbedBuilder getEmbed(BotCommand cmd){
		EmbedBuilder embed = new EmbedBuilder();
		embed.setTitle("Command Info: `"+cmd.getName()+"`");
		int n = cmd.getAbbreviations().size();
		String s = "";
		for (int j=0; j<n; j++){ 
			s+="`"+cmd.getAbbreviations().get(j)+"`";
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
		return embed;
	}
	private static EmbedBuilder getEmbed(){
		BotCommand cmd;
		EmbedBuilder embed = new EmbedBuilder();
		embed.setTitle("All Commands");
		embed.setDescription("Type `"+Constants.CMDPREFIXE+HelpCommand.CMDNAME+"` followed by a command name to see more details about that particular command.")
		.setTimestamp(java.time.Instant.now());
		for (BotCommand.CommandCategory c : BotCommand.CommandCategory.getCategories()){
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
		return embed;
	}
}    
