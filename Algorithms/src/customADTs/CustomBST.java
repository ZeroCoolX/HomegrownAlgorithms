/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package customADTs;

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
    
    /*doesn't work nicely yet...*/
    public Value get(Key k){
        return get(root, k);
    }
    /*nope...not yet*/
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
    
    public void put(Key k, Value v){
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
    
    public void preOrder(){
        preOrder(root);
    }
    
    
private void preOrder(Node n){
        System.out.println("n.key = " + n.key + "\t\tn.val = " + n.val + "\t\tn.coordinates = (" + n.x + "," + n.y + ")");
        if(n.left != null){
            preOrder(n.left);
        }
        if(n.right != null){
            preOrder(n.right);
        }
}
    
    public void algorithmPrettyPrint(){
        allDepths();
        algorithmPrettyPrint(root, 0);
        preOrder();
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
