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
        protected Coordinate horRef;
        protected Coordinate verRef;
        protected boolean isBase;//refers to this block being on a wall or corner or center somewhere

        public boolean isIsBase() {
            return isBase;
        }

        public void setIsBase(boolean isBase) {
            this.isBase = isBase;
        }

        public Coordinate getHorRef() {
            return horRef;
        }

        public void setHorRef(Coordinate horRef) {
            this.horRef = horRef;
        }

        public Coordinate getVerRef() {
            return verRef;
        }

        public void setVerRef(Coordinate verRef) {
            this.verRef = verRef;
        }
        

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
        range.push(new Coordinate(x, y));
        range.push(new Coordinate(x, y));
        range.push(new Coordinate(x + 1, y));
        range.push(new Coordinate(x - 1, y));
        range.push(new Coordinate(x, y));
        range.push(new Coordinate(x, y));
        range.push(new Coordinate(x, y + 1));
        range.push(new Coordinate(x, y - 1));
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
        //obstacle ID so each view block has their own identifier
        int obId = 1;
        int placeAllPossibleBlocks = 0;
        
        //place possible known blocks
        /*
        place middle left, top, right, down if present
        place actual middle if present.
        */
        //all four corners if present
        
/******************CONSTANT LOCATIONS******************/
        block = map.get(new Coordinate(0, 0));
        if (block instanceof Block && !(block instanceof EmptyBlock)&& !inPath(block.getPosition())) {//top left
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
        if (block instanceof Block && !(block instanceof EmptyBlock)&& !inPath(block.getPosition())) {//bottom right
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
        if (block instanceof Block && !(block instanceof EmptyBlock)&& !inPath(block.getPosition())) {//top right
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
        if (block instanceof Block && !(block instanceof EmptyBlock)&& !inPath(block.getPosition())) {//bottom left
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
        if (block instanceof Block && !(block instanceof EmptyBlock)&& !inPath(block.getPosition())) {//top middle
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
        if (block instanceof Block && !(block instanceof EmptyBlock)&& !inPath(block.getPosition())) {//bottom middle
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
        if (block instanceof Block && !(block instanceof EmptyBlock)&& !inPath(block.getPosition())) {//middle right
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
        if (block instanceof Block && !(block instanceof EmptyBlock)&& !inPath(block.getPosition())) {//middle left
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
        assignReferences();
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

                    if (map.get(new Coordinate(j, i)) instanceof Block && !(map.get(new Coordinate(j, i)) instanceof EmptyBlock) && (map.get(new Coordinate(j, i)).isPlaced())) {//only process those blocks placed looking for all unplaced blocks in their range
                        //block is already placed and thus has the necessary data to be used at a reference
                        Block ref = map.get(new Coordinate(j, i));
                        //holds all the avaliable blocks this block could possible use as a reference
                        System.out.println("Obtaining valid references for block: " + block.getPosition());
                        avaliableBlocks = validRefs(block.getPosition(), 2);
                        System.out.println("Size: " + avaliableBlocks.size());
                        //should never be null but catch to be safe
                        try {
                            //run through the entire map trying to place clusters from both top left and bottom right
                                //for every non placed block in range of ref placed block place all those blocks
                                for (Map.Entry<Coordinate, Block> entry : avaliableBlocks.entrySet()) {
                                    System.out.println("Processing block: " + entry.getValue().getPosition());
                                    view += (viewStart + "\n");
                                    //just need to grab the first one
                                    if (entry.getValue() instanceof Block && !(entry.getValue() instanceof EmptyBlock)) {
                                        //found the first non placed block that can use ref as a reference
                                        block = entry.getValue();
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
        System.out.println("Successfully Placed " + writtenBlocks + " blocks and was unable to place (due to unreachable location) " + (numRocks - writtenBlocks) + " locations\nSuccessfully placed blocks: ");
        for(Map.Entry<Coordinate, Block> entry : map.entrySet()){
            if(entry.getValue().isPlaced()){
                System.out.print(entry.getValue().getPosition());
            } 
        }
        return levelXML;
    }
    
    //This is for all the little blocks that were unplacable due to a lack of valid references - but it is not the end for these brave souls!
    private void applyPadding(Block unplacable){
        //we need a horizontal and a vertical reference
        HashMap<Coordinate, Block> refs = validRefs(unplacable.getPosition(), 3);
        boolean vPlaced = false;
        boolean yPlaced = false;
        if(refs.size() > 1){//we want it to be two every time
            RockBlock newBlock = null;
            RockBlock newBlock2 = null;
            for(Map.Entry<Coordinate, Block> ent : refs.entrySet()){
                //at most it will only have two object. hopefully it will have 2 objects always -__-
                Block ref = ent.getValue();
                if(unplacable.getPosition().getX() == ref.getPosition().getX()){//if the x's are the same vertical ref
                    unplacable.setVerRef(ref.getPosition());
                    vPlaced = true;
                }else if(unplacable.getPosition().getY() == ref.getPosition().getY()){
                    unplacable.setHorRef(ref.getPosition());
                    yPlaced = true;
                }else{
                    //uhhoh. If its not one of the above we have bigger problems.
                }
                if(yPlaced && vPlaced){
                    unplacable.setPlaced(true);
                    newBlock = new RockBlock();
                    newBlock.setPosition(new Coordinate(unplacable.getVerRef().getX(), unplacable.getVerRef().getY()));
                    map.put(newBlock.getPosition(), newBlock);
                    newBlock2 = new RockBlock();
                    newBlock2.setPosition(new Coordinate(unplacable.getHorRef().getX(), unplacable.getHorRef().getY()));
                    map.put(newBlock2.getPosition(), newBlock2);
                    break;
                }
            }
            //now get valid refs for each of the new blocks and call it a day!
            for (int j = 0; j < 2; ++j) {
                Block b = (j==0?newBlock:newBlock2);
                HashMap<Coordinate, Block> refs2 = validRefs(b.getPosition(), 1);
                if (refs2.size() > 1) {
                    Coordinate horRef = null;
                    Coordinate vertRef = null;
                    boolean hFound = false;
                    boolean vFound = false;
                    int vertref = (b.getPosition().getX());
                    int horref = (b.getPosition().getY());
                    for (Map.Entry<Coordinate, Block> entry : refs2.entrySet()) {
                        Coordinate co = entry.getKey();
                        Block refBlock = entry.getValue();
                        System.out.println("Checking reference: " + co);
                        //make sure refBlock isn't also referencing this block : !(co.equals(refBlock.getVer/HorRef())) <-- if it is don't use it because of cyclicness
                        if (!vFound && !(co.equals(refBlock.getVerRef())) && (co.getX() == vertref || co.getX() == vertref - 1 || co.getX() == vertref + 1)) {//this is the vertical reference, store the block and move on, can be inline or offset by one
                            System.out.println("storing vertical reference: " + co);
                            vertRef = entry.getKey();
                            vFound = true;
                        } else if (!hFound && !(co.equals(refBlock.getHorRef())) && (co.getY() == horref || co.getY() == horref - 1 || co.getY() == horref + 1)) {//this is the horizontal reference, store the block and move on can be inline, or offset by 1
                            System.out.println("storing vertical reference: " + co);
                            horRef = entry.getKey();
                            hFound = true;
                        }
                        if (vFound && hFound) {
                            b.setPlaced(true);
                            b.setVerRef(vertRef);
                            b.setHorRef(horRef);
                            break;//no need to check the others
                        }
                    }
                }
            }
        }else{
            //check center and corners
            System.out.println("FAILURE TO PAD!!!");
        }
        
    }
    
    private void assignReferences() {
        int placeAllPossibleBlocks = 0;
        int obId = 1;
        //its possible some blocks were skipped so we need to retrace 5 times to make sure all blocks are placed
        while (placeAllPossibleBlocks < 5) {
            Block toPlaceBlock;
            //traverse left to right top to bottom
            for (int i = 0; i < maxY; ++i) {//increments rows
                for (int j = 0; j < maxX; ++j) {//increments columns
                    Coordinate curCoord = new Coordinate(j, i);
                    Block refBlock = map.get(curCoord);
                    if (refBlock instanceof Block && !(refBlock instanceof EmptyBlock) && (refBlock.isPlaced())) {//only process those blocks placed looking for all unplaced blocks in their range
                        HashMap<Coordinate, Block> refs = validRefs(refBlock.getPosition(), 2);
                        boolean hFound = false;
                        boolean vFound = false;
                        if (refs.size() > 0) {//this is your captain speaking. We have liftoff.
                            System.out.println("assigning singular block references");
                            for (Map.Entry<Coordinate, Block> ent : refs.entrySet()) {
                                toPlaceBlock = ent.getValue();
                                toPlaceBlock.setId(obId);//used for variable naming
                                obId++;
                                toPlaceBlock.setPlaced(true);
                                if (!vFound && !(refBlock.getPosition().equals(toPlaceBlock.getVerRef()))) {
                                    toPlaceBlock.setVerRef(refBlock.getPosition());
                                    vFound = true;
                                }
                                if (!hFound && !(refBlock.getPosition().equals(toPlaceBlock.getHorRef()))) {
                                    toPlaceBlock.setHorRef(refBlock.getPosition());
                                    hFound = true;
                                }
                            }
                        }
                    }
                }
            }
            /*ALRIGHT.
            1st we place all the ones we know are bases. corners, centers, stuff like that.
            2nd we traverse the map from top left to bottom right ONLY processing on already placed blocks checking for any NON placed block within cluster range
            3rd we traverse the map again t-l to b-r this time ONLY processing on NON placed blocks checking for any PLACED blocks that can be used as references
            THEN repeat (n/4) times n=the number of blocks placed, so if 20 blocks are placed the entire process runs 5 times. 
            running the entire process n/4 times (involving cluster referencing and longer aligned referencing at each runthrough) should be plenty*/
            //after placing as many blocks as possible in clusters go through each unplaced block and see if they can be placed off one of the already placed blocks
            for (int i = 0; i < maxY; ++i) {//increments rows
                for (int j = 0; j < maxX; ++j) {//increments columns
                    Coordinate curCoord = new Coordinate(j, i);
                    Block refBlock = map.get(curCoord);
                    HashMap<Coordinate, Block> refs = validRefs(refBlock.getPosition(), 1);
                    System.out.println("trying to assign bi-block references!\nrefs.size() = " + refs.size());
                    if (refs.size() > 1) {//need at least two blocks if not in the immediate range
                        Coordinate horRef = null;
                        Coordinate vertRef = null;
                        hFound = false;
                        vFound = false;
                        int vertref = (refBlock.getPosition().getX());
                        int horref = (refBlock.getPosition().getY());
                        for (Map.Entry<Coordinate, Block> ent : refs.entrySet()) {
                            Coordinate co = ent.getKey();
                            Block refBlock = ent.getValue();
                            System.out.println("Checking reference: " + co);
                            //make sure refBlock isn't also referencing this block : !(co.equals(refBlock.getVer/HorRef())) <-- if it is don't use it because of cyclicness
                            if (!vFound && !(co.equals(refBlock.getVerRef())) && (co.getX() == vertref || co.getX() == vertref - 1 || co.getX() == vertref + 1)) {//this is the vertical reference, store the block and move on, can be inline or offset by one
                                System.out.println("storing vertical reference: " + co);
                                vertRef = ent.getKey();
                                vFound = true;
                            } else if (!hFound && !(co.equals(refBlock.getHorRef())) && (co.getY() == horref || co.getY() == horref - 1 || co.getY() == horref + 1)) {//this is the horizontal reference, store the block and move on can be inline, or offset by 1
                                System.out.println("storing vertical reference: " + co);
                                horRef = ent.getKey();
                                hFound = true;
                            }
                            if (vFound && hFound) {
                                curBlock.setPlaced(true);
                                curBlock.setVerRef(vertRef);
                                curBlock.setHorRef(horRef);
                                break;//no need to check the others
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
        for (Map.Entry<Coordinate, Block> ent : map.entrySet()) {
            if(ent.getValue() instanceof Block && !(ent.getValue() instanceof EmptyBlock) && !ent.getValue().isPlaced()){
                System.out.println("aplly padding!!");
                //applyPadding(ent.getValue());
            }
        }
        System.out.println("Displaying blocks and references in format\nBlock:x | Vref:v | Href:h");
        for (Map.Entry<Coordinate, Block> ent : map.entrySet()) {
            if(ent.getValue() instanceof Block && !(ent.getValue() instanceof EmptyBlock)){
                System.out.println("Block:"+ent.getValue().getPosition()+" | Vref:"+ent.getValue().getVerRef()+" | Href:"+ent.getValue().getHorRef());
            }
        }
        System.exit(1);
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
        Stack<Coordinate> rangePrime = (version == 1 ? calculateRange(coord) : calculateRangeInner(coord));
        System.out.println("RANGE for " + coord + " is as follows: ");
        Stack<Coordinate> range = new Stack<Coordinate>();
        while(!rangePrime.isEmpty()){
            Coordinate c = rangePrime.pop();
            System.out.print(" "+c + " ");
            range.push(c);
        }
        int useX = 0;
        if (version == 1) {
            System.out.println("using version 1");
            while (!range.isEmpty()) {//x+1, x-1, y+1, y-1
                ++useX;
                Coordinate co = range.pop();//pop increment y dec y pop inc y dec y pop inc x dec x pop inc x dec x
                Coordinate co2 = new Coordinate(co.getX(), co.getY());
                while ((useX > 4 ? co2.getX() : co2.getY()) >= 0) {
                    if (useX > 4) {
                        System.out.println("decrementing x");
                        co2.increment(-1, 0);//decrement X
                    } else {
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
                                System.out.println("placing it!");
                                validrefs.put(co2, map.get(co2));
                                //found one so break!
                                break;
                            }
                        }
                    }
                }
                co2 = new Coordinate(co.getX(), co.getY());
                while ((useX > 4 ? co2.getX() : co2.getY()) < (useX > 4 ? maxX : maxY)) {
                    if (useX > 4) {
                        System.out.println("incrementing x");
                        co2.increment(1, 0);//increment X
                    } else {
                        System.out.println("incrementing y");
                        co2.increment(0, 1);//increment Y
                    }
                    System.out.println("checking : " + co2.toString());
                    if (isValidCoord(co2)) {
                        System.out.println("valid!");
                        if (map.get(co2) instanceof Block) {
                            System.out.println("im a block!");
                            if (!(map.get(co2) instanceof EmptyBlock)) {
                                System.out.println("not empty!");
                                System.out.println("placing it!");
                                validrefs.put(co2, map.get(co2));
                                //found one so break!
                                break;
                            }
                        }
                    }
                }
            }
        } else if (version == 2) {
            System.out.println("using version 2");
            while (!range.isEmpty()) {
                Coordinate co = range.pop();
                //System.out.println("checking : " + co.toString());
                if (isValidCoord(co)) {
                    //System.out.println("valid!");
                    if (map.get(co) instanceof Block) {
                        //System.out.println("im a block!");
                        if (!(map.get(co) instanceof EmptyBlock)) {
                            System.out.println("not empty!");
                            System.out.println("placing it!");
                            validrefs.put(co, map.get(co));
                        }
                    }
                }
            }
        } else {
            System.out.println("using version 3");
            boolean vFound = false;
            boolean hFound = false;
            //verticals
            if (!vFound) {
                for (int i = coord.getX(); i >= 0; --i) {//check left first
                    Coordinate co = new Coordinate(i, coord.getY());
                    //System.out.println("checking : " + co.toString());
                    if (isValidCoord(co)) {
                        //System.out.println("valid!");
                        if (map.get(co) instanceof Block) {
                            //System.out.println("im a block!");
                            if ((map.get(co) instanceof EmptyBlock)) {
                                if (!inPath(co)) {
                                    System.out.println("is empty and in the path!");
                                    System.out.println("placing it!");
                                    validrefs.put(co, map.get(co));
                                    vFound = true;
                                    break;
                                }
                            }
                        }
                    }
                }
            }
            if (!vFound) {
                for (int i = coord.getX(); i < maxX; ++i) {//check right first
                    Coordinate co = new Coordinate(i, coord.getY());
                    //System.out.println("checking : " + co.toString());
                    if (isValidCoord(co)) {
                        //System.out.println("valid!");
                        if (map.get(co) instanceof Block) {
                            //System.out.println("im a block!");
                            if ((map.get(co) instanceof EmptyBlock)) {
                                if (!inPath(co)) {
                                    System.out.println("is empty and in the path!");
                                    System.out.println("placing it!");
                                    validrefs.put(co, map.get(co));
                                    vFound = true;
                                    break;
                                }
                            }
                        }
                    }
                }
            }
            if (!hFound) {
                for (int i = coord.getY(); i >= 0; --i) {//check up first
                    Coordinate co = new Coordinate(coord.getX(), i);
                    //System.out.println("checking : " + co.toString());
                    if (isValidCoord(co)) {
                        //System.out.println("valid!");
                        if (map.get(co) instanceof Block) {
                            //System.out.println("im a block!");
                            if ((map.get(co) instanceof EmptyBlock)) {
                                if (!inPath(co)) {
                                    System.out.println("is empty and in the path!");
                                    System.out.println("placing it!");
                                    validrefs.put(co, map.get(co));
                                    hFound = true;
                                    break;
                                }
                            }
                        }
                    }
                }
            }
            if (!hFound) {
                for (int i = coord.getY(); i < maxY; ++i) {//check down first
                    Coordinate co = new Coordinate(coord.getX(), i);
                    //System.out.println("checking : " + co.toString());
                    if (isValidCoord(co)) {
                        //System.out.println("valid!");
                        if (map.get(co) instanceof Block) {
                            //System.out.println("im a block!");
                            if ((map.get(co) instanceof EmptyBlock)) {
                                if (!inPath(co)) {
                                    System.out.println("is empty and in the path!");
                                    System.out.println("placing it!");
                                    validrefs.put(co, map.get(co));
                                    hFound = true;
                                    break;
                                }
                            }
                        }
                    }
                }
            }
        }
        return validrefs;
    }
    
    private boolean inPath(Coordinate co){
        if(shortestPathRock.getAllPreviousPositions().containsKey(co)){
            return true;//obviously because its one of the actual blocks
        }
        Coordinate cBefore;
        Coordinate cAfter;
        cBefore = shortestPathRock.getPreviousPositions().get(1);
        for (int i = 1; i < shortestPathRock.getPreviousPositions().size() - 1; i++) {
            cAfter = shortestPathRock.getPreviousPositions().get(i + 1);
            if(co.getX() == cBefore.getX() && co.getX() == cAfter.getX()){//if the x's are the same that means the path is vertical and check if co.y is between before and after
                if(co.getY()>cBefore.getY()){//we're coming down the mountain
                    if(co.getY() < cAfter.getY()){
                        return true;
                    }
                }else{//cannot be equal or it would have already returned true
                    if(co.getY()<cBefore.getY()){//we're going up the mountain
                        if(co.getY() > cAfter.getY()){
                            return true;
                        }
                    }
                }
            }else if(co.getY() == cBefore.getY() && co.getY() == cAfter.getY()){
                if(co.getX()>cBefore.getX()){//we're coming down the mountain
                    if(co.getX() < cAfter.getX()){
                        return true;
                    }
                }else{//cannot be equal or it would have already returned true
                    if(co.getX()<cBefore.getX()){//we're going up the mountain
                        if(co.getX() > cAfter.getX()){
                            return true;
                        }
                    }
                }
                
            }
            cBefore = cAfter;
        }
        //if we made it all the way through the path return false because we are not in the path
        System.out.println("NOT IN THE PATH");
        return false;
    }

    private boolean isValidCoord(Coordinate c){
        System.out.println("returning " + (c.getX()<maxX && c.getX()>=0 && c.getY()<maxY && c.getY()>=0) + "for valid coord");
        return (c.getX()<maxX && c.getX()>=0 && c.getY()<maxY && c.getY()>=0);
    }
}
