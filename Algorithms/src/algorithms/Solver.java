/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package algorithms;

import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;

public class Solver implements Runnable {

    private HashMap<Coordinate, Block> map;
    private MovingBlock shortestPathRock;
    private static List<Long> execTimes = Collections.synchronizedList(new ArrayList<Long>());
    private static boolean foundGoodMap = false;
    private static int maxX = 11;
    private static int maxY = 17;
    private static int minMoves = 7;
    private static int maxMoves = 30;
    private static int retryCount = 0;

    private static boolean superDebug = false;
    private static boolean showPath = true;

    private enum Direction {
        UP, DOWN, LEFT, RIGHT
    }

    public static void main(String[] args) {
//        try {
//            ExecutorService executor = Executors.newCachedThreadPool();
//            for (int i = 0; i < 50; i++) {
//                executor.submit(new Solver());
//            }
//            executor.shutdown();
//            executor.awaitTermination(1, TimeUnit.HOURS);
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
        Long totalTime = System.currentTimeMillis();
        Solver s = new Solver();
        s.run();
        long total = 0;
        for (Long t : execTimes) {
            total += t;
        }
        System.out.println("Total Time Spent Looking For Map = "+((System.currentTimeMillis()-totalTime)/1000.00)+" seconds");
        System.out.println("Average Time Per Map For Creation/Solve = " + (total / execTimes.size()) + "ms");
    }

    public Solver() {
    }

    public void run() {
        do {
            long start = System.currentTimeMillis();
            createAndSolve();
            long diff = System.currentTimeMillis() - start;
            execTimes.add(diff);
        } while (!foundGoodMap);

        MovingBlock block = new MovingBlock();
        Coordinate start = new Coordinate(0,0);
        block.setPosition(start);
        map.put(start, block);

        levelXML = new StringBuilder();
        System.out.println("Printing!!\n\n\n\n\n");
        System.out.println(build().toString());
    }

    public void createAndSolve() {
        if (foundGoodMap) {
            return;
        }
        shortestPathRock = null;
        retryCount++;
        map = new HashMap<>();
        Queue<MovingBlock> moveQueue = new LinkedList<>();
        int rockCount = 0;
        boolean solved = false;
        if (!superDebug && !foundGoodMap) {
            System.out.print("\rTotal Number Of Puzzles Tried: " + retryCount + "\t");
        }

        int randomNum = ThreadLocalRandom.current().nextInt(75, 95 + 1); // Try out different block densities between 25% & 90%
        for (int i = 0; i < maxY; i++) { // Generate 25x25 map
            for (int a = 0; a < maxX; a++) {
                Coordinate currentCoordinate = new Coordinate(a,i);
                EmptyBlock emptyBlock = new EmptyBlock(currentCoordinate);
                emptyBlock.setPosition(currentCoordinate);
                map.put(currentCoordinate, emptyBlock);
                if ((i != 0 && a != 0) && Math.random() * 100 > 60) {
                    RockBlock rockBlock = new RockBlock();
                    rockBlock.setPosition(currentCoordinate);
                    map.put(currentCoordinate, rockBlock);
                    rockCount++;
                }
            }
        }

        /*
        This is test map for bubbles
         */
//        for(int i=1;i<maxY;i++){
//            RockBlock rockBlock = new RockBlock();
//            rockBlock.setPosition(new Coordinate(1,i));
//            map.put(new Coordinate(1,i), rockBlock);
//        }
//
//        Coordinate finishCoordinate = new Coordinate(5, maxY-1);
//        FinishBlock finishBlock = new FinishBlock();
//        finishBlock.setPosition(finishCoordinate);
//        map.put(finishCoordinate, finishBlock);
//
//        RockBlock rockBlock = new RockBlock(); // Put a solid rock next to the win block always
//
//        Coordinate finishRockCoordinate = new Coordinate(4, maxY-1);
//        rockBlock.setPosition(finishRockCoordinate);
//        map.put(finishRockCoordinate, rockBlock);
//
//        BubbleBlock bubbleBlock = new BubbleBlock();
//        Coordinate bubbleCoordinate = new Coordinate(5,0);
//        bubbleBlock.setPosition(bubbleCoordinate);
//        map.put(bubbleCoordinate, bubbleBlock);
//
//        BubbleBlock bubbleBlock2 = new BubbleBlock();
//        Coordinate bubbleCoordinate2 = new Coordinate(maxX-1,4);
//        bubbleBlock2.setPosition(bubbleCoordinate2);
//        map.put(bubbleCoordinate2, bubbleBlock2);

        /*
        End test map
         */


        int finishXAxis = ThreadLocalRandom.current().nextInt(0, maxX);
        int finishXRockBlockPlusMinus;
        if (finishXAxis >= (maxX - 1)) {
            finishXRockBlockPlusMinus = -1;
        } else if (finishXAxis == 0) {
            finishXRockBlockPlusMinus = 1;
        } else {
            finishXRockBlockPlusMinus = (Math.random() * 100 > 50) ? -1 : 1;
        }

        int finishYAxis = ThreadLocalRandom.current().nextInt(0, maxY);
        int finishYRockBlockPlusMinus = 0;
        if (finishYAxis >= (maxY - 1)) {
            finishYRockBlockPlusMinus = -1;
        } else if (finishYAxis == 0) {
            finishYRockBlockPlusMinus = 1;
        } else {
            finishYRockBlockPlusMinus = (Math.random() * 100 > 50) ? -1 : 1;
        }

        boolean moveX = (Math.random() * 100 > 50);

        Coordinate finishCoordinate = new Coordinate(finishXAxis, finishYAxis);
        FinishBlock finishBlock = new FinishBlock();
        finishBlock.setPosition(finishCoordinate);
        map.put(finishCoordinate, finishBlock);

        RockBlock rockBlock = new RockBlock(); // Put a solid rock next to the win block always

        Coordinate finishRockCoordinate = new Coordinate(finishXAxis + (moveX ? finishXRockBlockPlusMinus : 0), finishYAxis + (moveX ? 0 : finishYRockBlockPlusMinus));
        rockBlock.setPosition(finishRockCoordinate);
        map.put(finishRockCoordinate, rockBlock);
        rockCount++;


        MovingBlock blockRight = new MovingBlock();
        blockRight.setPosition(new Coordinate(0, 0)); // Start at (0,0) and go right
        blockRight.setCurrentDirection(Direction.RIGHT);
        blockRight.savePreviousPosition();
        moveQueue.add(blockRight);
        MovingBlock blockDown = new MovingBlock();
        blockDown.setPosition(new Coordinate(0, 0)); // Start at (0,0) and go down
        blockDown.setCurrentDirection(Direction.DOWN);
        blockDown.savePreviousPosition();
        moveQueue.add(blockDown);

        do {
            MovingBlock currentBlock = moveQueue.remove();
            currentBlock.move();
            Block nextBlock = getNextBlock(currentBlock, currentBlock.currentDirection);
            if (nextBlock != null) {
                if (superDebug) {
                    System.out.println("Type of next block: " + nextBlock.getBlockType());
                }
                nextBlock.onTouch(currentBlock);
                if (map.get(currentBlock.getPosition()) instanceof FinishBlock) { // Currently sitting on the finish block
                    if (currentBlock.getPreviousPositions().size() < (minMoves + 1)) {
                        return; // We will try whole process again because it was too easy
                    }
                    if (shortestPathRock == null || currentBlock.getPreviousPositions().size() < shortestPathRock.getPreviousPositions().size()) {
                        solved = true;
                        if(shortestPathRock != null){
                            System.out.println("FOUND SHORTER PATH!");
                        }
                        shortestPathRock = currentBlock; // We found either the first answer or a shorter answer
                    }
                }
            }
            if (currentBlock.getPreviousPositions().size() <= maxMoves && (shortestPathRock == null || currentBlock.getPreviousPositions().size() < shortestPathRock.getPreviousPositions().size())) {
                for (Direction direction : Direction.values()) {
                    if (canTravelInDirection(currentBlock, direction)) {
                        MovingBlock copied = new MovingBlock();
                        currentBlock.copy(copied);
                        copied.setCurrentDirection(direction);
                        moveQueue.add(copied);
                    } else {
                        if (superDebug) {
                            System.out.println("Can't Move " + direction);
                        }
                    }
                }
            } else {
                if (superDebug) {
                    System.out.println("Made it to max moves!");
                }
            }
        } while (!moveQueue.isEmpty());


        if (solved) {
            foundGoodMap = true;
            System.out.println(); // Finish the counter off
            System.out.println("DONE IN " + (shortestPathRock.getPreviousPositions().size() - 1) + " Moves!"); // One less move than you really took!
            for (int i = 0; i < shortestPathRock.getPreviousPositions().size() - 1; i++) {
                System.out.println("Went To " + shortestPathRock.getPreviousPositions().get(i + 1));
            }
            System.out.println("Total # Of Rocks: " + rockCount);
            printMap();
        } else {
            if (superDebug) {
                System.out.println("Could Not Solve Puzzle, Trying Again!");
            }
        }
    }

    private void printMap() {
        HashMap<Coordinate, Direction> finalPathTaken = null;
        if(shortestPathRock != null) {
            finalPathTaken = shortestPathRock.getAllPreviousPositions();
        }
        for (int i = 0; i < maxY; i++) {
            for (int a = 0; a < maxX; a++) {
                Coordinate coordinate = new Coordinate(a, i);
                if (showPath && finalPathTaken != null && finalPathTaken.containsKey(coordinate) && !(map.get(coordinate) instanceof FinishBlock) && !(map.get(coordinate) instanceof RockBlock)) {
                    switch (finalPathTaken.get(coordinate)) {
                        case UP:
                            System.out.print("^");
                            break;
                        case DOWN:
                            System.out.print("v");
                            break;
                        case RIGHT:
                            System.out.print(">");
                            break;
                        case LEFT:
                            System.out.print("<");
                            break;
                    }
                    System.out.print(" ");
                } else {
                    System.out.print(map.get(coordinate).printMapObject() + " ");
                }
            }
            System.out.println("");
        }
    }


    private boolean canTravelInDirection(MovingBlock block, Direction direction) {
        if (block.getLastDirection() != null) {
            switch (block.getLastDirection()) { // Don't move back where we just came
                case UP:
                    if (direction == Direction.DOWN) {
                        return false;
                    }
                    break;
                case DOWN:
                    if (direction == Direction.UP) {
                        return false;
                    }
                    break;
                case RIGHT:
                    if (direction == Direction.LEFT) {
                        return false;
                    }
                    break;
                case LEFT:
                    if (direction == Direction.RIGHT) {
                        return false;
                    }
                    break;
            }
        }

        Block nextBlock = getNextBlock(block, direction);
        if (nextBlock instanceof FinishBlock) {
            Block secondNextBlock = getNextBlock(nextBlock, direction);
            if (secondNextBlock == null || !secondNextBlock.canTravel(direction)) {
                return true;
            }
        }
        if (nextBlock == null || !nextBlock.canTravel(direction)) {
            return false;
        }
        switch (direction) { // Handle Outside Walls
            case UP:
                if (block.getPosition().getY() <= 0) {
                    return false;
                }
                break;
            case DOWN:
                if (block.getPosition().getY() >= maxY) {
                    return false;
                }
                break;
            case RIGHT:
                if (block.getPosition().getX() >= maxX) {
                    return false;
                }
                break;
            case LEFT:
                if (block.getPosition().getX() <= 0) {
                    return false;
                }
                break;
        }
        return true;
    }

    private Block getNextBlock(Block block, Direction direction) {
        switch (direction) {
            case UP:
                return map.get(new Coordinate(block.getPosition().getX(), block.getPosition().getY() - 1));
            case DOWN:
                return map.get(new Coordinate(block.getPosition().getX(), block.getPosition().getY() + 1));
            case RIGHT:
                return map.get(new Coordinate(block.getPosition().getX() + 1, block.getPosition().getY()));
            case LEFT:
                return map.get(new Coordinate(block.getPosition().getX() - 1, block.getPosition().getY()));
            default:
                return null;
        }
    }

    abstract class Block {
        protected Coordinate position;
        protected ArrayList<Coordinate> previousPositions = new ArrayList<>();
        protected int id;
        protected boolean placed;

        protected void savePreviousPosition() {
            previousPositions.add(new Coordinate(position));
        }

        public <T extends Block> T copy(T copy) {
            copy.setPosition(new Coordinate(this.getPosition().getX(), this.getPosition().getY()));
            copy.setPreviousPositions((ArrayList<Coordinate>) this.getPreviousPositions().clone());
            if (copy instanceof MovingBlock && this instanceof MovingBlock) {
                ((MovingBlock) copy).setCurrentDirection(((MovingBlock) this).getCurrentDirection());
                HashMap<Coordinate, Direction> allPreviousDirections = new HashMap<>();
                allPreviousDirections.putAll(((MovingBlock) this).getAllPreviousPositions());
                ((MovingBlock) copy).setAllPreviousPositions(allPreviousDirections);
            }
            return copy;
        }

        public Coordinate getPosition() {
            return position;
        }

        public void setPosition(Coordinate position) {
            this.position = position;
        }

        public ArrayList<Coordinate> getPreviousPositions() {
            return previousPositions;
        }

        public void setPreviousPositions(ArrayList<Coordinate> previousPositions) {
            this.previousPositions = previousPositions;
        }

        public int getId() {
            return id;
        }

        public void setId(int id) {
            this.id = id;
        }

        public boolean isPlaced() {
            return placed;
        }

        public void setPlaced(boolean placed) {
            this.placed = placed;
        }

        public abstract void onTouch(MovingBlock block);

        public abstract boolean canTravel(Direction direction);

        public abstract String getBlockType();

        public abstract String printMapObject();
    }

    class MovingBlock extends Block {
        private Direction currentDirection;
        private Direction lastDirection;
        private HashMap<Coordinate, Direction> allPreviousPositions = new HashMap<>();


        public MovingBlock() {

        }

        @Override
        public String getBlockType() {
            return "Moving Block";
        }

        @Override
        public String printMapObject() {
            return "0";
        }

        protected void savePreviousPosition() {
            this.lastDirection = this.currentDirection;
            super.savePreviousPosition();
        }

        private void savePositionForPrinting() {
            allPreviousPositions.put(new Coordinate(getPosition()), getCurrentDirection());
        }

        public void onTouch(MovingBlock block) {
            // Don't do anything cuz there will only ever be 1 of these types!
        }

        public boolean canTravel(Direction direction) {
            return true;
        }

        public void move() {
            while (canTravelInDirection(this, currentDirection)) {
                switch (currentDirection) {
                    case UP:
                        moveUp();
                        break;
                    case DOWN:
                        moveDown();
                        break;
                    case RIGHT:
                        moveRight();
                        break;
                    case LEFT:
                        moveLeft();
                        break;
                }
                savePositionForPrinting();
            }
            if (superDebug) {
                System.out.println("Went " + currentDirection + " From " + previousPositions.get(previousPositions.size() - 1).toString() + " to " + position);
            }
            savePreviousPosition();
        }

        public void move(int x, int y) {
            this.position.increment(x, y);
        }

        public void moveRight() {
            move(1, 0);
        }

        public void moveLeft() {
            move(-1, 0);
        }

        public void moveUp() {
            move(0, -1);
        }

        public void moveDown() {
            move(0, 1);
        }

        public Direction getCurrentDirection() {
            return currentDirection;
        }

        public void setCurrentDirection(Direction currentDirection) {
            this.currentDirection = currentDirection;
        }

        public Direction getLastDirection() {
            return lastDirection;
        }

        public void setLastDirection(Direction lastDirection) {
            this.lastDirection = lastDirection;
        }

        public HashMap<Coordinate, Direction> getAllPreviousPositions() {
            return allPreviousPositions;
        }

        public void setAllPreviousPositions(HashMap<Coordinate, Direction> allPreviousPositions) {
            this.allPreviousPositions = allPreviousPositions;
        }
    }

    class EmptyBlock extends Block {

        public EmptyBlock(Coordinate coordinate){
            setPosition(coordinate);
        }

        @Override
        public String getBlockType() {
            return "Empty Block";
        }

        @Override
        public String printMapObject() {
            return "*";
        }

        public void onTouch(MovingBlock block) {
            block.move();
            block.savePositionForPrinting();
        }

        @Override
        public boolean canTravel(Direction direction) {
            return true;
        }
    }

    class FinishBlock extends Block {
        @Override
        public void onTouch(MovingBlock block) {
            // Do nothing
        }

        @Override
        public String printMapObject() {
            return "W";
        }

        @Override
        public boolean canTravel(Direction direction) {
            Block nextBlock = getNextBlock(this, direction);
            return !(nextBlock instanceof RockBlock);
        }

        @Override
        public String getBlockType() {
            return "Finish Block";
        }
    }

    class RockBlock extends Block {
        public void onTouch(MovingBlock block) {
            // lets split here

        }

        @Override
        public String getBlockType() {
            return "Rock Block";
        }

        @Override
        public boolean canTravel(Direction direction) {
            return false;
        }

        @Override
        public String printMapObject() {
            return "B";
        }
    }

    class Coordinate {
        private int x;
        private int y;

        public Coordinate(int x, int y) {
            this.x = x;
            this.y = y;
        }

        public Coordinate(Coordinate coordinate) {
            this.x = coordinate.getX();
            this.y = coordinate.getY();
        }

        public void increment(int x, int y) {
            this.x = this.x + x;
            this.y = this.y + y;
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

        public String toString() {
            return "(" + x + "," + y + ")";
        }

        public boolean equals(Object coordinate) {
            return coordinate instanceof Coordinate && ((Coordinate) coordinate).getX() == this.x && ((Coordinate) coordinate).getY() == this.y;
        }

        public int hashCode() {
            return toString().hashCode();
        }
    }






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


    private static StringBuilder levelXML;


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

    private StringBuilder build(){
        //constant xml file header
        levelXML.append(viewStart + "\n");
        levelXML.append(fullAssetDataName(assetData.ID) + constGridName + qm + "\n");
        levelXML.append(fullAssetDataName(assetData.PAR_WDTH) + qm + "\n");
        levelXML.append(fullAssetDataName(assetData.PAR_HGTH) + qm + "\n");
        levelXML.append(viewEnd + "\n");

        //store xml block for runner because it needs to be appended at the end, but
        String runnerXML = "";
        //Current block being written
        Block block = null;
        String view = "";
        HashMap<Coordinate, Block> avaliableBlocks;
        
        
        //before doing anything we know we can store the mover
        block = (MovingBlock) (map.get(new Coordinate(0, 0)));
        block.setPlaced(true);
        //write block heading
        view += (fullAssetDataName(assetData.ID) + (constRunner) + qm + "\n");//variable name
        view += (fullAssetDataName(assetData.WDTH) + qm + "\n");//const width
        view += (fullAssetDataName(assetData.HGTH) + qm + "\n");//const height
        view += (fullAssetDataName(assetData.BKRND) + fullAssetName((assets.P_DUD)) + qm + "\n");//const background with respect to the blocktype
        //for now we know he will always start in the top left
        view += (fullAbsoluteName(absoluteLayouts.A_PAR_TP) + tr + qm + "\n");
        view += (fullAbsoluteName(absoluteLayouts.A_PAR_LT) + tr + qm + "\n");
        view+=(viewEnd + "\n");
        levelXML.append(view);
        view = "";
        //obstacle ID so each view block has their own identifier
        int obId = 1;

        for(int i = 0; i<maxY; ++i){//increments rows
            for(int j = 0; j < maxX; ++j){//increments rocolumnsws
                //go through each block starting from the top left to the bottom right
                System.out.println("First block is: "+map.get(new Coordinate(j,i)).getClass().toString());
                
                if(map.get(new Coordinate(j,i)) instanceof Block && !(map.get(new Coordinate(j,i)) instanceof EmptyBlock) && map.get(new Coordinate(j,i)).isPlaced()){//only process the blocks already placed!
                    //block is already placed and thus has the necessary data to be used at a reference
                    Block ref = map.get(new Coordinate(j,i));
                    //holds all the avaliable blocks this block could possible use as a reference
                    avaliableBlocks = validRefs(block.getPosition());
                    System.out.println("Size: "+avaliableBlocks.size());
                    //should never be null but catch to be safe
                    try {
                        //for every non placed block in range of ref placed block place all those blocks
                        for (Map.Entry<Coordinate, Block> entry : avaliableBlocks.entrySet()) {
                            view+=(viewStart + "\n");
                            //just need to grab the first one
                            if (entry.getValue() instanceof Block && !(entry.getValue() instanceof EmptyBlock)) {
                                //found the first of avaliableBlocks.size()
                                block = entry.getValue();
                                block.setId(obId++);//used for variable naming
                                block.setPlaced(true);
                                //write block heading
                                view+=(fullAssetDataName(assetData.ID) + (ref instanceof FinishBlock ? (constFinish) : (constObstacle + block.getId())) + qm + "\n");//variable name
                                view+=(fullAssetDataName(assetData.WDTH) + qm + "\n");//const width
                                view+=(fullAssetDataName(assetData.HGTH) + qm + "\n");//const height
                                view+=(fullAssetDataName(assetData.BKRND) + (ref instanceof FinishBlock ? (fullAssetName((assets.P_FIN))) : (fullAssetName((assets.P_OBST)))) + qm + "\n");//const background with respect to the blocktype
                                //current block - reference < 0 its either left of the ref or above it
                                int yDist = block.getPosition().getY() - ref.getPosition().getY();
                                int xDist = block.getPosition().getX() - ref.getPosition().getX();

                                //if either of the distances are 0 this means they either share the same X or the same Y plane
                                if (xDist == 0 || yDist == 0) {
                                    if (yDist == 0) {
                                        if (xDist < 0) {//block is to the left of the reference
                                            //we know its left so put left but check if we need margin
                                            view += (fullRelativeName(relativeLayouts.TO_LT_OF) + (ref instanceof MovingBlock ? (constRunner) : (ref instanceof FinishBlock ? (constFinish) : (constObstacle + ref.getId()))) + qm + "\n");
                                            if (Math.abs(xDist) == 2) {
                                                view += (fullRelativeName(relativeLayouts.MGN_RT) + constDimen + qm + "\n");//margin right moves the block left
                                            }
                                        } else {//its to the right, it can never be equal
                                            //we know its right so put right but check if we need margin
                                            view += (fullRelativeName(relativeLayouts.TO_RT_OF) + (ref instanceof MovingBlock ? (constRunner) : (ref instanceof FinishBlock ? (constFinish) : (constObstacle + ref.getId()))) + qm + "\n");
                                            if (Math.abs(yDist) == 2) {
                                                view += (fullRelativeName(relativeLayouts.MGN_LT) + constDimen + qm + "\n");//margin left moves the block right
                                            }
                                        }
                                    } else {//if its not xDist we know its yDist==0 otherwise we'd never have gottten in here
                                        if (yDist < 0) {//block is above  reference
                                            view += (fullRelativeName(relativeLayouts.ABV) + (ref instanceof MovingBlock ? (constRunner) : (ref instanceof FinishBlock ? (constFinish) : (constObstacle + ref.getId()))) + qm + "\n");
                                            if (Math.abs(yDist) == 2) {
                                                view += (fullRelativeName(relativeLayouts.MGN_BTM) + constDimen + qm + "\n");//margin bottom moves the block up
                                            }
                                        } else {//its below, it can never be equal
                                            view += (fullRelativeName(relativeLayouts.BLW) + (ref instanceof MovingBlock ? (constRunner) : (ref instanceof FinishBlock ? (constFinish) : (constObstacle + ref.getId()))) + qm + "\n");
                                            if (Math.abs(yDist) == 2) {
                                                view += (fullRelativeName(relativeLayouts.MGN_TP) + constDimen + qm + "\n");//margin top moves the block down
                                            }
                                        }
                                    }
                                } else {//this means we're in some diagonal direction
                                    //xDist < 0 means current block is left of reference, likewise xDist > 0 means current block is right of reference
                                    //yDist < 0 means current block is above reference, likewise yDist > 0 means current block is below reference
                                    if (xDist < 0) {//block is to the left of the reference
                                        //we know its left so put left but check if we need margin
                                        view += (fullRelativeName(relativeLayouts.TO_LT_OF) + (ref instanceof MovingBlock ? (constRunner) : (ref instanceof FinishBlock ? (constFinish) : (constObstacle + ref.getId()))) + qm + "\n");
                                        if (Math.abs(xDist) == 2) {
                                            view += (fullRelativeName(relativeLayouts.MGN_RT) + constDimen + qm + "\n");//margin right moves the block left
                                        }
                                    } else {//its to the right, it can never be equal
                                        //we know its right so put right but check if we need margin
                                        view += (fullRelativeName(relativeLayouts.TO_RT_OF) + (ref instanceof MovingBlock ? (constRunner) : (ref instanceof FinishBlock ? (constFinish) : (constObstacle + ref.getId()))) + qm + "\n");
                                        if (Math.abs(xDist) == 2) {
                                            view += (fullRelativeName(relativeLayouts.MGN_LT) + constDimen + qm + "\n");//margin left moves the block right
                                        }
                                    }
                                    if (yDist < 0) {//block is above  reference
                                        view += (fullRelativeName(relativeLayouts.ABV) + (ref instanceof MovingBlock ? (constRunner) : (ref instanceof FinishBlock ? (constFinish) : (constObstacle + ref.getId()))) + qm + "\n");
                                        if (Math.abs(yDist) == 2) {
                                            view += (fullRelativeName(relativeLayouts.MGN_BTM) + constDimen + qm + "\n");//margin bottom moves the block up
                                        }
                                    } else {//its below, it can never be equal
                                        view += (fullRelativeName(relativeLayouts.BLW) + (ref instanceof MovingBlock ? (constRunner) : (ref instanceof FinishBlock ? (constFinish) : (constObstacle + ref.getId()))) + qm + "\n");
                                        if (Math.abs(yDist) == 2) {
                                            view += (fullRelativeName(relativeLayouts.MGN_TP) + constDimen + qm + "\n");//margin top moves the block down
                                        }
                                    }
                                }
                                view += (viewEnd + "\n");
                                levelXML.append(view);
                                view = "";
                            }
                        }
                    } catch (NullPointerException npe) {
                        System.out.println("XML so far!\n\n" + levelXML.toString());
                        System.out.println("Error: " + npe.getMessage());
                        npe.printStackTrace();
                        System.exit(1);
                    } catch (Exception e) {
                        System.out.println("XML so far!\n\n" + levelXML.toString());
                        System.out.println("Unknown Error: " + e.getMessage());
                        e.printStackTrace();
                        System.exit(1);
                    }


                }
            }
        }
        return levelXML;
    }

    //Generates a list of all blocks within range of a given coordinate 
    private HashMap<Coordinate, Block> validRefs(Object coordinate){
        HashMap<Coordinate, Block> validrefs = new HashMap<Coordinate, Block>();
        Coordinate coord = null;
        if(coordinate instanceof Coordinate){
            coord = (Coordinate)coordinate;
        }else{
            return null;//show not happen but just in case
        }
        int x = coord.getX();
        int y = coord.getY();
        System.out.println(coord);

        /*  I RECOGNIZE the code below is repetitive as hell. But I tried being clever..with the commented out code...and it failed missing some blocks, so in
            the interest of time I linearly wrote the blocks necessary to check. The code within this method works awesomely. Sure its ugly atm but I don't care.
            I will fix it later.
        */
        
        //check horizontal
        /*boolean hor = true;
        boolean useInc = false;
        int inc = 1;
        
        for (int i = 1; i < 3; ++i) {
            if (hor) {//horizontal - only check the index if its even a valid index
                System.out.println("checking: " + new Coordinate(x + (useInc ? inc : 0), y + i).toString());
                if (isValidCoord(x + (useInc ? inc : 0), y + i)) {
                    System.out.println("valid!");
                    if(map.get(new Coordinate(x + (useInc ? inc : 0), y + i)) instanceof Block){
                        System.out.println("instance of block!");
                        if(!(map.get(new Coordinate(x + (useInc ? inc : 0), y + i)) instanceof EmptyBlock) ){
                            System.out.println("and NOT instance of EmptyBllock!");
                            if(map.get(new Coordinate(x + (useInc ? inc : 0), y + i)).isPlaced()){
                                System.out.println("isPlaced");
                                   validrefs.put(new Coordinate(x + (useInc ? inc : 0), y + i), map.get(new Coordinate(x + (useInc ? inc : 0), y + i)));
                            }
                        }
                    }
                }// b(x, y+1) b(x, y+2), b(x+1, y+1) b(x+1, y+2)
                System.out.println("checking: " + new Coordinate(x + (useInc ? inc : 0), y - i).toString());
                if (isValidCoord(x + (useInc ? inc : 0), y - i) ) {
                    System.out.println("valid!");
                    if(map.get(new Coordinate(x + (useInc ? inc : 0), y - i)) instanceof Block){
                        System.out.println("instance of block!");
                        if(!(map.get(new Coordinate(x + (useInc ? inc : 0), y - i)) instanceof EmptyBlock)){
                            System.out.println("and NOT EmptyBlock");
                            if(map.get(new Coordinate(x + (useInc ? inc : 0), y - i)).isPlaced()){
                                System.out.println("isPlaced");
                                validrefs.put(new Coordinate(x + (useInc ? inc : 0), y - i), map.get(new Coordinate(x + (useInc ? inc : 0), y - i)));
                            }
                        }
                    }
                }// b(x, y-1) b(x, y-2), b(x+1, y-1) b(x+1, y-2)
                if (i == 2) {
                    hor = false;
                    i = 1;
                }
            } else {//vertical - only check the index if its even a valid index
                System.out.println("checking: " + new Coordinate(x + i, y + (useInc ? inc : 0)).toString());
                if (isValidCoord(x + i, y + (useInc ? inc : 0))) {
                    System.out.println("is valid!");
                    if(map.get(new Coordinate(x + i, y + (useInc ? inc : 0))) instanceof Block){
                        System.out.println("is a block!");
                        if(!(map.get(new Coordinate(x + i, y + (useInc ? inc : 0))) instanceof EmptyBlock) ){
                            System.out.println("not empty!");
                            if(map.get(new Coordinate(x + i, y + (useInc ? inc : 0))).isPlaced()){
                                System.out.println("isPlaced!");
                                validrefs.put(new Coordinate(x + i, y + (useInc ? inc : 0)),  map.get(new Coordinate(x + i, y + (useInc ? inc : 0))));
                            }
                        }
                    }
                }//b(x+1, y) b(x+2, y) b-repeated(x+1, y+1) b(x+2, y+1)
                System.out.println("checking: " + new Coordinate(x - i, y + (useInc ? inc : 0)).toString());
                if (isValidCoord(x - i, y + (useInc ? inc : 0))) {
                    System.out.println("is valid!");
                    if(map.get(new Coordinate(x - i, y + (useInc ? inc : 0))) instanceof Block){
                        System.out.println("is a block!");
                        if(!(map.get(new Coordinate(x - i, y + (useInc ? inc : 0))) instanceof EmptyBlock) ){
                            System.out.println("not empty!");
                            if(map.get(new Coordinate(x - i, y + (useInc ? inc : 0))).isPlaced()){
                                System.out.println("is placed!");
                                validrefs.put(new Coordinate(x - i, y + (useInc ? inc : 0)), map.get(new Coordinate(x - i, y + (useInc ? inc : 0))));
                            }
                        }
                    }
                }// b(x-1, y) b(x-2, y) b(x-1, y+1) b(x-2, y+1)
                if (i == 2) {
                    if (!useInc) {
                        hor = true;
                        i = 1;
                        useInc = true;
                    } else {
                        break;
                    }
                }
            }
        }*/
        //^^^^tried getting all fancy. Just ended up screwing up the math
        
        //check the randoms that didn't get checked - and only if its  valid
                /*
                b[x+2][y-1],b[x-1][y-2],b[x-1][y+2],b[x-2][y-1],b[x-1][y-1]
                */
        Coordinate co = new Coordinate(x - 1, y);
        System.out.println("checking : " + co.toString());
        if (isValidCoord(co)) {
            System.out.println("valid!");
            if (map.get(co) instanceof Block) {
                System.out.println("im a block!");
                if (!(map.get(co) instanceof EmptyBlock)) {
                    System.out.println("not empty!");
                    if (!map.get(co).isPlaced()) {
                        System.out.println("placing it!");
                        validrefs.put(co, map.get(co));
                    }
                }
            }
        }
        co = new Coordinate(x + 1, y);
        System.out.println("checking : " + co.toString());
        if (isValidCoord(co)) {
            System.out.println("valid!");
            if (map.get(co) instanceof Block) {
                System.out.println("im a block!");
                if (!(map.get(co) instanceof EmptyBlock)) {
                    System.out.println("not empty!");
                    if (!map.get(co).isPlaced()) {
                        System.out.println("placing it!");
                        validrefs.put(co, map.get(co));
                    }
                }
            }
        }
        co = new Coordinate(x, y + 1);
        System.out.println("checking : " + co.toString());
        if (isValidCoord(co)) {
            System.out.println("valid!");
            if (map.get(co) instanceof Block) {
                System.out.println("im a block!");
                if (!(map.get(co) instanceof EmptyBlock)) {
                    System.out.println("not empty!");
                    if (!map.get(co).isPlaced()) {
                        System.out.println("placing it!");
                        validrefs.put(co, map.get(co));
                    }
                }
            }
        }
        co = new Coordinate(x, y - 1);
        System.out.println("checking : " + co.toString());
        if (isValidCoord(co)) {
            System.out.println("valid!");
            if (map.get(co) instanceof Block) {
                System.out.println("im a block!");
                if (!(map.get(co) instanceof EmptyBlock)) {
                    System.out.println("not empty!");
                    if (!map.get(co).isPlaced()) {
                        System.out.println("placing it!");
                        validrefs.put(co, map.get(co));
                    }
                }
            }
        }
        co = new Coordinate(x + 1, y + 1);
        System.out.println("checking : " + co.toString());
        if (isValidCoord(co)) {
            System.out.println("valid!");
            if (map.get(co) instanceof Block) {
                System.out.println("im a block!");
                if (!(map.get(co) instanceof EmptyBlock)) {
                    System.out.println("not empty!");
                    if (!map.get(co).isPlaced()) {
                        System.out.println("placing it!");
                        validrefs.put(co, map.get(co));
                    }
                }
            }
        }
        co = new Coordinate(x - 1, y + 1);
        System.out.println("checking : " + co.toString());
        if (isValidCoord(co)) {
            System.out.println("valid!");
            if (map.get(co) instanceof Block) {
                System.out.println("im a block!");
                if (!(map.get(co) instanceof EmptyBlock)) {
                    System.out.println("not empty!");
                    if (!map.get(co).isPlaced()) {
                        System.out.println("placing it!");
                        validrefs.put(co, map.get(co));
                    }
                }
            }
        }
        co = new Coordinate(x + 1, y - 1);
        System.out.println("checking : " + co.toString());
        if (isValidCoord(co)) {
            System.out.println("valid!");
            if (map.get(co) instanceof Block) {
                System.out.println("im a block!");
                if (!(map.get(co) instanceof EmptyBlock)) {
                    System.out.println("not empty!");
                    if (!map.get(co).isPlaced()) {
                        System.out.println("placing it!");
                        validrefs.put(co, map.get(co));
                    }
                }
            }
        }
        co = new Coordinate(x - 1, y - 1);
        System.out.println("checking : " + co.toString());
        if (isValidCoord(co)) {
            System.out.println("valid!");
            if (map.get(co) instanceof Block) {
                System.out.println("im a block!");
                if (!(map.get(co) instanceof EmptyBlock)) {
                    System.out.println("not empty!");
                    if (!map.get(co).isPlaced()) {
                        System.out.println("placing it!");
                        validrefs.put(co, map.get(co));
                    }
                }
            }
        }
        co = new Coordinate(x - 2, y);
        System.out.println("checking : " + co.toString());
        if (isValidCoord(co)) {
            System.out.println("valid!");
            if (map.get(co) instanceof Block) {
                System.out.println("im a block!");
                if (!(map.get(co) instanceof EmptyBlock)) {
                    System.out.println("not empty!");
                    if (!map.get(co).isPlaced()) {
                        System.out.println("placing it!");
                        validrefs.put(co, map.get(co));
                    }
                }
            }
        }
        co = new Coordinate(x - 2, y + 1);
        System.out.println("checking : " + co.toString());
        if (isValidCoord(co)) {
            System.out.println("valid!");
            if (map.get(co) instanceof Block) {
                System.out.println("im a block!");
                if (!(map.get(co) instanceof EmptyBlock)) {
                    System.out.println("not empty!");
                    if (!map.get(co).isPlaced()) {
                        System.out.println("placing it!");
                        validrefs.put(co, map.get(co));
                    }
                }
            }
        }
        co = new Coordinate(x - 2, y - 1);
        System.out.println("checking : " + co.toString());
        if (isValidCoord(co)) {
            System.out.println("valid!");
            if (map.get(co) instanceof Block) {
                System.out.println("im a block!");
                if (!(map.get(co) instanceof EmptyBlock)) {
                    System.out.println("not empty!");
                    if (!map.get(co).isPlaced()) {
                        System.out.println("placing it!");
                        validrefs.put(co, map.get(co));
                    }
                }
            }
        }
        co = new Coordinate(x, y + 2);
        System.out.println("checking : " + co.toString());
        if (isValidCoord(co)) {
            System.out.println("valid!");
            if (map.get(co) instanceof Block) {
                System.out.println("im a block!");
                if (!(map.get(co) instanceof EmptyBlock)) {
                    System.out.println("not empty!");
                    if (!map.get(co).isPlaced()) {
                        System.out.println("placing it!");
                        validrefs.put(co, map.get(co));
                    }
                }
            }
        }
        co = new Coordinate(x - 1, y + 2);
        System.out.println("checking : " + co.toString());
        if (isValidCoord(co)) {
            System.out.println("valid!");
            if (map.get(co) instanceof Block) {
                System.out.println("im a block!");
                if (!(map.get(co) instanceof EmptyBlock)) {
                    System.out.println("not empty!");
                    if (!map.get(co).isPlaced()) {
                        System.out.println("placing it!");
                        validrefs.put(co, map.get(co));
                    }
                }
            }
        }
        co = new Coordinate(x + 1, y + 2);
        System.out.println("checking : " + co.toString());
        if (isValidCoord(co)) {
            System.out.println("valid!");
            if (map.get(co) instanceof Block) {
                System.out.println("im a block!");
                if (!(map.get(co) instanceof EmptyBlock)) {
                    System.out.println("not empty!");
                    if (!map.get(co).isPlaced()) {
                        System.out.println("placing it!");
                        validrefs.put(co, map.get(co));
                    }
                }
            }
        }
        co = new Coordinate(x, y - 2);
        System.out.println("checking : " + co.toString());
        if (isValidCoord(co)) {
            System.out.println("valid!");
            if (map.get(co) instanceof Block) {
                System.out.println("im a block!");
                if (!(map.get(co) instanceof EmptyBlock)) {
                    System.out.println("not empty!");
                    if (!map.get(co).isPlaced()) {
                        System.out.println("placing it!");
                        validrefs.put(co, map.get(co));
                    }
                }
            }
        }
        co = new Coordinate(x - 1, y - 2);
        System.out.println("checking : " + co.toString());
        if (isValidCoord(co)) {
            System.out.println("valid!");
            if (map.get(co) instanceof Block) {
                System.out.println("im a block!");
                if (!(map.get(co) instanceof EmptyBlock)) {
                    System.out.println("not empty!");
                    if (!map.get(co).isPlaced()) {
                        System.out.println("placing it!");
                        validrefs.put(co, map.get(co));
                    }
                }
            }
        }
        co = new Coordinate(x + 1, y - 2);
        System.out.println("checking : " + co.toString());
        if (isValidCoord(co)) {
            System.out.println("valid!");
            if (map.get(co) instanceof Block) {
                System.out.println("im a block!");
                if (!(map.get(co) instanceof EmptyBlock)) {
                    System.out.println("not empty!");
                    if (!map.get(co).isPlaced()) {
                        System.out.println("placing it!");
                        validrefs.put(co, map.get(co));
                    }
                }
            }
        }
        co = new Coordinate(x + 2, y);
        System.out.println("checking : " + co.toString());
        if (isValidCoord(co)) {
            System.out.println("valid!");
            if (map.get(co) instanceof Block) {
                System.out.println("im a block!");
                if (!(map.get(co) instanceof EmptyBlock)) {
                    System.out.println("not empty!");
                    if (!map.get(co).isPlaced()) {
                        System.out.println("placing it!");
                        validrefs.put(co, map.get(co));
                    }
                }
            }
        }
        co = new Coordinate(x + 2, y + 1);
        System.out.println("checking : " + co.toString());
        if (isValidCoord(co)) {
            System.out.println("valid!");
            if (map.get(co) instanceof Block) {
                System.out.println("im a block!");
                if (!(map.get(co) instanceof EmptyBlock)) {
                    System.out.println("not empty!");
                    if (!map.get(co).isPlaced()) {
                        System.out.println("placing it!");
                        validrefs.put(co, map.get(co));
                    }
                }
            }
        }
        co = new Coordinate(x + 2, y - 1);
        System.out.println("checking : " + co.toString());
        if (isValidCoord(co)) {
            System.out.println("valid!");
            if (map.get(co) instanceof Block) {
                System.out.println("im a block!");
                if (!(map.get(co) instanceof EmptyBlock)) {
                    System.out.println("not empty!");
                    if (!map.get(co).isPlaced()) {
                        System.out.println("placing it!");
                        validrefs.put(co, map.get(co));
                    }
                }
            }
        }
        return validrefs;
    }

    private boolean isValidCoord(Coordinate c){
        System.out.println("returning " + (c.getX()<maxX && c.getX()>=0 && c.getY()<maxY && c.getY()>=0) + "for valid coord");
        return (c.getX()<maxX && c.getX()>=0 && c.getY()<maxY && c.getY()>=0);
    }
}
