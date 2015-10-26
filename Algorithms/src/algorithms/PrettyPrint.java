/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package algorithms;

import customADTs.CustomBST;
import java.util.HashMap;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Random;

/**
 *
 * @author dewit
 */
public class PrettyPrint {
    
public static void main(String [] args){
        /*testing*/
        /*CustomBST b = new CustomBST();
        b.put(4, "d");
        b.put(2, "b");
        b.put(3, "c");
        b.put(6, "f");
        b.put(1, "a");
        b.put(5, "e");
        b.put(7, "g");
        b.put(9, "i");*/
        //testing dynamic building and pretty printing
        CustomBST b = fillToN(50, "abcdefghijklmnopqrstuvwxyz");
        b.algorithmPrettyPrint();
        b.drawTree();
        //System.out.println("grabbing value at key 6: " + b.get(6));
    }
    
    /*i%25 for alphabet*/
    public static CustomBST fillToN(int n, String valueSet) {
        CustomBST b = new CustomBST();
        HashMap<Integer, String> items = new HashMap();
        for (int i = 0; i < n; ++i) {
            String str = "";
            try {
                if (i % valueSet.length() < valueSet.length() - 1) {
                    str = valueSet.substring(i % valueSet.length(), (i + 1) % valueSet.length());
                } else if (i % valueSet.length() == valueSet.length() - 1) {
                    str = valueSet.substring(i % valueSet.length());
                }
                items.put((i+1), str);
            } catch (NullPointerException e) {
                e.printStackTrace();
            }
        }
        Random randomGenerator = new Random();
        while (!items.isEmpty()) {
            int rand = randomGenerator.nextInt(101);
            if (items.containsKey(rand)) {
                for (Map.Entry<Integer, String> e : items.entrySet()) {
                    if (e.getKey() == rand) {
                        //System.out.println("storing ("+e.getKey()+","+e.getValue()+")");
                        b.put(e.getKey(), e.getValue());
                        items.remove(rand);
                        break;
                    }
                }
            }
        }
        return b;
    }
    
}
