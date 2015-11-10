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
public class Block {
    
    //x and y coordinates for the block
    private int x;
    private int y;
    private int dirIntoBlock;//1=n, 2=s, 3=e, 4=w
    private int id;

    /*
    Indicates what type of block this is:
        0 - regular obstacle
        2 - bubble
        3 - breakable
        4 - portal
        5 - molten
        6 - ice
        8 - dude
       
        1 - traversible path
       -1 - free space
    
    */
    private int type;
    
    public Block(int x, int y, int type, int ID){
       this.x = x;
       this.y = y;
       this.type = type;this.id = ID;
    }
    
    
    public int getDirIntoBlock() {
        return dirIntoBlock;
    }

    public void setDirIntoBlock(int dirIntoBlock) {
        this.dirIntoBlock = dirIntoBlock;
    }

    public int getX() {
        return x;
    }

    public void setX(int x) {
        this.x = x;
    }

    public int getY() {
        return y;
    }

    public void setY(int y) {
        this.y = y;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }
    
    public String getCoordinates(){
        return "("+x+","+y+")";
    }
    
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }
    
}

