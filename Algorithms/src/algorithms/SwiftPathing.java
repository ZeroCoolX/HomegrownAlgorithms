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
        //arbitrary values for now testing.
        generate(16,10, 5, 16, 3);
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

    
    /* 
      height X width
        -1-1-1-1-1
        -1-1-1-1-1
        -1-1-1-1-1         
        -1-1-1-1-1
        -1-1-1-1-1
        -1-1-1-1-1
        -1-1-1-1-1
        -1-1-1-1-1
        
        SUMMARY of logic:
            so in the matrix for any given n(x,y) x=columns, y=rows and m(x,y) there can't be anything in between n and m.
            but we put one block in n(x,y). the only other next block would be wither n(x+/-1,2,..n, y) or n(x,y+/-1,2,..,n)  basically either horizontally or vertically.
            so we randomly choose to either go vertical or horizontal however, we cannot go BACk the way we came ever so there are only 3 possibilities
            wherever we go, the next block will be UP one row. meaning if the block we placed is n(x,y) the block horizontally will need to be m(x+/-1,y+/-1) based off which side of a block we're on
            each time we place a block tho, we have to mark out where blocks cannot be placed. so if a block is at n(x,y) and we place one at say m(x, y-3) then we mark off n(x, y-1) and n(x, y-2)
            we can pass through (the opposite directional plane) by swiping but not putting a block in there so the marks only refer to PLACING a block.        
            then we just do this as many times as minNumMoves.
    */
    private static void generate(int height, int width, int minMinMove, int maxMinMove, int numLevels) {
        //dimensions for the playable area.
        int[][] grid = new int[height][width];
        
        //Alwasy use the same instance of Random number generator so no duplicate sequence of randoms (oxymoronic I know but it happens) occur 
        Random randGen = new Random();
        
        //counter to keep track of how many solvable levels have been created
        int levelsCompleted = 0;
        
        //keep generating levels
        while(levelsCompleted < numLevels){   
            //min moves will be in the range of [minMinMove,maxMinMove]
            int minNumMoves = randGen.nextInt(maxMinMove-minMinMove)+minMinMove;
            
            //initialize whole grid to -1 (-1 signifies free spaces to place a block)
            for (int i = 0; i < grid.length; ++i) {
                for (int j = 0; j < grid[i].length; ++j) {
                    grid[i][j] = -1;
                }
            }
            
            //for now we assume the start block starts in the top left most corner. Will absolutely be randomly placed later on
            grid[0/*row*/][0/*column*/] = 0;
            System.out.println("--------------------"
                    + "----------------------------"
                    + "GENERING LEVEL "+ (levelsCompleted+1)
                    +"----------------------"
                    + "--------------------------");
            print(grid);
            try {
                //populate one level
                levelsCompleted += populateGrid(grid, 0, 0, minNumMoves, width, height, randGen);
            } catch (IllegalStateException ie) {
                System.out.println(ie.toString() + "\n\t"+ ie.getMessage());
            } catch (Exception e) {
                System.out.println(e.toString() + "\n\t"+ e.getMessage());
            }
        }
        
        
    }
    
    /*
    METHOD NOTES:
        The movable block that actually traverses the paths is referred to as the runner

        -1 indicates a free space
        0 indicated a block
        1 indicates an unavaliable space
        8 indicates the runner 
        returns a 1 is it was successful, otherwise error
    
    
        Cardinal directions. 
            
                                      North - decreasing x values
                                        X
           decreasing y values - East  Y Y  West - increasing y values
                                        X
                                      South - increasing x values
    */
    private static int populateGrid(int[][] g, int x, int y, int moves, int width, int height, Random R) throws IllegalStateException, Exception{
        try{
        //keeps track of how many moves in a level
        int counter = 0;
        //X and Y coordinate for the current runner
        //changes once per move (obviously)
        int xDir = x;
        int yDir = y;
        
        //temporary values used for holding the NEXT X or Y coordinate
        //changes a lot duing the course of the program
        int tempXDir = x;
        int tempYDir = y;
        
        //stored the X and Y coordinates of the starting position used later on to make sure the finish block it not right next to it 
        // because on average this would make the puzzle wayyyy easier to solve...and we don't want that >:)
        //never change till program exits
        int xStart = x;
        int yStart = y;
        
        //indicate true if a direction is traversable and false if it is not (blocked, or out of bounds)
        boolean north = false;
        boolean south = false;
        boolean east = false;
        boolean west = false;
                
        System.out.println("moves = " + moves);
        //holds the direction we last went to get to where we are now
        int lastDir = 0;//1=north, 2=south, 3=east, 4=wests
        
        /*
        MAIN MEAT
        each runthrough of the loop generates one path from one block to another
        as long as it doesn't get suck in an infinite loop or fail because it drove itself into a place it couldn't get out of it executes 
            the number of moves specified by the parameter
        EXCEPT unless the final block is adjacent to the start position. If this happens, then the logic below doens't increase the counter 
            and thus there could  technically be: moves+n (n=number of moves to get away from the start position)
        */
        while (counter < moves) {
            /*
            Indicate if there is a block somewhere exactly along the line the moveable block (dude) jsut came from.
            They let the runner know if it can presumably retrace its path to get out of some "stuck" corner or even to just add multiple ways of reaching a goal
            must reset these each runthrough
            */
            int block_north = -1;
            int block_south = -1;
            int block_east = -1;
            int block_west = -1;
            System.out.println("yPos = " + tempYDir + "\nxPos = " + tempXDir + "\nxDir = " + xDir + "\nyDir = " + yDir + "\nwidth = " + width + "\nheight = " + height);
            
            if (yDir < 2 || g[xDir][yDir-2]==1) {//runner cannot go west if IT is far west as possible (1)
                west = false;
                System.out.println("west is impossible");
            } else {
                if(g[xDir][yDir-1] == 0 || g[xDir][yDir-2] == 0){//impossible to go west direction if either the direct next or next two blocks are blocked (2)
                    west = false;//this is partly so we don't have a ton of very small (2 unit) swipes (3)
                    System.out.println("west is impossible");
                }else{//otherwise west is either completely traversable or retracable to exactly the first block in the row (4)
                    for (int i = yDir - 2; i >= 0; --i) {
                        if (g[xDir][i] == 0) {
                            System.out.println("west blocked but retracable");
                            //block locations stored so we know if west is chosen as the direction to traverse we MUST go exactly to this block. no further no lesser. (5)
                            block_west = i;
                            break;
                        }
                    }
                    west = true;
                }
            }
            /*
            NOTICE: 
                for the next three if blocks below, they all use the same logic juust with different details such as coordinates and directions so
                refer to the numbers corresponding to the above comments to understand code.
                In the future the common logic will be extracted but for now there's admittedly some repetition
            */
            if (yDir >= width - 2 || g[xDir][yDir+2]==1) { //(1)
                east = false;
                System.out.println("east is impossible");
            } else {
                if(g[xDir][yDir+1] == 0 || g[xDir][yDir+2] == 0){//(2)
                    east = false;//(3)
                    System.out.println("east is impossible");
                }else{//(4)
                    for (int i = yDir + 2; i < width; ++i) {
                        if (g[xDir][i] == 0) {
                            System.out.println("east is blocked but retracable");
                            //(5)
                            block_east = i;
                            break;
                        }
                    }
                    east = true;
                }
            }
            if (xDir < 2 || g[xDir-2][yDir]==1) {//(1)
                north = false;
                System.out.println("north is impossible");
            } else {
                if(g[xDir-1][yDir] == 0 || g[xDir-2][yDir] == 0){//(2)
                    north = false;//(3)
                    System.out.println("north is impossible");
                }else{//(4)
                    for (int i = xDir - 2; i >= 0; --i) {
                        if ( g[i][yDir] == 0) {
                            System.out.println("north blocked but possible to retrace");
                            //(5)
                            block_north = i;
                            break;
                        }
                    }
                    north = true;
                }
            }
            if (xDir >= height - 2 || g[xDir+2][yDir]==1) {//(1)
                south = false;
                System.out.println("south is impossible");
            } else {
                if(g[xDir+1][yDir] == 0 || g[xDir+2][yDir] == 0){//(2)
                    south = false;//(3)
                    System.out.println("south is impossible");
                }else{//(4)
                    for (int i = xDir + 2; i < height; ++i) {
                        if (g[i][yDir] == 0) {
                            System.out.println("south blocked but retracable");
                            //(5)
                            block_south = i;
                            break;
                        }
                    }
                    south = true;
                }
            }
            
            /*
            NATURAL BLOCK MOVEMENT:
                1. We know that we cannot go directly back from whichever way we came from except for if the path behind us does not have an obstacle block in our way, and
                there are free (-1) spaces, and a new block and the runner can fit there.
                2. We know we cannot go forward once we set a block because this would be like the runner going "through" the block.
                    Obviously this chenges with breakable and bubble blocks, but they aren't implemented yet
            */
            switch(lastDir){
                case 1://north 
                    System.out.println("north is impossible");
                    //Since we had to go north to get to where we are now we know rule 2 above tells us we cannot go further ATM (1)
                    north = false;
                    //default the opposite direction to false until we check for the special cases of rule 1 above (didn't find any -1's and/or 0's) (2)
                    south = false;
                    /*
                        We must traverse from out current block location back one space along the line we came from and being our search there
                        at each block behind us until we find the FIRST -1. Then we grab the block before it meaning the block that would have came before it (meaning the
                        runner would technically hit THIS block before the -1) we know we cannot just over the block in our way, BUT we know we can GO that direction
                        in the act of retracing our steps. So as long as there isn't a line of 1's beind us with no -1's we're allowed to go that way
                    (3)
                    */
                    for(int i = xDir+1; i<height; ++i){
                        if (g[i][yDir]==-1 ){//make sure we aren't jumping over any blocks (3)
                            if(g[i-1][yDir]!=0){
                                //allowed to retrace steps (5)
                                south = true;
                                break;
                            }else{
                                /*a block is in our way so mark this direction impossible even though technically we might be able to 
                                traverse EXACTLY to this block, but above there was logic that took care of that so even if the path 
                                is untraversable here, later it will show that if there is a block and nothing in between we can go back exactly to that block
                                (6)
                                */
                                south = false;
                                break;
                            }
                        }
                    }
                    break;
                    /*
                    NOTICE:
                        Once again the code below in the following case statements follow the same logic just with different details so refer to comment numbers
                        to understand the code with the above commented case statement.
                        Admittedly again there is repeated code, but later it will be extracted
                    */
                case 2://south
                    System.out.println("south is impossible");
                    //(1)
                    south = false;
                    //(2)
                    north = false;
                    //(3)
                    for(int i = xDir-1; i>=0; --i){
                        if (g[i][yDir]==-1){//(4)
                            if(g[i+1][yDir]!=0){
                                //(5)
                                north = true;
                                break;
                            }else{
                                //(6)
                                north = false;
                                break;
                            }
                        }
                    }
                    break;
                case 3://east
                    System.out.println("east is impossible");
                    //(1)
                    east = false;
                    //(2)
                    west = false;
                    //(3)
                    for(int i = yDir-1; i>=0; --i){
                        if (g[xDir][i]==-1){//(4)
                            if(g[xDir][i+1]!=0){
                                //(5)
                                west = true;
                                break;
                            }else{
                                //(6)
                                west = false;
                                break;
                            }
                        }
                    }
                    break;
                case 4://west
                    System.out.println("west is impossible");
                    //(1)
                    west = false;
                    //(2)
                    east = false;
                    //(3)
                    for(int i = yDir+1; i<width; ++i){
                        if (g[xDir][i]==-1){//(4)
                            if(g[xDir][i-1]!=0){
                                //(5)
                                east = true;
                                break;
                            }else{
                                //(6)
                                east = false;
                                break;
                            }
                        }
                    }
                    break;
            }
            /*CONTINE COMMENTING HERE*/
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
                            tempXDir = R.nextInt(xDir - 1);//beacuse the upper bound is not included
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
                            tempXDir = ((height - 1) - (xDir + 2)) > 0 ? R.nextInt(((height - 1) - (xDir + 2))) + (xDir + 2) : (xDir + 2);
                            System.out.println("trying to use xPos as " + tempXDir);
                            while (g[tempXDir][yDir] == 1 || blocked) {
                                System.out.println("trying AIAIN to use xPos as " + tempXDir);
                                tempXDir = R.nextInt(((height - 1) - xDir + 2)) + (xDir + 2);
                                blocked = false;
                                //this is to check that there are no blocks in the line of sight to the next block
                                for (int i = previousX; (i < height) && i < tempXDir; ++i) {
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
                            tempYDir = ((width - 1) - (yDir + 2)) > 0 ? R.nextInt(((width - 1) - (yDir + 2))) + (yDir + 2) : (yDir + 2);
                            System.out.println("trying to use yPos as " + tempYDir);
                            while (g[xDir][tempYDir] == 1 || blocked) {
                                System.out.println("trying AGAIN to use yDir as " + tempYDir);
                                tempYDir = R.nextInt(((width - 1) - (yDir + 2))) + (yDir + 2);
                                blocked = false;
                                //this is to check that there are no blocks in the line of sight to the next block
                                for (int i = previousY; (i < width) && i < tempYDir; ++i) {
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
                            yDir = tempYDir;
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
                            tempYDir = R.nextInt(yDir - 1);//because the upper bound is not included
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
                if(counter == moves && xDir-2 == xStart && yDir-2 == yStart){
                    continue;
                }else{
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
        return 1;
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

