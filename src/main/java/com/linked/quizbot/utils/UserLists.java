package com.linked.quizbot.utils;

import java.io.File;
import java.util.Collection;
import java.util.Comparator;
import java.util.ArrayList;
import java.util.List;
import java.util.HashMap;

import com.linked.quizbot.Constants;

import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.HashSet;

public class UserLists implements Iterable<QuestionList>{
    private String userId;
    private int numberOfGamesPlayed;
    private int totalPointsEverGained;
    private List<Double> sessionPointsPerQuestion;
    private List<QuestionList> allLists;
    public static Set<UserLists> allUserLists = new HashSet<>();
    //private HashMap<String, Double> pointsPerTheme;

    public UserLists(String userId) {
        this.userId = userId.replace("[a-zA-Z]", "");
        allLists = getUserListQuestions(userId);
        initAttributes();
    }
    public UserLists(String userId, Collection<? extends QuestionList> c){
        this.userId = userId.replace("[a-zA-Z]", "");
        allLists = new ArrayList<>(c);
        initAttributes();
    } 
    private void initAttributes(){
        numberOfGamesPlayed = 0;
        totalPointsEverGained = 0;
        allUserLists.add(this);
        for (QuestionList l:allLists) {
            l.setAuthorId(getUserId());
        }
        allLists.sort(QuestionList.comparatorByListId());
    }
    public static String getCodeForIndexQuestionList(QuestionList l){
        return l.getListId();
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
        allUserLists.remove(this);
        int indexList = myBinarySearchIndexOf(allLists, 0, allLists.size()-1, l, QuestionList.comparatorByName());
        //System.out.println("    $> index "+indexList+" for:"+l.getName());
        if (indexList>=0) {
            QuestionList k = allLists.get(indexList);
            k.addAll(l);
            int i = allLists.indexOf(k);
            allLists.set(i, k);
            k.exportListQuestionAsJson();
            allUserLists.add(this);
            return;
        }
        allLists.add(l);
        l.exportListQuestionAsJson();
        allLists.sort(QuestionList.comparatorByListId());
        allUserLists.add(this);
    }
    public static void deleteList(QuestionList l){
        UserLists userLists = new UserLists(l.getAuthorId());
        userLists.allLists.remove(l);
        File f = new File(l.getPathToList());
        f.delete();
        allUserLists.remove(userLists);
        allUserLists.add(userLists);
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
    public List<Double> getSessionPointsPerQuestion() {
        return sessionPointsPerQuestion;
    }
    public List<QuestionList> getAllLists() {
        List<QuestionList> res = new ArrayList<>(allLists);
        return res;
    }
    public String getUserId(){ return userId;}
    
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
                if (!Constants.isBugFree()) System.out.printf("   $> time getUserListQuestions = `%.3f ms`\n", (System.nanoTime() - start) / 1000000.00);
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
        if (!Constants.isBugFree()) System.out.printf("   $> time getUserListQuestions = `%.3f ms`\n", (System.nanoTime() - start) / 1000000.00);
        return res;
    }
    public static void exportAllUserLists() {
        for (UserLists userLists: UserLists.allUserLists) {
            userLists.exportUserLists();
        }
    }
    public void exportUserLists() {
        List<QuestionList> lists = getAllLists();
        for (QuestionList l : lists) {
            l.exportListQuestionAsJson();
            System.out.println("  $> exported "+l.getPathToList()+"; ");
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
