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
    
    private class Man{
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
    
    private class Woman{
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
                
    }
    
    private static void algorithmStableMarriage(int n){//n refers to the number of pairs to be used
        HashMap<Integer,Integer> rel = new HashMap<Integer,Integer>();//key,value
        for(int i = 0; i < n; ++i){
            rel.put(i,null);
        }
    }
    
    private void algorithmStableMarriage(){
        HashMap<Integer,Integer> rel = new HashMap<Integer,Integer>();//key,value
        Man[][] Q = new Man[3][3];
        Woman[][] P = new Woman[3][3];
        rel.put(0, null);
        rel.put(1,null);
        rel.put(2,null);
        while(!isFull(rel)){
            Man m = new Man(getNextMan(rel));//really the key from the hashmap of a man
            int x = 0;
            if(rel.get(m.getId()) == null){
                x = -1;
                Woman w;
                do{
                    ++x;
                    w = P[m.getId()][x];//grab specific woman from the matrix
                }while(w.hasRejected(m.getId()));//continue looping while w has rejected M so that when it breaks, its a woman who hasn't rejected m
                if(!rel.containsValue(w.getId())){//w is not any mans lover
                    rel.put(m.getId(), w.getId());
                }else{
                    //shes already a lover, but will she change lovers...thats the question
                }
            }
        }
    }
    
    //If the hashmap does not contain any key with a value of null that means everything is paired up and thus "is full"
    private boolean isFull(HashMap<Integer, Integer> hm){
        return !hm.containsValue(null);
    }
    
    private int getNextMan(HashMap<Integer, Integer> hm){
        for(Map.Entry<Integer,Integer> e : hm.entrySet()){
            if(e.getValue() == null){
                return e.getKey();
            }
        }
        return -1;
    }
    
}
