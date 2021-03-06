/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package customADTs;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

/**
 *
 * @author dewit
 * @param <Key>
 * @param <Value>
 */
public class CustomBST<Key extends Comparable<Key>, Value> {
    //BST root
    private Node root;             

    private class Node {
        //key
        private final Key key;  
        //value
        private Value val;   
        //right and left subtree, parent
        private Node left, right, parent;  
        //Number of nodes in subtree (including itself)
        private int subs;
        //x coordinate for printing
        private int x;   
        //y coordinate for printing
        private int y;
        //depth for specific node
        private int depth;
        //testing ability to count children beneath a node and store it
        private int children;
        
        public int getKey(){
            return (Integer)key;
        }
        public Node(Key key, Value val, int subs) {
            this.key = key;
            this.val = val;
            this.subs = subs;
        }
    }
    
    //constructor
    public CustomBST(){}
    
    public boolean isEmpty(){
        return size() == 0;
    }
    
    public int allSizes(){
        return allSizes(root);
    }
    
    private int allSizes(Node n){
        if(isExternal(n)){
            n.children = 1;
        }else{
            if(n.left != null){
                n.children += allSizes(n.left);
            }
            if(n.right != null){
                n.children += allSizes(n.right);
            }
            ++n.children;
        }
        return n.children;
    }
    
    public int size(){
        return size(root);
    }
    
    private int size(Node n){
        if(n == null){
            return 0;
        }else{
            return n.subs;
        }
    }
    
    
    public Value get(Key k){
        return get(root, k);
    }

    private Value get(Node n, Key k){
        if(n == null){
            return null;
        }
        int comp = k.compareTo(n.key);
        if(comp < 0){//move left
            return get(n.left, k);
        }else if (comp > 0){//move right
            return get(n.right, k);
        }else{
            return n.val;
        }
    }
    
    public void put(Key k, Value v){
        if(v == null){
            return;//dont support this...
        }else{
            root = put(root, k, v);
        }
    }
    
    private Node put(Node n, Key k, Value v){
        if(n == null){
            return new Node(k, v, 1);
        }
        int comp = k.compareTo(n.key);
        //left of node
        if(comp < 0){
            n.left = put(n.left, k, v);
            n.left.parent = n;
        }else if (comp > 0){//right of node
            n.right = put(n.right, k, v);
            n.right.parent = n;
        }else{//no dupes so change val
            n.val = v;
        }
        n.subs = 1+size(n.left) + size(n.right);
        return n;
    }
    
    private boolean isExternal(Node n){
        return (n.right == null && n.left == null);
    }
    
    public int height(){
        return height(root);
    }
    
    private int height(Node n){
        if(isExternal(n)){//external
            return 0;
        }else{
            int h = 0;
            if(n.left != null){
                h = Math.max(h, height(n.left));
            }
            if(n.right != null){
                h = Math.max(h, height(n.right));
            }
            return 1 + h;
        }
    }
    
    public void preOrderDataPrint(){
        preOrderDataPrint(root);
    }
    
    
    private void preOrderDataPrint(Node n){
        System.out.println("n.key = " + n.key + "\t\tn.val = " + n.val + "\t\tn.children = " + n.children + "\t\tn.coordinates = (" + n.x + "," + n.y + ")");
        if(n.left != null){
            preOrderDataPrint(n.left);
        }
        if(n.right != null){
            preOrderDataPrint(n.right);
        }
    }
    
    private int[][] createMatrix(){
        int[][] matrix = new int [height()+1][size()];
        for(int i = 0; i < matrix.length; ++ i){
            for(int j = 0; j < matrix[i].length; ++j){
                matrix[i][j] = -1;
            }
        }
        return createMatrix(root,matrix);
    }
    
    private int[][] createMatrix(Node n, int [][] matrix){
        if(n == null){
            return matrix;
        }
        System.out.println("storing key "  +n.key + "in ["+n.x+"]["+n.y+"]");
        matrix[n.x][n.y] = n.getKey();//process n
        createMatrix(n.left, matrix);//left
        createMatrix(n.right, matrix);//right
        return matrix;
        
    }
    
    public void drawTree(){
        drawTree(createMatrix());
    }
    
    /*example of test tree
            4
          /   \
         2     6
        / \   / \
       1   3 5   7
                  \
                   9
        
    */
    private void drawTree(int[][] matrix){
        //process node then left and right then continue
        for(int i = 0; i < matrix.length; ++ i){
            boolean leftMost = true;
            int padding = 0;
            for(int j = 0; j < matrix[i].length; ++j){
                
                if(matrix[i][j] > -1){
                    for(int k = 0; k < (leftMost?j:j-padding); ++k){
                        System.out.print("\t");
                    }
                    padding = j;
                    System.out.print(matrix[i][j]);
                    leftMost = false;
                }
            }
            System.out.print("\n");
        }
    }
    
    public void algorithmPrettyPrint(){
        allDepths();
        algorithmPrettyPrint(root, 0);
        preOrderDataPrint();
    }
    
    private void algorithmPrettyPrint(Node n, int breaker){
        if(n == null){
            return;
        }
        ++breaker;
        n.x = n.depth;
        if(n == root || n.key.compareTo(n.parent.key) <= 0){
            //its on the left or root -> meaning 1st
            if(n == root){
                n.y = countChildren(n.left);
            }else{
                if(n.right == null){
                    n.y = n.parent.y-1;
                }else{
                    n.y = n.parent.y - (1 + countChildren(n.right));
                }
            }
        }else{
            if(n.left == null){
                n.y = n.parent.y+1;
            }else{
                n.y = n.parent.y + (1 + countChildren(n.left));
            }
        }
        if(breaker > size()){
            return;
        }else{
            algorithmPrettyPrint(n.left, breaker);
            algorithmPrettyPrint(n.right, breaker);
        }
    }
    
    public int countChildren(){
        return countChildren(root);
    }
    
    private int countChildren(Node n){
        return size(n);
    }
    
    public void allDepths(){
        allDepths(root);
    }
    
    private void allDepths(Node n){
        if(n == root){
            n.depth = 0;
        }else{
            n.depth = 1 + n.parent.depth;
        }
        if(n.left != null){
            allDepths(n.left);
        }
        if(n.right != null){
            allDepths(n.right);
        }
    }
}
