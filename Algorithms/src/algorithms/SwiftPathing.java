/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package algorithms;
import java.util.ArrayList;
import java.util.Random;
/**
 *
 * @author dewit
 */
public class SwiftPathing {
    

    public static void main(String[] args) {
        generate();
    }

    public enum assets {

        P_BR, P_BU, P_DU, P_FI, P_MO, P_MOSH, P_OB, P_PO, H, A_D, A_U, A_L, A_R, D_OW
    }

    private String realName(assets asset) {
        switch (asset) {
            case P_BR:
                return "@drawable/play_breakable";
            case P_BU:
                return "@drawable/play_bubble";
            case P_DU:
                return "@drawable/play_dude";
            case P_FI:
                return "@drawable/play_finish";
            case P_MO:
                return "@drawable/play_molten";
            case P_MOSH:
                return "@drawable/play_moltenshadow";
            case P_OB:
                return "@drawable/play_obstacle";
            case P_PO:
                return "@drawable/play_portal";
            case H:
                return "@drawable/hint";
            case A_D:
                return "@drawable/arrow_down";
            case A_U:
                return "@drawable/arrow_up";
            case A_L:
                return "@drawable/arrow_left";
            case A_R:
                return "@drawable/arrow_right";
            case D_OW:
                return "@dimen/obstacle_width";
            default:
                //should never happen
                return "@drawable/";
        }
    }

    
    //randomized a min number of moves from n-m (for now)
    private static void generate() {
        //say we actually have coordinates <-- will get later
        int[][] grid = new int[16][10];
        /* this was for a smaller test but you get the idea
        -1-1-1-1-1
        -1-1-1-1-1
        -1-1-1-1-1
        -1-1-1-1-1
        -1-1-1-1-1
        -1-1-1-1-1
        -1-1-1-1-1
        -1-1-1-1-1
         */
        Random randGen = new Random();
        
        int minNumMoves = 0;
        while(minNumMoves < 5){
            minNumMoves = randGen.nextInt(16);
        }
        //for now assume the start block starts in the top left most corner
        
        /*
            so in the matrix for any given n(x,y) x=columns, y=rows and m(x,y) there can't be anything in between n and m.
            but we put one block in n(x,y). the only other next block would be wither n(x+/-1,2,..n, y) or n(x,y+/-1,2,..,n)  basically either horizontally or vertically.
            so we randomly choose to either go vertical or horizontal however, we cannot go BACk the way we came ever so there are only 3 possibilities
            wherever we go, the next block will be UP one row. meaning if the block we placed is n(x,y) the block horizontally will need to be m(x+/-1,y+/-1) based off which side of a block we're on
            each time we place a block tho, we have to mark out where blocks cannot be placed. so if a block is at n(x,y) and we place one at say m(x, y-3) then we mark off n(x, y-1) and n(x, y-2)
            we can pass through (the opposite directional plane) by swiping but not putting a block in there so the marks only refer to PLACING a block.        
            then we just do this as many times as minNumMoves.
            
        */
        
        //initialize whole grid to -1
        for(int i = 0; i < grid.length; ++i) {
            for (int j = 0; j < grid[i].length; ++j) {
                grid[i][j] = -1;
            }
        }
        //set the first movement block in upper left hand
        grid[0/*row*/][0/*column*/] = 0;
        print(grid);

        try{
            populateGrid(grid, 0, 0, minNumMoves, 10, 16, randGen);
        }catch(IllegalStateException ie){
            
        }catch(Exception e){
            
        }
        
        
    }
    
    //-1 indicates a free space
    //0 indicated a block
    //1 indicates an unavaliable space
    //returns a 1 is it was successful, otherwise 0
    private static int populateGrid(int[][] g, int x, int y, int moves, int width, int height, Random R) throws IllegalStateException, Exception{
        try{
        /*
        
         North
         X
         South
        
         East Y West
         */
        
        //
        int counter = 0;
        int xDir = x;
        int yDir = y;
        int xPos = x;
        int yPos = y;
        boolean north = false;
        boolean south = false;
        boolean east = false;
        boolean west = false;
        
        /*backups for if we need to revert and retry another path -- these may not be needed but keeping them around for nows*/

                
        System.out.println("moves = " + moves);
        int lastDir = 0;//1=north, 2=south, 3=east, 4=wests
        while (counter < moves) {
            //must reset these each runthrough
            int block_north = -1;
            int block_south = -1;
            int block_east = -1;
            int block_west = -1;
            System.out.println("yPos = " + yPos + "\nxPos = " + xPos + "\nxDir = " + xDir + "\nyDir = " + yDir + "\nwidth = " + width + "\nheight = " + height);
            if (yDir < 2 || g[xDir][yDir-2]==1) {//furthest left
                west = false;
                System.out.println("west is impossible");
            } else {
                if(g[xDir][yDir-1] == 0 || g[xDir][yDir-2] == 0){//impossible to go this direction if either the direct next or next two blocks is blocked
                    west = false;
                    System.out.println("west is impossible");
                }else{
                    for (int i = yDir - 2; i >= 0; --i) {
                        //System.out.println("checking left");
                        if (g[xDir][i] == 0) {
                            System.out.println("west blocked but retracable");
                            block_west = i;//used in y value 
                            break;
                        }
                    }
                    west = true;
                }
            }
            if (yDir >= width - 2 || g[xDir][yDir+2]==1) {//furthest right 
                east = false;
                System.out.println("east is impossible");
            } else {
                if(g[xDir][yDir+1] == 0 || g[xDir][yDir+2] == 0){//impossible to go this direction if either the direct next or next two blocks is blocked
                    east = false;
                    System.out.println("east is impossible");
                }else{
                    for (int i = yDir + 2; i < width; ++i) {
                        //System.out.println("checking right");
                        if (g[xDir][i] == 0) {
                            System.out.println("east is blocked but retracable");
                            block_east = i;//used in y value
                            break;
                        }
                    }
                    east = true;
                }
            }
            if (xDir < 2 || g[xDir-2][yDir]==1) {//furthest north
                north = false;
                System.out.println("north is impossible");
            } else {
                if(g[xDir-1][yDir] == 0 || g[xDir-2][yDir] == 0){//impossible to go this direction if either the direct next or next two blocks is blocked
                    north = false;
                    System.out.println("north is impossible");
                }else{
                    for (int i = xDir - 2; i >= 0; --i) {
                        //System.out.println("checking up");
                        if ( g[i][yDir] == 0) {
                            System.out.println("north blocked but possible to retrace");
                            block_north = i;//used in x value
                            break;
                        }
                    }
                    north = true;
                }
            }
            if (xDir >= height - 2 || g[xDir+2][yDir]==1) {//furthest south
                south = false;
                System.out.println("south is impossible");
            } else {
                if(g[xDir+1][yDir] == 0 || g[xDir+2][yDir] == 0){//impossible to go this direction if either the direct next or next two blocks is blocked
                    south = false;
                    System.out.println("south is impossible");
                }else{
                    for (int i = xDir + 2; i < height; ++i) {
                        //System.out.println("checking down");
                        if (g[i][yDir] == 0) {
                            System.out.println("south blocked but retracable");
                            block_south = i;//used in x value
                            break;
                        }
                    }
                    south = true;
                }
            }
            //we know that we cannot go directly back from whichever way we came from (except a special case), and that we cannot go forward which "through" the block
            switch(lastDir){
                case 1:
                    System.out.println("north is impossible");
                    north = false;
                    //south is only impossible if there are not two open spaces and NO blocks behind it 
                    south = false;
                    //south is only impossible if there are not two open spaces and NO blocks behind it <--false...thats stupid to backtrack
                    for(int i = xDir+1; i<height; ++i){
                        if (g[i][yDir]==-1 ){//make sure we aren't jumping over any blocks
                            if(g[i-1][yDir]!=0){
                                //its POSSIBLE to go this direction
                                south = true;
                                break;
                            }else{
                                south = false;
                                break;
                            }
                        }
                    }
                    break;
                case 2:
                    System.out.println("south is impossible");
                    south = false;
                    north = false;
                    //north is only impossible if there are not two open spaces and NO blocks behind it <false..
                    for(int i = xDir-1; i>=0; --i){
                        if (g[i][yDir]==-1){
                            if(g[i+1][yDir]!=0){
                                //its POSSIBLE to go this direction
                                north = true;
                                break;
                            }else{
                                north = false;
                                break;
                            }
                        }
                    }
                    break;
                case 3:
                    System.out.println("east is impossible");
                    east = false;
                    //default to false just in case
                    west = false;
                    //west is only impossible if there are not two open spaces or  one block behind exactly past the open spaces, it can just use that block to backtrack<-- false
                    for(int i = yDir-1; i>=0; --i){
                        if (g[xDir][i]==-1){
                            if(g[xDir][i+1]!=0){
                                //its POSSIBLE to go this direction
                                west = true;
                                break;
                            }else{
                                west = false;
                                break;
                            }
                        }
                    }
                    break;
                case 4:
                    System.out.println("west is impossible");
                    west = false;
                    east = false;
                    //east is only impossible if there are not two open spaces and NO blocks behind it <-- false
                    for(int i = yDir+1; i<width; ++i){
                        if (g[xDir][i]==-1){
                            if(g[xDir][i-1]!=0){
                                //its POSSIBLE to go this direction
                                east = true;
                                break;
                            }else{
                                east = false;
                                break;
                            }
                        }
                    }
                    break;
            }
            //add all possible directions to the list
            ArrayList<Integer> possibleDirs = new ArrayList();
            if (north) {//1
                System.out.println("adding north");
                possibleDirs.add(1);
            }
            if (south) {//2
                System.out.println("adding south");
                possibleDirs.add(2);
            }
            if (east) {//3
                System.out.println("adding east");
                possibleDirs.add(3);
            }
            if (west) {//4
                System.out.println("adding west");
                possibleDirs.add(4);
            }

            //just storing the previous directions before any changes occus
            int previousX = xDir;
            int previousY = yDir;
            try {
                if (possibleDirs.isEmpty()) {
                    throw new IllegalStateException();
                }
            } catch (IllegalStateException ie) {
                throw new IllegalStateException("Every direction is impossible", ie);
            }
            //randomly get a value based off the number of directions possible to go
            int randDir = possibleDirs.get(R.nextInt(possibleDirs.size()));
            
            //indicates if a block is between the current position and the position in the desired direction.
            boolean blocked = false;
            
            int infinityCounter = 0;
            try {
                //figure out the next x or y
                switch (randDir) {
                    case 1://north
                        lastDir = 1;
                        System.out.println("randomly choose north");
                        //decreasing upwards
                        if (block_north > 0) {//this allows the use of another side of a block
                            xDir = block_north;
                        } else {
                            int tempXDir = R.nextInt(xDir - 1);//beacuse the upper bound is not included
                            System.out.println("trying to use tempXDir as " + tempXDir);
                            while (g[tempXDir][yDir] == 1 || blocked/*|| g[tempXDir][yDir]==0*/) {
                                System.out.println("trying AGAIN to use tempXDir as " + tempXDir);
                                tempXDir = R.nextInt(xDir - 1);//beacuse the upper bound is not included
                                blocked = false;
                                //this is to check that there are no blocks in the line of sight to the next block
                                for (int i = previousX; (i >= 0) && i > tempXDir; --i) {
                                    //if we make it all the way down the line we need a new direction...
                                    System.out.println("i = " + i);
                                    if (g[i][yDir] == 0) {
                                        blocked = true;
                                        break;
                                    }
                                }
                                ++infinityCounter;
                                if (infinityCounter > 100) {
                                    throw new IllegalStateException();
                                }
                            }
                            xDir = tempXDir;
                        }
                        System.out.println("new position to place block is (" + xDir + "," + yDir + ")");
                        g[xDir][yDir] = 0;
                        ++xDir;
                        g[xDir][yDir] = 8;
                        System.out.println("new position for movement block is block is (" + xDir + "," + yDir + ")");
                        for (int i = xDir + 1; (i < height) && i <= previousX && (g[i][yDir] != 0); ++i) {
                            g[i][yDir] = 1;
                        }
                        break;
                    case 2://south
                        //increasing downwards
                        lastDir = 2;
                        System.out.println("randomly choose south");
                        if (block_south > 0) {//this allows the use of another side of a block
                            xDir = block_south;
                        } else {
                            //xPos = xDir;
                            xPos = ((height - 1) - (xDir + 2)) > 0 ? R.nextInt(((height - 1) - (xDir + 2))) + (xDir + 2) : (xDir + 2);
                            System.out.println("trying to use xPos as " + xPos);
                            while (g[xPos][yDir] == 1 || blocked/*|| g[xPos][yDir]==0*/) {
                                System.out.println("trying AIAIN to use xPos as " + xPos);
                                xPos = R.nextInt(((height - 1) - xDir + 2)) + (xDir + 2);
                                blocked = false;
                                //this is to check that there are no blocks in the line of sight to the next block
                                for (int i = previousX; (i < height) && i < xPos; ++i) {
                                    System.out.println("i = " + i);
                                    if (g[i][yDir] == 0) {
                                        blocked = true;
                                        break;
                                    }
                                }
                                ++infinityCounter;
                                if (infinityCounter > 100) {
                                    throw new IllegalStateException();
                                }
                            }
                            xDir = xPos;
                        }
                        g[xDir][yDir] = 0;
                        --xDir;
                        g[xDir][yDir] = 8;
                        System.out.println("new position to place block is (" + xDir + "," + yDir + ")");
                        for (int i = xDir - 1; (i >= 0) && i >= previousX && (g[i][yDir] != 0); --i) {
                            System.out.println("i = " + i);
                            g[i][yDir] = 1;
                        }
                        break;
                    case 3://east
                        //increasing to the right
                        lastDir = 3;
                        System.out.println("randomly choose east");
                        if (block_east > 0) {
                            yDir = block_east;
                        } else {
                            //yPos = yDir;
                            yPos = ((width - 1) - (yDir + 2)) > 0 ? R.nextInt(((width - 1) - (yDir + 2))) + (yDir + 2) : (yDir + 2);
                            System.out.println("trying to use yPos as " + yPos);
                            while (g[xDir][yPos] == 1 || blocked/*|| g[xDir][yPos]==0*/) {
                                System.out.println("trying AGAIN to use yDir as " + yPos);
                                yPos = R.nextInt(((width - 1) - (yDir + 2))) + (yDir + 2);
                                blocked = false;
                                //this is to check that there are no blocks in the line of sight to the next block
                                for (int i = previousY; (i < width) && i < yPos; ++i) {
                                    System.out.println("i = " + i);
                                    if (g[xDir][i] == 0) {
                                        blocked = true;
                                        break;
                                    }
                                }
                                ++infinityCounter;
                                if (infinityCounter > 100) {
                                    throw new IllegalStateException();
                                }
                            }
                            yDir = yPos;
                        }
                        g[xDir][yDir] = 0;
                        --yDir;
                        g[xDir][yDir] = 8;
                        System.out.println("new position to place block is (" + xDir + "," + yDir + ")");
                        for (int i = yDir - 1; (i >= 0) && i >= previousY && (g[xDir][i] != 0); --i) {
                            System.out.println("i = " + i);
                            g[xDir][i] = 1;
                        }
                        break;
                    case 4://west
                        //descreasing to the left
                        lastDir = 4;
                        System.out.println("randomly choose west");
                        if (block_west > 0) {
                            yDir = block_west;
                        } else {
                            int tempYDir = R.nextInt(yDir - 1);//because the upper bound is not included
                            System.out.println("trying to use yDir as " + yDir);
                            while (g[xDir][tempYDir] == 1 || blocked/*|| g[xDir][tempYDir]==0*/) {
                                tempYDir = R.nextInt(yDir - 1);
                                blocked = false;
                                //this is to check that there are no blocks in the line of sight to the next block
                                for (int i = previousY; (i >= 0) && i > tempYDir; --i) {
                                    System.out.println("i = " + i);
                                    if (g[xDir][i] == 0) {
                                        blocked = true;
                                        break;
                                    }
                                }
                                System.out.println("trying AGAIN to use yDir as " + yDir);
                                ++infinityCounter;
                                if (infinityCounter > 100) {
                                    throw new IllegalStateException();
                                }
                            }
                            yDir = tempYDir;
                        }
                        g[xDir][yDir] = 0;
                        ++yDir;
                        g[xDir][yDir] = 8;
                        System.out.println("new position to place block is (" + xDir + "," + yDir + ")");
                        for (int i = yDir + 1; (i < width) && i <= previousY && (g[xDir][i] != 0); ++i) {
                            System.out.println("i = " + i);
                            g[xDir][i] = 1;
                        }
                        break;
                }
            } catch (IllegalStateException is) {
                throw new IllegalStateException("Stuck in an infinite loop due to the limited number of directions possible", is);
            } catch (Exception e){
                e.printStackTrace();
                throw new Exception("Some unknow error happened?!", e);
            }
                if(counter == moves && xDir-2 == 0 && yDir-2 == 0){continue;}
                else{
                    ++counter;
                }
            //print for logging purposes
            print(g);
        }
        }catch(IllegalStateException is){
                throw new IllegalStateException(is.getMessage(), is);
        }catch(Exception e){
                throw new Exception(e.getMessage(), e);
        }
    }

    //print out the matrix for testing and logging purposes
    private static void print(int[][] grid) {
        for (int i = 0; i < grid.length; ++i) {
            for (int j = 0; j < grid[i].length; ++j) {
                System.out.print(grid[i][j] + "\t");
            }
            System.out.print("\n");

        }
    }

}

