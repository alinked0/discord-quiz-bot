package com.linked.quizbot.commands;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.Arrays;

public enum CommandCategory {
	GAME, NAVIGATION, EDITING, READING, OTHER;
	private String name;
    static {
        GAME.name = "Game"; 
		NAVIGATION.name = "Navigation"; 
		EDITING.name = "Editing"; 
		READING.name = "Reading"; 
		OTHER.name = "Other";
    }
	public static Set<CommandCategory> getCategories() {
		Set<CommandCategory> res = new HashSet<>();
		res.addAll(Arrays.asList(GAME, NAVIGATION, EDITING, READING, OTHER));
		return res;
	}
	public String toString(){
		return name;
	}
}
