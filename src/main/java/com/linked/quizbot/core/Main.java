package com.linked.quizbot.core;

import com.linked.quizbot.Constants;
import com.linked.quizbot.events.MessageListener;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.requests.GatewayIntent;

import com.linked.quizbot.events.ReactionListener;
import com.linked.quizbot.events.SlashCommandListener;
import com.linked.quizbot.events.readyEvent;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import com.linked.quizbot.utils.QuestionList;

import net.dv8tion.jda.api.entities.Activity;

public class Main {
	public static void main (String[] args) {
		System.out.printf(" $> %s\n", System.getProperty("user.dir"));
		String s = System.getProperty("user.dir").substring(0, 1);
		if (s.equals("/")){
			Constants.setForLinux();
		} else {
			Constants.setForWindows();
		}
		JDA jda = JDABuilder.createDefault(Constants.TOKEN,
			GatewayIntent.GUILD_MESSAGES, 
			GatewayIntent.MESSAGE_CONTENT,
			GatewayIntent.GUILD_MEMBERS, 
			GatewayIntent.DIRECT_MESSAGES, 
			GatewayIntent.DIRECT_MESSAGE_REACTIONS, 
			GatewayIntent.GUILD_MESSAGE_REACTIONS
		).setActivity(Activity.playing("q!help")).build();
		jda.addEventListener(
			new SlashCommandListener(), 
			new ReactionListener(), 
			new MessageListener(),
			new readyEvent()
		);
	}
}
// QuestionList l;
// String path = Constants.LISTSPATH + Constants.SEPARATOR+"939614721244553248"+Constants.SEPARATOR+"Math-Prime_numbers.json";
// l = new QuestionList(path);

// for (int i = 1; i<=2; i++) {
// 	l.setAuthorId("468026374557270017");
// 	l.exportListQuestionAsJson();
// }

// QuestionList list0 = linked.get(0);
// Question q0 = list0.get(0);