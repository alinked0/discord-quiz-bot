package com.linked.quizbot.utils;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * A utility class designed to manage filtering and sorting operations on a collection
 * of QuestionList objects. It translates command tokens into executable Java predicates
 * and comparators, mirroring the conceptual system described in the Karuta example.
 */
public class CollectionManager {
	public static Predicate<QuestionList> parseFilter(String token) {
	    String lowerToken = token.toLowerCase();
		
	    if (lowerToken.startsWith("size") || lowerToken.startsWith("date")) {
	        return parseNumericFilter(token);
	    }
	    if (lowerToken.startsWith("tag")) {
	        return parseTagFilter(token);
	    }
		
	    if (lowerToken.startsWith("name") || lowerToken.startsWith("id") || lowerToken.startsWith("author")) {
	        return parseStringFilter(token);
	    }
	    return list -> true;
	}
	
	private static Predicate<QuestionList> parseStringFilter(String token) {
	    
	    if (token.contains("=")) {
	        String op;
	        String[] parts;
			
	        if (token.contains("!=")) {
	            op = "!=";
	            parts = token.split("!=", 2);
	        } else if (token.contains("~=")) {
	            op = "~="; 
	            parts = token.split("~=", 2);
	        } else {
	            op = "="; 
	            parts = token.split("=", 2);
	        }

	        String field = parts[0].toLowerCase();
	        String value = parts[1];

	        return list -> {
	            String listValue = switch (field) {
	                case "name" -> list.getName();
	                case "id" -> list.getId();
	                case "author" -> list.getAuthorId();
	                default -> null;
	            };

	            if (listValue == null) return false;

	            return switch (op) {
	                case "=" -> listValue.equalsIgnoreCase(value);
	                case "!=" -> !listValue.equalsIgnoreCase(value);
	                case "~=" -> listValue.toLowerCase().contains(value.toLowerCase());
	                default -> true;
	            };
	        };
	    }
	    return list -> true;
	}
	
	private static Predicate<QuestionList> parseNumericFilter(String token) {
	    
	    String operator = token.replaceAll("[^!<=>]+", "");
	    String fieldPart = token.split("[!<=>]+")[0].trim().toLowerCase();
	    String valuePart = token.split("[!<=>]+")[1].trim();
	    long value;

	    try {
	        value = Long.parseLong(valuePart);
	    } catch (NumberFormatException e) {
	        System.err.println("[\\u001b[33mERROR\\u001b[0m] Invalid numeric value in filter: " + valuePart);
	        return list -> true;
	    }

	    return list -> {
	        long listValue = switch (fieldPart) {
	            case "size" -> list.size();
	            case "date" -> list.getTimeCreatedMillis();
	            default -> -1;
	        };

	        return switch (operator) {
	            case ">" -> listValue > value;
	            case "<" -> listValue < value;
	            case ">=" -> listValue >= value;
	            case "<=" -> listValue <= value;
	            case "=" -> listValue == value;
	            case "!=" -> listValue != value;
	            default -> true;
	        };
	    };
	}
	
	private static Predicate<QuestionList> parseTagFilter(String token) {
	    boolean exclude = token.startsWith("tag!");
	    String tagName = token.replaceAll("^(tag!?[=:])", "").trim();

	    if (tagName.isEmpty()) {
	        return list -> true; 
	    }

	    return list -> {
	        boolean hasTag = list.getTagNames().contains(tagName);
	        return exclude ? !hasTag : hasTag;
	    };
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
	
	public static Comparator<QuestionList> getComparator(String sortToken) {
	    switch (sortToken) {
	        case "name":
	            return QuestionList.comparatorByName();
	        case "date":
	            return QuestionList.comparatorByDate();
	        case "id":
	            return QuestionList.comparatorById();
	        case "size":
	            return QuestionList.comparatorBySize();
	        default:
	            return QuestionList.comparatorByDate().reversed();
	    }
	}
}
