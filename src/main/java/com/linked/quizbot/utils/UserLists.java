package com.linked.quizbot.utils;
import com.linked.quizbot.utils.QuestionList;

import java.io.File;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.ArrayList;
import java.util.List;
import java.util.HashMap;

import com.linked.quizbot.Constants;

import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.HashSet;
import java.util.ArrayList;
import java.util.ListIterator;

public class UserLists implements Iterable<QuestionList>{
    private String userId;
    private int numberOfGamesPlayed;
    private int totalPointsEverGained;
    private List<Double> sessionPointsPerQuestion;
    private List<QuestionList> allLists;
    private List<String> allThemes;
    private Map<String, List<QuestionList>> listsByTheme;
    public static Set<UserLists> allUserLists = new HashSet<>();
    //private HashMap<String, Double> pointsPerTheme;

    public UserLists(String userId) {
        this.userId = userId.replace("[a-zA-Z]", "");
        allLists = getUserListQuestions(userId);
        initAttributes();
        initThemes();
    }
    public UserLists(String userId, Collection<? extends QuestionList> c){
        this.userId = userId.replace("[a-zA-Z]", "");
        allLists = new ArrayList<>(c);
        initAttributes();
        initThemes();
    } 
    private void initAttributes(){
        numberOfGamesPlayed = 0;
        totalPointsEverGained = 0;
        allUserLists.add(this);
        for (QuestionList l:allLists) {
            l.setAuthorId(getUserId());
        }
        allLists.sort((e,f)-> e.getName().compareTo(f.getName()));
    }
    private void initThemes(){
        listsByTheme = new HashMap<>();
        allThemes = new ArrayList<>();
        for (QuestionList l : allLists){
            String theme = l.getTheme(); 
            if(!allThemes.contains(theme)){
                allThemes.add(theme);
            }
        }
        allThemes.sort((e,f)-> e.compareTo(f));
        for(String t : allThemes) {
            listsByTheme.put(t, new ArrayList<QuestionList>());
        }
        for (QuestionList l : allLists){
            listsByTheme.get(l.getTheme()).add(l);
        }
    }
    public static String getCodeForIndexQuestionList(QuestionList l, String userId){
        for (UserLists u : allUserLists) {
            if (u.getUserId().equals(userId)) {
                return u.getCodeForIndexQuestionList(l);
            }
        }
        return null;
    }
    public String getCodeForIndexQuestionList(QuestionList l){
        String theme = l.getTheme();
        int indexTheme = myBinarySearchIndexOf(allThemes, 0, allThemes.size()-1, theme);
        List<QuestionList> list = listsByTheme.get(theme);
        int indexList = myBinarySearchIndexOf(list, 0, list.size()-1, l);
        return (indexTheme+1)+" " +(indexList+1);
    }
    public static void addListToUser(String userId, QuestionList l) {
        for (UserLists u : allUserLists) {
            if (u.getUserId().equals(userId)) {
                u.addList(l);
                return;
            }
        }
    }
    public void addList(QuestionList l) {
        int h = allLists.size();
        allUserLists.remove(this);
        System.out.println("    $> "+allThemes);
        int indexTheme = myBinarySearchIndexOf(allThemes, 0, allThemes.size()-1, l.getTheme());
        System.out.println("    $> index "+indexTheme+" for:"+l.getTheme());
        String theme = l.getTheme();
        int indexList = myBinarySearchIndexOf(allLists, 0, allLists.size()-1, l);
        System.out.println("    $> index "+indexList+" for:"+l.getName());
        if (indexList>=0) {
            QuestionList k = allLists.get(indexList);
            k.addAll(l);
            int i = listsByTheme.get(theme).indexOf(k);
            listsByTheme.get(theme).set(i,k);
            i = allLists.indexOf(k);
            allLists.set(i, k);
            k.exportListQuestionAsJson();
            allUserLists.add(this);
            return;
        }
        if (indexTheme<0){
            allThemes.add(theme);
            listsByTheme.put(theme, new ArrayList<>());
        }
        allLists.add(l);
        listsByTheme.get(theme).add(l);
        l.exportListQuestionAsJson();
        allLists.sort((e,f)-> e.getName().compareTo(f.getName()));
        allThemes.sort((e,f)-> e.compareTo(f));
        listsByTheme.get(theme).sort((e,f)-> e.getName().compareTo(f.getName()));
        allUserLists.add(this);
    }
    public static void deleteList(QuestionList l){
        UserLists userLists = new UserLists(l.getAuthorId());
        userLists.allLists.remove(l);
        File f = new File(l.getPathToList());
        f.delete();
        String theme=l.getTheme(); 
        int n = userLists.listsByTheme.get(theme).size();
        if (n<=1) {
            userLists.listsByTheme.remove(theme);
            userLists.allThemes.remove(theme);
        }
        allUserLists.remove(userLists);
        allUserLists.add(userLists);
    }
    public static int myBinarySearchIndexOf(List<String> tab, int start, int end, String q){
        if (start > end){
            return -1*start-1;
        }
        int m = (start+end)/2;
        int comp = tab.get(m).compareTo(q);
        if(comp == 0){
            return m;
        }
        if (comp >0){
            return myBinarySearchIndexOf(tab, start, m-1, q);
        }
        return myBinarySearchIndexOf(tab, m+1, end, q);
    }public static int myBinarySearchIndexOf(List<QuestionList> tab, int start, int end, QuestionList q){
        if (start > end){
            return -1*start-1;
        }
        int m = (start+end)/2;
        int comp = tab.get(m).getName().compareTo(q.getName());
        if(comp == 0){
            return m;
        }
        if (comp >0){
            return myBinarySearchIndexOf(tab, start, m-1, q);
        }
        return myBinarySearchIndexOf(tab, m+1, end, q);
    }
    public List<Double> getSessionPointsPerQuestion() {
        return sessionPointsPerQuestion;
    }
    public List<QuestionList> getAllLists() {
        List<QuestionList> res = new ArrayList<>(allLists);
        return res;
    }
    public String getUserId(){ return userId;}

    public List<String> getAllThemes() { return allThemes;}

    public Map<String, List<QuestionList>> getListsByTheme(){ return listsByTheme;}
    public List<QuestionList> getListsByTheme(String theme){ 
        return getListsByTheme().getOrDefault(theme, null);
    }
    public long getUserIdLong(){ return Long.parseLong(userId);}

    public QuestionList get(int index) {
        return allLists.get(index);
    }

    @Override
    public Iterator<QuestionList> iterator(){
        return getAllLists().iterator();
    }

    public Comparator<? super UserLists> comparator() {
        return (e, f)->(Long.compare(e.getUserIdLong(),f.getUserIdLong()));
    }
    public static List<QuestionList> getUserListQuestions(String userId) {
        userId = userId.replace("[a-zA-Z]", "");
        long start = System.nanoTime();
        List<QuestionList> res;
        for (UserLists u : allUserLists) {
            if (u.equals(userId)) {
                res = u.getAllLists();
                System.out.printf("   $> time getUserListQuestions = `%.3f ms`\n", (System.nanoTime() - start) / 1000000.00);
                return res;
            } 
        }

        res = new ArrayList<>();
        
        File folder = new File(Constants.LISTSPATH+Constants.SEPARATOR+ userId);
        File[] listOfFiles = folder.listFiles();
        
        if(listOfFiles != null) {
            //Arrays.sort(listOfFiles, Comparator.comparingLong(File::lastModified).reversed());
            for (int i = 0; i < listOfFiles.length; i++) {
                //if (listOfFiles[i].isFile())
                if (listOfFiles[i].getName().equals("tmp")) continue;
                QuestionList l = QuestionList.importListQuestionFromJson(listOfFiles[i].getAbsolutePath());
                res.add(l);
                //l.exportListQuestionAsJson();
            }
        }
        allUserLists.add(new UserLists(userId, res));
        System.out.printf("   $> time getUserListQuestions = `%.3f ms`\n", (System.nanoTime() - start) / 1000000.00);
        return res;
    }
    public static void exportAllUserLists() {
        for (UserLists userLists: UserLists.allUserLists) {
            List<QuestionList> lists = userLists.getAllLists();
            for (QuestionList l : lists) {
                l.exportListQuestionAsJson();
                System.out.println(l.getPathToList()+"; ");
            }
        }
    }
    @Override
    public int hashCode(){
        return getUserId().hashCode();
    }
    @Override
    public boolean equals(Object o){
        if (this == o) {return true;}
        if(o instanceof  UserLists) {
            UserLists u = (UserLists) o;
            return getUserId().equals(u.getUserId());
        }
        if(o instanceof  String) {
            String u = (String) o;
            return getUserId().equals(u.replace("[a-zA-Z]", ""));
        }
        return false;
    }
}
