package com.linked.quizbot.events;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import com.linked.quizbot.Constants;
import com.linked.quizbot.commands.BotCommand;
import com.linked.quizbot.commands.CommandOutput;
import com.linked.quizbot.core.BotCore;
import com.linked.quizbot.core.MessageSender;
import com.linked.quizbot.core.viewers.Explain;
import com.linked.quizbot.core.viewers.QuizBot;
import com.linked.quizbot.core.viewers.Viewer;
import com.linked.quizbot.utils.QuestionList;

import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.entities.emoji.Emoji;
import net.dv8tion.jda.api.events.guild.GuildReadyEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.commands.Command;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;


public class ReadyEventListener extends ListenerAdapter {
	@Override
	public void onReady(net.dv8tion.jda.api.events.session.ReadyEvent event){
		List<SlashCommandData> commandData = BotCommand.getSlashCommandDataList();
		
		// Clear all global commands
		event.getJDA().retrieveCommands().queue(commands -> {
			for (Command cmd : commands){
				event.getJDA().deleteCommandById(cmd.getId()).queue();
			}
		});
		
		// Re-register updated global commands
		event.getJDA().updateCommands().addCommands(commandData).queue();
	}
	
	
	@Override
	public void onGuildReady(GuildReadyEvent event){
		List<SlashCommandData> commandData = BotCommand.getSlashCommandDataList();
		
		// Clear guild-specific commands
		event.getGuild().retrieveCommands().queue(commands -> {
			for (Command cmd : commands){
				event.getGuild().deleteCommandById(cmd.getId()).queue();
			}
		});
		
		for (SlashCommandData cmd : commandData){
			event.getJDA().upsertCommand(cmd).queue();
		}
		event.getGuild().updateCommands().addCommands(commandData).queue();
		if (!BotCore.isBugFree()){
			TextChannel c = event.getGuild().getTextChannelById(Constants.DEBUGCHANNELID);
			if (c!=null){
				c.sendMessage("Bot is ready for testing.")
				.queue(msg -> 
				{
					CommandOutput.Builder output = new CommandOutput.Builder();
					List<String> emojis = List.of(
						Constants.EMOJIDEL,
						Constants.EMOJITRUE,
						Constants.EMOJIFALSE,
						Constants.EMOJICORRECT,
						Constants.EMOJIINCORRECT,
						Constants.EMOJIMORETIME,
						Constants.EMOJINEXTQUESTION,
						Constants.EMOJIPREVQUESTION,
						Constants.EMOJISTOP,
						Constants.EMOJIEXPLICATION,
						Constants.EMOJIWHITESQUARE
					);
					List<Emoji> l = new ArrayList<>();
					for (String e : emojis){l.add(Emoji.fromFormatted(e));}
					
					QuestionList q = QuestionList.getExampleQuestionList();
					MessageSender.addButtons(msg, l, mess -> MessageSender.addReactions(mess, l.iterator()));
					
					Viewer v = new Viewer(q);
					output.add("## Viewer:\n");
					output.addAll(v.start().getTextMessages());
					output.addAll(v.next().getTextMessages());
					
					v = new QuizBot(q);
					output.add("## QuizBot:\n");
					output.addAll(v.start().getTextMessages());
					output.addAll(v.next().getTextMessages());
					
					v = new Explain((QuizBot) v, q.getAuthorId());
					output.add("## Explain:\n");
					output.addAll(v.start().getTextMessages());
					output.addAll(v.next().getTextMessages());
					
					MessageSender.sendCommandOutput(
						output.addFile(q.pathToList()).build(),
						msg.getChannel(),
						msg
					);
				});
			}
		}
	}
}
