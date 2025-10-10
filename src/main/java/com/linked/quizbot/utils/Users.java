package com.linked.quizbot.utils;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.linked.quizbot.Constants;

/**
 * A central registry and utility class for managing all {@link User} objects in the application.
 * <p>
 * This class serves as a static access point to user data, providing methods for
 * retrieving, adding, and updating users. It is responsible for loading all user data
 * from local files on startup and exporting it to ensure data persistence.
 * This class also contains utility methods for performing efficient binary searches
 * on sorted lists of users and quiz lists.
 * </p>
 * **This class is tested using JUnit 5.**
 * * @author alinked0
 * @version 1.0
 * @since 2025-02-01
 * @see User
 * @see QuestionList
 */
public class Users {
	public static final List<User> allUsers = new ArrayList<>();

	/*static {
		loadAllUsers();
	}*/

	public static User get(String userId) {
		return Users.getUser(userId);
	}
	public static void clear() {
        allUsers.clear();
        QuestionList.Hasher.clearGeneratedCodes();
    }
	public static void addUser(User user){
		int index = myBinarySearchIndexOf(Users.allUsers, user, User.comparatorByUserId());
		if (index < 0){
			index = -index - 1;
			Users.allUsers.add(index, user);
		}else {
			Users.allUsers.set(index, user);
		}
	}
	public static void update(User user){
		addUser(user);
	}
	public static User getUser(String userId){
		int index = User.myBinarySearchUserId(Users.allUsers, userId);
		if (index >=0){
			return Users.allUsers.get(index);
		}
		addUser(new User(userId));
		index = User.myBinarySearchUserId(Users.allUsers, userId);
		if (index >=0){
			return Users.allUsers.get(index);
		}
		return null;
	}
	public static QuestionList getById(String id) {
		QuestionList l=null;
		for (User u : Users.allUsers){
			l = u.getById(id);
			if (l!=null){
				return l;
			}
		}
		return l;
	}
	public static QuestionList getByName(String listName){
		QuestionList res = null;
		for (User u : Users.allUsers){
			res = u.getByName(listName);
			if (res != null){
				return res;
			}
		}
		return res;
	}
	public static void addListToUser(String userId, QuestionList l) {
		User u = getUser(userId);
		u.addList(l);
		update(u);
	}
	public static boolean createTag(String userId, String tagName, String emoji) {
		User user = Users.get(userId);
		return user.createTag(tagName, emoji);
	}
	public static boolean deleteTag(String userId, String tagName) {
		User user = Users.get(userId);
		return user.deleteTag(tagName);
	}
	public static boolean addTagToList(String id, String tagName) {
		QuestionList tmp = new QuestionList.Builder().id(id).build();
		for (User u : Users.allUsers) {
			if (u.addTagToList(tmp, tagName)) {
				return true;
			}   
		}
		return false;
	}
	public static boolean removeTagFromList(String id, String tagName) {
		QuestionList l = Users.getById(id);
		User u = Users.get(l.getAuthorId());
		return u.removeTagFromList(l, tagName);
	}
	public static boolean deleteList(QuestionList l){
		User user = Users.get(l.getAuthorId());
		boolean b = user.deleteList(l);
		Users.update(user);
		return b;
	}
	public static <T> int myBinarySearchIndexOf(List<T> tab, int start, int end, T q, Comparator<? super T> compare){
		if (start > end){
			return -1*start-1;
		}
		int m = (start+end)/2;
		int comp = compare.compare(tab.get(m), q);
		if(comp == 0){
			return m;
		}
		if (comp >0){
			return myBinarySearchIndexOf(tab, start, m-1, q, compare);
		}
		return myBinarySearchIndexOf(tab, m+1, end, q, compare);
	}
	public static <T> int myBinarySearchIndexOf(List<T> tab, T q, Comparator<? super T> compare){
		return myBinarySearchIndexOf(tab, 0, tab.size()-1, q, compare);
	}
	
	public Iterator<User> iterator(){
		return Users.allUsers.iterator();
	}
	
	public static Map<String, QuestionList> importLists(String userId) {
		Map<String, QuestionList> res = new HashMap<>();
		File folder = new File(Constants.LISTSPATH+Constants.SEPARATOR+ userId+Constants.SEPARATOR);
		File[] listOfFiles = folder.listFiles();
		if(listOfFiles != null) {
			for (int i = 0; i < listOfFiles.length; i++) {
				String listId = listOfFiles[i].getName();
				if (List.of("tmp").contains(listOfFiles[i].getName())) continue;
				try{
					QuestionList l = QuestionList.Parser.fromJsonFile(listOfFiles[i].getAbsolutePath()).build();
					res.put(listId, l);
				}catch (IOException e) {
					System.err.println(String.format("[ERROR] An error occurred while importing a list. listid:%s , userId:%s",listId, userId));
					e.printStackTrace();
				}
			}
		}
		return res;
	}
	public static void exportAllUserLists() {
		User user;
		for (int i=0; i<Users.allUsers.size(); i++) {
			user = Users.allUsers.get(i);
			user.exportUserLists();
			System.out.println("[INFO] exported UserLists ("+(i+1)+"/"+Users.allUsers.size()+"); ");
		}
	}
	public static void loadAllUsers(){
		Users.clear();
		File folder = new File(Constants.USERDATAPATH);
		String userId;
		if (folder.exists()){
			for (File f : folder.listFiles()){
				if (f.isDirectory()){
					userId = f.getName();
					if (userId.length()>=Constants.DISCORDIDLENMIN){
						addUser(new User(userId));
					}
				}
			}

		}
	}
	public static void exportAllUserData(){
		User user;
		for (int i=0; i<Users.allUsers.size(); i++) {
			user = Users.allUsers.get(i);
			user.exportUserData();
			System.out.println("[INFO] exported UserData ("+(i+1)+"/"+Users.allUsers.size()+"); ");
		}
	}
}
