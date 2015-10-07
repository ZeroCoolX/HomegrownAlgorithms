/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package algorithms;

import customADTs.CustomHeap;
import static customADTs.CustomHeap.inPlaceSort;
import static customADTs.CustomHeap.printHeapSort;

/**
 *
 * @author dewit
 */
public class HeapSort {
    public static void main(String [] args){
        algorithmHeapSort();
    }
    
    private static void algorithmHeapSort(){
            //always minus 1 for going to parent and plus 1 for going to children
        int[] unsortedHeap = {30,88,21,5,11,43,1,13,12,3,53,32,86,190,17,117,24,76,65,80,19,39,22,99,81,69,16,61,55,17};
        CustomHeap heap = new CustomHeap(unsortedHeap);
        printHeapSort(heap.getHeapArray());  
        inPlaceSort(heap.getHeapArray());
        printHeapSort(heap.getHeapArray());
    }
}
