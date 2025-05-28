package com.linked.quizbot;
import com.linked.quizbot.Credentials;
import com.linked.quizbot.commands.list.CreateListCommand;

import net.dv8tion.jda.api.entities.emoji.Emoji;

/**
 * The Constants class is the class that contains all static variables
 *  nessacery during the initiation of the Main.java.
 * 
 * This is incomplete, and requires you to write the Credentions class, 
 * that will contains the bot token and infomation on your testing guild.
 * 
 * <p>
 * package com.linked.quizbot;
 * public class Credentials {
 * public static final String 
 * 		TOKEN = "",
 * 		AUTHORID = "",
 * 		DEBUGGUILDID = "",
 * 		DEBUGCHANNELID = "";
 * }
 * </p>
 * 
 */
public class Constants {
	public static boolean AREWETESTING;
	public static String 
		root = System.getProperty("user.dir").substring(0, 1),
		SEPARATOR= root.equals("/")?"/":"\\",
		RESOURCESPATH= "src"+SEPARATOR+"main"+SEPARATOR+"resources",
		LISTSPATH=RESOURCESPATH+ SEPARATOR +"lists",
		TOKEN,
		AUTHORID = "",
		DEBUGGUILDID = "",
		DEBUGCHANNELID = "",
		CMDPREFIXE = "q!",
		NOEXPLICATION = "No explanation found.",
		UPDATEEXPLANATION = "The bot is curretly getting an update, please be patient.";
	public static final int 
		CHARSENDLIM = 2000,
		READTIMEMIN = 5,
		READTIMELONGMIN = 10,
		INCRTIMESEC = 5;
	public static final Emoji 
		EMOJIDEL = Emoji.fromUnicode("U+1F525"),
		EMOJITRUE = Emoji.fromUnicode("U+2714 U+FE0F"),
		EMOJIFALSE = Emoji.fromUnicode("U+274C"),
		EMOJIMORETIME = Emoji.fromUnicode("U+23F0"),
		EMOJINEXTQUESTION = Emoji.fromUnicode("U+23ED U+FE0F"),
		EMOJIPREVQUESTION = Emoji.fromUnicode("U+23EE U+FE0F"),
		EMOJIWHITESQUARE = Emoji.fromUnicode("U+2B1C"),
		EMOJIEXPLICATION = Emoji.fromUnicode("U+2754");
	public static boolean isBugFree(){
		return !AREWETESTING;
	}
	public static boolean canIRunThisHere(String guildId){
		boolean b = Constants.DEBUGGUILDID.equals(guildId);
		return isBugFree()?!b:b;
	}
	public static boolean isDebugGuild(String guildId){
		return Constants.DEBUGGUILDID.equals(guildId);
	}
}