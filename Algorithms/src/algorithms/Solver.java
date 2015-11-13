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
    private static int maxY = 15;
    private static int minMoves = 7;
    private static int maxMoves = 30;
    private static int retryCount = 0;
    private int numRocks;
    
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
                if ((i != 0 && a != 0) && Math.random() * 100 > 85) {
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
            numRocks = rockCount;
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
    
    class MoltenBlock extends Block {

        public MoltenBlock(Coordinate coordinate){
            setPosition(coordinate);
        }

        @Override
        public String getBlockType() {
            return "Molten Block";
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
    private static final String tr="=true";
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
    
    private Stack<Coordinate> calculateRangeInner(Coordinate c){
        int x = c.getX();
        int y = c.getY();
        Stack<Coordinate> range = new Stack<Coordinate>();
        range.push(new Coordinate(x - 1, y));
        range.push(new Coordinate(x + 1, y));
        range.push(new Coordinate(x, y + 1));
        range.push(new Coordinate(x, y - 1));
        range.push(new Coordinate(x + 1, y + 1));
        range.push(new Coordinate(x - 1, y + 1));
        range.push(new Coordinate(x + 1, y - 1));
        range.push(new Coordinate(x - 1, y - 1));
        range.push(new Coordinate(x - 2, y));
        range.push(new Coordinate(x - 2, y + 1));
        range.push(new Coordinate(x - 2, y - 1));
        range.push(new Coordinate(x, y + 2));
        range.push(new Coordinate(x - 1, y + 2));
        range.push(new Coordinate(x + 1, y + 2));
        range.push(new Coordinate(x, y - 2));
        range.push(new Coordinate(x - 1, y - 2));
        range.push(new Coordinate(x + 1, y - 2));
        range.push(new Coordinate(x + 2, y));
        range.push(new Coordinate(x + 2, y + 1));
        range.push(new Coordinate(x + 2, y - 1));
        return range;
    }
    
    private Stack<Coordinate> calculateRange(Coordinate c){
        int x = c.getX();
        int y = c.getY();
        Stack<Coordinate> range = new Stack<Coordinate>();
        range.push(new Coordinate(x + 1, y));
        range.push(new Coordinate(x - 1, y));
        range.push(new Coordinate(x + 2, y));
        range.push(new Coordinate(x - 2, y));
        range.push(new Coordinate(x, y + 1));
        range.push(new Coordinate(x, y - 1));
        range.push(new Coordinate(x, y + 2));
        range.push(new Coordinate(x, y - 2));
        return range;
    }

    private StringBuilder build() {
        //constant xml file header
        levelXML.append(viewStart + "\n");
        levelXML.append(fullAssetDataName(assetData.ID) + constGridName + qm + "\n");
        levelXML.append(fullAssetDataName(assetData.PAR_WDTH) + qm + "\n");
        levelXML.append(fullAssetDataName(assetData.PAR_HGTH) + qm + "\n");
        levelXML.append(viewEnd + "\n");

        //store xml block for runner because it needs to be appended at the end, but
        String runnerView = "";
        //Current block being written
        Block block = null;
        String view = "";
        HashMap<Coordinate, Block> avaliableBlocks;
        int writtenBlocks = 0;

        //before doing anything we know we can store the mover
        block = (MovingBlock) (map.get(new Coordinate(0, 0)));
        block.setPlaced(true);
        //write block heading
        runnerView += viewStart + "\n";
        runnerView += (fullAssetDataName(assetData.ID) + (constRunner) + qm + "\n");//variable name
        runnerView += (fullAssetDataName(assetData.WDTH) + qm + "\n");//const width
        runnerView += (fullAssetDataName(assetData.HGTH) + qm + "\n");//const height
        runnerView += (fullAssetDataName(assetData.BKRND) + fullAssetName((assets.P_DUD)) + qm + "\n");//const background with respect to the blocktype
        //for now we know he will always start in the top left
        runnerView += (fullAbsoluteName(absoluteLayouts.A_PAR_TP) + tr + qm + "\n");
        runnerView += (fullAbsoluteName(absoluteLayouts.A_PAR_LT) + tr + qm + "\n");
        runnerView += (viewEnd + "\n");
        ++writtenBlocks;
        view = "";
        //obstacle ID so each view block has their own identifier
        int obId = 1;
        int placeAllPossibleBlocks = 0;
        
        //place possible known blocks
        /*
        place middle left, top, right, down if present
        place actual middle if present.
        */
        //all four corners if present
        
        //also place some other blocks for easier building
        if(map.get(new Coordinate(maxX-1, maxY-1)) instanceof EmptyBlock){
            map.put(new Coordinate(maxX-1, maxY-1), new RockBlock());
            map.get(new Coordinate(maxX-1, maxY-1)).setPosition(new Coordinate(maxX-1, maxY-1));
            block = map.get(new Coordinate(maxX-1, maxY-1));
            block.setPlaced(true);
            block.setId(obId);
            ++obId;
            //write block heading
            view += viewStart + "\n";
            view += (fullAssetDataName(assetData.ID) + (constObstacle + block.getId()) + qm + "\n");//variable name
            view += (fullAssetDataName(assetData.WDTH) + qm + "\n");//const width
            view += (fullAssetDataName(assetData.HGTH) + qm + "\n");//const height
            view += (fullAssetDataName(assetData.BKRND) + (fullAssetName((assets.P_OBST))) + qm + "\n");//const background with respect to the blocktype
            //for now we know he will always start in the top left
            view += (fullAbsoluteName(absoluteLayouts.A_PAR_BT) + tr + qm + "\n");
            view += (fullAbsoluteName(absoluteLayouts.A_PAR_RT) + tr + qm + "\n");
            view += (viewEnd + "\n");
        }
        
/******************CONSTANT LOCATIONS******************/
        block = map.get(new Coordinate(0, 0));
        if (block instanceof Block && !(block instanceof EmptyBlock)) {//top left
            System.out.println("Processing block: " + block.getPosition());
            view += (viewStart + "\n");
            block.setId(obId);//used for variable naming
            obId++;
            block.setPlaced(true);
            System.out.println("Placing block on map: " + block.getPosition());
            //write block heading
            view += (fullAssetDataName(assetData.ID) + (block instanceof MovingBlock ? (constRunner) : (block instanceof FinishBlock ? (constFinish) : (constObstacle + block.getId()))) + qm + "\n");//variable name
            view += (fullAssetDataName(assetData.WDTH) + qm + "\n");//const width
            view += (fullAssetDataName(assetData.HGTH) + qm + "\n");//const height
            view += (fullAssetDataName(assetData.BKRND) + (block instanceof MovingBlock ? (constRunner) : (block instanceof FinishBlock ? (fullAssetName((assets.P_FIN))) : (fullAssetName((assets.P_OBST))))) + qm + "\n");//const background with respect to the blocktype
            view += (fullAbsoluteName(absoluteLayouts.A_PAR_TP) + tr + qm + "\n");
            view += (fullAbsoluteName(absoluteLayouts.A_PAR_LT) + tr + qm + "\n");
            view += (viewEnd + "\n");
            levelXML.append(view);
            view = "";
            ++writtenBlocks;
        }
        block = map.get(new Coordinate(maxX-1, maxY-1));
        if (block instanceof Block && !(block instanceof EmptyBlock)) {//bottom right
            System.out.println("Processing block: " + block.getPosition());
            view += (viewStart + "\n");
            block.setId(obId);//used for variable naming
            obId++;
            block.setPlaced(true);
            System.out.println("Placing block on map: " + block.getPosition());
            //write block heading
            view += (fullAssetDataName(assetData.ID) + (block instanceof MovingBlock ? (constRunner) : (block instanceof FinishBlock ? (constFinish) : (constObstacle + block.getId()))) + qm + "\n");//variable name
            view += (fullAssetDataName(assetData.WDTH) + qm + "\n");//const width
            view += (fullAssetDataName(assetData.HGTH) + qm + "\n");//const height
            view += (fullAssetDataName(assetData.BKRND) + (block instanceof MovingBlock ? (constRunner) : (block instanceof FinishBlock ? (fullAssetName((assets.P_FIN))) : (fullAssetName((assets.P_OBST))))) + qm + "\n");//const background with respect to the blocktype
            view += (fullAbsoluteName(absoluteLayouts.A_PAR_BT) + tr + qm + "\n");
            view += (fullAbsoluteName(absoluteLayouts.A_PAR_RT) + tr + qm + "\n");
            view += (viewEnd + "\n");
            levelXML.append(view);
            view = "";
            ++writtenBlocks;
        }
        block = map.get(new Coordinate(0, maxY-1));
        if (block instanceof Block && !(block instanceof EmptyBlock)) {//top right
            System.out.println("Processing block: " + block.getPosition());
            view += (viewStart + "\n");
            block.setId(obId);//used for variable naming
            obId++;
            block.setPlaced(true);
            System.out.println("Placing block on map: " + block.getPosition());
            //write block heading
            view += (fullAssetDataName(assetData.ID) + (block instanceof MovingBlock ? (constRunner) : (block instanceof FinishBlock ? (constFinish) : (constObstacle + block.getId()))) + qm + "\n");//variable name
            view += (fullAssetDataName(assetData.WDTH) + qm + "\n");//const width
            view += (fullAssetDataName(assetData.HGTH) + qm + "\n");//const height
            view += (fullAssetDataName(assetData.BKRND) + (block instanceof MovingBlock ? (constRunner) : (block instanceof FinishBlock ? (fullAssetName((assets.P_FIN))) : (fullAssetName((assets.P_OBST))))) + qm + "\n");//const background with respect to the blocktype
            view += (fullAbsoluteName(absoluteLayouts.A_PAR_TP) + tr + qm + "\n");
            view += (fullAbsoluteName(absoluteLayouts.A_PAR_RT) + tr + qm + "\n");
            view += (viewEnd + "\n");
            levelXML.append(view);
            view = "";
            ++writtenBlocks;
        }
        block = map.get(new Coordinate(maxX-1, 0));
        if (block instanceof Block && !(block instanceof EmptyBlock)) {//bottom left
            System.out.println("Processing block: " + block.getPosition());
            view += (viewStart + "\n");
            block.setId(obId);//used for variable naming
            obId++;
            block.setPlaced(true);
            System.out.println("Placing block on map: " + block.getPosition());
            //write block heading
            view += (fullAssetDataName(assetData.ID) + (block instanceof MovingBlock ? (constRunner) : (block instanceof FinishBlock ? (constFinish) : (constObstacle + block.getId()))) + qm + "\n");//variable name
            view += (fullAssetDataName(assetData.WDTH) + qm + "\n");//const width
            view += (fullAssetDataName(assetData.HGTH) + qm + "\n");//const height
            view += (fullAssetDataName(assetData.BKRND) + (block instanceof MovingBlock ? (constRunner) : (block instanceof FinishBlock ? (fullAssetName((assets.P_FIN))) : (fullAssetName((assets.P_OBST))))) + qm + "\n");//const background with respect to the blocktype
            view += (fullAbsoluteName(absoluteLayouts.A_PAR_BT) + tr + qm + "\n");
            view += (fullAbsoluteName(absoluteLayouts.A_PAR_LT) + tr + qm + "\n");
            view += (viewEnd + "\n");
            levelXML.append(view);
            view = "";
            ++writtenBlocks;
        }
        //check centers left, right, up, down, and exact middle
        int midX = maxX/2;
        int midY = maxY/2;
        block = map.get(new Coordinate(0, midY));
        if (block instanceof Block && !(block instanceof EmptyBlock)) {//top middle
            System.out.println("Processing block: " + block.getPosition());
            view += (viewStart + "\n");
            block.setId(obId);//used for variable naming
            block.setPlaced(true);
            obId++;
            System.out.println("Placing block on map: " + block.getPosition());
            //write block heading
            view += (fullAssetDataName(assetData.ID) + (block instanceof MovingBlock ? (constRunner) : (block instanceof FinishBlock ? (constFinish) : (constObstacle + block.getId()))) + qm + "\n");//variable name
            view += (fullAssetDataName(assetData.WDTH) + qm + "\n");//const width
            view += (fullAssetDataName(assetData.HGTH) + qm + "\n");//const height
            view += (fullAssetDataName(assetData.BKRND) + (block instanceof MovingBlock ? (constRunner) : (block instanceof FinishBlock ? (fullAssetName((assets.P_FIN))) : (fullAssetName((assets.P_OBST))))) + qm + "\n");//const background with respect to the blocktype
            view += (fullAbsoluteName(absoluteLayouts.A_PAR_TP) + tr + qm + "\n");
            view += (fullAbsoluteName(absoluteLayouts.CENT_HOR) + tr + qm + "\n");
            view += (viewEnd + "\n");
            levelXML.append(view);
            view = "";
            ++writtenBlocks;
        }
        block = map.get(new Coordinate(midX, 0));
        if (block instanceof Block && !(block instanceof EmptyBlock)) {//bottom middle
            System.out.println("Processing block: " + block.getPosition());
            view += (viewStart + "\n");
            block.setId(obId);//used for variable naming
            obId++;
            block.setPlaced(true);
            System.out.println("Placing block on map: " + block.getPosition());
            //write block heading
            view += (fullAssetDataName(assetData.ID) + (block instanceof MovingBlock ? (constRunner) : (block instanceof FinishBlock ? (constFinish) : (constObstacle + block.getId()))) + qm + "\n");//variable name
            view += (fullAssetDataName(assetData.WDTH) + qm + "\n");//const width
            view += (fullAssetDataName(assetData.HGTH) + qm + "\n");//const height
            view += (fullAssetDataName(assetData.BKRND) + (block instanceof MovingBlock ? (constRunner) : (block instanceof FinishBlock ? (fullAssetName((assets.P_FIN))) : (fullAssetName((assets.P_OBST))))) + qm + "\n");//const background with respect to the blocktype
            view += (fullAbsoluteName(absoluteLayouts.A_PAR_BT) + tr + qm + "\n");
            view += (fullAbsoluteName(absoluteLayouts.CENT_HOR) + tr + qm + "\n");
            view += (viewEnd + "\n");
            levelXML.append(view);
            view = "";
            ++writtenBlocks;
        }
        block = map.get(new Coordinate(maxX-1, midY));
        if (block instanceof Block && !(block instanceof EmptyBlock)) {//middle right
            System.out.println("Processing block: " + block.getPosition());
            view += (viewStart + "\n");
            block.setId(obId);//used for variable naming
            obId++;
            block.setPlaced(true);
            System.out.println("Placing block on map: " + block.getPosition());
            //write block heading
            view += (fullAssetDataName(assetData.ID) + (block instanceof MovingBlock ? (constRunner) : (block instanceof FinishBlock ? (constFinish) : (constObstacle + block.getId()))) + qm + "\n");//variable name
            view += (fullAssetDataName(assetData.WDTH) + qm + "\n");//const width
            view += (fullAssetDataName(assetData.HGTH) + qm + "\n");//const height
            view += (fullAssetDataName(assetData.BKRND) + (block instanceof MovingBlock ? (constRunner) : (block instanceof FinishBlock ? (fullAssetName((assets.P_FIN))) : (fullAssetName((assets.P_OBST))))) + qm + "\n");//const background with respect to the blocktype
            view += (fullAbsoluteName(absoluteLayouts.A_PAR_RT) + tr + qm + "\n");
            view += (fullAbsoluteName(absoluteLayouts.CENT_VERT) + tr + qm + "\n");
            view += (viewEnd + "\n");
            levelXML.append(view);
            view = "";
            ++writtenBlocks;
        }
        block = map.get(new Coordinate(0, midY));
        if (block instanceof Block && !(block instanceof EmptyBlock)) {//middle left
            System.out.println("Processing block: " + block.getPosition());
            view += (viewStart + "\n");
            block.setId(obId);//used for variable naming
            obId++;
            block.setPlaced(true);
            System.out.println("Placing block on map: " + block.getPosition());
            //write block heading
            view += (fullAssetDataName(assetData.ID) + (block instanceof MovingBlock ? (constRunner) : (block instanceof FinishBlock ? (constFinish) : (constObstacle + block.getId()))) + qm + "\n");//variable name
            view += (fullAssetDataName(assetData.WDTH) + qm + "\n");//const width
            view += (fullAssetDataName(assetData.HGTH) + qm + "\n");//const height
            view += (fullAssetDataName(assetData.BKRND) + (block instanceof MovingBlock ? (constRunner) : (block instanceof FinishBlock ? (fullAssetName((assets.P_FIN))) : (fullAssetName((assets.P_OBST))))) + qm + "\n");//const background with respect to the blocktype
            view += (fullAbsoluteName(absoluteLayouts.A_PAR_LT) + tr + qm + "\n");
            view += (fullAbsoluteName(absoluteLayouts.CENT_VERT) + tr + qm + "\n");
            view += (viewEnd + "\n");
            levelXML.append(view);
            view = "";
            ++writtenBlocks;
        }
        block = map.get(new Coordinate(midX, midY));
        if (block instanceof Block && !(block instanceof EmptyBlock)) {//middle EXACTLY!!!!
            System.out.println("Processing block: " + block.getPosition());
            view += (viewStart + "\n");
            block.setId(obId);//used for variable naming
            obId++;
            block.setPlaced(true);
            System.out.println("Placing block on map: " + block.getPosition());
            //write block heading
            view += (fullAssetDataName(assetData.ID) + (block instanceof MovingBlock ? (constRunner) : (block instanceof FinishBlock ? (constFinish) : (constObstacle + block.getId()))) + qm + "\n");//variable name
            view += (fullAssetDataName(assetData.WDTH) + qm + "\n");//const width
            view += (fullAssetDataName(assetData.HGTH) + qm + "\n");//const height
            view += (fullAssetDataName(assetData.BKRND) + (block instanceof MovingBlock ? (constRunner) : (block instanceof FinishBlock ? (fullAssetName((assets.P_FIN))) : (fullAssetName((assets.P_OBST))))) + qm + "\n");//const background with respect to the blocktype
            view += (fullAbsoluteName(absoluteLayouts.CENT_VERT) + tr + qm + "\n");
            view += (fullAbsoluteName(absoluteLayouts.CENT_HOR) + tr + qm + "\n");
            view += (viewEnd + "\n");
            levelXML.append(view);
            view = "";
            ++writtenBlocks;
        }
/******************CONSTANT LOCATIONS******************/
        
        /*
            Alright. Now we need to first do the same checks as before in case there are any blocks within the "margin avaliable" range
        */
        
        //allows for retracing to see is we missed any
        while (writtenBlocks < numRocks) {
            for (int i = 0; i < maxY; ++i) {//increments rows
                System.out.println("i = " + i);
                for (int j = 0; j < maxX; ++j) {//increments rocolumnsws
                    System.out.println("j = " + j);
                    System.out.println("WRITTEN " + writtenBlocks + " so far building our way up to " + numRocks);
                    //go through each block starting from the top left to the bottom right
                    System.out.println("First block is: " + map.get(new Coordinate(j, i)).getClass().toString() + " with coordinates : " + map.get(new Coordinate(j, i)).getPosition());

                    if (map.get(new Coordinate(j, i)) instanceof Block && !(map.get(new Coordinate(j, i)) instanceof EmptyBlock) && !(map.get(new Coordinate(j, i)).isPlaced())) {//only try and place blocks not placeds
                        //block is already placed and thus has the necessary data to be used at a reference
                        block = map.get(new Coordinate(j, i));
                        //holds all the avaliable blocks this block could possible use as a reference
                        System.out.println("Obtaining valid references for block: " + block.getPosition());
                        avaliableBlocks = validRefs(block.getPosition(), 2);
                        System.out.println("Size: " + avaliableBlocks.size());
                        //should never be null but catch to be safe
                        try {
                            if (avaliableBlocks.size() > 0) {//if there are at least 1 avaliable blocks we're good to use that as a reference! otherwise we gotta search further
                                //for every non placed block in range of ref placed block place all those blocks
                                for (Map.Entry<Coordinate, Block> entry : avaliableBlocks.entrySet()) {
                                    System.out.println("Processing block: " + entry.getValue().getPosition());
                                    view += (viewStart + "\n");
                                    //just need to grab the first one
                                    if (entry.getValue() instanceof Block && !(entry.getValue() instanceof EmptyBlock)) {
                                        //found the first of avaliableBlocks.size()
                                        Block ref = entry.getValue();
                                        block.setId(obId);//used for variable naming
                                        obId++;
                                        block.setPlaced(true);
                                        ++writtenBlocks;
                                        System.out.println("Placing block on map: " + block.getPosition());
                                        //write block heading
                                        view += (fullAssetDataName(assetData.ID) + (block instanceof FinishBlock ? (constFinish) : (constObstacle + block.getId())) + qm + "\n");//variable name
                                        view += (fullAssetDataName(assetData.WDTH) + qm + "\n");//const width
                                        view += (fullAssetDataName(assetData.HGTH) + qm + "\n");//const height
                                        view += (fullAssetDataName(assetData.BKRND) + (block instanceof FinishBlock ? (fullAssetName((assets.P_FIN))) : (fullAssetName((assets.P_OBST)))) + qm + "\n");//const background with respect to the blocktype
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
                                    //we only needed to process one of them
                                    break;
                                }
                            }else {
                                //if there were no blocks in the immediate range we need to check accross the entire map 
                                //holds all the avaliable blocks this block could possible use as a reference
                                System.out.println("Obtaining valid references for block: " + block.getPosition() + " version 2");
                                //max amount could be 8 items. if its 0...gtfo
                                avaliableBlocks = validRefs(block.getPosition(), 2);
                                System.out.println("Size: " + avaliableBlocks.size());
                                if(avaliableBlocks.size() > 0){
                                    //we need to find a horizontal reference and a vertical reference from the list
                                    Block horRef = null;
                                    Block vertRef = null;
                                    boolean hFound = false;
                                    boolean vFound = false;
                                    int vertref = (block.getPosition().getX()); 
                                    int horref = (block.getPosition().getY());
                                    for (Map.Entry<Coordinate, Block> entry : avaliableBlocks.entrySet()) {
                                        Coordinate co = entry.getKey();
                                        System.out.println("Checking reference: " + co);
                                        if(!vFound && (co.getX() == vertref-1 || co.getX() == vertref+1)){//this is the vertical reference, store the block and move on
                                            System.out.println("storing vertical reference: " + co);
                                            vertRef = entry.getValue();
                                            vFound = true;
                                        }else if(!hFound && (co.getY() == horref-1 || co.getY() == horref+1)){//this is the horizontal reference, store the block and move on
                                            System.out.println("storing vertical reference: " + co);
                                            horRef = entry.getValue();
                                            hFound = true;
                                        }
                                    }
                                    //once we have the two references, just write the block using the two references
                                    if(vFound && hFound){
                                        view += (viewStart + "\n");
                                        block.setId(obId);//used for variable naming
                                        obId++;
                                        block.setPlaced(true);
                                        ++writtenBlocks;
                                        //write block heading
                                        view += (fullAssetDataName(assetData.ID) + (block instanceof FinishBlock ? (constFinish) : (constObstacle + block.getId())) + qm + "\n");//variable name
                                        view += (fullAssetDataName(assetData.WDTH) + qm + "\n");//const width
                                        view += (fullAssetDataName(assetData.HGTH) + qm + "\n");//const height
                                        view += (fullAssetDataName(assetData.BKRND) + (block instanceof FinishBlock ? (fullAssetName((assets.P_FIN))) : (fullAssetName((assets.P_OBST)))) + qm + "\n");//const background with respect to the blocktype
                                        //block.x < vref.x - left      block.x > vref.x - right
                                        int xDist = block.getPosition().getX() - vertRef.getPosition().getX();
                                        int yDist = block.getPosition().getY() - vertRef.getPosition().getY();
                                        view += (fullRelativeName((xDist < 0)?relativeLayouts.TO_LT_OF:relativeLayouts.TO_RT_OF) + (vertRef instanceof MovingBlock ? (constRunner) : (vertRef instanceof FinishBlock ? (constFinish) : (constObstacle + vertRef.getId()))) + qm + "\n");
                                        //block.y < hor.y - up      block.x > hor.x - down
                                        view += (fullRelativeName((yDist < 0)?relativeLayouts.ABV:relativeLayouts.BLW) + (horRef instanceof MovingBlock ? (constRunner) : (horRef instanceof FinishBlock ? (constFinish) : (constObstacle + horRef.getId()))) + qm + "\n"); 
                                        if(Math.abs(xDist)==2){//need to add margin left or right
                                            if(xDist < 0){
                                                view += (fullRelativeName(relativeLayouts.MGN_RT) + constDimen + qm + "\n");//margin bottom moves the block up
                                            }else{
                                                view += (fullRelativeName(relativeLayouts.MGN_LT) + constDimen + qm + "\n");//margin bottom moves the block up
                                            }
                                        }
                                        if(Math.abs(yDist)==2){//need to add margin up or down
                                            if(yDist < 0){
                                                view += (fullRelativeName(relativeLayouts.MGN_BTM) + constDimen + qm + "\n");//margin bottom moves the block up
                                            }else{
                                                view += (fullRelativeName(relativeLayouts.MGN_TP) + constDimen + qm + "\n");//margin bottom moves the block up
                                            }
                                        }
                                        view += (viewEnd + "\n");
                                        levelXML.append(view);
                                        view = "";
                                    }else{
                                        System.out.println("was IN version 2 but couldn't find two references!!!");
                                    }
                                    //if two references were not found, skip this block and move on
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
            ++placeAllPossibleBlocks;
            System.out.println("Retracing map for the " + placeAllPossibleBlocks + " time");
            if (placeAllPossibleBlocks > 5) {
                //if we have traversed from (0,0) to (w-1,h-1) 5+ times this means we've placed all the blocks we can. 
                //The blocks that are left and unreachable and must be placed using the filler!
                break;
            }
        }
        System.out.println("Successfully Placed " + writtenBlocks + " blocks and was unable to place (due to unreachable location) " + (numRocks - writtenBlocks) + " locations");
        return levelXML;
    }
    

    //Generates a list of all blocks within range of a given coordinate 
    /*
        get the first block already placed  in the coordinates (x+1, y^) (x+1, yv) (x-1, y^) (x-1, yv) (<x, y+1) (<x, y+1) (x>, y-1) (x>, y-1)
    */
    private HashMap<Coordinate, Block> validRefs(Object coordinate, int version) {
        HashMap<Coordinate, Block> validrefs = new HashMap<Coordinate, Block>();
        Coordinate coord = null;
        if (coordinate instanceof Coordinate) {
            coord = (Coordinate) coordinate;
        } else {
            return null;//show not happen but just in case
        }
        System.out.println(coord);
        Stack<Coordinate> range = (version == 1 ? calculateRange(coord) : calculateRangeInner(coord));
        int useX = 0;
        if (version == 1) {
            while (!range.isEmpty()) {//x+1, x-1, y+1, y-1
                ++useX;
                Coordinate co = range.pop();//pop increment y dec y pop inc y dec y pop inc x dec x pop inc x dec x
                Coordinate co2 = co;
                while ((useX > 4 ? co2.getX() : co2.getY()) >= 0) {
                    if(useX > 4){
                        System.out.println("decrementing x");
                        co2.increment(-1, 0);//decrement X
                    }else{
                        System.out.println("decrementing y");
                        co2.increment(0, -1);//decrement Y
                    }
                    System.out.println("checking : " + co2.toString());
                    if (isValidCoord(co2)) {
                        System.out.println("valid!");
                        if (map.get(co2) instanceof Block) {
                            System.out.println("im a block!");
                            if (!(map.get(co2) instanceof EmptyBlock)) {
                                System.out.println("not empty!");
                                if (map.get(co2).isPlaced()) {
                                    System.out.println("placing it!");
                                    validrefs.put(co2, map.get(co2));
                                    //found one so break!
                                    break;
                                }
                            }
                        }
                    }
                }
                co2 = co;
                while ((useX > 4 ? co2.getX() : co2.getY()) < (useX > 4 ? maxX : maxY)) {
                    if(useX > 4){
                        System.out.println("decrementing x");
                        co2.increment(1, 0);//increment X
                    }else{
                        System.out.println("decrementing y");
                        co2.increment(0, 1);//increment Y
                    }
                    System.out.println("checking : " + co2.toString());
                    if (isValidCoord(co2)) {
                        System.out.println("valid!");
                        if (map.get(co2) instanceof Block) {
                            System.out.println("im a block!");
                            if (!(map.get(co2) instanceof EmptyBlock)) {
                                System.out.println("not empty!");
                                if (map.get(co2).isPlaced()) {
                                    System.out.println("placing it!");
                                    validrefs.put(co2, map.get(co2));
                                    //found one so break!
                                    break;
                                }
                            }
                        }
                    }
                }
            }
        } else {
            while (!range.isEmpty()) {
                Coordinate co = range.pop();
                System.out.println("checking : " + co.toString());
                if (isValidCoord(co)) {
                    System.out.println("valid!");
                    if (map.get(co) instanceof Block) {
                        System.out.println("im a block!");
                        if (!(map.get(co) instanceof EmptyBlock)) {
                            System.out.println("not empty!");
                            if (map.get(co).isPlaced()) {
                                System.out.println("placing it!");
                                validrefs.put(co, map.get(co));
                            }
                        }
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
