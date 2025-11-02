package com.linked.quizbot.utils;

public interface Displayable <T> {
	/**
	 * Returns a string representation of the object in a specific format (e.g., CSV, JSON).
	 * @return The formatted string.
	 */
	String toDisplayString(T o);
}
