
package algorithms;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
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
    private static boolean recreateMap = false;
    private static int maxX = 11;
    private static int maxY = 15;
    private static int midX = (maxX - 1) / 2;
    private static int midY = (maxY - 1) / 2;
    private static int minMoves = 15;
    private static int maxMoves = 20;
    private static int retryCount = 0;
    private int numRocks;
    private int numBlocksToWrite = 0;
    private static int levelNum = 1;
    private static String levelGenre = "fire";//right now its hardcoded
    //private final File templateXML = new File("/Users/dewit/Documents/shift_files/level_files/level_template/pack_layout_template.xml");//the path is relative to my comp atm, but it will be hardcoded in the future nonetheless
    //private final File templateDir = new File("/Users/dewit/Documents/shift_files/level_files/level_template/");
    private final File templateXML = new File("C:\\Users\\Christian\\Documents\\TestGame\\app\\src\\main\\res\\layout\\pack_layout_template.xml");//the path is relative to my comp atm, but it will be hardcoded in the future nonetheless
    private final File templateDir = new File("C:\\Users\\Christian\\Documents\\TestGame\\app\\src\\main\\res\\layout\\");
    
    private static boolean superDebug = false;
    private static boolean showPath = true;

    private enum Direction {
        UP, DOWN, LEFT, RIGHT
    }

    public static void main(String[] args) {
        Long totalTime = System.currentTimeMillis();
        if (args.length > 0) {
            /*levelGenre = args[0];
            Solver s = new Solver();
            recreateMap = true;
            s.setMapFromString(args[0]);
            s.createAndSolve();
        } else {
            Solver s = new Solver();
            s.run();
        }*/
            levelGenre = args[0];
        }
            Solver s = new Solver();
            s.run();
        long total = 0;
        for (Long t : execTimes) {
            total += t;
        }
        System.out.println("Total Time Spent Looking For Map = " + ((System.currentTimeMillis() - totalTime) / 1000.00) + " seconds");
        if (!recreateMap) {
            System.out.println("Average Time Per Map For Creation/Solve = " + (total / execTimes.size()) + "ms");
        }
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

        // For creating actual xml maps, we need to add a block to the top left
        MovingBlock block = new MovingBlock();
        Coordinate start = new Coordinate(0, 0);
        block.setPosition(start);
        map.put(start, block);

        levelXML = new StringBuilder();
        levelXML = build(true);
        System.out.println(levelXML.toString());
        System.out.println("\n\nCreating new level file...");
        String levelName = "pack_"+levelGenre+"_level";
        XMLCreator levelXMLFile = new XMLCreator(levelXML, levelName, templateXML,levelNum);
        if(levelXMLFile.getLevel()!=null){
            File levelFile = new File(levelXMLFile.getLevel().getAbsolutePath());
            System.out.println("\n\nSuccessfully created new level file:\n\t"+levelFile.getAbsolutePath());
        }else{
            System.out.println("\n\nFile not created : unable to create file within:\n\t"+templateDir);
        }
    }

    public boolean createAndSolve() {
        if (!recreateMap) {
            map = new HashMap<>();
        }
        if (foundGoodMap) {
            return false;
        }


        shortestPathRock = null;
        retryCount++;
        Queue<MovingBlock> moveQueue = new LinkedList<>();
        boolean solved = false;
        if (!superDebug && !foundGoodMap && !recreateMap) {
            System.out.print("\rTotal Number Of Puzzles Tried: " + retryCount + "\t");
        }

        /*
        Test Block
         */
//        Coordinate randomPortalBlockCoordinate = new Coordinate(3, 3);
//        PortalBlock portalBlockCompanion = new PortalBlock(randomPortalBlockCoordinate);
//        map.put(randomPortalBlockCoordinate, portalBlockCompanion);


        int rockCount = 0;
        int bubbleCount = 0;
        int moltenCount = 0;
        int portalCount = 0;

        //int randomNum = ThreadLocalRandom.current().nextInt(50, 95 + 1); // Try out different block densities between 25% & 90% 
        int randomNum = ThreadLocalRandom.current().nextInt(91, 95 + 1); //<--trying less dense 25% and 75%?
        if (!recreateMap) {
            for (int i = 0; i < maxY; i++) { // Generate 25x25 map
                for (int a = 0; a < maxX; a++) {
                    Coordinate currentCoordinate = new Coordinate(a, i);
                    if (map.containsKey(currentCoordinate) && map.get(currentCoordinate) instanceof RockBlock) {
                        System.out.println("skipping stupid place: "+map.get(currentCoordinate).getBlockType());
                        continue; // We previously placed a portal block here, don't overwrite it!
                    }
                    EmptyBlock emptyBlock = new EmptyBlock(currentCoordinate);
                    map.put(currentCoordinate, emptyBlock);
                    if (i == 0 && a == 0) {
                        continue; // Don't put rocks at start
                    }
                    if (Math.random() * 100 > randomNum * 1.00) {
                        RockBlock rockBlock = new RockBlock(currentCoordinate);
                        map.put(currentCoordinate, rockBlock);
                        rockCount++;
                    } else if (Math.random() * 100 > randomNum * 1.2) {
                        BubbleBlock bubbleBlock = new BubbleBlock(currentCoordinate);
                        map.put(currentCoordinate, bubbleBlock);
                        bubbleCount++;
                    } else if (Math.random() * 100 > randomNum * 1.02) {
                        MoltenBlock moltenBlock = new MoltenBlock(currentCoordinate);
                        map.put(currentCoordinate, moltenBlock);
                        moltenCount++;
                    } else if (Math.random() * 100 > randomNum * 10.2) { // Turned off right now
                        int randomX = ThreadLocalRandom.current().nextInt(1, maxX);
                        int randomY = ThreadLocalRandom.current().nextInt(1, maxY);

                        while (randomX == currentCoordinate.getX() || randomY == currentCoordinate.getY()) { // Make sure we don't happen to get the exact same spot (very unlikely)
                            randomX = ThreadLocalRandom.current().nextInt(1, maxX);
                            randomY = ThreadLocalRandom.current().nextInt(1, maxY);
                        }
                        while (diff(i, randomX) < 5 || diff(a, randomY) < 5) { // Make sure they are at least a little bit away from each other
                            randomX = ThreadLocalRandom.current().nextInt(1, maxX);
                            randomY = ThreadLocalRandom.current().nextInt(1, maxY);
                        }
//                        for (int t = -1; t <= 1; t += 2) {
//                            Coordinate coordinate = new Coordinate(randomX + i, randomY); // Check around portal exit
//                            Coordinate coordinate2 = new Coordinate(randomX, randomY + i);
//                            Coordinate coordinate3 = new Coordinate(a + i, i); // Check around portal just placed
//                            Coordinate coordinate4 = new Coordinate(a, i + i);
//                            if (map.containsKey(coordinate) && map.get(coordinate) instanceof RockBlock) {
//                                addRockBlock(coordinate);
//                                rockCount++;
//                            }
//                            if (map.containsKey(coordinate2) && map.get(coordinate2) instanceof RockBlock) {
//                                addRockBlock(coordinate2);
//                                rockCount++;
//                            }
//                            if (map.containsKey(coordinate3) && map.get(coordinate3) instanceof RockBlock) {
//                                addRockBlock(coordinate3);
//                                rockCount++;
//                            }
//                            if (map.containsKey(coordinate4) && map.get(coordinate4) instanceof RockBlock) {
//                                addRockBlock(coordinate4);
//                                rockCount++;
//                            }
//                        }
//                        if (i == 0 || i == maxY) { // These 2 if blocks make sure that if a portal sits on an edge, we put a rock on the corresponding side on the companion portal
//                            Coordinate neededBlockCoordinate = new Coordinate(randomX, randomY + (i == 0 ? -1 : 1));
//                            addRockBlock(neededBlockCoordinate);
//                            rockCount++;
//                        }
//                        if (a == 0 || a == maxX) {
//                            Coordinate neededBlockCoordinate = new Coordinate(randomX + (a == 0 ? -1 : 1), randomY);
//                            addRockBlock(neededBlockCoordinate);
//                            rockCount++;
//                        }


//                        Coordinate randomPortalBlockCoordinate = new Coordinate(randomX, randomY);
//                        PortalBlock portalBlockCompanion = new PortalBlock(randomPortalBlockCoordinate);

//                        PortalBlock portalBlock = new PortalBlock(currentCoordinate);
//                        portalBlock.setPortalExit(portalBlockCompanion);
//                        portalBlockCompanion.setPortalExit(portalBlock);


//                        map.put(currentCoordinate, portalBlock);
//                        map.put(randomPortalBlockCoordinate, portalBlockCompanion);

//                        System.out.println("Putting Portal Block In "+currentCoordinate+"\tw/Companion At "+randomPortalBlockCoordinate);
//                        portalCount += 2;
                    }
                }
            }
        }


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


        if (!recreateMap) {
            Coordinate finishCoordinate = new Coordinate(finishXAxis, finishYAxis);
            FinishBlock finishBlock = new FinishBlock(finishCoordinate);
            map.put(finishCoordinate, finishBlock);


            Coordinate finishRockCoordinate = new Coordinate(finishXAxis + (moveX ? finishXRockBlockPlusMinus : 0), finishYAxis + (moveX ? 0 : finishYRockBlockPlusMinus));
            RockBlock rockBlock = new RockBlock(finishRockCoordinate); // Put a solid rock next to the win block always
            map.put(finishRockCoordinate, rockBlock);
            rockCount++;
        }


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
                        return false; // We will try whole process again because it was too easy
                    }
                    if (shortestPathRock == null || currentBlock.getPreviousPositions().size() < shortestPathRock.getPreviousPositions().size()) {
                        solved = true;
                        if (shortestPathRock != null) {
                            System.out.println("FOUND SHORTER PATH!");
                        }
                        shortestPathRock = currentBlock; // We found either the first answer or a shorter answer
                    }
                }
            }
            if (currentBlock.getPreviousPositions().size() <= maxMoves && (shortestPathRock == null || currentBlock.getPreviousPositions().size() < shortestPathRock.getPreviousPositions().size()) && !currentBlock.isDeadBlock()) {
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
            System.out.println("Total # Of: Rocks: " + rockCount + "\tBubbles: " + bubbleCount + "\tMoltens: " + moltenCount + "\tPortals: " + portalCount);
            numRocks = rockCount;
            if (!recreateMap) {
                printMap();
            }
            return true;
        } else {
            if (superDebug) {
                System.out.println("Could Not Solve Puzzle, Trying Again!");
            }
        }
        return false;
    }

    private void printMap() {
        HashMap<Coordinate, Direction> finalPathTaken = null;
        if (shortestPathRock != null) {
            finalPathTaken = shortestPathRock.getAllPreviousPositions();
        }

//        for(Map.Entry<Coordinate,Block> entry : map.entrySet()){
//            System.out.println(entry.getKey()+"\t"+entry.getValue().getBlockType());
//        }


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
                    if (map.get(coordinate) == null) {
                        System.out.print("N ");
                    } else {
                        System.out.print(map.get(coordinate).printMapObject() + " ");
                    }
                }
            }
            System.out.println("");
        }

        System.out.println("Regenerate Map With The Following:\n" + base64EncodeMap());
    }

    private int diff(int first, int second) {
        if (first > second) {
            return first - second;
        }
        return second - first;
    }

    private String base64EncodeMap() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < maxY; i++) {
            for (int a = 0; a < maxX; a++) {
                Coordinate coordinate = new Coordinate(a, i);
                sb.append("[").append(coordinate.printForEncoding()).append("|").append(map.get(coordinate).printMapObject()).append("]");
            }
        }
        byte[] bytes = Base64.getEncoder().encode(sb.toString().getBytes());
        return new String(bytes);
    }

    private void setMapFromString(String base64Encoded) {
        map = new HashMap<>();
        String decoded = new String(Base64.getDecoder().decode(base64Encoded.getBytes()));
        for (int i = 0; i < decoded.length(); i += 11) {
            String piece = decoded.substring(i, i + 11);
            Coordinate coordinate = new Coordinate(piece.substring(1, 8));
            Block block;
            switch (piece.substring(9, 10)) {
                case "*":
                    block = new EmptyBlock(coordinate);
                    break;
                case "B":
                    block = new BubbleBlock(coordinate);
                    break;
                case "R":
                    block = new RockBlock(coordinate);
                    break;
                case "W":
                    block = new FinishBlock(coordinate);
                    break;
                case "M":
                    block = new MoltenBlock(coordinate);
                    break;
                case "P":
                    block = new PortalBlock(coordinate);
                    break;
                default:
                    block = new EmptyBlock(coordinate);
                    break;
            }
            map.put(coordinate, block);
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
        } else if (nextBlock instanceof EmptyBlock) {
            Block secondNextBlock = getNextBlock(nextBlock, direction);
            if (secondNextBlock != null && secondNextBlock instanceof BubbleBlock) {
                if (block.getPreviousPositions().size() == ((BubbleBlock) secondNextBlock).turnPopped) {
                    return false; // Don't allow to go through bubble block right after popping
                }
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

        public abstract void onTouch(MovingBlock block);

        public abstract boolean canTravel(Direction direction);

        public abstract String getBlockType();

        public abstract String printMapObject();
    }

    class MovingBlock extends Block {
        private boolean deadBlock; // For like if you hit a molten block
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

        public boolean isDeadBlock() {
            return deadBlock;
        }

        public void setDeadBlock(boolean deadBlock) {
            this.deadBlock = deadBlock;
        }
    }

    class EmptyBlock extends Block {

        public EmptyBlock(Coordinate coordinate) {
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

        public FinishBlock(Coordinate coordinate) {
            setPosition(coordinate);
        }

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

        public RockBlock(Coordinate coordinate) {
            setPosition(coordinate);
        }

        @Override
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
            return "R";
        }
    }

    class BubbleBlock extends RockBlock {
        private int turnPopped = 0;
        private boolean popped = false;

        public BubbleBlock(Coordinate coordinate) {
            super(coordinate);
        }

        @Override
        public void onTouch(MovingBlock block) {
            turnPopped = block.getPreviousPositions().size() + 1;
            pop();
            switch (block.currentDirection) {
                case UP:
                    block.moveDown();
                    break;
                case DOWN:
                    block.moveUp();
                    break;
                case RIGHT:
                    block.moveRight();
                    break;
                case LEFT:
                    block.moveRight();
                    break;
            }
        }

        @Override
        public String getBlockType() {
            return "Bubble Block";
        }

        @Override
        public boolean canTravel(Direction direction) {
            return popped;
        }

        @Override
        public String printMapObject() {
            return "B";
        }

        public void pop() {
            this.popped = true;
        }
    }

    class MoltenBlock extends RockBlock {
        public MoltenBlock(Coordinate coordinate) {
            super(coordinate);
        }

        @Override
        public void onTouch(MovingBlock block) {
            block.setDeadBlock(true);
        }

        @Override
        public String getBlockType() {
            return "Molten Block";
        }

        @Override
        public boolean canTravel(Direction direction) {
            return false;
        }

        @Override
        public String printMapObject() {
            return "M";
        }
    }

    class PortalBlock extends RockBlock {
        private PortalBlock portalExit;

        public PortalBlock(Coordinate coordinate) {
            super(coordinate);
            setPosition(coordinate);
        }


        @Override
        public void onTouch(MovingBlock block) {
            block.setLastDirection(block.currentDirection);
            block.setPosition(portalExit.getPosition());
            switch (block.currentDirection) {
                case UP:
                    block.setCurrentDirection(Direction.DOWN);
                    block.moveDown();
                    break;
                case DOWN:
                    block.setCurrentDirection(Direction.UP);
                    block.moveUp();
                    break;
                case RIGHT:
                    block.setCurrentDirection(Direction.LEFT);
                    block.moveLeft();
                    break;
                case LEFT:
                    block.setCurrentDirection(Direction.RIGHT);
                    block.moveRight();
                    break;
            }
        }

        @Override
        public String getBlockType() {
            return "Portal Block";
        }

        @Override
        public boolean canTravel(Direction direction) {
            return false;
        }

        @Override
        public String printMapObject() {
            return "P";
        }

        public PortalBlock getPortalExit() {
            return portalExit;
        }

        public void setPortalExit(PortalBlock portalExit) {
            this.portalExit = portalExit;
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

        public Coordinate(String xyPair) {
            try {
                this.x = Integer.parseInt(xyPair.substring(1, 3));
                this.y = Integer.parseInt(xyPair.substring(4, 6));
            } catch (NumberFormatException e) {
                e.printStackTrace();
            }
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

        public String printForEncoding() {
            return "(" + (x >= 10 ? x : "0" + x) + "," + (y >= 10 ? y : "0" + y) + ")";
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
    private static final String drawFile = "=\"@drawable/";
    private static final String qm = "\"";
    private static final String tr = "=\"true";
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
    public static enum absoluteLayouts {

        CENT_VERT, CENT_HOR, A_PAR_RT, A_PAR_LT, A_PAR_TP, A_PAR_BT
    }

    //Relatively answered layouts based on another asset
    public static enum relativeLayouts {

        TO_LT_OF, TO_RT_OF, ALIGN_RT, ALIGN_TP, ALIGN_BT, ALIGN_LT, ABV, BLW, MGN_TP, MGN_BTM, MGN_LT, MGN_RT
    }

    //data about the asset
    public static enum assetData {

        ID, WDTH, HGTH, BKRND, PAR_WDTH, PAR_HGTH
    }

    //syntaxtually correct line up until variable name
    private static String fullAssetDataName(assetData assData) {
        switch (assData) {
            case ID:
                return idPrefix + idFile + "";
            case BKRND:
                return backgroundPrefix + drawFile + "";
            case WDTH:
                return layoutPrefix + "width" + dimenFile + constDimen;
            case HGTH:
                return layoutPrefix + "height" + dimenFile + constDimen;
            case PAR_WDTH:
                return layoutPrefix + "width" + constParent;
            case PAR_HGTH:
                return layoutPrefix + "height" + constParent;
            default:
                //should never happen
                return "error";
        }
    }

    //syntaxtually correct line up until variable name
    private static String fullRelativeName(relativeLayouts layout) {
        switch (layout) {
            case TO_LT_OF:
                return layoutPrefix + "toLeftOf" + idFile;
            case TO_RT_OF:
                return layoutPrefix + "toRightOf" + idFile;
            case ALIGN_RT:
                return layoutPrefix + "alignRight" + idFile;
            case ALIGN_LT:
                return layoutPrefix + "alignLeft" + idFile;
            case ALIGN_TP:
                return layoutPrefix + "alignTop" + idFile;
            case ALIGN_BT:
                return layoutPrefix + "alignBottom" + idFile;
            case ABV:
                return layoutPrefix + "above" + idFile;
            case BLW:
                return layoutPrefix + "below" + idFile;
            case MGN_TP:
                return layoutPrefix + "marginTop" + dimenFile;
            case MGN_BTM:
                return layoutPrefix + "marginBottom" + dimenFile;
            case MGN_LT:
                return layoutPrefix + "marginLeft" + dimenFile;
            case MGN_RT:
                return layoutPrefix + "marginRight" + dimenFile;
            default:
                //should never happen
                return "error";
        }
    }

    //syntaxtually correct line up until variable name
    private static String fullAbsoluteName(absoluteLayouts layout) {
        switch (layout) {
            case CENT_VERT:
                return layoutPrefix + "centerVertical";
            case CENT_HOR:
                return layoutPrefix + "centerHorizontal";
            case A_PAR_RT:
                return layoutPrefix + "alignParentRight";
            case A_PAR_LT:
                return layoutPrefix + "alignParentLeft";
            case A_PAR_TP:
                return layoutPrefix + "alignParentTop";
            case A_PAR_BT:
                return layoutPrefix + "alignParentBottom";
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

    private Stack<Coordinate> calculateRangeInner(Coordinate c) {
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
        return range;
    }

    private Stack<Coordinate> calculateRange(Coordinate c) {
        int x = c.getX();
        int y = c.getY();
        Stack<Coordinate> range = new Stack<Coordinate>();
        range.push(new Coordinate(x, y));
        range.push(new Coordinate(x + 1, y));
        range.push(new Coordinate(x - 1, y));
        range.push(new Coordinate(x, y + 1));
        range.push(new Coordinate(x, y - 1));
        return range;
    }
    
    private String getBaseBlockXML(Block base){
        String returnView = "";
        Coordinate c1 = new Coordinate(0,0);//top left
        Coordinate c2 = new Coordinate(maxX-1,maxY-1);//bottom right
        Coordinate c4 = new Coordinate(0, maxY - 1);//bottom left
        Coordinate c3 = new Coordinate(maxX - 1, 0);//top right
        Coordinate c5 = new Coordinate(0, midY);//mid left
        Coordinate c6 = new Coordinate(midX, 0);//mid top
        Coordinate c7 = new Coordinate(midX, maxY-1);//mid bottom
        Coordinate c8 = new Coordinate(maxX - 1, midY);//mid right
        Coordinate c9 = new Coordinate(midX, midY);//mid middle
        if(base.getPosition().equals(c1)){
            returnView += (fullAbsoluteName(absoluteLayouts.A_PAR_TP) + tr + qm + "\n");
            returnView += (fullAbsoluteName(absoluteLayouts.A_PAR_LT) + tr + qm + "\n");
        }else if(base.getPosition().equals(c2)){
            returnView += (fullAbsoluteName(absoluteLayouts.A_PAR_BT) + tr + qm + "\n");
            returnView += (fullAbsoluteName(absoluteLayouts.A_PAR_RT) + tr + qm + "\n");
        }else if(base.getPosition().equals(c3)){
            returnView += (fullAbsoluteName(absoluteLayouts.A_PAR_TP) + tr + qm + "\n");
            returnView += (fullAbsoluteName(absoluteLayouts.A_PAR_RT) + tr + qm + "\n");
        }else if(base.getPosition().equals(c4)){
            returnView += (fullAbsoluteName(absoluteLayouts.A_PAR_BT) + tr + qm + "\n");
            returnView += (fullAbsoluteName(absoluteLayouts.A_PAR_LT) + tr + qm + "\n");
        }else if(base.getPosition().equals(c5)){
            returnView += (fullAbsoluteName(absoluteLayouts.A_PAR_LT) + tr + qm + "\n");
            returnView += (fullAbsoluteName(absoluteLayouts.CENT_VERT) + tr + qm + "\n");
        }else if(base.getPosition().equals(c6)){
            returnView += (fullAbsoluteName(absoluteLayouts.A_PAR_TP) + tr + qm + "\n");
            returnView += (fullAbsoluteName(absoluteLayouts.CENT_HOR) + tr + qm + "\n");
        }else if(base.getPosition().equals(c7)){
            returnView += (fullAbsoluteName(absoluteLayouts.A_PAR_BT) + tr + qm + "\n");
            returnView += (fullAbsoluteName(absoluteLayouts.CENT_HOR) + tr + qm + "\n");
        }else if(base.getPosition().equals(c8)){
            returnView += (fullAbsoluteName(absoluteLayouts.A_PAR_RT) + tr + qm + "\n");
            returnView += (fullAbsoluteName(absoluteLayouts.CENT_VERT) + tr + qm + "\n");
        }else if(base.getPosition().equals(c9)){
            returnView += (fullAbsoluteName(absoluteLayouts.CENT_VERT) + tr + qm + "\n");
            returnView += (fullAbsoluteName(absoluteLayouts.CENT_HOR) + tr + qm + "\n");
        }
        return returnView;
    }

    private StringBuilder build(boolean debug) {
        //store xml block for runner because it needs to be appended at the end, but
        String runnerView = "";
        //Current block being written
        Block block = null;
        String view = "";
        //obstacle ID so each view block has their own identifier

        /**
         * ****************CONSTANT LOCATIONS***************** place all
         * constant blocks I.E. corners, middle vals...etc
         */
        block = map.get(new Coordinate(0, 0));//we know this one it real
        if (block instanceof Block) {//top left
            if(debug){System.out.println("Processing block: " + block.getPosition());}
            //block.setId(obId);//used for variable naming
            //obId++;
            block.setPlaced(true);
            block.setHorRef(block.getPosition());
            block.setVerRef(block.getPosition());
            if(debug){System.out.println("Placing block on map: " + block.getPosition());}
            ++numBlocksToWrite;
        }
        block = map.get(new Coordinate(maxX - 1, maxY - 1));
        if (block instanceof Block && !inPath(block.getPosition())) {//bottom right
            if(debug){System.out.println("Processing block: " + block.getPosition());}
            if (block instanceof EmptyBlock) {//otherwise, if this spot is an empty block ,lets place one there as an ancor!
                MoltenBlock constBlock = new MoltenBlock(block.getPosition());
                map.put(constBlock.getPosition(), constBlock);
                block = constBlock;
            }
            //block.setId(obId);//used for variable naming
            //obId++;
            block.setPlaced(true);
            block.setHorRef(block.getPosition());
            block.setVerRef(block.getPosition());
            if(debug){System.out.println("Placing block on map: " + block.getPosition());}
            ++numBlocksToWrite;
        }
        block = map.get(new Coordinate(0, maxY - 1));
        if (block instanceof Block && !inPath(block.getPosition())) {//top right
            if (block instanceof EmptyBlock) {
                MoltenBlock constBlock = new MoltenBlock(block.getPosition());
                map.put(constBlock.getPosition(), constBlock);
                block = constBlock;
            }
            if(debug){System.out.println("Processing block: " + block.getPosition());}
            //block.setId(obId);//used for variable naming
            //obId++;
            block.setPlaced(true);
            block.setHorRef(block.getPosition());
            block.setVerRef(block.getPosition());
            if(debug){System.out.println("Placing block on map: " + block.getPosition());}
            ++numBlocksToWrite;
        }
        block = map.get(new Coordinate(maxX - 1, 0));
        if (block instanceof Block && !(block instanceof EmptyBlock) && !inPath(block.getPosition())) {//bottom left
            if(debug){System.out.println("Processing block: " + block.getPosition());}
            if (block instanceof EmptyBlock) {
                MoltenBlock constBlock = new MoltenBlock(block.getPosition());
                map.put(constBlock.getPosition(), constBlock);
                block = constBlock;
            }
            //block.setId(obId);//used for variable naming
            //obId++;
            block.setPlaced(true);
            block.setHorRef(block.getPosition());
            block.setVerRef(block.getPosition());
            if(debug){System.out.println("Placing block on map: " + block.getPosition());}
            ++numBlocksToWrite;
        }
        //check centers left, right, up, down, and exact middle
        block = map.get(new Coordinate(0, midY));
        if (block instanceof Block && !inPath(block.getPosition())) {//middle left
            if(debug){System.out.println("Processing block: " + block.getPosition());}
            if (block instanceof EmptyBlock) {
                MoltenBlock constBlock = new MoltenBlock(block.getPosition());
                map.put(constBlock.getPosition(), constBlock);
                block = constBlock;
            }
            //block.setId(obId);//used for variable naming
            block.setPlaced(true);
            block.setHorRef(block.getPosition());
            block.setVerRef(block.getPosition());
            //obId++;
            if(debug){System.out.println("Processing block: " + block.getPosition());}
            ++numBlocksToWrite;
        }
        block = map.get(new Coordinate(midX, 0));
        if (block instanceof Block && !inPath(block.getPosition())) {//top middle
            if (block instanceof EmptyBlock) {
                MoltenBlock constBlock = new MoltenBlock(block.getPosition());
                map.put(constBlock.getPosition(), constBlock);
                block = constBlock;
            }
            if(debug){System.out.println("Processing block: " + block.getPosition());}
            //block.setId(obId);//used for variable naming
            //obId++;
            block.setPlaced(true);
            block.setHorRef(block.getPosition());
            block.setVerRef(block.getPosition());
            if(debug){System.out.println("Processing block: " + block.getPosition());}
            ++numBlocksToWrite;
        }
        block = map.get(new Coordinate(maxX - 1, midY));
        if (block instanceof Block && !inPath(block.getPosition())) {//middle right
            if(debug){System.out.println("Processing block: " + block.getPosition());}
            if (block instanceof EmptyBlock) {
                MoltenBlock constBlock = new MoltenBlock(block.getPosition());
                map.put(constBlock.getPosition(), constBlock);
                block = constBlock;
            }
            //block.setId(obId);//used for variable naming
            //obId++;
            block.setPlaced(true);
            block.setHorRef(block.getPosition());
            block.setVerRef(block.getPosition());
            if(debug){System.out.println("Processing block: " + block.getPosition());}
            ++numBlocksToWrite;
        }
        block = map.get(new Coordinate(0, midY));
        if (block instanceof Block && !inPath(block.getPosition())) {//middle left
            if(debug){System.out.println("Processing block: " + block.getPosition());}
            if (block instanceof EmptyBlock) {
                MoltenBlock constBlock = new MoltenBlock(block.getPosition());
                map.put(constBlock.getPosition(), constBlock);
                block = constBlock;
            }
            //block.setId(obId);//used for variable naming
            //obId++;
            block.setPlaced(true);
            block.setHorRef(block.getPosition());
            block.setVerRef(block.getPosition());
            if(debug){System.out.println("Processing block: " + block.getPosition());}
            ++numBlocksToWrite;
        }
        block = map.get(new Coordinate(midX, midY));
        if (block instanceof Block && !inPath(block.getPosition())) {//middle EXACTLY!!!!
            if (block instanceof EmptyBlock) {
                MoltenBlock constBlock = new MoltenBlock(block.getPosition());
                map.put(constBlock.getPosition(), constBlock);
                block = constBlock;
            }
            if(debug){System.out.println("Processing block: " + block.getPosition());}
            //block.setId(obId);//used for variable naming
            //obId++;
            block.setPlaced(true);
            block.setHorRef(block.getPosition());
            block.setVerRef(block.getPosition());
            if(debug){System.out.println("Processing block: " + block.getPosition());}
            ++numBlocksToWrite;

        }
        if(true){
            System.out.println("Before running placing algorithms the following blocks were placed!");
            for (Map.Entry<Coordinate, Block> ent : map.entrySet()) {
                if (ent.getValue() instanceof Block && !(ent.getValue() instanceof EmptyBlock) && ent.getValue().isPlaced()) {
                    System.out.println("Block:" + ent.getValue().getPosition() + " | Vref:" + ent.getValue().getVerRef() + " | Href:" + ent.getValue().getHorRef());
                }
            }
        }

        assignReferences(debug);
        assignIds();
        try {
            /**
             * ****************WRITING XML*****************
             */
            //constant view blockbefore block placement
            levelXML.append(viewStart + "\n");
            levelXML.append(fullAssetDataName(assetData.ID) + constGridName + qm + "\n");
            levelXML.append(fullAssetDataName(assetData.PAR_WDTH) + qm + "\n");
            levelXML.append(fullAssetDataName(assetData.PAR_HGTH) + qm + "\n");
            levelXML.append(viewEnd + "\n");
            view = "";//just clear for a new view block
            block = null;//just clear out for a new block
            //writtenBlocks <= numBlocksToWrite; 
            for (Map.Entry<Coordinate, Block> ent : map.entrySet()) {
                if(debug){System.out.println("attempting to write:" +ent.getKey());}
                if (ent.getValue() instanceof Block && ent.getValue() instanceof MovingBlock) {//we must save the runner for the very last view block appended
                    if(debug){System.out.println(""+ent.getKey() + " is the mover");}
                    block = ent.getValue();
                    runnerView += (viewStart + "\n");
                    runnerView += (fullAssetDataName(assetData.ID) + constRunner + qm + "\n");//variable name
                    runnerView += (fullAssetDataName(assetData.WDTH) + qm + "\n");//const width
                    runnerView += (fullAssetDataName(assetData.HGTH) + qm + "\n");//const height
                    runnerView += (fullAssetDataName(assetData.BKRND) + (fullAssetName(assets.P_DUD)) + qm + "\n");//const background with respect to the blocktype
                    runnerView += (viewEnd + "\n");
                } else if (ent.getValue() instanceof Block && !(ent.getValue() instanceof EmptyBlock) && ent.getValue().isPlaced()) {//we don't write any EmptyBlocks
                    if(debug){System.out.println("We need to write: "+ent.getKey());}
                    block = ent.getValue();
                    view += (viewStart + "\n");
                    view += (fullAssetDataName(assetData.ID) + (block instanceof MovingBlock ? (constRunner) : (block instanceof FinishBlock ? (constFinish) : (constObstacle + block.getId()))) + qm + "\n");//variable name
                    view += (fullAssetDataName(assetData.WDTH) + qm + "\n");//const width
                    view += (fullAssetDataName(assetData.HGTH) + qm + "\n");//const height
                    view += (fullAssetDataName(assetData.BKRND) + (block instanceof MovingBlock ? (fullAssetName(assets.P_DUD))
                            : (block instanceof FinishBlock ? (fullAssetName(assets.P_FIN))
                                    : (block instanceof MoltenBlock ? (fullAssetName(assets.P_MOLT))
                                            : (block instanceof PortalBlock ? (fullAssetName(assets.P_PORT))
                                                    : (block instanceof BubbleBlock ? (fullAssetName(assets.P_BUB))
                                                            : (fullAssetName(assets.P_OBST))))))) + qm + "\n");//const background with respect to the blocktype
                    //for the relative locations just start at 
                    //current block - reference < 0 its either left of the ref or above it
                    if (block.getPosition().equals(block.getHorRef()) && block.getPosition().equals(block.getVerRef())) {//if ALL the positions are the same then this is a constant base block
                        //returns constant base locs
                        view += getBaseBlockXML(block);
                    } else if((block.getPosition().equals(block.getHorRef()) && !block.getPosition().equals(block.getVerRef())) || (!block.getPosition().equals(block.getHorRef()) && block.getPosition().equals(block.getVerRef()))){
                    	//if the block references itself for one ref, but not the other, it's aligned to a wall and refs another block
                    	if(block.getPosition().equals(block.getHorRef())){
                    		if(block.getPosition().getX() == 0){
                                view += (fullAbsoluteName(absoluteLayouts.A_PAR_LT) + tr + qm + "\n");
                    		}else if(block.getPosition().getX() == maxX-1){
                                view += (fullAbsoluteName(absoluteLayouts.A_PAR_RT) + tr + qm + "\n");
                    		}else if(block.getPosition().getX() == midX){
                                view += (fullAbsoluteName(absoluteLayouts.CENT_HOR) + tr + qm + "\n");
                    		}
                    		
                    		Block vRef = map.get(block.getVerRef());
                    		if(block.getPosition().getY() < block.getVerRef().getY()){
                                view += (fullRelativeName(relativeLayouts.ABV) + (vRef instanceof MovingBlock ? (constRunner) : (vRef instanceof FinishBlock ? (constFinish) : (constObstacle + vRef.getId()))) + qm + "\n");
                    		}else if(block.getPosition().getY() > block.getVerRef().getY()){
                                view += (fullRelativeName(relativeLayouts.BLW) + (vRef instanceof MovingBlock ? (constRunner) : (vRef instanceof FinishBlock ? (constFinish) : (constObstacle + vRef.getId()))) + qm + "\n");
                    		}else{
                                view += (fullRelativeName(relativeLayouts.ALIGN_TP) + (vRef instanceof MovingBlock ? (constRunner) : (vRef instanceof FinishBlock ? (constFinish) : (constObstacle + vRef.getId()))) + qm + "\n");
                    		}
                
                    	}else if(block.getPosition().equals(block.getVerRef())){
                    		if(block.getPosition().getY() == 0){
                                view += (fullAbsoluteName(absoluteLayouts.A_PAR_TP) + tr + qm + "\n");
                    		}else if(block.getPosition().getY() == maxY-1){
                                view += (fullAbsoluteName(absoluteLayouts.A_PAR_BT) + tr + qm + "\n");
                    		}else if(block.getPosition().getY() == midY){
                                view += (fullAbsoluteName(absoluteLayouts.CENT_VERT) + tr + qm + "\n");
                    		}
                    		
                    		Block hRef = map.get(block.getHorRef());
                    		if(block.getPosition().getX() < block.getHorRef().getX()){
                                view += (fullRelativeName(relativeLayouts.TO_LT_OF) + (hRef instanceof MovingBlock ? (constRunner) : (hRef instanceof FinishBlock ? (constFinish) : (constObstacle + hRef.getId()))) + qm + "\n");
                    		}else if(block.getPosition().getX() > block.getHorRef().getX()){
                                view += (fullRelativeName(relativeLayouts.TO_RT_OF) + (hRef instanceof MovingBlock ? (constRunner) : (hRef instanceof FinishBlock ? (constFinish) : (constObstacle + hRef.getId()))) + qm + "\n");
                    		}else{
                                view += (fullRelativeName(relativeLayouts.ALIGN_LT) + (hRef instanceof MovingBlock ? (constRunner) : (hRef instanceof FinishBlock ? (constFinish) : (constObstacle + hRef.getId()))) + qm + "\n");
                    		}
                    	}
                  
                    }else if (block.getHorRef().equals(block.getVerRef())) {//if the horizontal and vertical references are the same coordinate we know this is a reference by cluster
                        Block ref = map.get(block.getHorRef());//arbitrairy since the same
                        //System.out.println("checking block: "+block.getPosition()+"blockID: " + block.getId() + " with ref"+ref.getPosition()+"refID:" + ref.getId());
                        int yDist = block.getPosition().getY() - ref.getPosition().getY();// block.getPosition().getY() - ref.getPosition().getY();
                        int xDist = block.getPosition().getX() - ref.getPosition().getX();//block.getPosition().getX() - ref.getPosition().getX();
                        //if either of the distances are 0 this means they either share the same X or the same Y plane
                        //System.out.println("xDist = " + xDist + " yDist = " + yDist);
                        if (xDist == 0 || yDist == 0) {
                            if (yDist == 0) {
                                if (xDist < 0) {//block is to the left of the reference
                                    //we know its left so put left but check if we need margin
                                    //--------HORIZONTAL POSITION----------------
                                        view += (fullRelativeName(relativeLayouts.TO_LT_OF) + (ref instanceof MovingBlock ? (constRunner) : (ref instanceof FinishBlock ? (constFinish) : (constObstacle + ref.getId()))) + qm + "\n");
                                    //--------VERTICAL POSITION----------------
                                        view += (fullRelativeName(relativeLayouts.ALIGN_TP) + (ref instanceof MovingBlock ? (constRunner) : (ref instanceof FinishBlock ? (constFinish) : (constObstacle + ref.getId()))) + qm + "\n");
                                    
                                    //--------MARGIN--------------
                                    if (Math.abs(xDist) == 2 && block.getPosition().getX() != 0) {
                                        view += (fullRelativeName(relativeLayouts.MGN_RT) + constDimen + qm + "\n");//margin right moves the block left
                                    }
                                } else if (xDist > 0) {//its to the right, it can never be equal
                                    //we know its right so put right but check if we need margin
                                    //--------HORIZONTAL POSITION----------------
                                        view += (fullRelativeName(relativeLayouts.TO_RT_OF) + (ref instanceof MovingBlock ? (constRunner) : (ref instanceof FinishBlock ? (constFinish) : (constObstacle + ref.getId()))) + qm + "\n");
                                  
                                    
                                    //--------VERTICAL POSITION----------------
                                        view += (fullRelativeName(relativeLayouts.ALIGN_TP) + (ref instanceof MovingBlock ? (constRunner) : (ref instanceof FinishBlock ? (constFinish) : (constObstacle + ref.getId()))) + qm + "\n");
                                    
                                    //--------MARGIN--------------
                                    if (Math.abs(yDist) == 2 && block.getPosition().getX() != maxX-1) {
                                        view += (fullRelativeName(relativeLayouts.MGN_LT) + constDimen + qm + "\n");//margin left moves the block right
                                    }
                                }else{
                                	System.out.println("xDist == 0 shouldn't be going here...");
                                	}
                                } else if(xDist == 0) {//not yDist ==0, must be xDist == 0
                                	if (yDist < 0) {//block is above  reference
                                    //--------VERTICAL POSITION----------------
                                        view += (fullRelativeName(relativeLayouts.ABV) + (ref instanceof MovingBlock ? (constRunner) : (ref instanceof FinishBlock ? (constFinish) : (constObstacle + ref.getId()))) + qm + "\n");
                                        
                                    
                                    //--------HORIZONTAL POSITION----------------
                                        view += (fullRelativeName(relativeLayouts.ALIGN_LT) + (ref instanceof MovingBlock ? (constRunner) : (ref instanceof FinishBlock ? (constFinish) : (constObstacle + ref.getId()))) + qm + "\n");
                                    
                                    //--------MARGIN--------------
                                    if (Math.abs(yDist) == 2 && block.getPosition().getY() != 0) {
                                        view += (fullRelativeName(relativeLayouts.MGN_BTM) + constDimen + qm + "\n");//margin bottom moves the block up
                                    }
                                } else if(yDist > 0){//its below, it can never be equal
                                    //--------VERTICAL POSITION----------------
                                        view += (fullRelativeName(relativeLayouts.BLW) + (ref instanceof MovingBlock ? (constRunner) : (ref instanceof FinishBlock ? (constFinish) : (constObstacle + ref.getId()))) + qm + "\n");
                                    
                                    //--------HORIZONTAL POSITION----------------
                                        view += (fullRelativeName(relativeLayouts.ALIGN_LT) + (ref instanceof MovingBlock ? (constRunner) : (ref instanceof FinishBlock ? (constFinish) : (constObstacle + ref.getId()))) + qm + "\n");
                                    
                                    //--------MARGIN--------------
                                    if (Math.abs(yDist) == 2 && block.getPosition().getY() != maxY-1) {
                                        view += (fullRelativeName(relativeLayouts.MGN_TP) + constDimen + qm + "\n");//margin top moves the block down
                                    }
                                }else{
                                	System.out.println("yDist == 0, shouldn't be happening...");
                                }
                            }
                        } else {//this means we're in some diagonal direction
                            //xDist < 0 means current block is left of reference, likewise xDist > 0 means current block is right of reference
                            //yDist < 0 means current block is above reference, likewise yDist > 0 means current block is below reference
                            if (xDist < 0) {//block is to the left of the reference
                                //we know its left so put left but check if we need margin
                                //--------HORIZONTAL POSITION----------------
                                    view += (fullRelativeName(relativeLayouts.TO_LT_OF) + (ref instanceof MovingBlock ? (constRunner) : (ref instanceof FinishBlock ? (constFinish) : (constObstacle + ref.getId()))) + qm + "\n");
                                //--------MARGIN--------------
                                if (Math.abs(xDist) == 2 && block.getPosition().getX() != 0) {
                                    view += (fullRelativeName(relativeLayouts.MGN_RT) + constDimen + qm + "\n");//margin right moves the block left
                                }
                            } else if(xDist > 0) {//its to the right, it can never be equal
                                    view += (fullRelativeName(relativeLayouts.TO_RT_OF) + (ref instanceof MovingBlock ? (constRunner) : (ref instanceof FinishBlock ? (constFinish) : (constObstacle + ref.getId()))) + qm + "\n");
                                //--------MARGIN--------------
                                if (Math.abs(xDist) == 2 && block.getPosition().getX() != maxX-1) {
                                    view += (fullRelativeName(relativeLayouts.MGN_LT) + constDimen + qm + "\n");//margin left moves the block right
                                }
                            }else{
                            	System.out.println("xDist == 0 2nd time, should not be a thing...");
                            }
                            if (yDist < 0) {//block is above  reference
                                //--------VERTICAL POSITION----------------
                                    view += (fullRelativeName(relativeLayouts.ABV) + (ref instanceof MovingBlock ? (constRunner) : (ref instanceof FinishBlock ? (constFinish) : (constObstacle + ref.getId()))) + qm + "\n");
                                //--------MARGIN--------------
                                if (Math.abs(yDist) == 2 && block.getPosition().getY() != 0) {
                                    view += (fullRelativeName(relativeLayouts.MGN_BTM) + constDimen + qm + "\n");//margin bottom moves the block up
                                }
                            } else if(yDist > 0) {//its below, it can never be equal
                                //--------VERTICAL POSITION----------------
                                    view += (fullRelativeName(relativeLayouts.BLW) + (ref instanceof MovingBlock ? (constRunner) : (ref instanceof FinishBlock ? (constFinish) : (constObstacle + ref.getId()))) + qm + "\n");
                                //--------MARGIN--------------
                                if (Math.abs(yDist) == 2 && block.getPosition().getY() != maxY-1) {
                                    view += (fullRelativeName(relativeLayouts.MGN_TP) + constDimen + qm + "\n");//margin top moves the block down
                                }
                            }else{
                            	System.out.println("yDist == 0 2nd time, should not be a thing...");
                            }
                        }
                    } else {//this means (by process of elimination) that the blocks use align to two different blocks
                        //Vertical references are above, below, alignTiop
                        //Horizontal references are toRightOf, toLeftOf, alignLeft
                        Block refV = map.get(block.getVerRef());
                        Block refH = map.get(block.getHorRef());
                        if(debug){System.out.println("Attempting to reference block : " + block.getPosition() + " vertically with: " + block.getVerRef() + " and horizontally with: "+block.getHorRef());}
                        //first set the vertical align to find below, above, alignTop
                        if (block.getPosition().getY() == refV.getPosition().getY()) {
                            //(arbitrary so choose top always
                                view += (fullRelativeName(relativeLayouts.ALIGN_TP) + (refV instanceof MovingBlock ? (constRunner) : (refV instanceof FinishBlock ? (constFinish) : (constObstacle + refV.getId()))) + qm + "\n");
                        } else {
                        	//if block Y < ref Y, block is above ref, else below
                                view += (fullRelativeName((block.getPosition().getY() < refV.getPosition().getY()) ? relativeLayouts.ABV : relativeLayouts.BLW) + (refV instanceof MovingBlock ? (constRunner) : (refV instanceof FinishBlock ? (constFinish) : (constObstacle + refV.getId()))) + qm + "\n");
                        }
                        if (block.getPosition().getX() == refH.getPosition().getX()) {
                            //arbitrarily choose alignLeft
                            view += (fullRelativeName(relativeLayouts.ALIGN_LT) + (refH instanceof MovingBlock ? (constRunner) : (refH instanceof FinishBlock ? (constFinish) : (constObstacle + refH.getId()))) + qm + "\n");
                        } else {
                             view += (fullRelativeName((block.getPosition().getX() < refH.getPosition().getX()) ? relativeLayouts.TO_LT_OF : relativeLayouts.TO_RT_OF) + (refH instanceof MovingBlock ? (constRunner) : (refH instanceof FinishBlock ? (constFinish) : (constObstacle + refH.getId()))) + qm + "\n");
                        }
                    }
                    //we handle const bases, clusters, and aligns. There is nothing else but to finish off the view block, append it and repeat
                    view += (viewEnd + "\n");
                    levelXML.append(view);
                    view = "";
                }
            }
            levelXML.append(runnerView);
            //after all is said and done print out all blocks that got placed
                //System.out.println("Displaying all placed blocks and references in format\nBlock:x | Vref:v | Href:h");
                if(debug){for (Map.Entry<Coordinate, Block> ent : map.entrySet()) {
                    if (ent.getValue() instanceof Block && !(ent.getValue() instanceof EmptyBlock)) {
                    System.out.println("Block:" + ent.getValue().getPosition() +"Block ID:"+ent.getValue().getId()+ " | Vref:" + ent.getValue().getVerRef() + "Vref ID:"+ map.get(ent.getValue().getVerRef()).getId() +" | Href:" + ent.getValue().getHorRef()+ "Href ID:"+ map.get(ent.getValue().getHorRef()).getId());
                    }
                }}
        } catch (NullPointerException npe) {
            if(debug){System.out.println("XML so far!\n\n" + levelXML.toString());}
            System.out.println("Error: " + npe.getMessage());
            npe.printStackTrace();
            System.exit(1);
        } catch (Exception e) {
            if(debug){System.out.println("XML so far!\n\n" + levelXML.toString());}
            System.out.println("Unknown Error: " + e.getMessage());
            e.printStackTrace();
            System.exit(1);
        }
        return levelXML;
    }
    private void assignIds() {
        int obId = 1;
        for (Map.Entry<Coordinate, Block> ent : map.entrySet()) {
            if (ent.getValue() instanceof Block && !(ent.getValue() instanceof EmptyBlock)) {
                ent.getValue().setId(obId);
                ++obId;
            }
        }
    }

    //This is for all the little blocks that were unplacable due to a lack of valid references - but it is not the end for these brave souls!
    private void applyPadding(Block unplacable, boolean debug) {
        //we need a horizontal and a vertical reference
        HashMap<Coordinate, Block> refs = validRefs(unplacable.getPosition(), 3, true);
        boolean vPlaced = false;
        boolean hPlaced = false;
        if (refs.size() > 1) {//we want it to be two every time
            MoltenBlock newBlock = null;
            MoltenBlock newBlock2 = null;
            for (Map.Entry<Coordinate, Block> ent : refs.entrySet()) {
                //at most it will only have two object. hopefully it will have 2 objects always -__-
                Block ref = ent.getValue();
                if (!vPlaced && unplacable.getPosition().getX() == ref.getPosition().getX()) {//if the x's are the same vertical ref
                    //System.out.println("setting reference:"+ref.getPosition());
                    map.get(unplacable.getPosition()).setVerRef(map.get(ref.getPosition()).getPosition());
                    vPlaced = true;
                } else if (!hPlaced && unplacable.getPosition().getY() == ref.getPosition().getY()) {
                    //System.out.println("setting ref: " + ref.getPosition());
                    map.get(unplacable.getPosition()).setHorRef(map.get(ref.getPosition()).getPosition());
                    hPlaced = true;
                } else {
                    //uhhoh. If its not one of the above we have bigger problems.
                    //System.out.println("could not set!");
                }
                if (hPlaced && vPlaced) {
                    map.get(unplacable.getPosition()).setPlaced(true);
                    //unplacable.setId(obId);//used for variable naming
                    //obId++;
                    ++numBlocksToWrite;
                    newBlock = new MoltenBlock(new Coordinate(map.get(unplacable.getPosition()).getVerRef().getX(), map.get(unplacable.getPosition()).getVerRef().getY()));
                    map.put(newBlock.getPosition(), newBlock);
                    newBlock2 = new MoltenBlock(new Coordinate(map.get(unplacable.getPosition()).getHorRef().getX(), map.get(unplacable.getPosition()).getHorRef().getY()));
                    map.put(newBlock2.getPosition(), newBlock2);
                    break;
                }
            }
            //now get valid refs for each of the new blocks and call it a day!
            for (int j = 0; j < 2; ++j) {
                Block b = map.get((j == 0 ? newBlock : newBlock2).getPosition());
                HashMap<Coordinate, Block> refs2 = validRefs(b.getPosition(), 1, debug);
                if (refs2.size() > 1) {
                    Coordinate horRef = null;
                    Coordinate vertRef = null;
                    boolean hFound = false;
                    boolean vFound = false;
                    int vertref = (map.get(b.getPosition()).getPosition().getX());
                    int horref = (map.get(b.getPosition()).getPosition().getY());
                    for (Map.Entry<Coordinate, Block> entry : refs2.entrySet()) {
                        Coordinate co = map.get(entry.getKey()).getPosition();
                        Block refBlock = map.get(entry.getValue().getPosition());
                        if(debug){System.out.println("Checking reference: " + co);}
                        //make sure refBlock isn't also referencing this block : !(co.equals(refBlock.getVer/HorRef())) <-- if it is don't use it because of cyclicness
                        if (!vFound && !(co.equals(refBlock.getVerRef())) && (co.getX() == vertref || co.getX() == vertref - 1 || co.getX() == vertref + 1)) {//this is the vertical reference, store the block and move on, can be inline or offset by one
                            if(debug){System.out.println("storing vertical reference: " + co);}
                            vertRef = map.get(entry.getKey()).getPosition();
                            vFound = true;
                        } else if (!hFound && !(co.equals(refBlock.getHorRef())) && (co.getY() == horref || co.getY() == horref - 1 || co.getY() == horref + 1)) {//this is the horizontal reference, store the block and move on can be inline, or offset by 1
                            if(debug){System.out.println("storing vertical reference: " + co);}
                            horRef = map.get(entry.getKey()).getPosition();
                            hFound = true;
                        }
                        if (vFound && hFound) {
                            map.get(b.getPosition()).setPlaced(true);
                            //b.setId(obId);//used for variable naming
                            //obId++;
                            ++numBlocksToWrite;
                            map.get(b.getPosition()).setVerRef(map.get(vertRef).getPosition());
                            map.get(b.getPosition()).setHorRef(map.get(horRef).getPosition());
                            break;//no need to check the others
                        }
                    }
                }
            }
        } else {
            //check center and corners
            System.out.println("Padding FAILURE occurred - this block "+unplacable.getPosition()+"is impossible and should be deleted...");
        }

    }

    private void assignReferences(boolean debug) {
        int placeAllPossibleBlocks = 0;
        int runTimes = 5;
        //its possible some blocks were skipped so we need to retrace 5 times to make sure all blocks are placed
        ArrayList<Coordinate> priorityCoords = new ArrayList<Coordinate>();
        priorityCoords.add(new Coordinate(0, 0));
        priorityCoords.add(new Coordinate(0, midY));
        priorityCoords.add(new Coordinate(0, maxY-1));
        priorityCoords.add(new Coordinate(midX, 0));
        priorityCoords.add(new Coordinate(maxX-1, 0));
        priorityCoords.add(new Coordinate(maxX-1, maxY-1));
        priorityCoords.add(new Coordinate(midX, midY));
        if(map.get(new Coordinate(midX,midY)) instanceof Block && !(map.get(new Coordinate(midX,midY)) instanceof EmptyBlock) && map.get(new Coordinate(midX,midY)).isPlaced()){
        	//cluster around center
        	priorityCoords.add(new Coordinate(midX-1,midY-1));
        	priorityCoords.add(new Coordinate(midX,midY-1));
        	priorityCoords.add(new Coordinate(midX+1,midY-1));
        	priorityCoords.add(new Coordinate(midX-1,midY));
        	priorityCoords.add(new Coordinate(midX+1,midY));
        	priorityCoords.add(new Coordinate(midX-1,midY+1));
        	priorityCoords.add(new Coordinate(midX,midY+1));
        	priorityCoords.add(new Coordinate(midX+1,midY+1));
        }
        //cluster around corners
        if(map.get(new Coordinate(0,0)) instanceof Block && !(map.get(new Coordinate(0,0)) instanceof EmptyBlock) && map.get(new Coordinate(0,0)).isPlaced()){
        	//top left
        	priorityCoords.add(new Coordinate(1,0));
        	priorityCoords.add(new Coordinate(0,1));
        	priorityCoords.add(new Coordinate(1,1));
        }
        if(map.get(new Coordinate(maxX-1,0)) instanceof Block && !(map.get(new Coordinate(maxX-1,0)) instanceof EmptyBlock) && map.get(new Coordinate(maxX-1,0)).isPlaced()){
        	//top right
        	priorityCoords.add(new Coordinate(maxX-2,0));
        	priorityCoords.add(new Coordinate(maxX-1,1));
        	priorityCoords.add(new Coordinate(maxX-2,1));
        }
        if(map.get(new Coordinate(0,maxY-1)) instanceof Block && !(map.get(new Coordinate(0,maxY-1)) instanceof EmptyBlock) && map.get(new Coordinate(0,maxY-1)).isPlaced()){
        	//bottom left
        	priorityCoords.add(new Coordinate(0,maxY-2));
        	priorityCoords.add(new Coordinate(1,maxY-1));
        	priorityCoords.add(new Coordinate(1,maxY-1));
        }
        if(map.get(new Coordinate(maxX-1,maxY-1)) instanceof Block && !(map.get(new Coordinate(maxX-1,maxY-1)) instanceof EmptyBlock) && map.get(new Coordinate(maxX-1,maxY-1)).isPlaced()){
        	//bottom right
        	priorityCoords.add(new Coordinate(maxX-1,maxY-2));
        	priorityCoords.add(new Coordinate(maxX-2,maxY-1));
        	priorityCoords.add(new Coordinate(maxX-1,maxY-2));
        }
        //all center_vertical
        for(int i = 0; i < maxX; i ++){
        	if(!priorityCoords.contains(new Coordinate(i, midY))){
                priorityCoords.add(new Coordinate(i, midY));        
        	}
        }
      //all center_horizontal
        for(int i = 0; i < maxY; i ++){
        	if(!priorityCoords.contains(new Coordinate(midX, i))){
                priorityCoords.add(new Coordinate(midY, i));        
        	}
        }
      //all along top and bottom edges
        for(int i = 0; i < maxX; i ++){
        	if(!priorityCoords.contains(new Coordinate(i, 0))){
                priorityCoords.add(new Coordinate(i, 0));        
        	}
        	if(!priorityCoords.contains(new Coordinate(i, maxY-1))){
                priorityCoords.add(new Coordinate(i, maxY-1));        
        	}
        }
        //all along right and left edges
        for(int i = 0; i < maxY; i ++){
        	if(!priorityCoords.contains(new Coordinate(0, i))){
                priorityCoords.add(new Coordinate(0, i));        
        	}
        	if(!priorityCoords.contains(new Coordinate(maxX-1, i))){
                priorityCoords.add(new Coordinate(maxX-1, i));        
        	}
        }
      //all 1 from top/bottom
        for(int i = 0; i < maxX; i ++){
        	if(!priorityCoords.contains(new Coordinate(i, 1))){
                priorityCoords.add(new Coordinate(i, 1));        
        	}
        	if(!priorityCoords.contains(new Coordinate(i, maxY-2))){
                priorityCoords.add(new Coordinate(i, maxY-2));        
        	}
        }
      //all 1 from sides
        for(int i = 0; i < maxY; i ++){
        	if(!priorityCoords.contains(new Coordinate(1, i))){
                priorityCoords.add(new Coordinate(1, i));        
        	}
        	if(!priorityCoords.contains(new Coordinate(maxX-2, i))){
                priorityCoords.add(new Coordinate(maxX-2, i));        
        	}
        }
        //rest
        for(int i = 0; i < maxY; i ++){
        	for(int j = 0; j < maxX; j++){
        		Coordinate tempCoord = new Coordinate(j, i);
        		if(!priorityCoords.contains(tempCoord)){
        			priorityCoords.add(tempCoord);
        		}
        	}
        }
        System.out.println("priorityCoords.size() = "+priorityCoords.size());





    	/*
        while(numBlocksToWrite < 6){
        	for(int i = 0; i < baseCoords.size(); i++){
        		if(!inPath(baseCoords.get(i)) && !map.get(baseCoords.get(i)).isPlaced()){
        			Block newBlock = new RockBlock(baseCoords.get(i));
        			map.put(baseCoords.get(i), newBlock);
        			map.get(newBlock.getPosition()).setPlaced(true);
        			numBlocksToWrite++;
        			System.out.println("creating new block @ ("+baseCoords.get(i).getX()+","+baseCoords.get(i).getY()+") ");
        		}
        		if(numBlocksToWrite > 5) {
        			System.out.println("numBlocks > 5, breaking");
        			break;
        		}
        	}
        }
        */
        while (placeAllPossibleBlocks < runTimes) {
            Block toPlaceBlock;
            //traverse left to right top to bottom
            int timesRun = 0;
            int runThreshhold = 30;
            while(timesRun < runThreshhold){
            	System.out.println("numBlocksToWrite = "+numBlocksToWrite);
            	System.out.println("Iterating through grid time "+timesRun);
            	for(int i = 0; i < priorityCoords.size(); ++i){
            			Coordinate currentCoord = priorityCoords.get(i);
            			Block currentBlock = map.get(currentCoord);
            			if((currentBlock instanceof Block) && !(currentBlock instanceof EmptyBlock) && !currentBlock.isPlaced()){
            				ArrayList<Block> horizontalRefs = getHorizontalReferences(currentCoord, false);
            				ArrayList<Block> verticalRefs = getVerticalReferences(currentCoord, false);

            				if(currentBlock.getHorRef() == null){
            					if(isAgainstWall("horizontal",currentCoord)){
            						map.get(currentBlock.getPosition()).setHorRef(currentCoord);
            						System.out.println("Block @ ("+currentCoord.getX()+","+currentCoord.getY()+"), ID = "+currentBlock.getId()+" set h ref to self");
            					}else if(horizontalRefs.size() > 0){
            						Coordinate horRef = map.get(horizontalRefs.get(0).getPosition()).getPosition();
            						map.get(currentBlock.getPosition()).setHorRef(map.get(horizontalRefs.get(0).getPosition()).getPosition());
            						System.out.println("Block @ ("+currentCoord.getX()+","+currentCoord.getY()+") set horizontal ref to ("+horRef.getX()+","+horRef.getY()+") , ID = "+map.get(horRef).getId());
            					}
            				}
            				if(currentBlock.getVerRef() == null){
            					if(isAgainstWall("vertical",currentCoord)){
            						map.get(currentBlock.getPosition()).setVerRef(currentCoord);
            						System.out.println("Block @ ("+currentCoord.getX()+","+currentCoord.getY()+"), ID = "+currentBlock.getId()+" set v ref to self");
            					}else if(verticalRefs.size() > 0){
            						Coordinate verRef = map.get(verticalRefs.get(0).getPosition()).getPosition();
            						map.get(currentBlock.getPosition()).setVerRef(map.get(verticalRefs.get(0).getPosition()).getPosition());
            						System.out.println("Block @ ("+currentCoord.getX()+","+currentCoord.getY()+") set v ref to ("+verRef.getX()+","+verRef.getY()+") , ID = "+map.get(verRef).getId());
            					}
            				}
            				if(currentBlock.getVerRef() != null && currentBlock.getHorRef() != null){
            					System.out.println("Block @ ("+currentCoord.getX()+","+currentCoord.getY()+"), ID = "+currentBlock.getId()+" setting placed to true");
            					map.get(currentBlock.getPosition()).setPlaced(true);
            					numBlocksToWrite++;
            				}
            			}
            		
            		}
            	 timesRun++;
            }/*
            for (int i = 0; i < maxY; ++i) {//increments rows
                for (int j = 0; j < maxX; ++j) {//increments columns
                    Coordinate curCoord = new Coordinate(j, i);
                    Block refBlock = map.get(curCoord);
                    if (refBlock instanceof Block && !(refBlock instanceof EmptyBlock) && (refBlock.isPlaced())) {//only process those blocks placed looking for all unplaced blocks in their range
                        HashMap<Coordinate, Block> refs = validRefs(refBlock.getPosition(), 2, debug);
                        if (refs.size() > 0) {//this is your captain speaking. We have liftoff.
                            if(debug){System.out.println("assigning singular block references");}
                            for (Map.Entry<Coordinate, Block> ent : refs.entrySet()) {
                                toPlaceBlock = map.get(ent.getValue().getPosition());
                                if(debug){System.out.println("processing block: " + toPlaceBlock.getPosition() + " trying to reference: " + refBlock.getPosition());}
                                //toPlaceBlock.setId(obId);//used for variable naming
                                //obId++;
                                map.get(toPlaceBlock.getPosition()).setPlaced(true);
                                ++numBlocksToWrite;
                                if(debug){System.out.println("toPlaceBlock.isPlaced(): " + toPlaceBlock.isPlaced());}
                                if(debug){System.out.println("map.get(toPlaceBlock.getPosition()).isPlaced(): " + map.get(toPlaceBlock.getPosition()).isPlaced());}
                                if (!(refBlock.getPosition().equals(toPlaceBlock.getVerRef()))) {
                                    if(debug){System.out.println("PLACING vert and hor the same since singular: " + refBlock.getPosition());}
                                    map.get(toPlaceBlock.getPosition()).setVerRef(map.get(refBlock.getPosition()).getPosition());
                                    map.get(toPlaceBlock.getPosition()).setHorRef(map.get(refBlock.getPosition()).getPosition());
                                }
                            }
                        }
                    }
                }
            }
            */
            if(true){
                System.out.println("Blocks placed after cluster runthrough: " + (placeAllPossibleBlocks + 1));
                for (Map.Entry<Coordinate, Block> ent : map.entrySet()) {
                    if (ent.getValue() instanceof Block && !(ent.getValue() instanceof EmptyBlock) && ent.getValue().isPlaced()) {
                        System.out.println("Block:" + ent.getValue().getPosition() + " | Vref:" + ent.getValue().getVerRef() + " | Href:" + ent.getValue().getHorRef());
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
            /*
            for (int i = 0; i < maxY; ++i) {//increments rows
                for (int j = 0; j < maxX; ++j) {//increments columns
                    Coordinate curCoord = new Coordinate(j, i);
                    Block blockToPlace = map.get(curCoord);
                    if (blockToPlace instanceof Block && !(blockToPlace instanceof EmptyBlock) && !(blockToPlace.isPlaced())) {//only process those blocks unplaced, looking for those placed blocks to use as refs
                        //we use a NON placed block as the 
                        HashMap<Coordinate, Block> refs = validRefs(blockToPlace.getPosition(), 1, debug);
                        if(debug){System.out.println("trying to assign bi-block references for block: "+blockToPlace.getPosition()+"!\nrefs.size() = " + refs.size());}
                        if (refs.size() > 1) {//need at least two blocks if not in the immediate range
                            Coordinate horRef = null;
                            Coordinate vertRef = null;
                            boolean hFound = false;
                            boolean vFound = false;
                            int vertref = 0;
                            int horref = 0;
                            for (Map.Entry<Coordinate, Block> ent : refs.entrySet()) {
                                Block refBlock = map.get(ent.getValue().getPosition());
                                vertref = refBlock.getPosition().getX();
                                horref = refBlock.getPosition().getY();
                                if(debug){System.out.println("Checking reference: " + refBlock.getPosition() + " for block: " + blockToPlace.getPosition());}
                                //make sure the block were trying to place isnt already one of the blocks references <-- if it is don't use it because of cyclicness
                                if (!vFound && !(blockToPlace.getPosition().equals(refBlock.getVerRef())) && (blockToPlace.getPosition().getX() == vertref || blockToPlace.getPosition().getX() == vertref - 1 || blockToPlace.getPosition().getX() == vertref + 1)) {//this is the vertical reference, store the block and move on, can be inline or offset by one
                                    if(debug){System.out.println("storing vertical reference: " + refBlock.getPosition());}
                                    vertRef = refBlock.getPosition();
                                    vFound = true;
                                }
                                if (!hFound && !(blockToPlace.getPosition().equals(refBlock.getHorRef())) && (blockToPlace.getPosition().getY() == horref || blockToPlace.getPosition().getY() == horref - 1 || blockToPlace.getPosition().getY() == horref + 1)) {//this is the horizontal reference, store the block and move on can be inline, or offset by 1
                                    if(debug){System.out.println("storing hor reference: " + refBlock.getPosition());}
                                    horRef = refBlock.getPosition();
                                    hFound = true;
                                }
                                if (vFound && hFound) {
                                    //System.out.println("setting placed to true for block: " + blockToPlace.getPosition());
                                    map.get(blockToPlace.getPosition()).setPlaced(true);
                                    //blockToPlace.setId(obId);
                                    //++obId;
                                    map.get(blockToPlace.getPosition()).setVerRef(map.get(vertRef).getPosition());
                                    map.get(blockToPlace.getPosition()).setHorRef(map.get(horRef).getPosition());
                                    break;//no need to check the others
                                }
                            }
                        }
                    }
                }
            }
            */
            ++placeAllPossibleBlocks;
            if(debug){System.out.println("Retracing map for the " + placeAllPossibleBlocks + " time");}
            if (placeAllPossibleBlocks >= runTimes) {
                    //if we have traversed from (0,0) to (w-1,h-1) 5+ times this means we've placed all the blocks we can. 
                //The blocks that are left and unreachable and must be placed using the padding but retrace once eachtime we pad!
                boolean breakout = true;
                for (Map.Entry<Coordinate, Block> ent : map.entrySet()) {
                    if (ent.getValue() instanceof Block && !(ent.getValue() instanceof EmptyBlock) && !ent.getValue().isPlaced()) {
                        if(debug){System.out.println("Applying padding for block:" + ent.getKey());}
                        breakout = false;
                        applyPadding(ent.getValue(), debug);
                        --placeAllPossibleBlocks;
                        break;
                    }
                }
                if (breakout) {
                    break;
                }
            }
        }
        if(true){
            System.out.println("Displaying blocks and references in format\nBlock:x | Vref:v | Href:h");
            for (Map.Entry<Coordinate, Block> ent : map.entrySet()) {
                if (ent.getValue() instanceof Block && !(ent.getValue() instanceof EmptyBlock)) {
                    System.out.println("Block:" + ent.getValue().getPosition() + " | Vref:" + ent.getValue().getVerRef() + " | Href:" + ent.getValue().getHorRef());
                }
            }
        }
        //now all blocks on the map have references
    }
    private boolean isAgainstWall(String direction, Coordinate coord){
    	boolean againstWall = false;
    	if(direction.equals("vertical")){
    		if(coord.getY() == 0 || coord.getY() == midY || coord.getY() == maxY-1){
    			againstWall = true;
    		}
    	}else if(direction.equals("horizontal")){
    		if(coord.getX() == 0 || coord.getX() == midX || coord.getX() == maxX-1){
    			againstWall = true;
    		}
    	}
    	return againstWall;
    }
    private ArrayList<Block> getVerticalReferences(Object coordinate, boolean debug) {
        HashMap<Coordinate, Block> verticalRefs = new HashMap<Coordinate, Block>();
        Coordinate coord = null;
        Block blockToPlace = map.get(coordinate);
        if (coordinate instanceof Coordinate) {
            coord = (Coordinate) coordinate;
        } else {
            return null;//show not happen but just in case
        }
        if(debug){System.out.println("Getting valid vertical refs for block: " + map.get(coord).getId());}
        for (int i = 0; i < maxX; i++) {//iterate from 0 to maxX, grabbing all above, below, alignTop
        	for(int j = -1; j < 2; j++){//add -1, 0, 1 to coordX to grab above and below of block as well
        		Coordinate co = new Coordinate(i, coord.getY()+j);
        		if (isValidCoord(co)) {
        			if (map.get(co) instanceof Block) {
        				if (!(map.get(co) instanceof EmptyBlock)) {
        					if(map.get(co).isPlaced()){
        						if (!inPath(co)) {
        							if(!(blockToPlace.getPosition().equals(map.get(co).getVerRef()))){
        									verticalRefs.put(map.get(co).getPosition(), map.get(co));
        							}
        							break;
        						}
        					}
        				}
        			}
        		}
            }
        }
        return prioritizeReferences("vertical",verticalRefs,false);
    }
    private ArrayList<Block> getHorizontalReferences(Object coordinate, boolean debug) {
        HashMap<Coordinate, Block> horizontalRefs = new HashMap<Coordinate, Block>();
        Coordinate coord = null;
        Block blockToPlace = map.get(coordinate);
        if (coordinate instanceof Coordinate) {
            coord = (Coordinate) coordinate;
        } else {
            return null;//show not happen but just in case
        }
        if(false){System.out.println("Getting valid horizontal refs for block: " + map.get(coord).getId());}
        for (int i = 0; i < maxY; i++) {//iterate from 0 to maxY, grabbing all toLeft, toRight, alignLeft
        	for(int j = -1; j < 2; j++){//add -1, 0, 1 to coordX to grab toLeft and toRight of block as well
        		Coordinate co = new Coordinate(coord.getX()+j, i);
        		if (isValidCoord(co)) {
        			if (map.get(co) instanceof Block) {
        				if (!(map.get(co) instanceof EmptyBlock)) {
        					if(map.get(co).isPlaced()){
        						if (!inPath(co)) {
        							if(!(blockToPlace.getPosition().equals(map.get(co).getHorRef()))){
        									horizontalRefs.put(map.get(co).getPosition(), map.get(co));
        							}
        							break;
        						}
        					}
        				}
        			}
        		}
            }
        }
        return prioritizeReferences("horizontal",horizontalRefs,false);
    }
    
    private ArrayList<Block> prioritizeReferences(String refType,HashMap<Coordinate, Block> refs, boolean debug) {
    	ArrayList<Coordinate> prioritizedCoords = new ArrayList<>();
    	ArrayList<Coordinate> coords = new ArrayList<Coordinate>(refs.keySet());
    	ArrayList<Coordinate> baseCoords = new ArrayList<>();
    	baseCoords.add(new Coordinate(0, 0));
    	baseCoords.add(new Coordinate(0, midY));
    	baseCoords.add(new Coordinate(0, maxY-1));
    	baseCoords.add(new Coordinate(midX, 0));
    	baseCoords.add(new Coordinate(midX, midY));
    	baseCoords.add(new Coordinate(midX, maxY-1));
    	baseCoords.add(new Coordinate(maxX-1, 0));
    	baseCoords.add(new Coordinate(maxX-1, midY));
    	baseCoords.add(new Coordinate(maxX-1, maxY-1));
    	for(int i = 0; i < coords.size(); i++){
			if(baseCoords.contains(coords.get(i)) && !prioritizedCoords.contains(coords.get(i))){
				prioritizedCoords.add(coords.get(i));
			}
		}
    	if(refType.equals("horizontal")){
    		for(int i = 0; i < coords.size(); i++){
    			if((coords.get(i).getY() == 0 || coords.get(i).getY() == midY || coords.get(i).getY() == maxY-1) && !prioritizedCoords.contains(coords.get(i))){
    				prioritizedCoords.add(coords.get(i));
    			}
    		}
    	}else if(refType.equals("vertical")){
    		for(int i = 0; i < coords.size(); i++){
    			if((coords.get(i).getX() == 0 || coords.get(i).getX() == midX || coords.get(i).getX() == maxX-1) && !prioritizedCoords.contains(coords.get(i))){
    				prioritizedCoords.add(coords.get(i));
    			}
    		}
    	}
    	for(Coordinate rest: coords){
			if(!prioritizedCoords.contains(rest)){
				prioritizedCoords.add(rest);
			}
		}; 
		ArrayList<Block> prioritizedReferences = new ArrayList<Block>(0);
    	for(int i = 0; i < prioritizedCoords.size(); i++){
    		prioritizedReferences.add(map.get(prioritizedCoords.get(i)));
    	}
    	return prioritizedReferences;
    }

    //Generates a list of all blocks within range of a given coordinate 
    // get the first block already placed  in the coordinates (x+1, y^) (x+1, yv) (x-1, y^) (x-1, yv) (<x, y+1) (<x, y+1) (x>, y-1) (x>, y-1)
    private HashMap<Coordinate, Block> validRefs(Object coordinate, int version, boolean debug) {
        HashMap<Coordinate, Block> validrefs = new HashMap<Coordinate, Block>();
        Coordinate coord = null;
        if (coordinate instanceof Coordinate) {
            coord = (Coordinate) coordinate;
        } else {
            return null;//show not happen but just in case
        }
        if(debug){System.out.println("Getting valid refs for block: " + coord);}
        Stack<Coordinate> range = (version == 1 ? calculateRange(coord) : calculateRangeInner(coord));
        int useX = 0;
        if (version == 1) {
            if(debug){System.out.println("using version 1");}
            /*
                    range.push(new Coordinate(x, y));
        range.push(new Coordinate(x + 1, y));
        range.push(new Coordinate(x - 1, y));
        range.push(new Coordinate(x, y + 1));
        range.push(new Coordinate(x, y - 1));
            */
            Coordinate co2 = new Coordinate(coord.getX(), coord.getY());
            //traverse all x's up
            //x, x-1, x+1
            for (int i = 0; i < 3; ++i) {
                while (co2.getY() > 0) {
                    co2.setY(co2.getY() - 1);
                    if (debug) {
                        System.out.println("checking : " + co2.toString());
                    }
                    if (isValidCoord(co2)) {
                        //if(debug){System.out.println("valid!");}
                        if (map.get(co2) instanceof Block) {
                            //if(debug){System.out.println("im a block!");}
                            if (!(map.get(co2) instanceof EmptyBlock)) {
                                //if(debug){System.out.println("not empty!");}
                                if (map.get(co2).isPlaced()) {//ONLY grab placed items
                                    if (debug) {
                                        System.out.println("placing it!");
                                    }
                                    validrefs.put(map.get(co2).getPosition(), map.get(co2));
                                }
                            }
                        }
                    }
                }
                if (i == 1) {
                    co2 = new Coordinate(coord.getX() + 1, coord.getY());
                } else if (i == 2) {
                    co2 = new Coordinate(coord.getX() - 1, coord.getY());
                }
            }
            //traverse all x's down
            for (int i = 0; i < 3; ++i) {
                while (co2.getY() < maxY - 1) {
                    co2.setY(co2.getY() + 1);
                    if (debug) {
                        System.out.println("checking : " + co2.toString());
                    }
                    if (isValidCoord(co2)) {
                        //if(debug){System.out.println("valid!");}
                        if (map.get(co2) instanceof Block) {
                            //if(debug){System.out.println("im a block!");}
                            if (!(map.get(co2) instanceof EmptyBlock)) {
                                //if(debug){System.out.println("not empty!");}
                                if (map.get(co2).isPlaced()) {//ONLY grab placed items
                                    if (debug) {
                                        System.out.println("placing it!");
                                    }
                                    validrefs.put(map.get(co2).getPosition(), map.get(co2));
                                }
                            }
                        }
                    }
                }
                if (i == 1) {
                    co2 = new Coordinate(coord.getX() + 1, coord.getY());
                } else if (i == 2) {
                    co2 = new Coordinate(coord.getX() - 1, coord.getY());
                }
            }
            //traverse all y left
            co2 = new Coordinate(coord.getX(), coord.getY());
            for (int i = 0; i < 3; ++i) {
                while (co2.getX() > 0) {
                    co2.setX(co2.getX() - 1);
                    if (debug) {
                        System.out.println("checking : " + co2.toString());
                    }
                    if (isValidCoord(co2)) {
                        //if(debug){System.out.println("valid!");}
                        if (map.get(co2) instanceof Block) {
                            //if(debug){System.out.println("im a block!");}
                            if (!(map.get(co2) instanceof EmptyBlock)) {
                                //if(debug){System.out.println("not empty!");}
                                if (map.get(co2).isPlaced()) {//ONLY grab placed items
                                    if (debug) {
                                        System.out.println("placing it!");
                                    }
                                    validrefs.put(map.get(co2).getPosition(), map.get(co2));
                                }
                            }
                        }
                    }
                }
                if (i == 1) {
                    co2 = new Coordinate(coord.getX(), coord.getY() + 1);
                } else if (i == 2) {
                    co2 = new Coordinate(coord.getX(), coord.getY() - 1);
                }
            }
            //traverse all y right
            co2 = new Coordinate(coord.getX(), coord.getY());
            for (int i = 0; i < 3; ++i) {
                while (co2.getX() < maxX - 1) {
                    co2.setX(co2.getX() + 1);
                    if (debug) {
                        System.out.println("checking : " + co2.toString());
                    }
                    if (isValidCoord(co2)) {
                        //if(debug){System.out.println("valid!");}
                        if (map.get(co2) instanceof Block) {
                            //if(debug){System.out.println("im a block!");}
                            if (!(map.get(co2) instanceof EmptyBlock)) {
                                //if(debug){System.out.println("not empty!");}
                                if (map.get(co2).isPlaced()) {//ONLY grab placed items
                                    if (debug) {
                                        System.out.println("placing it!");
                                    }
                                    validrefs.put(map.get(co2).getPosition(), map.get(co2));
                                }
                            }
                        }
                    }
                    if (i == 1) {
                        co2 = new Coordinate(coord.getX(), coord.getY() + 1);
                    } else if (i == 2) {
                        co2 = new Coordinate(coord.getX(), coord.getY() - 1);
                    }
                }              
            }
        } else if (version == 2) {
            if(debug){System.out.println("using version 2");}
            while (!range.isEmpty()) {
                Coordinate co = range.pop();
                //System.out.println("checking : " + co.toString());
                if (isValidCoord(co)) {
                    //System.out.println("valid!");
                    if (map.get(co) instanceof Block) {
                        //System.out.println("im a block!");
                        if (!(map.get(co) instanceof EmptyBlock)) {
                            if (!map.get(co).isPlaced()) {//ONLY grab non placed items
                                if(debug){System.out.println("not empty!");System.out.println("placing it!");}
                                validrefs.put(map.get(co).getPosition(), map.get(co));
                            }
                        }
                    }
                }
            }
        } else {
            //if(true){System.out.println("using version 3");}
            boolean vFound = false;
            boolean hFound = false;
            //verticals
            if (!vFound) {
                for (int i = coord.getX(); i >= 0; --i) {//check left first
                    Coordinate co = new Coordinate(i, coord.getY());
                   // System.out.println("checking : " + co.toString());
                    if (isValidCoord(co)) {
                        //System.out.println("valid!");
                        if (map.get(co) instanceof Block) {
                            //System.out.println("im a block!");
                            if ((map.get(co) instanceof EmptyBlock)) {
                                if (!inPath(co)) {
                                   // if(true){System.out.println("is empty and not in the path!");System.out.println("placing it!");}
                                    validrefs.put(map.get(co).getPosition(), map.get(co));
                                    vFound = true;
                                    break;
                                }
                            }
                        }
                    }
                }
            }
            if (!vFound) {
                for (int i = coord.getX(); i < maxX-1; ++i) {//check right first
                    Coordinate co = new Coordinate(i, coord.getY());
                   // System.out.println("checking : " + co.toString());
                    if (isValidCoord(co)) {
                        //System.out.println("valid!");
                        if (map.get(co) instanceof Block) {
                            //System.out.println("im a block!");
                            if ((map.get(co) instanceof EmptyBlock)) {
                                if (!inPath(co)) {
                                    //if(true){System.out.println("is empty and notin the path!");System.out.println("placing it!");}
                                    validrefs.put(map.get(co).getPosition(), map.get(co));
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
                                    //if(true){System.out.println("is empty and not in the path!");System.out.println("placing it!");}
                                    validrefs.put(map.get(co).getPosition(), map.get(co));
                                    hFound = true;
                                    break;
                                }
                            }
                        }
                    }
                }
            }
            if (!hFound) {
                for (int i = coord.getY(); i < maxY-1; ++i) {//check down first
                    Coordinate co = new Coordinate(coord.getX(), i);
                    //System.out.println("checking : " + co.toString());
                    if (isValidCoord(co)) {
                        //System.out.println("valid!");
                        if (map.get(co) instanceof Block) {
                            //System.out.println("im a block!");
                            if ((map.get(co) instanceof EmptyBlock)) {
                                if (!inPath(co)) {
                                   // if(true){System.out.println("is empty and not in the path!");System.out.println("placing it!");}
                                    validrefs.put(map.get(co).getPosition(), map.get(co));
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

    private boolean inPath(Coordinate co) {
        if (shortestPathRock.getAllPreviousPositions().containsKey(co)) {
            return true;//obviously because its one of the actual blocks
        }
        Coordinate cBefore;
        Coordinate cAfter;
        cBefore = new Coordinate(0,0);
        for (int i = 1; i < shortestPathRock.getPreviousPositions().size() - 1; i++) {
            cAfter = shortestPathRock.getPreviousPositions().get(i);
            if (co.getX() == cBefore.getX() && co.getX() == cAfter.getX()) {//if the x's are the same that means the path is vertical and check if co.y is between before and after
                if (co.getY() >= cBefore.getY()) {//we're coming down the mountain
                    if (co.getY() <= cAfter.getY()) {
                        return true;
                    }
                } else {//cannot be equal or it would have already returned true
                    if (co.getY() <= cBefore.getY()) {//we're going up the mountain
                        if (co.getY() >= cAfter.getY()) {
                            return true;
                        }
                    }
                }
            } else if (co.getY() == cBefore.getY() && co.getY() == cAfter.getY()) {
                if (co.getX() >= cBefore.getX()) {//we're coming down the mountain
                    if (co.getX() <= cAfter.getX()) {
                        return true;
                    }
                } else {//cannot be equal or it would have already returned true
                    if (co.getX() <= cBefore.getX()) {//we're going up the mountain
                        if (co.getX() >= cAfter.getX()) {
                            return true;
                        }
                    }
                }

            }
            cBefore = cAfter;
        }
        //if we made it all the way through the path return false because we are not in the path
        return false;
    }

    private boolean isValidCoord(Coordinate c) {
        return ((c.getX() < maxX && c.getX() >= 0 && c.getY() < maxY && c.getY() >= 0) && (!inPath(c)));
    }
    
    
    public class XMLCreator {
    
    private File level;

    public File getLevel() {
        return level;
    }

    public void setLevel(File level) {
        level = level;
    }
    
    
    public XMLCreator(StringBuilder xml, String outputFileName, File templateXML, int levelNum){
        level = populateLevelFile(xml, outputFileName, templateXML, levelNum);
    }
    
    //create a new directory using outputFilename (exempt of the extension obviously), create new file in that dir, and write fullxml to it
    private File populateLevelFile(StringBuilder xml, String outputFileName, File templateXML, int levelNum) {
        File newLevel = null;
        try {
            String fContent = parseAndInject(xml, outputFileName, templateXML);
            if (fContent.length() == 0 || fContent.equals("")) {
                throw new IllegalStateException();
            }
            

            //File levelsDir = new File("/Users/dewit/Documents/shift_files/level_files");
            File levelsDir = new File("C:\\Users\\Christian\\Documents\\TestGame\\app\\src\\main\\res\\layout\\");
            File newLevelDir = new File(levelsDir.getAbsolutePath() + "/" + outputFileName);
            if (!levelsDir.exists()) {//it should always exist..
                throw new IOException();//directory storing all levels does not exist?! O_O
            }
            //removed for lucky 8
            /*
            if (!newLevelDir.exists()) {
                if (!newLevelDir.mkdir()) {
                    throw new IOException();//making a directory failed
                }
            }
            */
            while((new File(levelsDir.getAbsolutePath() + "/" + outputFileName + (""+levelNum+"") +".xml")).exists()){
                ++levelNum;
            }
            newLevel = new File(levelsDir.getAbsolutePath() + "/" + outputFileName + (""+levelNum+"") +".xml");
            FileWriter fw = new FileWriter(newLevel.getAbsoluteFile());
            BufferedWriter bw = new BufferedWriter(fw);
            bw.write(fContent);
            bw.close();
        } catch (IOException io) {
            io.printStackTrace();
        } catch (IllegalStateException is) {
            is.printStackTrace();
        } catch(Exception e){
            //something ELSE went wrong..
            e.printStackTrace();
        }
        return newLevel != null ? newLevel : null;
    }
    
    //read through the given xml template until we reach the spot to add out level then continue filling in the rest and return the entire file contents
    private String parseAndInject(StringBuilder xml, String outputFileName, File templateXML) {
        StringBuilder fullFile = null;
        try {
            fullFile = new StringBuilder();
            if (templateXML == null) {
                throw new NullPointerException();//given xml template doesn't exist... -___-
            }
            try (Scanner scanner = new Scanner(templateXML)) {
                while (scanner.hasNextLine()) {
                    String line = scanner.nextLine();
                    if (line.contains("~")) {//DELIMETER! - inject xml
                        fullFile.append(xml.toString());
                    } else {//otherwise keep writing the file as normal
                        fullFile.append((line + "\n"));
                    }
                }
                scanner.close();
            } catch (IOException ioe) {
                throw ioe;
            }
        } catch (IOException io) {
            io.printStackTrace();
        } catch (NullPointerException np) {
            np.printStackTrace();
        } catch (Exception e) {
            //something ELSE went wrong..
            e.printStackTrace();
        }
        return fullFile != null ? fullFile.toString() : "";
    }
}
    
    
    
}
