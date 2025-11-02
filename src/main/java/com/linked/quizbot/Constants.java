package com.linked.quizbot;

import java.io.File;

import com.fasterxml.jackson.databind.ObjectMapper;

import net.dv8tion.jda.api.entities.emoji.Emoji;

/**
 * The Constants class is the class that contains all static variables
 *  necessary during the initiation of the Main.java.
 * 
 */
public class Constants {
	public static String 
		TOKEN,
		ADMINID,
		DEBUGGUILDID ,
		DEBUGCHANNELID;
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
	public static ObjectMapper MAPPER = new ObjectMapper();
	public static final String
		EMOJIDEL = Emoji.fromUnicode("🔥").getFormatted(),
		EMOJITRUE = Emoji.fromUnicode("✅").getFormatted(),
		EMOJIFALSE = Emoji.fromUnicode("❌").getFormatted(),
		EMOJICORRECT = Emoji.fromUnicode("✔️").getFormatted(),
		EMOJIINCORRECT = Emoji.fromUnicode("✖️").getFormatted(),
		EMOJIMORETIME = Emoji.fromUnicode("⏰").getFormatted(),
		EMOJINEXTQUESTION = Emoji.fromUnicode("▶️").getFormatted(),
		EMOJIPREVQUESTION = Emoji.fromUnicode("◀️").getFormatted(),
		EMOJISTOP = Emoji.fromUnicode("⏹️").getFormatted(),
		EMOJIRELOAD = Emoji.fromUnicode("🔁").getFormatted(),
		EMOJIWHITESQUARE = Emoji.fromUnicode("⬜").getFormatted(),
		EMOJIBLACKSQUARE = Emoji.fromUnicode("⬛").getFormatted(),
		EMOJIEXPLICATION = Emoji.fromUnicode("❓").getFormatted(),
		EMOJIBOX = Emoji.fromUnicode("🔲").getFormatted(),
		EMOJICHECKEDBOX = Emoji.fromUnicode("🔳").getFormatted();
	public static String 
		RED="\u001b[31m",
		ORANGE="\u001b[33m",
		BLUE="\u001b[34m",
		MAGENTA="\u001b[35m ",
		RESET="\u001b[0m";
	public static String 
		ERROR=String.format("[%s%s%s] ",  Constants.ORANGE, "ERROR", Constants.RESET),
		INFO=String.format("[%s%s%s] ", Constants.BLUE,"INFO", Constants.RESET),
		IO=String.format("[%s%s%s] ",  Constants.MAGENTA,"IO",Constants.RESET);
}