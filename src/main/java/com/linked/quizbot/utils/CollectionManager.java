package com.linked.quizbot.utils;

import java.util.Comparator;
import java.util.List;
import java.util.function.Predicate;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import com.linked.quizbot.Constants;

/**
 * A utility class designed to manage filtering and sorting operations on a collection
 * of QuestionList objects. It translates command tokens into executable Java predicates
 * and comparators, mirroring the collection command in the Karuta card collection game.
 */
public class CollectionManager {
	public static Predicate<QuestionList> parseFilter(User user, String token) {
		String lowerToken = token.toLowerCase();
		if (lowerToken.matches("(size|date|start|end).*")) {
			return parseNumericFilter(user, token);
		}
		if (lowerToken.matches("tag.*")) {
			return parseTagFilter(token);
		}		
		if (lowerToken.matches("(name|id|author).*")) {
			return parseStringFilter(token);
		}		
		return list -> false;
	}
	
	private static Predicate<QuestionList> parseStringFilter(String token) {
		
		if (token.contains("=")) {
			String op;
			String[] parts;
			
			if (token.contains("!=")) {
				op = "!=";
				parts = token.split("!=", 2);
			} else {
				op = "=";
				parts = token.split("=", 2);
			}
			
			String field = parts[0].toLowerCase();
			String value = parts[1];
			Pattern pattern = Pattern.compile(value, Pattern.CASE_INSENSITIVE);
			
			return list -> {
				String listValue = switch (field) {
					case "name" -> list.getName();
					case "id" -> list.getId();
					case "author" -> list.getAuthorId();
					default -> null;
				};
				
				if (listValue == null) return false;
				
				return switch (op) {
					case "=" -> pattern.matcher(listValue).matches();
					case "!=" -> !pattern.matcher(listValue).matches();
					default -> true;
				};
			};
		}
		return list -> false;
	}
	
	private static Predicate<QuestionList> parseNumericFilter(User user, String token) {
		
		String operator = token.replaceAll("[^!<=>]+", "");
		String fieldPart = token.split("[!<=>]+")[0].trim().toLowerCase();
		String valuePart = token.split("[!<=>]+")[1].trim();
		long value;
		
		try {
			value = Long.parseLong(valuePart);
		} catch (NumberFormatException e) {
			System.err.println(Constants.ERROR + "Invalid numeric value in filter: " + valuePart);
			return list -> false;
		}
		
		return list -> {
			List<Attempt> attempts = user.getAttempts(list.getId());
			Attempt lastAtt = attempts.isEmpty()?null:attempts.getFirst();
			long listValue = switch (fieldPart) {
				case "size" -> list.size();
				case "date" -> list.getTimeCreatedMillis();
				case "start" -> lastAtt==null?0L:lastAtt.getStart();
				case "end" -> lastAtt==null?0L:lastAtt.getEnd();
				default -> -1;
			};
			
			return switch (operator) {
				case ">" -> listValue > value;
				case "<" -> listValue < value;
				case ">=" -> listValue >= value;
				case "<=" -> listValue <= value;
				case "=" -> listValue == value;
				case "!=" -> listValue != value;
				default -> false;
			};
		};
	}
	
	private static Predicate<QuestionList> parseTagFilter(String token) {
		boolean exclude = token.startsWith("tag!");
		String tagRegex = token.replaceAll("^(tag!?=?)", "").trim();
		Pattern pat;
		if (tagRegex.isEmpty()) {
			return list -> true;
		}
		pat = Pattern.compile(tagRegex);
		Predicate<QuestionList> res =  list -> {
			boolean hasTag = false;
			for (String tagName: list.tagNames()){
				if (pat.matcher(tagName).matches()){
					hasTag = true;
					break;
				}
			}
			return hasTag;
		};
		return exclude ? res.negate():res;
	}
	
	public static List<QuestionList> applyFilters(List<QuestionList> collection, List<Predicate<QuestionList>> filters) {
		
		Predicate<QuestionList> combinedFilter = filters.stream().reduce(
			list -> true,
			Predicate::and 
		);
		
		return collection.stream()
						 .filter(combinedFilter)
						 .collect(Collectors.toList());
	}
	
	public static Comparator<QuestionList> parseComparator(User user, String sortToken) {
		String field = sortToken.toLowerCase().replaceAll("o=+", "");
		switch (field) {
			case "name":
				return QuestionList.comparatorByName();
			case "date":
				return QuestionList.comparatorByDate();
			case "id":
				return QuestionList.comparatorById();
			case "size":
				return QuestionList.comparatorBySize();
			case "start":
				return (e, f) -> {
						List<Attempt> a, b;
						Long u, v;
						a = user.getAttempts(e.getId());
						b = user.getAttempts(f.getId());
						if (a.isEmpty() || b.isEmpty()){
							if (a.isEmpty()){
								if (b.isEmpty()) return 0;
								return -1;
							}
							return 1;
						}
						u = a.getFirst().getStart();
						v = b.getFirst().getStart();
						return Long.compare(u, v);
					};
			case "score":
				return (e, f) -> {
						List<Attempt> a, b;
						Double u, v;
						a = user.getAttempts(e.getId());
						b = user.getAttempts(f.getId());
						if (a.isEmpty() || b.isEmpty()){
							if (a.isEmpty()){
								if (b.isEmpty()) return 0;
								return -1;
							}
							return 1;
						}
						u = a.getFirst().getScore();
						v = b.getFirst().getScore();
						return Double.compare(u, v);
					};
			default:
				return QuestionList.comparatorByDate();
		}
	}
}
