package com.linked.quizbot;

import java.io.File;

import net.dv8tion.jda.api.entities.emoji.Emoji;

/**
 * The Constants class is the class that contains all static variables
 *  necessary during the initiation of the Main.java.
 * 
 */
public class Constants {
	public static boolean AREWETESTING;
	public static String 
		TOKEN,
		AUTHORID = "",
		DEBUGGUILDID = "",
		DEBUGCHANNELID = "";
	public static String 
		SEPARATOR= File.separator,
		RESOURCESPATH= "src"+SEPARATOR+"main"+SEPARATOR+"resources",
		LISTSPATH=RESOURCESPATH+ SEPARATOR +"lists",
		USERDATAPATH=RESOURCESPATH+ SEPARATOR +"user-data",
		CMDPREFIXE = "q!",
		NOEXPLICATION = "No explanation found.",
		UPDATEEXPLANATION = "The bot is curretly getting an update, please be patient.";
	public static final int 
		CHARSENDLIM = 2000,
		READTIMEMIN = 5,
		READTIMELONGMIN = 10,
		INCRTIMESEC = 5,
		DISCORDIDLENMIN= 17,
		DISCORDIDLENMAX= 18;
	public static final Emoji
		EMOJIDEL = Emoji.fromUnicode("🔥"),
		EMOJITRUE = Emoji.fromUnicode("✅"), //✅
		EMOJIFALSE = Emoji.fromUnicode("❌"), //❌
		EMOJICORRECT = Emoji.fromUnicode("✔️"),//✔️
		EMOJIINCORRECT = Emoji.fromUnicode("✖️"),//✖️
		EMOJIMORETIME = Emoji.fromUnicode("⏰"), //	⏰
		EMOJINEXTQUESTION = Emoji.fromUnicode("U+23ED U+FE0F"),
		EMOJIPREVQUESTION = Emoji.fromUnicode("U+23EE U+FE0F"),
		EMOJISTOP = Emoji.fromUnicode("⏹️"),//⏹️
		EMOJIWHITESQUARE = Emoji.fromUnicode("U+2B1C"),
		EMOJIEXPLICATION = Emoji.fromUnicode("❓");//❓
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