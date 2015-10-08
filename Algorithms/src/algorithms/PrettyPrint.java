/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package algorithms;

import customADTs.CustomBST;
import java.util.NoSuchElementException;

/**
 *
 * @author dewit
 */
public class PrettyPrint {
    
public static void main(String [] args){
        /*testing*/
        CustomBST b = new CustomBST();
        b.put(4, "d");
        b.put(2, "b");
        b.put(3, "c");
        b.put(6, "f");
        b.put(1, "a");
        b.put(5, "e");
        b.put(7, "g");
        b.put(9, "i");
        //System.out.println("size = " + b.size());
        //System.out.println("root.height = " + b.height());
        b.algorithmPrettyPrint();
    }
    
}
