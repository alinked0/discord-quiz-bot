package com.linked.quizbot;
import com.linked.quizbot.Credentials;

import net.dv8tion.jda.api.entities.emoji.Emoji;
public class Constants {
	public static boolean AREWETESTING;
	public static String 
		SEPARATOR,
		RESOURCESPATH,
		LISTSPATH;
	public static final String 
		TOKEN = Credentials.TOKEN,
		AUTHORID = Credentials.AUTHORID,
		DEBUGGUILDID = Credentials.DEBUGGUILDID,
		DEBUGCHANNELID = Credentials.DEBUGCHANNELID;
	public static final String 	
		CMDPREFIXE = "q!",
		NOEXPLICATION = "No explanation found.";
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
	public static void setForLinux() {
		SEPARATOR = "/";
		RESOURCESPATH = "src"+SEPARATOR+"main"+SEPARATOR+"resources";
		LISTSPATH = RESOURCESPATH+ SEPARATOR +"lists";
		AREWETESTING = false;
	}
	public static void setForWindows() {
		SEPARATOR = "\\";
		RESOURCESPATH = "src"+SEPARATOR+"main"+SEPARATOR+"resources";
		LISTSPATH = RESOURCESPATH+ SEPARATOR +"lists";
		AREWETESTING = true;
	}
	public static boolean isAppBugFree(){
		return !AREWETESTING;
	}
	public static boolean canIRunThisHere(String guildId){
		boolean b = Constants.DEBUGGUILDID.equals(guildId);
		return AREWETESTING?b:!b;
	}
}