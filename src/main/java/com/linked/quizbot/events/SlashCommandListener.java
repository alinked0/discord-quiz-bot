package com.linked.quizbot.events;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.Channels;
import java.nio.file.Files;

import com.linked.quizbot.Constants;
import com.linked.quizbot.commands.BotCommand;
import com.linked.quizbot.commands.CommandCategory;
import com.linked.quizbot.core.BotCore;

import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.channel.middleman.MessageChannel;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;

/**
 * The class SlashCommandListener will serve as the first layer to any slash command
 * that means message commands like /help
 */
public class SlashCommandListener extends ListenerAdapter {
	
	@Override
	public void onSlashCommandInteraction(SlashCommandInteractionEvent event) {
		long start = System.nanoTime();
		User sender = event.getUser();
		// Ignorer les messages des bots
		if (sender.isBot()) return;
        
        if (event.isFromGuild()){
            if (!Constants.canIRunThisHere(event.getGuild().getId())){
                return;
            }
        }
		String userId = sender.getId();
		MessageChannel channel = event.getInteraction().getChannel();
		
		
		// log User
		BotCore.addUser(sender);
		
		int i =0, n;
		String[] args;
		String k;
		for (BotCommand cmd : BotCommand.getCommands()) {
			if (event.getName().equals(cmd.getName())) {
				if(BotCore.isShutingDown()){
					CommandCategory category = cmd.getCategory();
					if(category.equals(CommandCategory.EDITING) || category.equals(CommandCategory.GAME)){
						event.reply(Constants.UPDATEEXPLANATION).queue();
						return;
					}
				}
				n= event.getOptions().size();
				args = new String[n];
				if (n>0) {
					for (OptionData d : cmd.getOptionData()){
						OptionMapping tmp = event.getOption(d.getName());
						if(tmp !=null) {
							if (tmp.getType().equals(OptionType.USER)){
								args[i++] = ""+tmp.getAsUser().getId();
							}
							else if (tmp.getType().equals(OptionType.ATTACHMENT)){
								String h = "";
								try {
									URL website = new URL(tmp.getAsAttachment().getUrl());
									String path = Constants.LISTSPATH+Constants.SEPARATOR+userId+Constants.SEPARATOR+"tmp";
									File f = new File(path);
									ReadableByteChannel rbc = Channels.newChannel(website.openStream());
									FileOutputStream fos = new FileOutputStream(f);
									fos.getChannel().transferFrom(rbc, 0, Long.MAX_VALUE);
									if(!f.getParentFile().exists()) {
										f.getParentFile().mkdirs();
									}
									BufferedReader fd = Files.newBufferedReader(f.toPath());

									k = "";
									do {
										h+= k;
										k=fd.readLine();
									}while(k!=null);

									fd.close();
									f.delete();
								} catch (IOException e) {
									System.err.println(" $> An error occurred while taking an attachment.");
									e.printStackTrace();
								}
								args[i++] = h;
							} else {
								args[i++] = tmp.getAsString();
							}
						}
					}
				}

				event.reply(cmd.getDescription()).queue();

				System.out.print("  $> /"+cmd.getName());System.out.print(" ; args :");for (i=0; i<args.length; i++) { System.out.print(args[i]+":");}
				
				Message message = null;
				cmd.execute(sender, message, channel, args);
				if (!Constants.isBugFree()) System.out.printf("   $> time "+cmd.getName()+" = `%.3f ms`\n", (System.nanoTime() - start) / 1000000.00);
				return;
			}
		}
	}
}
