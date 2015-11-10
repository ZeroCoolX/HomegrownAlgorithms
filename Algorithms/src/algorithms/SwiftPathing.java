/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package algorithms;
import customADTs.Block;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Queue;
import java.util.Random;
import java.util.Stack;
/**
 *
 * @author dewit
 */
public class SwiftPathing {
    //constant syntax appearing on various lines. Every line has some or some combo of these
    private static final String layoutPrefix = "android:layout_";
    private static final String idPrefix = "android:id";
    private static final String backgroundPrefix = "android:background";
    private static final String idFile = "=\"@+id/";//can be used as a reference AND declaration
    private static final String dimenFile = "=\"@dimen/";
    private static final String drawFile="=\"@drawable/";
    private static final String qm = "\"";
    private static final String tr="=\"true";
    //constant names for variable names. Sequentially increasing numbers are just appended onto them for each new instance
    private static final String constObstacle = "obstacle";
    private static final String constRunner = "dude";
    private static final String constHint = "hint_";
    private static final String constDimen = "obstacle_width";//add the number at the end for the multiplier needed. Default to nothing
    private static final String constFinish = "finish";
    private static final String constParent = "=\"match_parent";
    private static final String constGridName = "rink";
    //Every block begins and ends with this 
    private static final String viewStart = "<View";
    private static final String viewEnd = "/>";
    
    /*
    in order to write the xml.
    for each block 
        line 1 - assiging an ID for for this block. its just the asset name and a sequentially increasing number
        line 2 - height
        line 3 - width
        line 4 - background of block
        line 5....line n - actual location 
    
    after each block the corresponding hint must be created referencing the block placed
    
    dude is ALWAYS the last block
    */
    
    

    public static void main(String[] args) {
        ArrayList<StringBuilder> allLevelsXML = new ArrayList<StringBuilder>();
        /*height and width is ACTUALLY one less in both directions since the outermost border signifies the walls since they can be used in the traversal*/
        generate(17,11, 10, 16, 1, 3, allLevelsXML);//height,width,min minimum # of moves, max minimum # of moves, number of levels to generate, string builder to store xml
    }
    
    //The actual objects placed on the map
    public static enum assets {
        P_BREAK, P_BUB, P_DUD, P_FIN, P_MOLT, P_MOLTSH, P_OBST, P_PORT, HNT, A_DWN, A_UP, A_LT, A_RT
    }
    
    //Binary answered layouts (either true or false)
    public static enum absoluteLayouts{
        CENT_VERT, CENT_HOR, A_PAR_RT, A_PAR_LT, A_PAR_TP, A_PAR_BT
    }
    
    //Relatively answered layouts based on another asset
    public static enum relativeLayouts{
        TO_LT_OF, TO_RT_OF, ALIGN_RT, ALIGN_LT, ABV, BLW, MGN_TP, MGN_BTM, MGN_LT, MGN_RT
    }
    
    //data about the asset
    public static enum assetData{
        ID, WDTH, HGTH, BKRND, PAR_WDTH, PAR_HGTH
    }
    
    //syntaxtually correct line up until variable name
    private static String fullAssetDataName(assetData assData) {
        switch (assData) {
            case ID:
                return idPrefix+idFile+"";
            case BKRND:
                return backgroundPrefix+drawFile+"";
            case WDTH:
                return layoutPrefix+"width"+dimenFile+constDimen;
            case HGTH:
                return layoutPrefix+"height"+dimenFile+constDimen;
            case PAR_WDTH:
                return layoutPrefix+"width"+constParent;
            case PAR_HGTH:
                return layoutPrefix+"height"+constParent;
            default:
                //should never happen
                return "error";
        }
    }
    
    //syntaxtually correct line up until variable name
    private static String fullRelativeName(relativeLayouts layout){
        switch (layout) {
            case TO_LT_OF:
                return layoutPrefix+"toLeftOf"+idFile;
            case TO_RT_OF:
                return layoutPrefix+"toRightOf"+idFile;            
            case ALIGN_RT:
                return layoutPrefix+"alignRight"+idFile;            
            case ALIGN_LT:
                return layoutPrefix+"alignLeft"+idFile;            
            case ABV:
                return layoutPrefix+"above"+idFile;            
            case BLW:
                return layoutPrefix+"below"+idFile;    
            case MGN_TP:
                return layoutPrefix+"marginTop"+dimenFile;            
            case MGN_BTM:
                return layoutPrefix+"marginBottom"+dimenFile;            
            case MGN_LT:
                return layoutPrefix+"marginLeft"+dimenFile;            
            case MGN_RT:
                return layoutPrefix+"marginRight"+dimenFile;   
            default:
                //should never happen
                return "error";
        }
    }
    
    //syntaxtually correct line up until variable name
    private static String fullAbsoluteName(absoluteLayouts layout){
        switch (layout) {
            case CENT_VERT:
                return layoutPrefix+"centerVertical"+qm;
            case CENT_HOR:
                return layoutPrefix+"centerHorizontal"+qm;            
            case A_PAR_RT:
                return layoutPrefix+"alignParentRight"+qm;            
            case A_PAR_LT:
                return layoutPrefix+"alignParentLeft"+qm;            
            case A_PAR_TP:
                return layoutPrefix+"alignParentTop"+qm;            
            case A_PAR_BT:
                return layoutPrefix+"alignParentBottom"+qm;                
            default:
                //should never happen
                return "error";
        }
    }

    //syntaxtually correct line up until variable name
    private static String fullAssetName(assets asset) {
        switch (asset) {
            case P_BREAK:
                return "play_breakable";
            case P_BUB:
                return "play_bubble";
            case P_DUD:
                return "play_dude";
            case P_FIN:
                return "play_finish";
            case P_MOLT:
                return "play_molten";
            case P_MOLTSH:
                return "play_moltenshadow";
            case P_OBST:
                return "play_obstacle";
            case P_PORT:
                return "play_portal";
            case HNT:
                return "hint";
            case A_DWN:
                return "arrow_down";
            case A_UP:
                return "arrow_up";
            case A_LT:
                return "arrow_left";
            case A_RT:
                return "arrow_right";
            default:
                //should never happen
                return "error";
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
    private static void generate(int height, int width, int minMinMove, int maxMinMove, int numLevels, int wallUse, ArrayList<StringBuilder>  allLevelsXML) {
        //dimensions for the playable area.
        Block[][] grid = new Block[height][width];
        
        //Alwasy use the same instance of Random number generator so no duplicate sequence of randoms (oxymoronic I know but it happens) occur 
        Random randGen = new Random();
        
        //counter to keep track of how many solvable levels have been created
        int levelsCompleted = 0;
        
        //keep generating levels
        while(levelsCompleted < numLevels){   
            //min moves will be in the range of [minMinMove,maxMinMove]
            int minNumMoves = randGen.nextInt(maxMinMove-minMinMove)+minMinMove;
            int idIncrementor = 0;
            //initialize whole grid to -1 (-1 signifies free spaces to place a block)
            for (int i = 0; i < grid.length; ++i) {
                for (int j = 0; j < grid[i].length; ++j) {
                    grid[i][j] = new Block(i,j,-1, ++idIncrementor);
                }
            }
                        print(grid);

            //Randomize the start X and Y coordinates for the runner 
            int xStart =(randGen.nextInt((height-1)-1)+1);
            int yStart = (randGen.nextInt((width-1)-1)+1);
                        
            grid[xStart/*row*/][yStart/*column*/].setType(8);
            System.out.println("--------------------"
                    + "----------------------------"
                    + "GENERING LEVEL "+ (levelsCompleted+1)
                    +"----------------------"
                    + "--------------------------");
            //just print the grid for logging purposes
            print(grid);
            try {
                //populate one level
                allLevelsXML.add(populateGrid(grid, xStart, yStart, minNumMoves, width, height, randGen, wallUse));
                ++levelsCompleted;
            } catch (IllegalStateException ie) {
                System.out.println(ie.toString() + "\n\t"+ ie.getMessage());
            } catch (Exception e) {
                System.out.println(e.toString() + "\n\t"+ e.getMessage());
            }
        }
        
        System.out.println("\n\n\n\n\t\t Successfully created " + levelsCompleted + " solvable levels!\n");
        int XMLCounter = 1;
        for(StringBuilder sb : allLevelsXML){
            System.out.println("\n\n********************************************************************* Level " + XMLCounter + " XML File *********************************************************************\n");
            System.out.println(sb.toString());
        }
        
        
    }
    
    /*
    METHOD NOTES:
        The movable block that actually traverses the paths is referred to as the runner
        -1 indicates a free space
        0 indicated a block
        1 indicates an unavaliable space
        8 indicates the runner 
        returns a 1 is it was successful, otherwise error (for now)
    
    
        Cardinal directions. 
            
                                      North - decreasing x values
                                        X
           decreasing y values - East  Y Y  West - increasing y values
                                        X
                                      South - increasing x values
    
        Format for XML blocks:
            view start
            id
            width
            height
            background
            horizontal pos
            vertical pos
            margin
            view end
    */
    private static StringBuilder populateGrid(Block[][] g, int x, int y, int moves, int width, int height, Random R, int wallUse) throws IllegalStateException, Exception {
        //Stores the generated XML for this particular level
        StringBuilder levelXML = new StringBuilder();
        Stack<Block> allBlocks = new Stack<Block>();
        Stack<Block> finalPath = new Stack<Block>();
        finalPath.push(new Block(x, y, 8, g[x][y].getId()));
        //stores the current block because it could potentially be the end
        int xFinish = x;
        int yFinish = y;
        
        //stored the X and Y coordinates of the starting position used later on to make sure the finish block it not right next to it 
        // because on average this would make the puzzle wayyyy easier to solve...and we don't want that >:)
        //never change till program exits
        int xStart = x;
        int yStart = y;
        
        try {
            //keeps track of how many moves in a level
            int counter = 0;
            //keeps track of how many times the runner's backtracked
            int backtrackCounter = 0;

            //X and Y coordinate for the current runner
            //changes once per move (obviously)
            int xDir = x;
            int yDir = y;

            //temporary values used for holding the NEXT X or Y coordinate
            //changes a lot duing the course of the program
            int tempXDir = x;
            int tempYDir = y;

            //indicate true if a direction is traversable and false if it is not (blocked, or out of bounds)
            boolean north = false;
            boolean south = false;
            boolean east = false;
            boolean west = false;
            
            boolean invalidFinish = false;

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

                //defualt wall use to false so it will only become true if we're on the nth move and we randomly decide to use it. Otherwise it will always be false
                boolean useWall = false;

                //used to tell if we chould count a move or not
                boolean backtracked = false;

                //If this is the nth move then we have a 50/50 chance of using a wall unstead of a block
                if (counter + 1 == wallUse) {
                    //generate a random number between [1,3) => either 1(heads) or 2(tails)
                    if ((R.nextInt(3 - 1) + 1) == 2) {//because the runner's chasing after that tail ^_^
                        System.out.println("allowed to use walls");
                        useWall = true;
                    } else {
                        System.out.println("NOT allowed to use walls");
                        useWall = false;
                    }
                }

                /*
                 NATURAL BLOCK MOVEMENT:
                 1. We know that we cannot go directly back from whichever way we came from except for if the path behind us does not have an obstacle block in our way, and
                 there are free (-1) spaces, and a new block and the runner can fit there. OR we CAN go back but only to exactly to location of a block behind us.
                 2. We know we cannot go forward once we set a block because this would be like the runner going "through" the block.
                 Obviously this chenges with breakable and bubble blocks, but they aren't implemented yet
                 */
                switch (lastDir) {
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
                        for (int i = xDir + 1; i < height; ++i) {
                            if (g[i][yDir].getType() == -1) {//make sure we aren't jumping over any blocks (3)
                                if (g[i - 1][yDir].getType() != 0) {
                                    //allowed to retrace steps (5)
                                    south = true;
                                    break;
                                } else {
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
                        for (int i = xDir - 1; i >= 0; --i) {
                            if (g[i][yDir].getType() == -1) {//(4)
                                if (g[i + 1][yDir].getType() != 0) {
                                    //(5)
                                    north = true;
                                    break;
                                } else {
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
                        for (int i = yDir - 1; i >= 0; --i) {
                            if (g[xDir][i].getType() == -1) {//(4)
                                if (g[xDir][i + 1].getType() != 0) {
                                    //(5)
                                    west = true;
                                    break;
                                } else {
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
                        for (int i = yDir + 1; i < width; ++i) {
                            if (g[xDir][i].getType() == -1) {//(4)
                                if (g[xDir][i - 1].getType() != 0) {
                                    //(5)
                                    east = true;
                                    break;
                                } else {
                                    //(6)
                                    east = false;
                                    break;
                                }
                            }
                        }
                        break;
                }

                if (yDir < 2) {//runner cannot go west if IT is far west as possible (1)
                    west = false;
                    System.out.println("west is impossible");
                } else {
                    if (g[xDir][yDir - 1].getType() == 0 || g[xDir][yDir - 2].getType() == 0) {//impossible to go west direction if either the direct next or next two blocks are blocked (2)
                        west = false;//this is partly so we don't have a ton of very small (2 unit) swipes (3)
                        System.out.println("west is impossible");
                    } else {//otherwise west is either completely traversable or retracable to exactly the first block in the row (4)

                        //if this is the last move, we cannot end on an already placed block(5)
                        if (counter < moves) {
                            for (int i = yDir - 2; i >= 0; --i) {
                                /*
                                 There are 3 scenarios that can happen since now with a perimeter of blocks surrounding the whole grid technically every time the runner looks out he could potentially see a block.
                                 1. The runner sees a block and he CANNOT use walls and this block is not on the perimeter -> use the block because this indicates its a real obstacle block within the grid
                                 2. The runner sees a block and he CANNOT use walls and this block IS on the perimeter -> don't use the block because this indicated its on the perimeter and thus a wall not an obstacle
                                 3. The runner sees a block and he CAN use walls  -> use the block because this indicated its either a real obstacle of a wall (whichever comes first, doesn't matter)
                                 (6)
                                 */
                                if (g[xDir][i].getType() == 0 && (!useWall ? !(i == 0 || i == width - 1) : true)) {
                                    System.out.println("west blocked but retracable");
                                    //block locations stored so we know if west is chosen as the direction to traverse we MUST go exactly to this block. no further no lesser. (7)
                                    block_west = i;
                                    break;
                                }
                            }
                        }
                        //but we could still potentially go this direction(8)
                        west = true;
                    }
                }
                /*
                 NOTICE: 
                 for the next three if blocks below, they all use the same logic juust with different details such as coordinates and directions so
                 refer to the numbers corresponding to the above comments to understand code.
                 In the future the common logic will be extracted but for now there's admittedly some repetition
                 */
                if (yDir >= width - 2) { //(1)
                    east = false;
                    System.out.println("east is impossible");
                } else {
                    if (g[xDir][yDir + 1].getType() == 0 || g[xDir][yDir + 2].getType() == 0) {//(2)
                        east = false;//(3)
                        System.out.println("east is impossible");
                    } else {//(4)
                        //(5)
                        if (counter < moves) {
                            for (int i = yDir + 2; i < width; ++i) {
                                //(6)
                                if (g[xDir][i].getType() == 0 && (!useWall ? !(i == 0 || i == width - 1) : true)) {
                                    System.out.println("east is blocked but retracable");
                                    //(7)
                                    block_east = i;
                                    break;
                                }
                            }
                        }
                        //(8)
                        east = true;
                    }
                }
                if (xDir < 2) {//(1)
                    north = false;
                    System.out.println("north is impossible");
                } else {
                    if (g[xDir - 1][yDir].getType() == 0 || g[xDir - 2][yDir].getType() == 0) {//(2)
                        north = false;//(3)
                        System.out.println("north is impossible");
                    } else {//(4)
                        //(5)
                        if (counter < moves) {
                            for (int i = xDir - 2; i >= 0; --i) {
                                //(6)
                                if (g[i][yDir].getType() == 0 && (!useWall ? !(i == 0 || i == height - 1) : true)) {
                                    System.out.println("north blocked but possible to retrace");
                                    //(7)
                                    block_north = i;
                                    break;
                                }
                            }
                        }
                        //(8)
                        north = true;
                    }
                }
                if (xDir >= height - 2) {//(1)
                    south = false;
                    System.out.println("south is impossible");
                } else {
                    if (g[xDir + 1][yDir].getType() == 0 || g[xDir + 2][yDir].getType() == 0) {//(2)
                        south = false;//(3)
                        System.out.println("south is impossible");
                    } else {//(4)
                        //(5)
                        if (counter < moves) {
                            for (int i = xDir + 2; i < height; ++i) {
                                //(6)
                                if (g[i][yDir].getType() == 0 && (!useWall ? !(i == 0 || i == height - 1) : true)) {
                                    System.out.println("south blocked but retracable");
                                    //(7)
                                    block_south = i;
                                    break;
                                }
                            }
                        }
                        //(8)
                        south = true;
                    }
                }

                //All possible directions runner could traverse are stored
                ArrayList<Integer> possibleDirs = new ArrayList();
                if (north) {
                    System.out.println("adding north");
                    possibleDirs.add(1);
                }
                if (south) {
                    System.out.println("adding south");
                    possibleDirs.add(2);
                }
                if (east) {
                    System.out.println("adding east");
                    possibleDirs.add(3);
                }
                if (west) {
                    System.out.println("adding west");
                    possibleDirs.add(4);
                }

                /*
                 The current X and Y coordinates at this given time need to be stored so that they can be used to 
                 map out the actual path between the current coordinates of the runner and the next location it appears at
                 */
                int previousX = xDir;
                int previousY = yDir;
                try {
                //its possible the runner (idiot) got itself stuck in a position where absolutely no directions are possible. 
                    //In this case the level becomes unsolvable and should terminate to re-generate a new one
                    if (possibleDirs.isEmpty()) {
                        throw new IllegalStateException();
                    }
                } catch (IllegalStateException ie) {
                    throw new IllegalStateException("Every direction is impossible", ie);
                }

                //Randomly select one of possible directions for the runner to go
                int randDir = possibleDirs.get(R.nextInt(possibleDirs.size()));

                //Indicates if a block is between the current position of the runner and the position desired in the specific direction.
                boolean blocked = false;

                //Keeps track of how many times the runner tries going a certain direction (and fails). Only allow 100 times before we restart the generation.
                int infinityCounter = 0;

                try {
                    //This logic figures out where to place the next block (and ultimately move the runner) in the specified direction
                    switch (randDir) {
                        case 1://north
                            //If we're going north and the way we got here was by going south, it doen't matter if we go to a new block we still backtracked to get there and shouldn't count this as a move
                            if (lastDir == 2) {
                                backtracked = true;
                            }
                            //keep track of the direction he chose(1)
                            lastDir = 1;
                            System.out.println("randomly choose north");
                            //If at any point above the runner saw that he could retrace his path and his a block allow it
                            //This allows for the runner to hit different sides of the same block obviously not at the same time, but after n moves.
                            //We immediately set the coordinates of the block as his target location because thats the only place he could go. (2)
                            if (block_north >= 0) {
                                xDir = block_north;
                                invalidFinish = true;
                                System.out.println("choosing exaclt block located xDir " + xDir);
                                if (g[xDir + 1][yDir].getType() == 1) {//indicates that on the way to the block we were traversing an already traversed path = backtracking
                                    System.out.println("BACKTRACKED");
                                    backtracked = true;
                                }
                            } else {
                                //If there wasn't a retraceable path and block, he can go ANYWHERE (with respect to rules of the logic)
                                //Get a random number in the direction wanted and see if he can achieve that target (3)
                                tempXDir = R.nextInt(xDir - 1);
                                System.out.println("trying to use tempXDir as " + tempXDir + " and yDir as " + yDir);
                                /*
                                 The while loop executes while the randomly selected location for the runner to go is in the way of a two other blocks,
                                 so he's gotta choose another one.
                                 Otherwise, if the location is on another block, we are blocked but not necessarily unable to place a block somewhere BEFORE that block so try again
                                 (4)
                                 */
                                while (g[tempXDir][yDir].getType() == 1 || blocked) {
                                    System.out.println("trying AGAIN to use tempXDir as " + tempXDir);
                                    //Get another random number(5)
                                    tempXDir = R.nextInt(xDir - 1);
                                    blocked = false;
                                    //This is to check that there are no blocks in the line of sight to the next block(6)
                                    for (int i = previousX; (i >= 0) && i > tempXDir; --i) {
                                        //If we make it all the way down the line without breaking its possible this entire line is made up on another blocks path we need a new direction...(7)
                                        System.out.println("i = " + i);
                                        if (g[i][yDir].getType() == 0) {
                                            blocked = true;
                                            break;
                                        }
                                    }
                                    //Increment the counter and make sure its not > 100. If it is, statistically we're stuck in a loop. Throw exception and get out to re-generate a new level(8)
                                    ++infinityCounter;
                                    if (infinityCounter > 100) {
                                        throw new IllegalStateException();
                                    }
                                }
                                //ONCE we make it past all the above, we have a successful path for the runner to traverse and a new place for the next block(9)
                                xDir = tempXDir;
                                backtracked = false;
                            }
                            //store THIS current block because it might be a finish block
                            xFinish = xDir;
                            yFinish = yDir;
                            
                            System.out.println("new position to place block is (" + xDir + "," + yDir + ")");
                            //Store the new block in the found place(10)
                            g[xDir][yDir].setType(0);
                            //Since the runner can't actually go INTO the block, it has to stop one space before the placed block since it "hits" the block
                            //NOTICE: This is only true for plain obscure block obstacles. Once Bubble, Breakable, and Portal blocks are implemented this will need jsut a little extra magic (11)
                            ++xDir;
                            g[xDir][yDir].setType(8);
                            System.out.println("new position for movement block is block is (" + xDir + "," + yDir + ")");
                            //Lastly all we have to do is traverse backwards from the new location of the runner to the location where it came from drawing out the path (inserting 1's) (12)
                            for (int i = xDir + 1; (i < height) && i <= previousX && (g[i][yDir].getType() != 0); ++i) {
                                g[i][yDir].setType(1);
                            }
                            break;
                        /*
                         NOTICE:
                         once again...proof I need to extract code out because of so much being repeated, but the case statements below all follow the same logic
                         save minor details so refer to the numbers corresponding with the comments above to understand the code
                         */
                        case 2://south
                            //If we're going south and the way we got here was by going north, it doen't matter if we go to a new block we still backtracked to get there and shouldn't count this as a move
                            if (lastDir == 1) {
                                backtracked = true;
                            }
                            //(1)
                            lastDir = 2;
                            System.out.println("randomly choose south");
                            //(2)
                            if (block_south >= 0) {
                                xDir = block_south;
                                invalidFinish = true;
                                System.out.println("choosing exaclt block located xDir " + xDir);
                                if (g[xDir - 1][yDir].getType() == 1) {//indicates that on the way to the block we were traversing an already traversed path = backtracking
                                    System.out.println("BACKTRACKED");
                                    backtracked = true;
                                }
                            } else {
                                //(3)
                                tempXDir = ((height - 1) - (xDir + 2)) > 0 ? R.nextInt(((height - 1) - (xDir + 2))) + (xDir + 2) : (xDir + 2);
                                System.out.println("trying to use tempXDir as " + tempXDir + " and yDir as " + yDir);
                                //(4)
                                while (g[tempXDir][yDir].getType() == 1 || blocked) {
                                    System.out.println("trying AIAIN to use tempXDir as " + tempXDir);
                                    //(5)
                                    tempXDir = R.nextInt(((height - 1) - xDir + 2)) + (xDir + 2);
                                    blocked = false;
                                    //(6)
                                    for (int i = previousX; (i < height - 1) && i < tempXDir; ++i) {
                                        System.out.println("i = " + i);
                                        //(7)
                                        if (g[i][yDir].getType() == 0) {
                                            blocked = true;
                                            break;
                                        }
                                    }
                                    //(8)
                                    ++infinityCounter;
                                    if (infinityCounter > 100) {
                                        throw new IllegalStateException();
                                    }
                                }
                                //(9)
                                xDir = tempXDir;
                                backtracked = false;
                            }
                            //store THIS current block because it might be a finish block
                            xFinish = xDir;
                            yFinish = yDir;
                            
                            //(10)
                            g[xDir][yDir].setType(0);
                            --xDir;
                            //(11)
                            g[xDir][yDir].setType(8);
                            System.out.println("new position to place block is (" + xDir + "," + yDir + ")");
                            //(12)
                            for (int i = xDir - 1; (i >= 0) && i >= previousX && (g[i][yDir].getType() != 0); --i) {
                                System.out.println("i = " + i);
                                g[i][yDir].setType(1);
                            }
                            break;
                        case 3://east
                            //If we're going east and the way we got here was by going west, it doen't matter if we go to a new block we still backtracked to get there and shouldn't count this as a move
                            if (lastDir == 4) {
                                backtracked = true;
                            }
                            ////(1)
                            lastDir = 3;
                            System.out.println("randomly choose east");
                            //(2)
                            if (block_east >= 0) {
                                yDir = block_east;
                                invalidFinish = true;
                                System.out.println("chosen to use exact block located at  yDir " + yDir);
                                if (g[xDir][yDir - 1].getType() == 1) {//indicates that on the way to the block we were traversing an already traversed path = backtracking
                                    System.out.println("BACKTRACKED");
                                    backtracked = true;
                                }
                            } else {
                                //(3)
                                tempYDir = ((width - 1) - (yDir + 2)) > 0 ? R.nextInt(((width - 1) - (yDir + 2))) + (yDir + 2) : (yDir + 2);
                                System.out.println("trying to use tempYDir as " + tempYDir + " and xDir as " + xDir);
                                //(4)
                                while (g[xDir][tempYDir].getType() == 1 || blocked) {
                                    System.out.println("trying AGAIN to use tempYDir as " + tempYDir);
                                    //(5)
                                    tempYDir = R.nextInt(((width - 1) - (yDir + 2))) + (yDir + 2);
                                    blocked = false;
                                    //(6)
                                    for (int i = previousY; (i < width - 1) && i < tempYDir; ++i) {
                                        System.out.println("i = " + i);
                                        //(7)
                                        if (g[xDir][i].getType() == 0) {
                                            blocked = true;
                                            break;
                                        }
                                    }
                                    //(8)
                                    ++infinityCounter;
                                    if (infinityCounter > 100) {
                                        throw new IllegalStateException();
                                    }
                                }
                                //(9)
                                yDir = tempYDir;
                                backtracked = false;
                            }
                            //store THIS current block because it might be a finish block
                            xFinish = xDir;
                            yFinish = yDir;
                            //(10)
                            g[xDir][yDir].setType(0);
                            --yDir;
                            //(11)
                            g[xDir][yDir].setType(8);
                            System.out.println("new position to place block is (" + xDir + "," + yDir + ")");
                            //(12)
                            for (int i = yDir - 1; (i >= 0) && i >= previousY && (g[xDir][i].getType() != 0); --i) {
                                System.out.println("i = " + i);
                                g[xDir][i].setType(1);
                            }
                            break;
                        case 4://west
                            //If we're going west and the way we got here was by going east, it doen't matter if we go to a new block we still backtracked to get there and shouldn't count this as a move
                            if (lastDir == 3) {
                                backtracked = true;
                            }
                            //(1)
                            lastDir = 4;
                            System.out.println("randomly choose west");
                            //(2)
                            if (block_west >= 0) {
                                yDir = block_west;
                                invalidFinish = true;
                                System.out.println("chosen to use exact block located at  yDir " + yDir);
                                if (g[xDir][yDir + 1].getType() == 1) {//indicates that on the way to the block we were traversing an already traversed path = backtracking
                                    System.out.println("BACKTRACKED");
                                    backtracked = true;
                                }
                            } else {
                                //(3)
                                tempYDir = R.nextInt(yDir - 1);
                                System.out.println("trying to use tempYDir as " + tempYDir + " and xDir as " + xDir);
                                //(4)s
                                while (g[xDir][tempYDir].getType() == 1 || blocked) {
                                    //(5)
                                    tempYDir = R.nextInt(yDir - 1);
                                    blocked = false;
                                    //(6)
                                    for (int i = previousY; (i >= 0) && i > tempYDir; --i) {
                                        System.out.println("i = " + i);
                                        //(7)
                                        if (g[xDir][i].getType() == 0) {
                                            blocked = true;
                                            break;
                                        }
                                    }
                                    System.out.println("trying AGAIN to use yDir as " + yDir);
                                    //(8)s
                                    ++infinityCounter;
                                    if (infinityCounter > 100) {
                                        throw new IllegalStateException();
                                    }
                                }
                                backtracked = false;
                                //(9)
                                yDir = tempYDir;
                            }
                            //store THIS current block because it might be a finish block
                            xFinish = xDir;
                            yFinish = yDir;
                            //(10)
                            g[xDir][yDir].setType(0);
                            ++yDir;
                            //(11)
                            g[xDir][yDir].setType(8);
                            System.out.println("new position to place block is (" + xDir + "," + yDir + ")");
                            //(12)
                            for (int i = yDir + 1; (i < width) && i <= previousY && (g[xDir][i].getType() != 0); ++i) {
                                System.out.println("i = " + i);
                                g[xDir][i].setType(1);
                            }
                            break;
                    }
                } catch (IllegalStateException is) {
                    is.printStackTrace();
                    throw new IllegalStateException("Stuck in an infinite loop due to the limited number of directions possible", is);
                } catch (Exception e) {
                    e.printStackTrace();
                    throw new Exception("Some unknow error happened?!", e);
                }
                    System.out.println("CEHCKING  ["+xDir+","+yDir+"] and xStart = " + xStart + " and yStart = " + yStart);
                //Make sure that the final block is not even in the same horizontal or vertical row/column of the start
                if(counter+1 == moves && invalidFinish){
                    ++backtrackCounter;
                    System.out.println("cannot finish on an already placed block");
                    invalidFinish = false;
                                        boolean alreadySeen = false;
                    for(Block b : finalPath){
                        //check to see if this is REALLY a backtrack or just traversing a know direction
                        if(b.getX() == xDir && b.getY() == yDir){
                            alreadySeen=true;
                            break;
                        }
                    }
                    //its technically a new direction so add it
                    if(!alreadySeen){
                        System.out.println("ADDING TO finalPath even tho we backtracked cuz did not find dupe");
                        finalPath.push(new Block(xDir, yDir, 8, g[xDir][yDir].getId()));
                    }
                }else if (counter+1 == moves && (xFinish == xStart || yFinish == yStart)) {
                    ++backtrackCounter;
                    System.out.println("cannot end in line with the start");
                                        boolean alreadySeen = false;
                    for(Block b : finalPath){
                        //check to see if this is REALLY a backtrack or just traversing a know direction
                        if(b.getX() == xDir && b.getY() == yDir){
                            alreadySeen=true;
                            break;
                        }
                    }
                    //its technically a new direction so add it
                    if(!alreadySeen){
                        System.out.println("ADDING TO finalPath even tho we backtracked cuz did not find dupe");
                        finalPath.push(new Block(xDir, yDir, 8, g[xDir][yDir].getId()));
                    }
                } else if (backtracked) {
                    System.out.println("we backtracked thus don't increment counter");
                    ++backtrackCounter;
                    boolean alreadySeen = false;
                    for(Block b : finalPath){
                        //check to see if this is REALLY a backtrack or just traversing a know direction
                        if(b.getX() == xDir && b.getY() == yDir){
                            alreadySeen=true;
                            break;
                        }
                    }
                    //its technically a new direction so add it
                    if(!alreadySeen){
                        System.out.println("ADDING TO finalPath even tho we backtracked cuz did not find dupe");
                        finalPath.push(new Block(xDir, yDir, 8, g[xDir][yDir].getId()));
                    }else{
                        finalPath.pop();
                    }
                    if (backtrackCounter > 700) {
                        throw new IllegalStateException("Stuck in an infinite loop due to an infinite backtrack cycle");
                    }
                } else if (counter+1 == moves && (xDir == 1 || xDir == height - 2) || (yDir == 1 || yDir == width - 2)) {
                    ++backtrackCounter;
                    System.out.println("cannot finish in a wall");
                                        boolean alreadySeen = false;
                    for(Block b : finalPath){
                        //check to see if this is REALLY a backtrack or just traversing a know direction
                        if(b.getX() == xDir && b.getY() == yDir){
                            alreadySeen=true;
                            break;
                        }
                    }
                    //its technically a new direction so add it
                    if(!alreadySeen){
                        System.out.println("ADDING TO finalPath even tho we backtracked cuz did not find dupe");
                        finalPath.push(new Block(xDir, yDir, 8, g[xDir][yDir].getId()));
                    }
                } else {
                    finalPath.push(new Block(xDir, yDir, 8, g[xDir][yDir].getId()));
                    ++counter;
                    //print for logging purposes
                    System.out.println("PRINTING and pushing ["+xDir+","+yDir+"]");
                    print(g);
                }
                System.out.println("counter = " + counter);
            }
        } catch (IllegalStateException is) {
            throw new IllegalStateException(is.getMessage(), is);
        } catch (Exception e) {
            throw new Exception(e.getMessage(), e);
        }
        
        //Before writing to XML we need to ensure that the min # of moves is at least moves-1. Its possible (since random block placement is a factor) that a presumed min 10 move puzzle could be solved in 2 moves..etc
        //ensureMinMoves(g, moves, xStart, yStart, xFinish, yFinish);
            System.out.print("Starting at BLOCK: ");
            for(Block b : finalPath){
                System.out.print("["+b.getX() + ","+b.getY()+"] \nGo to BLOCK: ");
            }
            
        //Stack<Block> allBlocks = getAllPossiblePathLocs(g);
        
         //not ready yet
        //backwardsTraversal(finalPath);
        Stack<Block> S = getAllPossiblePathLocs(g, width, height);
            System.out.print("Showing all blocks possible to be traversed Block: ");
            for(Block b : S){
                System.out.print("["+b.getX() + ","+b.getY()+"] \nBlock: ");
            }
        //depthFirstSearch(g, finalPath, xStart, yStart, width, height);
        
        //Collect blocks to place on the grid left to right, top to bottom in a hashmap. keys 1 to n, value block
        HashMap<Integer, Block> blocks = lineUpBlocks(g, height, width);
        
        //If we FINALLY reach here that is awesome and we have successfully generated one solvable level. Now we write the xml
        System.out.println("Successful level generated - writing XML...");
        //Before anything is said and done append the constant header to the file
        levelXML.append(
                viewStart + "\n"
                + fullAssetDataName(assetData.ID) + constGridName + qm + "\n"
                + fullAssetDataName(assetData.PAR_WDTH) + qm + "\n"
                + fullAssetDataName(assetData.PAR_HGTH) + qm + "\n"
                + viewEnd + "\n"
        );
        int obNum = 0;
        /*
        System.out.println("xFinish = " + xFinish + "\nyFinish = " + yFinish);
        for (int i = 0; i < g.length; ++i) {
            for (int j = 0; j < g[i].length; ++j) {
                //if we're on the perimeter even if there is a theoretical block, don't add it because it's acting as the wall
                if (!(i == 0 || i == width - 1) && !(j == 0 || j == height-1)) {
                    //this is a writable block!
                    if (g[i][j].getType() == 0) {
                        levelXML.append(
                                viewStart + "\n"
                                + fullAssetDataName(assetData.ID) + ((i==xFinish&&j==yFinish)?(constFinish):(constObstacle + (obNum++))) + qm + "\n"
                                + fullAssetDataName(assetData.WDTH) + qm + "\n"
                                + fullAssetDataName(assetData.HGTH) + qm + "\n"
                                + fullAssetDataName(assetData.BKRND) + fullAssetName(((i==xFinish&&j==yFinish)?assets.P_FIN:assets.P_OBST)) + qm
                        );
                        
                    }
                }
            }
        }*/
        try {
            int halfWayMark = width / 2;
            //go through each block in the hashmap and...good luck
            String codeBlock = "";
            Block lastSeenBlock = null;
            int blocksPerRow = 1;
            for (int k = 1; k <= blocks.size(); ++k) {
                //we know the first block will be topmost left or top rightmost
                codeBlock += viewStart + "\n"
                        + fullAssetDataName(assetData.ID) + ((k == xFinish && k == yFinish) ? (constFinish) : (constObstacle + (k))) + qm + "\n"
                        + fullAssetDataName(assetData.WDTH) + qm + "\n"
                        + fullAssetDataName(assetData.HGTH) + qm + "\n"
                        + fullAssetDataName(assetData.BKRND) + fullAssetName(((k == xFinish && k == yFinish) ? assets.P_FIN : assets.P_OBST)) + qm + "\n";
                Block currentBlock = blocks.get(k);
                if (k == 1) {//this is the beginning so top left
                    //alignParentTop, alignParentLeft
                    if (currentBlock.getY() != 1) {//its the rightmost
                        codeBlock += (fullAbsoluteName(absoluteLayouts.A_PAR_TP) + tr + qm + "\n");
                        codeBlock += (fullAbsoluteName(absoluteLayouts.A_PAR_RT) + tr + qm + "\n");
                    } else {
                        codeBlock += (fullAbsoluteName(absoluteLayouts.A_PAR_TP) + tr + qm + "\n");
                        codeBlock += (fullAbsoluteName(absoluteLayouts.A_PAR_LT) + tr + qm + "\n");
                    }
                } else {
                    //if(currentBlock.getX() < halfWayMark){//to the left of the middle
                    if (currentBlock.getX() == lastSeenBlock.getX()) {//this block is on the same row of the last placed block
                        ++blocksPerRow;
                        //we know its to the right of it so
                        boolean marginLeft = false;//if true pushes it right
                        int marginNeeded = 0;
                        if (currentBlock.getY() < lastSeenBlock.getY()) {
                            marginLeft = false;
                            marginNeeded = (lastSeenBlock.getY() - currentBlock.getY());
                        } else {
                            marginLeft = true;
                            marginNeeded = (currentBlock.getY() - lastSeenBlock.getY());
                        }
                        if (marginNeeded >= 1) {
                            codeBlock += (fullRelativeName(marginLeft ? relativeLayouts.TO_RT_OF : relativeLayouts.TO_LT_OF) + constObstacle + obNum + qm + "\n");
                            if (marginNeeded >= 2) {
                                codeBlock += (fullRelativeName(marginLeft ? relativeLayouts.MGN_LT : relativeLayouts.MGN_RT) + constDimen + (marginNeeded > 2 ? marginNeeded : "") + qm + "\n");//move it right one width
                            }
                        }
                    } else {//logically its the first block one row below the last block because otherwise its X value would be on the same plane as the last block
                        if (currentBlock.getY() != 1) {//cannot do this on the first one
                            int curX = currentBlock.getX();
                            int curY = currentBlock.getY();
                            //check above it
                            if (g[curX - 1][curY].getType() == 0) {
                                codeBlock += (fullRelativeName(relativeLayouts.BLW) + constObstacle + getObstacleNum(blocks, curX - 1, curY) + qm + "\n");
                            } else {//we need to find the closest one one level above it
                                Block closeBlock = findClosestBlockAbove(g, g[curX - 1][curY], width);
                                //sadly there was no blocks on the row above this one
                                int inc = 1;
                                while (closeBlock == null) {//theres no way we CANT find a block
                                    closeBlock = findClosestBlockAbove(g, g[curX - (1 + inc)][curY], width);
                                    ++inc;
                                }
                                //alright we found a block above it.
                                int xDist = currentBlock.getX() - closeBlock.getX();
                                int yDist = 0;
                                boolean marginLeft = false;//if true moves it right
                                if (currentBlock.getY() < closeBlock.getY()) {
                                    marginLeft = true;
                                    yDist = closeBlock.getY() - currentBlock.getY();
                                } else {
                                    marginLeft = false;//moves it left
                                    yDist = currentBlock.getY() - closeBlock.getY();
                                }
                                codeBlock += (fullRelativeName(relativeLayouts.BLW) + constObstacle + getObstacleNum(blocks, closeBlock.getX(), closeBlock.getY()) + qm + "\n");
                                codeBlock += (fullRelativeName(relativeLayouts.MGN_TP) + constDimen + (xDist > 2 ? xDist : "") + qm + "\n");//move it below the block
                                codeBlock += (fullRelativeName(marginLeft ? relativeLayouts.MGN_LT : relativeLayouts.MGN_RT) + constDimen + yDist + qm + "\n");//move it right one width
                            }
                        }
                    }
                               // }else{//to the right of the middle

                    // }
                }
                //last line of codeblock must be ending view tag
                codeBlock += viewEnd + "\n";
                lastSeenBlock = blocks.get(k);
                levelXML.append(codeBlock);
                codeBlock = "";
                ++obNum;
            }

            //Lastly we just need to append the runner - dude - onto the XML
            levelXML.append(
                    viewStart + "\n"
                    + fullAssetDataName(assetData.ID) + constRunner + qm + "\n"
                    + fullAssetDataName(assetData.WDTH) + qm + "\n"
                    + fullAssetDataName(assetData.HGTH) + qm + "\n"
                    + fullAssetDataName(assetData.BKRND) + fullAssetName(assets.P_DUD) + qm + "\n"
                    + viewEnd + "\n"
            );
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Failure");
            System.exit(1);
        }
        return levelXML;
    }
    
    private static Block findClosestBlockAbove(Block[][] g, Block b, int width){
        int x = b.getX();
        int yLess = b.getY();
        int yMore = b.getY();
        while(yLess >=1 || yMore < width-1){
            if(yLess >= 1){
                if(g[x][yLess].getType()==0){
                    return g[x][yLess];
                }
                --yLess;
            }
            if(yMore < width-1){
                if(g[x][yMore].getType()==0){
                    return g[x][yMore];
                }
                ++yMore;
            }
        }
        return null;
    }
    
    //check every map entry and return the key which corresponds to the obstacle variable number
    private static int getObstacleNum(HashMap<Integer, Block> hm, int x, int y) {
        for (Map.Entry<Integer, Block> e : hm.entrySet()) {
            Block b = e.getValue();
            if(b.getX() == x && b.getY() == y){
                return e.getKey();
            }
        }
        return -1;
    }
    
    
    private static HashMap<Integer, Block> lineUpBlocks(Block[][]g, int height, int width){
        System.out.println("Lining up blocks");
        HashMap<Integer, Block> blockLines = new HashMap<Integer, Block>();
        int blockNum = 1;
        //check the top left and right blocks for a starter
        if(g[1][1].getType()==-1){
            blockLines.put(blockNum, new Block(1, 1, 0, 1));
            ++blockNum;
        }else if(g[1][width-2].getType()==-1){
            blockLines.put(blockNum, new Block(1, width-2, 0, 1));
            ++blockNum;
        }
        for (int i = 0; i < g.length; ++i) {
            for (int j = 0; j < g[i].length; ++j) {
                System.out.print(g[i][j].getType() + "\t");
                if (!(i == 0 || i == width - 1) && !(j == 0 || j == height - 1)) {//inside the perimeter
                    if (g[i][j].getType() == 0) {
                        System.out.print("Storing block " + blockNum + ": " + g[i][j].getCoordinates() + "\t");
                        blockLines.put(blockNum, g[i][j]);
                        ++blockNum;
                    }
                }else{//on the perimeter
                    /*if (g[i][j].getType() == 0) {
                        System.out.print("Storing block " + blockNum + ": " + g[i][j].getCoordinates() + "\t");
                        blockLines.put(blockNum, new Block(g[i][j].getX(), g[i][j].getY(), -2));
                        ++blockNum;
                    }*/
                }
            }
            System.out.print("\n");
        }
        
        return blockLines;
    }
    
    //stopping at every 0 - if its not on the perimeter, push onto the stack each block either north, south, east, or west of it that has a 1 or an 8
    //if it is on the perimeter than just the nodes on the sides and opposite side of it
    //if it IS in the perimeter then just the node in front of it 
    //again all only if 1 or 8
    private static Stack<Block> getAllPossiblePathLocs(Block[][] g, int w, int h){
        Stack<Block> startLocs = new Stack<Block>();
        for (int i = 0; i < g.length; ++i) {
            for (int j = 0; j < g[i].length; ++j) {
                if(g[i][j].getType()==0){
                    Block b = g[i][j];
                    System.out.println("Processing " + b.getCoordinates());
                    Block put = null;
                    //big kahuna
                    if (b.getX() == 0) {//deal with IN north perimeter - can only check directly below
                        System.out.println("x==0");
                        if (g[b.getX() + 1][b.getY()].getType() == 1 || g[b.getX() + 1][b.getY()].getType() == 8) {//store it
                            System.out.println("pushing " + g[b.getX() + 1][b.getY()].getCoordinates());
                            put = g[b.getX() + 1][b.getY()];
                            if(!startLocs.contains(put)){startLocs.push(put);}
                        }
                    } else if (b.getX() == h - 1) {//deal with IN south perim - can only check directly above
                        System.out.println("x==h-1");
                        if (g[b.getX() - 1][b.getY()].getType() == 1 || g[b.getX() - 1][b.getY()].getType() == 8) {//store it
                            System.out.println("pushing " + g[b.getX() - 1][b.getY()].getCoordinates());
                            put = g[b.getX() - 1][b.getY()];
                            if(!startLocs.contains(put)){startLocs.push(put);}
                        }
                    } else if (b.getY() == 0) {//deal with IN west perimeter
                        System.out.println("y==0");
                        if (g[b.getX()][b.getY() + 1].getType() == 1 || g[b.getX()][b.getY() + 1].getType() == 8) {//store it
                            System.out.println("pushing " + g[b.getX()][b.getY() + 1].getCoordinates());
                            put = g[b.getX()][b.getY() + 1];
                            if(!startLocs.contains(put)){startLocs.push(put);}
                        }
                    } else if (b.getY() == w - 1) {//deal with IN east perim
                        System.out.println("y==w-1");
                        if (g[b.getX()][b.getY() - 1].getType() == 1 || g[b.getX()][b.getY() - 1].getType() == 8) {//store it
                            System.out.println("pushing " + g[b.getX()][b.getY() - 1].getCoordinates());
                            put = g[b.getX()][b.getY() - 1];
                            if(!startLocs.contains(put)){startLocs.push(put);}
                        }
                    } else {//Determined not ON any perimeter. check if ON the perim
                        if (b.getX() == 1 && b.getY() > 0 && b.getY() < w - 1) {//deal with IN north perimeter
                            System.out.println("x==1");
                            if (g[b.getX() + 1][b.getY()].getType() == 1 || g[b.getX() + 1][b.getY()].getType() == 8) {//check below it
                                System.out.println("pushing " + g[b.getX() + 1][b.getY()].getCoordinates());
                                put = g[b.getX() + 1][b.getY()];
                                if(!startLocs.contains(put)){startLocs.push(put);}
                            }
                            if (b.getY() > 0) {//check left
                                System.out.println("AND y > 1");
                                if (b.getY() < w - 1 && (g[b.getX()][b.getY() - 1].getType() == 1 || g[b.getX()][b.getY() - 1].getType() == 8)) {
                                    System.out.println("pushing " + g[b.getX()][b.getY() - 1].getCoordinates());
                                    put = g[b.getX()][b.getY() - 1];
                                    if(!startLocs.contains(put)){startLocs.push(put);}
                                }
                            }
                            if (b.getY() < w - 1) {//check right
                                System.out.println("AND y < w-1");
                                if (b.getY() > 0 && (g[b.getX()][b.getY() + 1].getType() == 1 || g[b.getX()][b.getY() + 1].getType() == 8)) {
                                    System.out.println("pushing " + g[b.getX()][b.getY() + 1].getCoordinates());
                                    put = g[b.getX()][b.getY() + 1];
                                    if(!startLocs.contains(put)){startLocs.push(put);}
                                }
                            }
                        } else if (b.getX() == h - 2 && b.getY() > 0 && b.getY() < w - 1) {//deal with IN south perim
                            System.out.println("x==h-2");
                            if (g[b.getX() - 1][b.getY()].getType() == 1 || g[b.getX() - 1][b.getY()].getType() == 8) {//check above it
                                System.out.println("pushing " + g[b.getX() - 1][b.getY()].getCoordinates());
                                put = g[b.getX() - 1][b.getY()];
                                if(!startLocs.contains(put)){startLocs.push(put);}
                            }
                            if (b.getY() > 0) {//check left
                                System.out.println("AND Y > 1");
                                if (b.getY() < w - 1 && (g[b.getX()][b.getY() - 1].getType() == 1 || g[b.getX()][b.getY() - 1].getType() == 8)) {
                                    System.out.println("pushing " + g[b.getX()][b.getY() - 1].getCoordinates());
                                    put = g[b.getX()][b.getY() - 1];
                                    if(!startLocs.contains(put)){startLocs.push(put);}
                                }
                            }
                            if (b.getY() < w - 1) {//check right
                                System.out.println("AND y < w-1");
                                if (b.getY() < w - 1 && (g[b.getX()][b.getY() + 1].getType() == 1 || g[b.getX()][b.getY() + 1].getType() == 1)) {
                                    System.out.println("pushing " + g[b.getX()][b.getY() + 1].getCoordinates());
                                    put = g[b.getX()][b.getY() + 1];
                                    if(!startLocs.contains(put)){startLocs.push(put);}
                                }
                            }
                        } else if (b.getY() == 1 && b.getX() > 0 && b.getX() < h - 1) {//deal with IN west perimeter
                            System.out.println("y == 1");
                            if (g[b.getX()][b.getY() + 1].getType() == 1 || g[b.getX()][b.getY() + 1].getType() == 8) {//check right
                                System.out.println("pushing " + g[b.getX()][b.getY() + 1].getCoordinates());
                                put = g[b.getX()][b.getY() + 1];
                                if(!startLocs.contains(put)){startLocs.push(put);}
                            }
                            if (b.getX() > 0) {//check above
                                System.out.println("AND x > 1");
                                if (b.getX() < h - 1 && (g[b.getX() + 1][b.getY()].getType() == 1 || g[b.getX() + 1][b.getY()].getType() == 8)) {
                                    System.out.println("pushing " + g[b.getX() + 1][b.getY()].getCoordinates());
                                    put = g[b.getX() + 1][b.getY()];
                                    if(!startLocs.contains(put)){startLocs.push(put);}
                                }
                            }
                            if (b.getX() < h - 1) {//check below
                                System.out.println("AND x < h-1");
                                if (b.getX() > 0 && (g[b.getX() - 1][b.getY()].getType() == 1 || g[b.getX() - 1][b.getY()].getType() == 8)) {
                                    System.out.println("pushing " + g[b.getX() - 1][b.getY()].getCoordinates());
                                    put = g[b.getX() - 1][b.getY()];
                                    if(!startLocs.contains(put)){startLocs.push(put);}
                                }
                            }
                        } else if (b.getY() == w - 2 && b.getX() > 0 && b.getY() < h - 1) {//deal with IN east perim
                            System.out.println("Y == w-2");
                            if (g[b.getX()][b.getY() - 1].getType() == 1 || g[b.getX()][b.getY() - 1].getType() == 8) {//check right
                                System.out.println("pushing " + g[b.getX()][b.getY() - 1].getCoordinates());
                                put = g[b.getX()][b.getY() - 1];
                                if(!startLocs.contains(put)){startLocs.push(put);}
                            }
                            if (b.getX() > 0) {//check above
                                System.out.println("AND X > 1");
                                if (b.getX() < h - 1 && (g[b.getX() + 1][b.getY()].getType() == 1 || g[b.getX() + 1][b.getY()].getType() == 8)) {
                                    System.out.println("pushing " + g[b.getX() + 1][b.getY()].getCoordinates());
                                    put = g[b.getX() + 1][b.getY()];
                                    if(!startLocs.contains(put)){startLocs.push(put);}
                                }
                            }
                            if (b.getY() < h - 1) {//check below
                                System.out.println("Y < h-1");
                                if (b.getX() > 0 && (g[b.getX() - 1][b.getY()].getType() == 1 || g[b.getX() - 1][b.getY()].getType() == 8)) {
                                    System.out.println("pushing " + g[b.getX() - 1][b.getY()].getCoordinates());
                                    put = g[b.getX() - 1][b.getY()];
                                    if(!startLocs.contains(put)){startLocs.push(put);}
                                }
                            }
                        } else {//Determined not ON any perimeter. Check all the four corners around it
                            System.out.println("ITs not on the perimeter nor in it");
                            if (g[b.getX() + 1][b.getY()].getType() == 1 || g[b.getX() + 1][b.getY()].getType() == 8) {//store it below
                                System.out.println("pushing " + g[b.getX() + 1][b.getY()].getCoordinates());
                                put = g[b.getX() + 1][b.getY()];
                                if(!startLocs.contains(put)){startLocs.push(put);}
                            }
                            if (g[b.getX() - 1][b.getY()].getType() == 1 || g[b.getX() - 1][b.getY()].getType() == 8) {//store it above
                                System.out.println("pushing " + g[b.getX() - 1][b.getY()].getCoordinates());
                                put = g[b.getX() - 1][b.getY()];
                                if(!startLocs.contains(put)){startLocs.push(put);}
                            }
                            if (g[b.getX()][b.getY() + 1].getType() == 1 || g[b.getX()][b.getY() + 1].getType() == 8) {//store it right
                                System.out.println("pushing " + g[b.getX()][b.getY() + 1].getCoordinates());
                                put = g[b.getX()][b.getY() + 1];
                                if(!startLocs.contains(put)){startLocs.push(put);}
                            }
                            if (g[b.getX()][b.getY() - 1].getType() == 1 || g[b.getX()][b.getY() - 1].getType() == 8) {//store it left
                                System.out.println("pushing " + g[b.getX()][b.getY() - 1].getCoordinates());
                                put = g[b.getX()][b.getY() - 1];
                                if(!startLocs.contains(put)){startLocs.push(put);}
                            }
                        }
                    }
                }
            }
        }
        return startLocs;
    }
    
    /*
    Algorithm:
        1.Pick the start node. Pick an edge to travel across. This means pick a direction for which there is another block. The edge cannot be contained in a hashmap of edge/node pairs (this would mean it was already traversed).
        2.If there are no edge to traverse and this is the start node algorithm terminates
        3.If there are no possible directions to go on the selected block, backtrack one node to the previous one and pick a different direction. repeat.
        4.IF there IS another possible direction to go on the selected block traverse the edge, and add it to the path.
        5.IF the current node (one space in front of the block we're on) is the finish node copy the path to completed paths and backtrack to the previous node. Go to step 2.        
    */
    private static void depthFirstSearch(Block[][]g, Stack<Block> S, int xS, int yS, int w, int h){
        //the key = block.id and the value is a collection of other keys this block can get to
        //this will be the large "bucket" that stores each path. A path is an arraylist with nodes in order of traversal
        ArrayList<ArrayList<Integer>> paths = new ArrayList<ArrayList<Integer>>();
        HashMap<Integer, ArrayList<Integer>> pathPairs = constructGraph(g, S, w, h);
        int blockId = g[xS][yS].getId();
        int startBlockId = blockId;
        ArrayList<Integer> currentPath = new ArrayList<Integer>();
        int failCheck = 0;
                print2(g);

        try{
        do{
            ++failCheck;
            if(failCheck>500){
                System.out.println("INFINTE LOOP failure");
                System.exit(1);
            }
            System.out.println("traverse from block " + blockId);
            //choose a direction from the arrayList and to to that block
            if(blockId == startBlockId && !hasMoreEdges(blockId, pathPairs)){
                System.out.println("Made our way back, leaving this place");
                //exit state - we have traversed every direction
                break;
            }else if(!hasMoreEdges(blockId, pathPairs)){
                System.out.println("STOP. there are no more ");
                //reached the end of a certain path. add the path into the bucket collection and backtrack one node
                paths.add(currentPath);
                currentPath.remove(currentPath.size()-1);//remove the last node
                blockId = currentPath.get(currentPath.size()-1);//backtrack one node
            }else{
                System.out.println("");
                ArrayList<Integer> blockPairs = pathPairs.get(blockId);//grab all possible directions to go
                System.out.println("traversing to another block!");
                int nextBlockId = blockPairs.remove(blockPairs.size()-1);//this is in effect selecting one out of all the directions to go
                System.out.println("nextBlock = " + nextBlockId);
                pathPairs.put(blockId, blockPairs);//put the list of paths back into the hashmap
                currentPath.add(nextBlockId);//store the node we traversed to
                blockId = nextBlockId;//move the current block to that block
            }
        }while(true);
        }catch(Exception e){
            System.out.println("Failure");
            e.printStackTrace();
            System.exit(1);
        }
        System.out.println("YAS!!!!\t WE MADE IT OUT!!!!\nThe paths created are those below:\n\n");
        for(ArrayList<Integer> A : paths){
            System.out.println("PATH:");
            for(int n : A){
                System.out.println("Node: " + n);
            }
        }
        print(g);
        System.out.println("pathPairs.size = " + pathPairs.size());
        for(Map.Entry<Integer,ArrayList<Integer>> e : pathPairs.entrySet()){
            System.out.print("Block ID: " + e.getKey() + " is connected to ");
            for(int i : e.getValue()){
                System.out.print("("+i+") ");
            }
            System.out.println("\n");
        }
    }
    
    private static Block getBlockById(int id, Block[][] g){
        for (int i = 0; i < g.length; ++i) {
            for (int j = 0; j < g[i].length; ++j) {
                if(g[i][j].getId()==id){
                    return g[i][j];
                }
            }
        }
        return null;
    }
    
    private static boolean hasMoreEdges(int key, HashMap<Integer, ArrayList<Integer>> pps){
        ArrayList<Integer> pairs = pps.get(key);
        return pairs.size()!=0;
    }
    
    /*
        Each block in the grid has an ID
        Copy old matrix into new one. 
        Pop off items from the path Stack placing a -8 at each location unless the type at the location IS an 8, then it stays the same. This means this is the finish (exit condition) node.
    */
    private static HashMap<Integer, ArrayList<Integer>> constructGraph(Block[][] g, Stack<Block> S, int w, int h){
         HashMap<Integer, ArrayList<Integer>> pps = new HashMap<Integer, ArrayList<Integer>>();

        //populate the pathPairs hashmap
        for(Block b : S){
            System.out.println("checking b:"+b.getCoordinates());
            ArrayList<Integer> pathPairs = new ArrayList<Integer>();
            
            //first determine which way NOT to check. i.e. in front of it
            
            //check for the first block north, south, east, and west that this block can see. Add itself and each connection to the hashmap
            //north - has to be greater than the top most row ie 1
            if(b.getX()>1 && g[b.getX()-1][b.getY()].getType()!=0){//if there is a block right in front of, or next to it, dont count that as a path.
                System.out.println("checking norths");
                for(int i = b.getX()-1; i>0; --i){
                    if(g[i][b.getY()].getType()==0){
                        //store it and break to check another direction
                        System.out.println("found block " + g[i][b.getY()].getCoordinates());
                        pathPairs.add(g[i+1][b.getY()].getId());
                        break;
                    }
                }
            }
            //south - has to be less than the bottom most row ie height-2
            if(b.getX()<h-2 && g[b.getX()+1][b.getY()].getType()!=0){//if there is a block right in front of, or next to it, dont count that as a path.
                System.out.println("checking south");
                for(int i = b.getX()+1; i<h-1; ++i){
                    if(g[i][b.getY()].getType()==0){
                        //store it and break to check another direction
                        System.out.println("found block " + g[i][b.getY()].getCoordinates());
                        pathPairs.add(g[i-1][b.getY()].getId());
                        break;
                    }
                }
            }
            //east - has to be less than the rightmost column ie width-2
            if(b.getY()<w-2 && g[b.getX()][b.getY()+1].getType()!=0){//if there is a block right in front of, or next to it, dont count that as a path.
                System.out.println("checking east");
                for(int i = b.getY()+1; i<w-1; ++i){
                    if(g[b.getX()][i].getType()==0){
                        //store it and break to check another direction
                        System.out.println("found block " + g[b.getX()][i].getCoordinates());
                        pathPairs.add(g[b.getX()][i-1].getId());
                        break;
                    }
                }
            }
            //west - has to be greater than left most column ie 1
            if(b.getY()>1 && g[b.getX()][b.getY()-1].getType()!=0){//if there is a block right in front of, or next to it, dont count that as a path.
                System.out.println("checking west");
                for(int i = b.getY()-1; i>0; --i){
                    if(g[b.getX()][i].getType()==0){
                        //store it and break to check another direction
                        System.out.println("found block " + g[b.getX()][i].getCoordinates());
                        pathPairs.add(g[b.getX()][i+1].getId());
                        break;
                    }
                }
            }
            //store the block ID and collection of every block it can connect to into the hashmap.
            System.out.println("storing block "+b.getId()+" with " + pathPairs.size()+" block pairs into pps");
            pps.put(b.getId(), pathPairs);
        }
        System.out.println("Returning pps with a size of " + pps.size());
        return pps;
    }
    
    
    //Not 100% accurate
    private static void backwardsTraversal(Stack<Block>paths){
        System.out.println("trying traversals");
        //first thing popped off is the last node.
        Stack<Block> correctPath = new Stack<Block>();
        
        boolean isXStreak = false;
        int streak = 0;
        int streakCounter = 1;
        
        //store last node
        correctPath.push(paths.pop());
        
        if(correctPath.peek().getX() == paths.peek().getX()){//just set the streak  either east or west
            streak = paths.peek().getX();
            isXStreak = true;
            ++streakCounter;
        }else if(correctPath.peek().getY() == paths.peek().getY()){//just set the streak   either north or south
            streak = paths.peek().getY();
            isXStreak = true;
            ++streakCounter;
        }
        
        int streakX = 0;
        int streakY = 0;
        
       /* try{
        Block[] blocks = new Block[paths.size()];
        int index = 0;
        int counter = 0;
        while(!paths.isEmpty()){
            Block b = paths.pop();
            System.out.println("storing " + b.getCoordinates());
            if(counter < 2){
                blocks[index] = b;
                ++index;
                ++counter;
            }else{
               Block oneLess = blocks[index-1];
               Block twoLess = blocks[index-2];
               if(b.getX() == twoLess.getX()){
                   if(b.getX() == oneLess.getX()){
                        blocks[index-1]=b;
                   }else{
                       blocks[index]=b;
                       ++index;
                   }
               }else if(b.getY() == twoLess.getY()){
                   if(b.getY() == oneLess.getY()){
                        blocks[index-1]=b;
                   }else{
                       blocks[index]=b;
                       ++index;
                   }
               }
            }
        }
        for(int i = 0; i < blocks.length; ++i){
            System.out.println(blocks[i].getCoordinates());
        }
        }catch(Exception e){
        e.printStackTrace();
        }*/
        

        while (!paths.isEmpty()) {
            Block b = paths.pop();
            System.out.println("streak is "+ streak + " counted at " + streakCounter + " checking "+b.getCoordinates());
            if (streakCounter < 3) {
                if (correctPath.peek().getX() == b.getX()) {//just set the streak
                    System.out.println("X's");
                    if(streak == b.getX() && isXStreak){
                    ++streakCounter;
                    }else{
                     streak = b.getY();
                     streak = 1;
                    }
                correctPath.push(b);
                } else if (correctPath.peek().getY() == b.getY()) {//just set the streak
                    System.out.println("Y's");
                    if(streak == b.getY()){
                    ++streakCounter;
                    }else{
                     streak = b.getX();
                     streak = 1;
                    }
                                    correctPath.push(b);
                }

            } else {
                System.out.println("NOT < 2");
                if (b.getX() == streak) {//compare Y's
                    System.out.println("X's, popping the streak"+correctPath.peek().getCoordinates() + " pushing " + b.getCoordinates());
                    streak = b.getX();
                    //correctPath.pop();
                    //correctPath.push(b);
                } else if (b.getY() == streak) {//compare X's
                                        System.out.println("Y's the streak, popping "+correctPath.peek().getCoordinates() + " pushing " + b.getCoordinates());
                    streak = b.getY();
                    //correctPath.pop();
                    //correctPath.push(b);
                } else {
                    System.out.println("neither");
                    if (correctPath.peek().getX() == b.getX()) {//just set the streak
                        if (streak == b.getX()) {
                            ++streakCounter;
                        } else {
                            streak = b.getY();
                            streak = 1;
                        }
                                            correctPath.push(b);

                    } else if (correctPath.peek().getY() == b.getY()) {//just set the streak
                        if (streak == b.getY()) {
                            ++streakCounter;
                        } else {
                            streak = b.getX();
                            streak = 1;
                        }
                                            correctPath.push(b);

                    }
                    System.out.println(" pushing " + b.getCoordinates());
                }
            }
        }
        
        while(!correctPath.isEmpty()){
            System.out.println(correctPath.pop().getCoordinates());
        }
    
    //Below are attempts at solving algorithms. None of them were 100% successful
    /*
    private static boolean correctForm(Block a, Block b, Block c, int xORy){
        if(xORy==1){//x
            if(b.getY() > a.getY() && c.getY() < a.getY()){
                return true;
            }else if(b.getY() < a.getY() && c.getY() > a.getY()){
                return true;
            }else{
                return false;
            }
        }else{//y
            if(b.getX() > a.getX() && c.getX() < a.getX()){
                return true;
            }else if(b.getX() < a.getX() && c.getX() > a.getX()){
                return true;
            }else{
                return false;
            }
        }
    }
    
    private static void mapPaths(Block[][] g, Stack<Block> nodes) {
        if (nodes.size() <= 3) {
            System.out.println("Useless paths...");
            return;
        }
        ArrayList<Stack<Block>> paths = new ArrayList<Stack<Block>>();
        Stack<Block> SBeginning = new Stack<Block>();
        Block pointer;
        SBeginning.push(nodes.pop());
        paths.add(SBeginning);
        while (!nodes.isEmpty()) {
            //check is the next node has anything in common
            Block next = nodes.pop();
            System.out.println("next node: " + next.getCoordinates());
            for (Stack<Block> S : paths) {
                pointer = S.peek();
                System.out.println("pointer node: " + pointer.getCoordinates());
                if (next.getX() == pointer.getX()) {
                    System.out.println("next and pointer X's are the same");
                    //it does so check the next node!
                    Block temp = nodes.pop();
                    System.out.println("temp node: " + temp.getCoordinates());
                    //check if the thrid node ALSO has the same x
                    if (temp.getX() == pointer.getX()) {//1==x, 2==y
                        System.out.println("");
                        Stack<Block> SS = S;
                        SS.push(next);
                        paths.add(SS);
                        //acreate another stack, copy old stack, and add one to it
                        //then for each stack before the added see if we can add the  that one too 
                        for (int i = 0; i < paths.size() - 2; ++i) {
                            if (paths.get(i).peek().getX() == temp.getX() || paths.get(i).peek().getY() == temp.getY()) {
                                paths.get(i).push(temp);
                            }
                        }
                    } else {
                        if(temp.getX() == next.getX() || temp.getY() == pointer.getY()){
                            S.push(next);
                            S.push(temp);
                        }else{
                            S.push(temp);
                        }
                    }
                } else if (next.getY() == pointer.getY()) {//same Y
                    Block temp = nodes.pop();
                    if (temp.getY() == pointer.getY()) {//same Y again!
                        Stack<Block> SS = S;
                        SS.push(next);
                        paths.add(SS);
                        //acreate another stack, copy old stack, and add one to it
                        //then for each stack before the added see if we can add the  that one too 
                        for (int i = 0; i < paths.size() - 2; ++i) {
                            if (paths.get(i).peek().getX() == temp.getX() || paths.get(i).peek().getY() == temp.getY()) {
                                paths.get(i).push(temp);
                            }
                        }
                    } else {
                        if(temp.getX() == next.getX() || temp.getY() == pointer.getY()){
                            S.push(next);
                            S.push(temp);
                        }else{
                            S.push(temp);
                        }
                    }
                }
            }
        }
*/
    }
    
    
    
    
    
    /*
//        totally recursive
 //       Start at the beginning check north south east and west. 
  //      Store all directions that are possible. 
  //      Just choose one of the directions. Always choose north first if possible, then south, then east, then west as convention. 1 stack for each direction.
  //      Using a stack push the coordinates the runner is currently on, onto a stack.
    //    Move to the next obstacle in the path and repeat.
      //  Do this for every direction possible
    private static void ensureMinMoves(Block[][] g, int moveGoal, int xStart, int yStart, int xFinish, int yFinish){
        Stack<Block> reversePaths = new Stack<Block>();
        System.out.println("\t\ttraversing the paths!");
        ArrayList<Stack<Block>> pathList = new ArrayList<Stack<Block>>();
        HashMap<Integer, Integer> pathMap = new HashMap<Integer, Integer>();
        traverse(g, xStart, yStart, xFinish, yFinish, reversePaths, -1, moveGoal, pathList, pathMap);
        System.out.println("DONE traversing SpathList.size = " + pathList.size());
        //Stack<Block> paths = new Stack<Block>();
        //while(!reversePaths.isEmpty()){
        //    paths.push(reversePaths.pop());
        //}
        for(Stack<Block> s : pathList){
            System.out.print("Starting at BLOCK: ");
            for(Block b : s){
                System.out.print("["+b.getX() + ","+b.getY()+"] \nGo to BLOCK: ");
            }
        }
    }
    
    private static boolean checkForFinish(Block[][]g, int x, int y){
        if(g[x+1][y].getType() == 8 || g[x-1][y].getType() == 8 || g[x][y+1].getType() == 8 || g[x-1][y].getType() == 8){
            return true;
        }
        return false;
    }
    
    
    private static void traverse(Block[][] g, int x, int y, int xFinish, int yFinish, Stack<Block> S, int lastDir, int moves, ArrayList<Stack<Block>> paths, HashMap<Integer, Integer> pathMap) {
        System.out.println("traversing path from " + g[x][y].getCoordinates() + " with type: " + g[x][y].getType());
        //WE HAVE MADE IT
        if (checkForFinish(g, x, y)) {
            //enqueue it and return
            S.push(g[xFinish][yFinish]);
            paths.add(S);
            System.out.println("hit the finish!");
            return;
        }
        Block b;
        //check the path for 1=north, 2=south, 3=east, 4=west
            //if the lastDirection was -1 that means this is the first runthrough so all directions are a possibility
            //every other block is either exactly north and south or east and west
            if (lastDir < 0 ) {
                System.out.println("First time must check every dir");
                System.out.println("cehcking north");
                b = checkPath(g, g[x][y], 1, pathMap, lastDir);
                if (b != null) {
                    pathMap.put(b.getX(), b.getY());
                    Stack<Block> SN = S;
                    System.out.println("creating new Stack");
                    System.out.println("the north path to "+b.getCoordinates()+"was good, store it");
                    SN.push(b);
                    traverse(g, b.getX(), b.getY(), xFinish, yFinish, SN, 1, moves, paths, pathMap);
                }
                b = checkPath(g, g[x][y], 2, pathMap, lastDir);
                System.out.println("checking south");
                if (b != null) {
                    pathMap.put(b.getX(), b.getY());
                    Stack<Block> SS = S;
                    System.out.println("creating new Stack");
                    System.out.println("the south path to "+b.getCoordinates()+"was good, store it");
                    SS.push(b);
                    traverse(g, b.getX(), b.getY(), xFinish, yFinish, SS, 2, moves, paths, pathMap);
                }
                b = checkPath(g, g[x][y], 3, pathMap, lastDir);
                System.out.println("checking east");
                if (b != null) {
                    pathMap.put(b.getX(), b.getY());
                    Stack<Block> SE = S;
                    System.out.println("creating new Stack");
                    System.out.println("the east path to "+b.getCoordinates()+"was good, store it");
                    SE.push(b);
                    traverse(g, b.getX(), b.getY(), xFinish, yFinish, SE, 3, moves, paths, pathMap);
                }
                b = checkPath(g, g[x][y], 4, pathMap, lastDir);
                System.out.println("checking west");
                if (b != null) {
                    pathMap.put(b.getX(), b.getY());
                    Stack<Block> SW = S;
                    System.out.println("creating new Stack");
                    System.out.println("the west path to "+b.getCoordinates()+"was good, store it");
                    SW.push(b);
                    traverse(g, b.getX(), b.getY(), xFinish, yFinish, SW, 4,moves, paths, pathMap);
                }
        }else {//anything BUT the first runthrough
            if (lastDir == 1 || lastDir == 2) {//only check east and west
                Block b1 = checkPath(g, g[x][y], 3, pathMap, lastDir);
                Block b2 = checkPath(g, g[x][y], 4, pathMap, lastDir);
                if(b1!= null && b2!=null){//store east, name a new stack for west
                    System.out.println("EAST AND WEST storing in current stack");
                    S.push(b1);
                    pathMap.put(b1.getX(), b1.getY());
                    System.out.println("the east path to "+b1.getCoordinates()+"was good, store it");
                    traverse(g, b1.getX(), b1.getY(), xFinish, yFinish, S, 3,moves, paths, pathMap);
                    System.out.println("copying old stack and creating new Stack");
                    Stack<Block> S1 = S;
                    S1.push(b2);
                    pathMap.put(b2.getX(), b2.getY());
                    System.out.println("the west path to "+b2.getCoordinates()+"was good, store it");
                    traverse(g, b2.getX(), b2.getY(), xFinish, yFinish, S1,4,moves, paths, pathMap);
                }else if(b1!=null){//store east
                    System.out.println("the east path to " + b1.getCoordinates() + "was good, store it");
                    System.out.println("storing in current stack");
                    S.push(b1);
                    pathMap.put(b1.getX(), b1.getY());
                    System.out.println("the east path to "+b1.getCoordinates()+"was good, store it");
                    traverse(g, b1.getX(), b1.getY(), xFinish, yFinish, S, 3,moves, paths, pathMap);
                }else if(b2!=null){//store west
                    System.out.println("storing in current stack");
                    S.push(b2);
                    pathMap.put(b2.getX(), b2.getY());
                    System.out.println("the west path to "+b2.getCoordinates()+"was good, store it");
                    traverse(g, b2.getX(), b2.getY(), xFinish, yFinish, S,4,moves, paths, pathMap);
                }else{
                    System.out.println("PATH END CANNOT GO ANYWHERE");
                    return;
                }
            } else if (lastDir == 3 || lastDir == 4) {//only check north and south
                Block b1 = checkPath(g, g[x][y], 1, pathMap, lastDir);
                Block b2 = checkPath(g, g[x][y], 2, pathMap, lastDir);
                if(b1!= null && b2!=null){//store east, name a new stack for west
                    System.out.println("NORTH AND SOUTH storing in current stack");
                    S.push(b1);
                    pathMap.put(b1.getX(), b1.getY());
                    System.out.println("the north path to "+b1.getCoordinates()+"was good, store it");
                    traverse(g, b1.getX(), b1.getY(), xFinish, yFinish, S, 1,moves, paths, pathMap);
                    System.out.println("copying old stack and creating new Stack");
                    Stack<Block> S1 = S;
                    S1.push(b2);
                    pathMap.put(b2.getX(), b2.getY());
                    System.out.println("the south path to "+b2.getCoordinates()+"was good, store it");
                    traverse(g, b2.getX(), b2.getY(), xFinish, yFinish, S1,2,moves, paths, pathMap);
                }else if(b1!=null){//store east
                    S.push(b1);
                    pathMap.put(b1.getX(), b1.getY());
                    System.out.println("storing in current stack");
                    System.out.println("the north path to "+b1.getCoordinates()+"was good, store it");
                    traverse(g, b1.getX(), b1.getY(), xFinish, yFinish, S, 1,moves, paths, pathMap);
                }else if(b2!=null){//store west
                    S.push(b2);
                    pathMap.put(b2.getX(), b2.getY());
                    System.out.println("the south path to "+b2.getCoordinates()+"was good, store it");
                    System.out.println("storing in current stack");
                    traverse(g, b2.getX(), b2.getY(), xFinish, yFinish, S,2,moves, paths, pathMap);
                }else{
                    System.out.println("PATH END cannot go anywhere");
                    return;
                }
            }
        }
        System.out.println("return s");
    }
    
    private static boolean isValidBlock(int lastDir, int checking){
        System.out.println("validating lastDir " + lastDir + " and checking "+checking);
        switch (lastDir) {
            case 1://north - cannot go south
                System.out.println("came going north cant go north or south");
                return checking != 2 && checking != 1;
            case 2://south - cannot go north
                System.out.println("came going south cant go south or north");
                return checking != 1 && checking != 2;
            case 3://east - cannot go west
                System.out.println("came from east cant go east or west");
                return checking != 4 && checking !=3;
            case 4://west - cannot go east
                System.out.println("came from west cant go west or east");
                return checking != 3 && checking !=4;
            default://will literally never happen. I hope..
                return true;
        }
    }
    
    //Check along the north, south, east, west planes to see if there is a block 
    //Its either horizontal or vertical
    //NOTE! the returned block is actually the block right before the block encountered, because the runner stops AT each block, not on top of.
    private static Block checkPath(Block[][] g,Block b, int direction, HashMap<Integer, Integer> pathMap,int lastDir) {
        //horizontal if true verticcal if false;
            switch (direction) {
            case 3://east
                System.out.println("checking east");
                for (int j = b.getY(); j < g[b.getX()].length; ++j) {
                    //as soon as we find a block return it
                    if (g[b.getX()][j].getType() == 0 && (j-1!=b.getY()) && isValidBlock(lastDir, direction)) {
                        System.out.println("found a block at "+g[b.getX()][j].getCoordinates());
                        return g[b.getX()][j-1];
                    }
                }
                break;
            case 4://west
                System.out.println("checking west");
                for (int j = b.getY(); j >= 0; --j) {
                    //as soon as we find a block return it
                    if (g[b.getX()][j].getType() == 0 && (j+1!=b.getY()) && isValidBlock(lastDir, direction)) {
                        System.out.println("found a block at "+g[b.getX()][j].getCoordinates());
                        return g[b.getX()][j+1];
                    }
                }
                break;
            case 2://south
                System.out.println("checking south");
                for (int i = b.getX(); i < g[b.getX()].length; ++i) {
                    //as soon as we find a block return it
                    if (g[i][b.getY()].getType() == 0 && (i-1!=b.getX()) && isValidBlock(lastDir, direction)) {
                        System.out.println("found a block at " + g[i][b.getY()].getCoordinates());
                        return g[i-1][b.getY()];
                    }
                }
                break;
            case 1://north
                System.out.println("checking north");
                for (int i = b.getX(); i >= 0; --i) {
                    //as soon as we find a block return it
                    if (g[i][b.getY()].getType() == 0 && (i+1!=b.getX()) && isValidBlock(lastDir, direction)) {
                        System.out.println("found a block at "+g[i][b.getY()].getCoordinates());
                        return g[i+1][b.getY()];
                    }
                }
                break;
        }
        //if we didn't find any blocks this way, then we shouldn't traverse it so return null
        return null;
    }
    
    
    private static boolean hasBeenTraversed(HashMap<Integer, Integer> hm, int x, int y){
        for(Map.Entry<Integer,Integer> e : hm.entrySet()){
            if(e.getValue() == y && e.getKey() == x){
                return true;
            }
        }
        return false;
    }*/

    //print out the matrix for testing and logging purposes
    private static void print(Block[][] g) {
        for (int i = 0; i < g.length; ++i) {
            for (int j = 0; j < g[i].length; ++j) {
                System.out.print(g[i][j].getType() + "\t");
            }
            System.out.print("\n");
        }
    }
    private static void print2(Block[][] g) {
        for (int i = 0; i < g.length; ++i) {
            for (int j = 0; j < g[i].length; ++j) {
                System.out.print(g[i][j].getId() + "\t");
            }
            System.out.print("\n");
        }
    }

}

