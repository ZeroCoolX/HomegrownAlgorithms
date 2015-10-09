/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package algorithms;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 *
 * @author dewit
 */
public class StableMarriage {
    
    private static class Man{
        private int id;

        public int getId() {
            return id;
        }

        public void setId(int id) {
            this.id = id;
        }
        
        public Man(int id){
            this.id = id;
        }
    }
    
    private static class Woman{
        private final ArrayList<Integer> rejectionList;
        private int id;

        public int getId() {
            return id;
        }

        public void setId(int id) {
            this.id = id;
        }
        
        public Woman(int id){
            rejectionList = new ArrayList();
            this.id = id;
        }
        
        public void addToRejected(int m){
            addRejected(m);
        }
        
        private void addRejected(int m){
            rejectionList.add(m);
        }
        
        public boolean hasRejected(int m){
            return rejected(m);
        }
        
        private boolean rejected(int m){
            //if it does contain M that means she has rejected him, otherwise she has not
            return rejectionList.contains(m);
        }
    }
    
    public static void main (String [] args){
        //System.out.println("Algorithm beginning...");
        printMarriages(algorithmStableMarriage());
        //System.out.println("Algorithm completed.\n");
    }
    
    private static void algorithmStableMarriage(int n){//n refers to the number of pairs to be used
        HashMap<Integer,Integer> rel = new HashMap<Integer,Integer>();//key,value
        for(int i = 0; i < n; ++i){
            rel.put(i,null);
        }
    }
    
    private static HashMap<Integer,Integer> algorithmStableMarriage(){
        HashMap<Integer,Integer> rel = new HashMap<Integer,Integer>();//key,value
        
        //matrix of mens preferences
        Woman[][] Q = new Woman[3][3];
        //man 1
        Q[0][0] = new Woman(0); 
        Q[0][1] = new Woman(1); 
        Q[0][2] = new Woman(2); 
        //man 2
        Q[1][0] = new Woman(0); 
        Q[1][1] = new Woman(2); 
        Q[1][2] = new Woman(1); 
        //man 3
        Q[2][0] = new Woman(0); 
        Q[2][1] = new Woman(1); 
        Q[2][2] = new Woman(2); 

        //matrix of womens preferences
        Man[][] P = new Man[3][3];
        //woman 1
        P[0][0] = new Man(1); 
        P[0][1] = new Man(0); 
        P[0][2] = new Man(2); 
        //woman 2
        P[1][0] = new Man(1); 
        P[1][1] = new Man(0); 
        P[1][2] = new Man(2); 
        //woman 3
        P[2][0] = new Man(0); 
        P[2][1] = new Man(1); 
        P[2][2] = new Man(2); 
        
        rel.put(0, -1);
        rel.put(1,-1);
        rel.put(2,-1);
        while(!isFull(rel)){
            Man m = new Man(getNextMan(rel));//really the key from the hashmap of a man
            //System.out.println("Processing Man: " + m.getId());
            int x = 0;
            if(rel.get(m.getId()) == -1){
                x = -1;
                Woman w;
                do{
                    ++x;
                    //System.out.println("Checking Woman: " + Q[m.getId()][x].getId());
                    w = Q[m.getId()][x];//grab specific woman from the matrix
                }while(w.hasRejected(m.getId()));//continue looping while w has rejected M so that when it breaks, its a woman who hasn't rejected m
                //System.out.println("Processing Woman: " + w.getId());
                if(!rel.containsValue(w.getId())){//w is not any mans lover
                    //System.out.println("Woman("+w.getId()+") is not the lover of Man("+m.getId()+") so pair them up.");
                    rel.put(m.getId(), w.getId());
                }else{
                    //System.out.println("Woman("+w.getId()+") already has a lover...further processing required");
                    //shes already a lover, but will she change lovers...thats the question
                    //System.out.println("Attempting to getKey("+w.getId()+")");
                    Man mPrime = new Man(getKey(rel, w.getId()));//the man that w is currently involved in
                    int y = 0;
                    Man mChoice;
                    for(int i = w.getId(); y < Q.length; ++y){
                        mChoice = P[i][y];
                        //System.out.println("Comparing \nMan(mChoice): " + mChoice.getId() + "\nMan(mPrime): " + mPrime.getId() + "\nMan(m): " + m.getId());
                        if(mChoice.getId() == mPrime.getId()){//if they have the same id's they're the same dudes
                            //System.out.println("Woman: " + w.getId() + " rejects Man: " + m.getId());
                            w.addToRejected(m.getId());
                            break;
                        }else if(mChoice.getId() == m.getId()){//again same sterff
                            w.addToRejected(mPrime.getId());
                            //System.out.println("Woman: " + w.getId() + " rejects Man: " + mPrime.getId() + " and accepts Man: " + m.getId());
                            rel.put(mPrime.getId(), -1);
                            rel.put(m.getId(), w.getId());
                            break;
                        }
                    }
                }
            }
        }
        //System.out.println("rel:\n"+rel.toString());
        return rel;
    }
    
    private static void printMarriages(HashMap<Integer, Integer> hm){
        System.out.println("Marriage Pairs:\n\n");
        for(Map.Entry<Integer,Integer> e : hm.entrySet()){
            System.out.println("\tMan:" + e.getKey() + " paired with Woman:" + e.getValue() + "\n");
        }
    }
    
    //If the hashmap does not contain any key with a value of null that means everything is paired up and thus "is full"
    private static boolean isFull(HashMap<Integer, Integer> hm){
        return !hm.containsValue(-1);
    }
    
    private static int getKey(HashMap<Integer, Integer> hm, int v){
        for(Map.Entry<Integer,Integer> e : hm.entrySet()){
            if(e.getValue() == v){
                return e.getKey();
            }
        }
        return -1;
    }
    
    private static int getNextMan(HashMap<Integer, Integer> hm){
        for(Map.Entry<Integer,Integer> e : hm.entrySet()){
            if(e.getValue() == -1){
                return e.getKey();
            }
        }
        return -1;
    }
    
}
