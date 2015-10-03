/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package algorithms;
import java.lang.*;

/**
 *
 * @author cpatton
 */
public class CustomHeap {
    
    private static class CustomInt{
        private int value;
        private boolean sorted;

        public CustomInt(int val){
            this.value = val;
            this.sorted = false;
        }
        
        public void setValue(int val){
            this.value = val;
        }
        
        public int getValue(){
            return this.value;
        }
        
        public void setSorted(boolean sorted){
            this.sorted = sorted;
        }
        
        public boolean isSorted(){
            return this.sorted;
        }
    }
    
    //will still need accessor and mutators
    public CustomHeap(int[] nodes){
        CustomHeap.CustomInt[] heap = new CustomHeap.CustomInt[nodes.length];
        //given an integer array, transfer everything into the CustomInt array
        for(int i = 1; i <= nodes.length; ++i){
            heap[i] = new CustomHeap.CustomInt(nodes[i]);
        }
        int heapSize=0;
        //contruct CustomInt array as a heap
        for(int i = 1; i <= heap.length; ++i){
            ++heapSize;
            //System.out.println("Inserting element " + heapSize + " with value of " + heap[heapSize-1].getValue());
            insert(heap, i, heapSize);
        }
    }
    
    public static void main (String[] args){
        //always minus 1 for going to parent and plus 1 for going to children
        String unsortedHeap = "[30,88,21,5,11,43,1,13,12,3,53,32,86,190,17,117,24,76,65,80,19,39,22,99,81,69,16,61,55,17]";
        //int[] heap = {30,88,21,5,11,43,1,13};
        CustomHeap.CustomInt[] heap = {
            new CustomHeap.CustomInt(30),
            new CustomHeap.CustomInt(88),
            new CustomHeap.CustomInt(21),
            new CustomHeap.CustomInt(5),
            new CustomHeap.CustomInt(11),
            new CustomHeap.CustomInt(43),
            new CustomHeap.CustomInt(1),
            new CustomHeap.CustomInt(13),
            new CustomHeap.CustomInt(12),
            new CustomHeap.CustomInt(3),
            new CustomHeap.CustomInt(53),
            new CustomHeap.CustomInt(32),
            new CustomHeap.CustomInt(86),
            new CustomHeap.CustomInt(190),
            new CustomHeap.CustomInt(17),
            new CustomHeap.CustomInt(117),
            new CustomHeap.CustomInt(24),
            new CustomHeap.CustomInt(76),
            new CustomHeap.CustomInt(65),
            new CustomHeap.CustomInt(80),
            new CustomHeap.CustomInt(19),
            new CustomHeap.CustomInt(39),
            new CustomHeap.CustomInt(22),
            new CustomHeap.CustomInt(99),
            new CustomHeap.CustomInt(81),
            new CustomHeap.CustomInt(69),
            new CustomHeap.CustomInt(16),
            new CustomHeap.CustomInt(61),
            new CustomHeap.CustomInt(55),
            new CustomHeap.CustomInt(17),
        };
        int heapSize = 0;
        for(int i = 1; i <= heap.length; ++i){
            ++heapSize;
            //System.out.println("Inserting element " + heapSize + " with value of " + heap[heapSize-1].getValue());
            insert(heap, i, heapSize);
        }
        System.out.print("Unsorted Heap: "+ unsortedHeap+"\nSorted Heap: [");
        for(int k = 0; k < heap.length; ++k){
            System.out.print(heap[k].getValue());
            if(k!=heap.length-1){
                System.out.print(",");
            }
        }
        System.out.println("]");
        
        heapSize = heap.length;
        for(int i = 1; i < heap.length; ++i){
            //System.out.println("Replacing element " + 1 + " with value of " + heap[heapSize-1].getValue());
            removeMin(heap, 1, heapSize);
            --heapSize;
        }
        System.out.print("Sorted items sm-lg: [");
        for(int k = heap.length-1; k >= 0; --k){
            System.out.print(heap[k].getValue());
            if(k!=0){
                System.out.print(",");
            }
        }
        System.out.println("]");
        
    }
    
    private static CustomHeap.CustomInt[] removeMin(CustomHeap.CustomInt[] heap, int v, int hSize){
        if(hSize == 0){//very last element
            return heap;
        }else if(hSize == 1){//only two elements left
            //System.out.print("heap only has two elements, ");
            if(heap[v-1].getValue() > heap[getParent(v)-1].getValue()){
                //System.out.println("swap " + heap[v-1].getValue() + " with " + heap[getLeftChild(v)-1].getValue() + ", return");
                heap[v-1].setSorted(true);
                return swap(heap,hSize, v);
            }else{
                //System.out.println("return");
                return heap;
            }
        }else{
            //System.out.println("More than 2 elements, must down heap bubble");
            heap[v-1].setSorted(true);
            return downHeapBubble(swap(heap, hSize, v), v, hSize);
        }
    }
    
    private static CustomHeap.CustomInt[] insert(CustomHeap.CustomInt[] heap, int v, int hSize){
        //System.out.println("...beginning insert()");
        if(hSize <= 1){
            //System.out.println("heap only has 1 element, return");
            return heap;
        }else if(hSize == 2){
            //System.out.print("heap only has two elements, ");
            if(heap[v-1].getValue() < heap[getParent(v)-1].getValue()){
                //System.out.println("swap " + heap[v-1].getValue() + " with " + heap[getLeftChild(v)-1].getValue() + ", return");
                return swap(heap,v, getLeftChild(v));
            }else{
                //System.out.println("return");
                return heap;
            }
        }else{
            //System.out.println("More than 2 elements, must up heap bubble");
            return upHeapBubble(heap, v);
        }
    }
    
    private static CustomHeap.CustomInt[] swap(CustomHeap.CustomInt[] heap, int v, int k){
        //System.out.println("Swapping element " + v + " with " + k);
        CustomInt temp = heap[v-1];
        heap[v-1]=heap[k-1];
        heap[k-1]=temp;
        return heap;
    }
    
    private static CustomHeap.CustomInt[] upHeapBubble(CustomHeap.CustomInt[] h, int v){
        //System.out.println("...beginning upHeapBubble()");
        if(getParent(v)!= 0 && h[v-1].getValue() < h[getParent(v)-1].getValue()){
            //System.out.println("moving up a level");
            return upHeapBubble(swap(h, v, getParent(v)), getParent(v));//getParent(v) becomes the new index of v since it's moving up in the heap
        }else{
            //System.out.println("returning out");
            return h;
        }
    }
    
    private static CustomHeap.CustomInt[] downHeapBubble(CustomHeap.CustomInt[] h, int v, int hSize){
        //System.out.println("...beginning downHeapBubble()");
        if(v<hSize-1){//the problem is is that the index is 4, and is < hSize-1 = 6 but it should STOP....maybe turn ints into objects
                if(getLeftChild(v)> h.length || h[getLeftChild(v)-1].isSorted()){
                    return h;
                }else if(getRightChild(v)> h.length || h[getRightChild(v)-1].isSorted()){
                    if(getLeftChild(v)<= h.length && !h[getLeftChild(v)-1].isSorted() && (h[v-1].getValue() > h[getLeftChild(v)-1].getValue())){
                       return downHeapBubble(swap(h, getLeftChild(v), v), getLeftChild(v), hSize); 
                    }else{
                        return h;
                    }
                }
                if(h[v-1].getValue() > h[getLeftChild(v)-1].getValue() || h[v-1].getValue() > h[getRightChild(v)-1].getValue()){//greater than both maybe
                    if(h[getLeftChild(v)-1].getValue() <= h[getRightChild(v)-1].getValue()){//swap v and left child
                        return downHeapBubble(swap(h, getLeftChild(v), v), getLeftChild(v), hSize);
                    }else{//swap v and right child
                        return downHeapBubble(swap(h, getRightChild(v), v), getRightChild(v), hSize);
                    }
                }else{
                    return h;
                }
        }else{
            return h;
        }
    }
    
    private static int getParent(int v){
        //System.out.println("calculating parent for index " + v + " which is " + v/2);
        return (v/2);
    }

    private static int getLeftChild(int v){
        //System.out.println("calculating left child for index " + v + " which is " + v*2);
        return (v*2);
    }
    
    private static int getRightChild(int v){
        //System.out.println("calculating right child for index " + v + " which is " + v*2+1);
        return (v*2)+1;
    }
}
