package com.linked.quizbot.events;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.Channels;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;

import com.linked.quizbot.Constants;
import com.linked.quizbot.commands.BotCommand;
import com.linked.quizbot.core.BotCore;

import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.channel.middleman.MessageChannel;
import net.dv8tion.jda.api.events.guild.GuildReadyEvent;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.entities.channel.ChannelType;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;
import net.dv8tion.jda.internal.entities.ReceivedMessage;

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

		// Vérifier si le message est "!embed"
		int i =0, n;
		String[] args;
		String h = "";
		for (BotCommand cmd : BotCommand.getCommands()) {
			if (event.getName().equals(cmd.getName())) {
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
									String k = "";
									do {
										h+= k;
										k=fd.readLine();
									}while(k!=null);
									fd.close();
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
				System.out.print("  $> /"+cmd.getName());
				System.out.print(" ; args :");
				for (i=0; i<args.length; i++) { System.out.print(args[i]+":");}
				System.out.println("");
				String tmp = "";
				for (i=0; i<args.length; i++) { tmp+=" "+args[i];}
				Message message = null;
				cmd.execute(sender, message, channel, args);
				System.out.printf("   $> time of "+cmd.getName()+" = `%.3f ms`\n", (System.nanoTime() - start) / 1000000.00);
				return;
			}
		}
	}
}
