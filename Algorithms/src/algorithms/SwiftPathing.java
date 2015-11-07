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
    //constant syntax appearing on various lines. Every line has some or some combo of these
    private static final String layoutPrefix = "android:layout_";
    private static final String idPrefix = "android:id";
    private static final String backgroundPrefix = "android:background";
    private static final String idFile = "=\"@+id/";//can be used as a reference AND declaration
    private static final String dimenFile = "=\"@dimen/";
    private static final String drawFile="=\"@drawable/";
    private static final String qm = "\"";
    //constant names for variable names. Sequentially increasing numbers are just appended onto them for each new instance
    private static final String constObstacle = "obstacle";
    private static final String constRunner = "dude";
    private static final String constHint = "hint_";
    private static final String constDimen = "obstacle_width";
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
                return layoutPrefix+"marginTop"+idFile;            
            case MGN_BTM:
                return layoutPrefix+"marginBottom"+idFile;            
            case MGN_LT:
                return layoutPrefix+"marginLeft"+idFile;            
            case MGN_RT:
                return layoutPrefix+"marginRight"+idFile;   
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
            
            //Randomize the start X and Y coordinates for the runner 
            int xStart =(randGen.nextInt((height-1)-1)+1);
            int yStart = (randGen.nextInt((width-1)-1)+1);
            
            grid[xStart/*row*/][yStart/*column*/] = 8;
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
    private static StringBuilder populateGrid(int[][] g, int x, int y, int moves, int width, int height, Random R, int wallUse) throws IllegalStateException, Exception {
        //Stores the generated XML for this particular level
        StringBuilder levelXML = new StringBuilder();
        
        //stores the current block because it could potentially be the end
        int xFinish = x;
        int yFinish = y;
        
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
                            if (g[i][yDir] == -1) {//make sure we aren't jumping over any blocks (3)
                                if (g[i - 1][yDir] != 0) {
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
                            if (g[i][yDir] == -1) {//(4)
                                if (g[i + 1][yDir] != 0) {
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
                            if (g[xDir][i] == -1) {//(4)
                                if (g[xDir][i + 1] != 0) {
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
                            if (g[xDir][i] == -1) {//(4)
                                if (g[xDir][i - 1] != 0) {
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
                    if (g[xDir][yDir - 1] == 0 || g[xDir][yDir - 2] == 0) {//impossible to go west direction if either the direct next or next two blocks are blocked (2)
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
                                if (g[xDir][i] == 0 && (!useWall ? !(i == 0 || i == width - 1) : true)) {
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
                    if (g[xDir][yDir + 1] == 0 || g[xDir][yDir + 2] == 0) {//(2)
                        east = false;//(3)
                        System.out.println("east is impossible");
                    } else {//(4)
                        //(5)
                        if (counter < moves) {
                            for (int i = yDir + 2; i < width; ++i) {
                                //(6)
                                if (g[xDir][i] == 0 && (!useWall ? !(i == 0 || i == width - 1) : true)) {
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
                    if (g[xDir - 1][yDir] == 0 || g[xDir - 2][yDir] == 0) {//(2)
                        north = false;//(3)
                        System.out.println("north is impossible");
                    } else {//(4)
                        //(5)
                        if (counter < moves) {
                            for (int i = xDir - 2; i >= 0; --i) {
                                //(6)
                                if (g[i][yDir] == 0 && (!useWall ? !(i == 0 || i == height - 1) : true)) {
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
                    if (g[xDir + 1][yDir] == 0 || g[xDir + 2][yDir] == 0) {//(2)
                        south = false;//(3)
                        System.out.println("south is impossible");
                    } else {//(4)
                        //(5)
                        if (counter < moves) {
                            for (int i = xDir + 2; i < height; ++i) {
                                //(6)
                                if (g[i][yDir] == 0 && (!useWall ? !(i == 0 || i == height - 1) : true)) {
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
                                System.out.println("choosing exaclt block located xDir " + xDir);
                                if (g[xDir + 1][yDir] == 1) {//indicates that on the way to the block we were traversing an already traversed path = backtracking
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
                                while (g[tempXDir][yDir] == 1 || blocked) {
                                    System.out.println("trying AGAIN to use tempXDir as " + tempXDir);
                                    //Get another random number(5)
                                    tempXDir = R.nextInt(xDir - 1);
                                    blocked = false;
                                    //This is to check that there are no blocks in the line of sight to the next block(6)
                                    for (int i = previousX; (i >= 0) && i > tempXDir; --i) {
                                        //If we make it all the way down the line without breaking its possible this entire line is made up on another blocks path we need a new direction...(7)
                                        System.out.println("i = " + i);
                                        if (g[i][yDir] == 0) {
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
                            }
                            //store THIS current block because it might be a finish block
                            xFinish = xDir;
                            yFinish = yDir;
                            
                            System.out.println("new position to place block is (" + xDir + "," + yDir + ")");
                            //Store the new block in the found place(10)
                            g[xDir][yDir] = 0;
                            //Since the runner can't actually go INTO the block, it has to stop one space before the placed block since it "hits" the block
                            //NOTICE: This is only true for plain obscure block obstacles. Once Bubble, Breakable, and Portal blocks are implemented this will need jsut a little extra magic (11)
                            ++xDir;
                            g[xDir][yDir] = 8;
                            System.out.println("new position for movement block is block is (" + xDir + "," + yDir + ")");
                            //Lastly all we have to do is traverse backwards from the new location of the runner to the location where it came from drawing out the path (inserting 1's) (12)
                            for (int i = xDir + 1; (i < height) && i <= previousX && (g[i][yDir] != 0); ++i) {
                                g[i][yDir] = 1;
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
                                System.out.println("choosing exaclt block located xDir " + xDir);
                                if (g[xDir - 1][yDir] == 1) {//indicates that on the way to the block we were traversing an already traversed path = backtracking
                                    System.out.println("BACKTRACKED");
                                    backtracked = true;
                                }
                            } else {
                                //(3)
                                tempXDir = ((height - 1) - (xDir + 2)) > 0 ? R.nextInt(((height - 1) - (xDir + 2))) + (xDir + 2) : (xDir + 2);
                                System.out.println("trying to use tempXDir as " + tempXDir + " and yDir as " + yDir);
                                //(4)
                                while (g[tempXDir][yDir] == 1 || blocked) {
                                    System.out.println("trying AIAIN to use tempXDir as " + tempXDir);
                                    //(5)
                                    tempXDir = R.nextInt(((height - 1) - xDir + 2)) + (xDir + 2);
                                    blocked = false;
                                    //(6)
                                    for (int i = previousX; (i < height - 1) && i < tempXDir; ++i) {
                                        System.out.println("i = " + i);
                                        //(7)
                                        if (g[i][yDir] == 0) {
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
                            }
                            //store THIS current block because it might be a finish block
                            xFinish = xDir;
                            yFinish = yDir;
                            
                            //(10)
                            g[xDir][yDir] = 0;
                            --xDir;
                            //(11)
                            g[xDir][yDir] = 8;
                            System.out.println("new position to place block is (" + xDir + "," + yDir + ")");
                            //(12)
                            for (int i = xDir - 1; (i >= 0) && i >= previousX && (g[i][yDir] != 0); --i) {
                                System.out.println("i = " + i);
                                g[i][yDir] = 1;
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
                                System.out.println("chosen to use exact block located at  yDir " + yDir);
                                if (g[xDir][yDir - 1] == 1) {//indicates that on the way to the block we were traversing an already traversed path = backtracking
                                    System.out.println("BACKTRACKED");
                                    backtracked = true;
                                }
                            } else {
                                //(3)
                                tempYDir = ((width - 1) - (yDir + 2)) > 0 ? R.nextInt(((width - 1) - (yDir + 2))) + (yDir + 2) : (yDir + 2);
                                System.out.println("trying to use tempYDir as " + tempYDir + " and xDir as " + xDir);
                                //(4)
                                while (g[xDir][tempYDir] == 1 || blocked) {
                                    System.out.println("trying AGAIN to use tempYDir as " + tempYDir);
                                    //(5)
                                    tempYDir = R.nextInt(((width - 1) - (yDir + 2))) + (yDir + 2);
                                    blocked = false;
                                    //(6)
                                    for (int i = previousY; (i < width - 1) && i < tempYDir; ++i) {
                                        System.out.println("i = " + i);
                                        //(7)
                                        if (g[xDir][i] == 0) {
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
                            }
                            //store THIS current block because it might be a finish block
                            xFinish = xDir;
                            yFinish = yDir;
                            //(10)
                            g[xDir][yDir] = 0;
                            --yDir;
                            //(11)
                            g[xDir][yDir] = 8;
                            System.out.println("new position to place block is (" + xDir + "," + yDir + ")");
                            //(12)
                            for (int i = yDir - 1; (i >= 0) && i >= previousY && (g[xDir][i] != 0); --i) {
                                System.out.println("i = " + i);
                                g[xDir][i] = 1;
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
                                System.out.println("chosen to use exact block located at  yDir " + yDir);
                                if (g[xDir][yDir + 1] == 1) {//indicates that on the way to the block we were traversing an already traversed path = backtracking
                                    System.out.println("BACKTRACKED");
                                    backtracked = true;
                                }
                            } else {
                                //(3)
                                tempYDir = R.nextInt(yDir - 1);
                                System.out.println("trying to use tempYDir as " + tempYDir + " and xDir as " + xDir);
                                //(4)s
                                while (g[xDir][tempYDir] == 1 || blocked) {
                                    //(5)
                                    tempYDir = R.nextInt(yDir - 1);
                                    blocked = false;
                                    //(6)
                                    for (int i = previousY; (i >= 0) && i > tempYDir; --i) {
                                        System.out.println("i = " + i);
                                        //(7)
                                        if (g[xDir][i] == 0) {
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
                                //(9)
                                yDir = tempYDir;
                            }
                            //store THIS current block because it might be a finish block
                            xFinish = xDir;
                            yFinish = yDir;
                            //(10)
                            g[xDir][yDir] = 0;
                            ++yDir;
                            //(11)
                            g[xDir][yDir] = 8;
                            System.out.println("new position to place block is (" + xDir + "," + yDir + ")");
                            //(12)
                            for (int i = yDir + 1; (i < width) && i <= previousY && (g[xDir][i] != 0); ++i) {
                                System.out.println("i = " + i);
                                g[xDir][i] = 1;
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
                //Make sure that the final block is not exactly next to the start block...That'd be too easy
                if (counter == moves && ((xDir - 2 == xStart && yDir - 2 == yStart) || (xDir + 2 == xStart && yDir + 2 == yStart))) {
                    System.out.println("cannot end next to the start dont increment counter");
                } else if (backtracked) {
                    System.out.println("we backtracked thus don't increment counter");
                    ++backtrackCounter;
                    if (backtrackCounter > 500) {
                        throw new IllegalStateException("Stuck in an infinite loop due to an infinite backtrack cycle");
                    }
                } else if ((xDir == 1 || xDir == height - 2) || (yDir == 1 || yDir == width - 2)) {
                    System.out.println("cannot finish in a wall");
                } else {
                    ++counter;
                    //print for logging purposes
                    System.out.println("PRINTING");
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
        int obNum = 1;
        System.out.println("xFinish = " + xFinish + "\nyFinish = " + yFinish);
        for (int i = 0; i < g.length; ++i) {
            for (int j = 0; j < g[i].length; ++j) {
                //if we're on the perimeter even if there is a theoretical block, don't add it because it's acting as the wall
                if (!(i == 0 || i == width - 1) && !(j == 0 || j == height-1)) {
                    //this is a writable block!
                    if (g[i][j] == 0) {
                        levelXML.append(
                                viewStart + "\n"
                                + fullAssetDataName(assetData.ID) + ((i==xFinish&&j==yFinish)?(constFinish):(constObstacle + (obNum++))) + qm + "\n"
                                + fullAssetDataName(assetData.WDTH) + qm + "\n"
                                + fullAssetDataName(assetData.HGTH) + qm + "\n"
                                + fullAssetDataName(assetData.BKRND) + fullAssetName(((i==xFinish&&j==yFinish)?assets.P_FIN:assets.P_OBST)) + qm + "\n"
                                + viewEnd + "\n"
                        );
                    }
                }
            }
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
        return levelXML;
    }
    
    private static void ensureMinMoves(int[][] g, int moveGoal, int xStart, int yStart, int xFinish, int yFinish){
    
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


