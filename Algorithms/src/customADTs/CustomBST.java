/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package customADTs;

/**
 *
 * @author dewit
 */
public class CustomBST<Key extends Comparable<Key>, Value> {
    //BST root
    private Node root;             

    private class Node {
        //key
        private Key key;  
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
    
    private int size(){
        return size(root);
    }
    
    private int size(Node n){
        if(n == null){
            return 0;
        }else{
            return n.subs;
        }
    }
    
    private Value get(Key k){
        return get(root, k);
    }
    
    private Value get(Node n, Key k){
        if(n == null){
            return null;
        }
        int comp = k.compareTo(n.key);
        if(comp < 0){//move left
            return get(n.left, n.key);
        }else if (comp > 0){//move right
            return get(n.right, n.key);
        }else{
            return n.val;
        }
    }
    
    private void put(Key k, Value v){
        if(v == null){
            return;//dont support this
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
    
    private void allDepths(){
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
