
package algorithms;

/**
 *
 * @author dewit
 */
/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;
import java.util.logging.Level;
import java.util.logging.Logger;

public class SolverV2 implements Runnable {

    private HashMap<Coordinate, Block> map;
    private ArrayList<RefBlock> references;
    private MovingBlock shortestPathRock;
    private static List<Long> execTimes = Collections.synchronizedList(new ArrayList<Long>());
    private static boolean foundGoodMap = false;
    private static boolean recreateMap = false;
    private static int startX = 0;
    private static int startY = 0;
    private static int maxX = 11;
    private static int maxY = 16;
    private static int midX = (maxX - 1) / 2;
    private static int midY = (maxY - 1) / 2;
    private static int minMoves = 5;
    private static int maxMoves = 8;
    private static int retryCount = 0;
    private int numRocks;
    private int numBlocksToWrite = 0;
    private static int levelNum = 1;
    private String visualDisplay = "";
    private String encodedMap = "";
    private static String levelGenre = "default";//right now its hardcoded
    private final File templateXML = new File("/Users/dewit/Documents/shift_files/level_files/level_template/pack_layout_template.xml");//the path is relative to my comp atm, but it will be hardcoded in the future nonetheless
    private final File fileDir = new File("/Users/dewit/Desktop/");
    //private final File templateXML = new File("C:\\Users\\Christian\\Documents\\TestGame\\app\\src\\main\\res\\layout\\pack_layout_template.xml");//the path is relative to my comp atm, but it will be hardcoded in the future nonetheless
    //private final File templateDir = new File("C:\\Users\\Christian\\Documents\\TestGame\\app\\src\\main\\res\\layout\\");
    //private final File templateXML = new File("/Users/nrichardson/Desktop/builder/pack_layout_template.xml");//the path is relative to my comp atm, but it will be hardcoded in the future nonetheless
    //private final File templateDir = new File("/Users/nrichardson/Desktop/builder/");

    private static boolean superDebug = false;
    private static boolean showRowAndColumnNumbers = true;
    private static boolean showPath = true;
    private static boolean forcePortalPassThrough = false; // FORCE at least 1 portal to exist and be USED on the map


    private static double minBlockDensity = .025;
    private static double maxBlockDensity = .25;
    private static double totalBlockDensity = 0; // going to be randomly between the two variables above
    private static double rockDensity = 0;
    private static double bubbleDensity = 0;
    private static double moltenDensity = 0;
    private static double breakableDensity = 0;
    private static double iceDensity = 0;
    private static double portalDensity = 0;

    private static int rockCount = 0;
    private static int bubbleCount = 0;
    private static int moltenCount = 0;
    private static int breakableCount = 0;
    private static int iceCount = 0;
    private static int portalCount = 0;

    private enum Direction {
        UP, DOWN, LEFT, RIGHT
    }

    public static void main(String[] args) {
        Long totalTime = System.currentTimeMillis();
        if (args.length > 0) {
            for (String arg : args) {
                if (arg.contains("=")) {
                    String[] split = arg.split("=");
                    double num = 0;
                    try {
                        if (!split[0].equals("recreate") && !split[0].equals("pack")) {
                            num = Double.parseDouble(split[1]);
                        }
                    } catch (NumberFormatException e) {
                        e.printStackTrace();
                    }
                    switch (split[0]) {
                        case "minDensity":
                            minBlockDensity = num;
                            break;
                        case "maxDensity":
                            maxBlockDensity = num;
                            break;
                        case "rocks":
                            rockDensity = num;
                            break;
                        case "bubbles":
                            bubbleDensity = num;
                            break;
                        case "moltens":
                            moltenDensity = num;
                            break;
                        case "breakables":
                            breakableDensity = num;
                            break;
                        case "ice":
                            iceDensity = num;
                            break;
                        case "portals":
                            portalDensity = num;
                            break;
                        case "pack":
                            levelGenre = split[1].trim();
                            break;
                        case "minMoves":
                            minMoves = (int) num;
                            break;
                        case "maxMoves":
                            maxMoves = (int) num;
                        case "recreate":
                            SolverV2 s = new SolverV2();
                            recreateMap = true;
                            s.setMapFromString(split[1].trim());
                            s.createAndSolve();
                            s.printMap();
                            s.createXmlFiles();
                            return;
                        default:
                            System.out.println("Unsupported Option! \"" + arg + "\"");
                    }
                }
            }
        }
        Solver s = new Solver();
        s.run();
        if (breakableDensity > 0) {
            s.findBlocksHitMultipleTimes();
        }
        s.createXmlFiles();
        long total = 0;
        for (Long t : execTimes) {
            total += t;
        }
        System.out.println("Total Time Spent Looking For Map = " + ((System.currentTimeMillis() - totalTime) / 1000.00) + " seconds");
        if (!recreateMap) {
            System.out.println("Average Time Per Map For Creation/Solve = " + (total / execTimes.size()) + "ms");
        }

    }
    
    public void createStringFiles(){
        String std = "S";
        String bubble = "b";
        String molten = "M";
        String frozen = "f";
        String breakable = "B";
        String finish = "F";
        String dude = "D";
        String portal = "P";
        int pc = 1;
        String empty = "-";
        String delim = "|";
        StringBuilder sb = new StringBuilder();
        String [][] stringMap = new String[11][16];
        //initialize everything to empty spaces for a clear board
        for(int i  = 0; i < 16; ++i){
            for(int j = 0; j < 11; ++j){
                stringMap[j][i] = empty;
            }
        }
        /*
        |-----------|-----------|-----------|-----------|-----------|-----------|-----------|-----------|-----------|-----------|-----------|-----------|-----------|-----------|-----------|-----------|-----------|
        */
        //for each entry in the map starting top left to bottom right, literally either print just a - or a corresponding letter
        for(int i = 0; i < 16; ++i){//row
            for(int j = 0; j < 11; ++j){//col
                Block b = map.get(new Coordinate(j, i));
                stringMap[j][i] = (b instanceof RockBlock ? std : (b instanceof MoltenBlock ? molten : b instanceof IceBlock ? frozen : (b instanceof MovingBlock ? dude : (b instanceof FinishBlock ? finish : (b instanceof BreakableBlock ? breakable : (b instanceof BubbleBlock ? bubble : (b instanceof PortalBlock ? portal+pc : empty)))))));
                if(b instanceof PortalBlock){
                    stringMap[((PortalBlock)b).portalExit.getX()][((PortalBlock)b).portalExit.getY()] = portal+pc;
                    ++pc;
                }
            }
        }
        sb.append(delim);
        for(int i  = 0; i < 16; ++i){
            for(int j = 0; j < 11; ++j){
                sb.append(stringMap[j][i]);
            }
            sb.append(delim);
        }
        sb.append("\n"+generatePath());
        System.out.println("String generated below:\n\n" + sb.toString());
    }
    
    public String generatePath(){
    ArrayList<Coordinate> positions = shortestPathRock.getPreviousPositions();
            String path = "";
            for (int i = positions.size() - 1; i > 0; i--) {
                Coordinate hintCoord = positions.get(i);
                Coordinate lastPosition = positions.get(i - 1);
                String direction = getHintDirection(lastPosition, hintCoord);
                path = (direction.equals("up") ? "1" : (direction.equals("right") ? "2" : (direction.equals("down") ? "3" : "4")))+path;
            }
            return path.toString();
    }

    public void createXmlFiles() {
        // Put the start guy on the map now!
        MovingBlock startBlock = new MovingBlock();
        Coordinate startPosition = new Coordinate(startX, startY);
        startBlock.setPosition(startPosition);
        map.put(startPosition, startBlock);
        createStringFiles();
    }

    public SolverV2() {
    }

    public void run() {
        do {
            long start = System.currentTimeMillis();
            totalBlockDensity = ThreadLocalRandom.current().nextDouble(minBlockDensity, maxBlockDensity);
            createAndSolve();
            long diff = System.currentTimeMillis() - start;
            execTimes.add(diff);
        } while (!foundGoodMap);
    }

    public void findBlocksHitMultipleTimes() {
        int replacedCount = 0;
        HashMap<RockBlock, Integer> hits = new HashMap<>();
        for (Map.Entry<Coordinate, Direction> previousPositions : shortestPathRock.getAllPreviousPositions().entrySet()) {
            Block nextBlock = getNextBlock(map.get(previousPositions.getKey()), previousPositions.getValue());
            if (nextBlock != null && nextBlock.getClass().getName().equals("algorithms.Solver$RockBlock")) {
                if (hits.containsKey(nextBlock)) {
                    Integer hitCount = hits.get(nextBlock);
                    hits.put((RockBlock) nextBlock, ++hitCount);
                } else {
                    hits.put((RockBlock) nextBlock, 1);
                }
            }
        }
        for (Map.Entry<RockBlock, Integer> hit : hits.entrySet()) {
            if (hit.getValue() <= 3) {
                replacedCount++;
                map.put(hit.getKey().getPosition(), new BreakableBlock(hit.getKey().getPosition()));
            }
        }
        System.out.println("Map With Breakables (Replaced " + replacedCount + " Rocks W/Breakables): ");
        printMap();
    }

    public boolean createAndSolve() {
        if (!recreateMap) {
            map = new HashMap<>();
            breakableCount = 0;
            rockCount = 0;
            bubbleCount = 0;
            moltenCount = 0;
            iceCount = 0;
            portalCount = 0;
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

        if (!recreateMap) {
            for (int i = 0; i < maxY; i++) { // Generate 11x15 map
                for (int a = 0; a < maxX; a++) {
                    Coordinate currentCoordinate = new Coordinate(a, i);
                    if (map.containsKey(currentCoordinate) && map.get(currentCoordinate) instanceof RockBlock) {
                        continue; // We previously placed a portal block here, don't overwrite it!
                    }
                    if (i == startY && a == startX) {
                        map.put(currentCoordinate, new EmptyBlock(currentCoordinate)); // Don't put something where the start block goes
                        continue;
                    }
                    placeBlock(currentCoordinate);
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

        if (!recreateMap) {
            Coordinate finishCoordinate = new Coordinate(finishXAxis, finishYAxis);
            FinishBlock finishBlock = new FinishBlock(finishCoordinate);
            map.put(finishCoordinate, finishBlock);

            boolean moveX = (Math.random() * 100 > 50);

            Coordinate finishRockCoordinate = new Coordinate(finishXAxis + (moveX ? finishXRockBlockPlusMinus : 0), finishYAxis + (moveX ? 0 : finishYRockBlockPlusMinus));
            RockBlock rockBlock = new RockBlock(finishRockCoordinate); // Put a solid rock next to the win block always
            map.put(finishRockCoordinate, rockBlock);
            rockCount++;

            startX = ThreadLocalRandom.current().nextInt(0, maxX - 1);
            startY = ThreadLocalRandom.current().nextInt(0, maxY - 1);
        }

        for (Direction direction : Direction.values()) {
            MovingBlock movingBlock = new MovingBlock();
            movingBlock.setPosition(new Coordinate(startX, startY));
            movingBlock.setCurrentDirection(direction);
            movingBlock.savePreviousPosition();
            moveQueue.add(movingBlock);
        }

        do {
            MovingBlock currentBlock = moveQueue.remove();
            currentBlock.move();

            Block nextBlock = getNextBlock(currentBlock, currentBlock.currentDirection);
            if (nextBlock != null) {
                if (superDebug) {
                    System.out.println("Type of next block: " + nextBlock.getBlockType());
                }
                nextBlock.onTouch(currentBlock);
            }
            if (map.get(currentBlock.getPosition()) instanceof FinishBlock) { // Currently sitting on the finish block
                if (!recreateMap && (currentBlock.getPreviousPositions().size() < (minMoves + 1) || currentBlock.isDeadBlock())) { // Make sure it didn't like end up in a molten next to finish block
                    return false; // We will try whole process again because it was too easy
                }
                if (shortestPathRock == null || currentBlock.getPreviousPositions().size() < shortestPathRock.getPreviousPositions().size()) {
                    solved = true;
                    MovingBlock copiedBlock = new MovingBlock();
                    currentBlock.copy(copiedBlock);
                    shortestPathRock = copiedBlock; // We found either the first answer or a shorter answer
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

        if (!recreateMap && forcePortalPassThrough && (portalCount == 0 || (shortestPathRock == null || !shortestPathRock.isPassedThroughPortal()))) { // If we force portals, make sure we passed through at least 1
            return false;
        }
        if (solved) {
            foundGoodMap = true;
            System.out.println(); // Finish the counter off
            System.out.println("DONE IN " + (shortestPathRock.getPreviousPositions().size() - 1) + " Moves!"); // One less move than you really took!
            for (int i = 0; i < shortestPathRock.getPreviousPositions().size() - 1; i++) {
                System.out.println("Went To " + shortestPathRock.getPreviousPositions().get(i + 1));
            }
            System.out.println("Total # Of: Rocks: " + rockCount + "\tBubbles: " + bubbleCount + "\tMoltens: " + moltenCount + "\tBreakables: " + breakableCount + "\tPortals: " + portalCount + "\tIce: " + iceCount);
            numRocks = rockCount;
            printMap();

            return true;
        } else {
            if (superDebug) {
                System.out.println("Could Not Solve Puzzle, Trying Again!");
            }
        }
        return false;
    }

    private void placeBlock(Coordinate coordinate) {
        /*
        Block placement logic is as follows:
        You pass in a total block density first. Say .3 means cover the whole board in about 30% of blocks. This is achieved by passing in "density=.3" to the command line
        Then you pass in each individual block type's density
        Say, I want about 50% of blocks placed to Rock Blocks, I pass in "rocks=.5" as a command line argument
        Say I want 10% of blocks placed to be Portal Blocks, I pass in "portals=.1" as a command line argument
        Any block I FAIL to pass in a density for, it defaults to 0. Say I pass in "portals=.2 moltens=.1 bubbles=.4", it will auto make RockBlock density be .3 (1.0 minus all others)
         */
        if (rockDensity + bubbleDensity + moltenDensity + portalDensity + iceDensity < 1.0) {
            rockDensity = 1.0 - (bubbleDensity + moltenDensity + portalDensity + iceDensity);
        }
        double rockStart = 0.0;
        double rockEnd = rockDensity;
        double bubbleStart = rockEnd;
        double bubbleEnd = bubbleStart + bubbleDensity;
        double moltenStart = bubbleEnd;
        double moltenEnd = moltenStart + moltenDensity;
        double breakableStart = moltenEnd;
        double breakableEnd = breakableStart;//breakableStart + breakableDensity; //Should NEVER generate here now!
        double portalStart = breakableEnd;
        double portalEnd = portalStart + portalDensity;
        double iceStart = portalEnd;
        double iceEnd = iceStart + iceDensity;
        double randBlockNum = Math.random(); // WHICH block to place

        double randTotalNum = Math.random(); // WHETHER or not we should place a block at all

        if (inRange(0, totalBlockDensity, randTotalNum)) {
            if (inRange(rockStart, rockEnd, randBlockNum)) {
                RockBlock rockBlock = new RockBlock(coordinate);
                map.put(coordinate, rockBlock);
                rockCount++;
            } else if (inRange(bubbleStart, bubbleEnd, randBlockNum)) {
                BubbleBlock bubbleBlock = new BubbleBlock(coordinate);
                map.put(coordinate, bubbleBlock);
                bubbleCount++;
            } else if (inRange(moltenStart, moltenEnd, randBlockNum)) {
                MoltenBlock moltenBlock = new MoltenBlock(coordinate);
                map.put(coordinate, moltenBlock);
                moltenCount++;
            } else if (inRange(breakableStart, breakableEnd, randBlockNum)) {
                BreakableBlock breakableBlock = new BreakableBlock(coordinate);
                map.put(coordinate, breakableBlock);
                breakableCount++;
            } else if (inRange(portalStart, portalEnd, randBlockNum)) {
                int randomX = ThreadLocalRandom.current().nextInt(1, maxX - 1);
                int randomY = ThreadLocalRandom.current().nextInt(1, maxY - 1);

                // Make sure they are at least a little bit away from each other & make sure we don't happen to get the exact same spot (very unlikely)
                while ((randomX == coordinate.getX() && randomY == coordinate.getY()) || diff(coordinate.getX(), randomX) < 3 || diff(coordinate.getY(), randomY) < 4 || map.get(new Coordinate(randomX,randomY)) instanceof PortalBlock) {
                    randomX = ThreadLocalRandom.current().nextInt(1, maxX - 1);
                    randomY = ThreadLocalRandom.current().nextInt(1, maxY - 1);
                }
                Coordinate randomPortalBlockCoordinate = new Coordinate(randomX, randomY);
                PortalBlock portalBlockCompanion = new PortalBlock(randomPortalBlockCoordinate);

                PortalBlock portalBlock = new PortalBlock(coordinate);

                portalBlockCompanion.setPortalExit(coordinate);
                portalBlock.setPortalExit(randomPortalBlockCoordinate);

                map.put(coordinate, portalBlock);
                map.put(randomPortalBlockCoordinate, portalBlockCompanion);
                map.put(coordinate, portalBlock);
                portalCount += 2;
            } else if (inRange(iceStart, iceEnd, randBlockNum)) {
                IceBlock iceBlock = new IceBlock(coordinate);
                map.put(coordinate, iceBlock);
                iceCount++;
            } else {
                System.out.println("Random Number was.... " + randBlockNum);
            }
        } else {
            EmptyBlock emptyBlock = new EmptyBlock(coordinate);
            map.put(coordinate, emptyBlock);
        }
    }

    private boolean inRange(double minInclusive, double maxExclusive, double val) {
        return val >= minInclusive && val < maxExclusive && minInclusive != maxExclusive; // If there is no range, always say false
    }


    private void printMap() {
        visualDisplay = "Map\n";
        HashMap<Coordinate, Direction> finalPathTaken = null;
        if (shortestPathRock != null) {
            finalPathTaken = shortestPathRock.getAllPreviousPositions();
        }

        if (showRowAndColumnNumbers) {
            System.out.print("   ");
            for (int a = 0; a < maxX; a++) {
                System.out.print(a + " ");
            }
            System.out.println();
        }
        for (int i = 0; i < maxY; i++) {
            if (showRowAndColumnNumbers) {
                System.out.print(i + (i < 10 ? "  " : " "));
            }
            for (int a = 0; a < maxX; a++) {
                if (i == startY && a == startX) {
                    System.out.print("S "); // Print the start block
                    visualDisplay += "S ";
                    continue;
                }
                Coordinate coordinate = new Coordinate(a, i);
                if (showPath && finalPathTaken != null && finalPathTaken.containsKey(coordinate) && !(map.get(coordinate) instanceof FinishBlock) && !(map.get(coordinate) instanceof RockBlock)) {
                    switch (finalPathTaken.get(coordinate)) {
                        case UP:
                            System.out.print("^");
                            visualDisplay += "^";
                            break;
                        case DOWN:
                            System.out.print("v");
                            visualDisplay += "v";
                            break;
                        case RIGHT:
                            System.out.print(">");
                            visualDisplay += ">";
                            break;
                        case LEFT:
                            System.out.print("<");
                            visualDisplay += "<";
                            break;
                    }
                    System.out.print(" ");
                    visualDisplay += " ";
                } else {
                    if (map.get(coordinate) == null) {
                        System.out.print("N "); // This isn't good to see!
                        visualDisplay += "N ";
                    } else {
                        System.out.print(map.get(coordinate).printMapObject() + " ");
                        visualDisplay += (map.get(coordinate).printMapObject() + " ");
                    }
                }
            }
            System.out.println("");
            visualDisplay += "\n";
        }
        encodedMap = base64EncodeMap();
        System.out.println("Regenerate Map With The Following:\n" + encodedMap);
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
                if (i == startY && a == startX) {
                    sb.append("[").append(coordinate.printForEncoding()).append("|").append(new MovingBlock().printMapObject()).append("]");
                } else {
                    sb.append("[").append(coordinate.printForEncoding()).append("|").append(map.get(coordinate).printMapObject()).append(map.get(coordinate) instanceof PortalBlock ? ((PortalBlock) map.get(coordinate)).getPortalExit().printForEncoding() : "").append("]");
                }
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
                case "I":
                    block = new IceBlock(coordinate);
                    break;
                case "Q":
                    block = new BreakableBlock(coordinate);
                    break;
                case "P":
                    block = new PortalBlock(coordinate);
                    Coordinate portalExit = new Coordinate(decoded.substring(i + 10, i + 17));
                    ((PortalBlock) block).setPortalExit(portalExit);
                    i += 7; // We messed with the default loop counter b/c the portal code is longer
                    break;
                case "S":
                    block = new EmptyBlock(coordinate); // Don't actually put in on the map YET, but we will later
                    startX = coordinate.getX();
                    startY = coordinate.getY();
                    break;
                default:
                    block = new EmptyBlock(coordinate);
                    break;
            }
            map.put(coordinate, block);
        }
    }

    private boolean canTravelInDirection(MovingBlock block, Direction direction) {
        Block nextBlock = getNextBlock(block, direction);
        if (block.getLastDirection() != null) {
            ArrayList<Coordinate> previousPositions = block.getPreviousPositions();
            if (previousPositions.get(previousPositions.size() - 1).equals(block.getPosition()) && nextBlock instanceof BreakableBlock && !((BreakableBlock) nextBlock).broken) {
                BreakableBlock breakableBlock = (BreakableBlock) nextBlock;
                while (!breakableBlock.broken) {
                    breakableBlock.onTouch(block);
                    block.savePreviousPosition();
                }
                return true; // Allow multiple hits right after each other
            }
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
            } else if (secondNextBlock != null && secondNextBlock instanceof BreakableBlock) {
                if (block.getPreviousPositions().size() == ((BreakableBlock) secondNextBlock).turnBroken) {
                    return false; // Don't allow to go through breakable block right after popping
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

        protected boolean isUsed;
        protected boolean isWritten;
        protected Coordinate position;
        protected ArrayList<Coordinate> previousPositions = new ArrayList<>();
        protected int id;
        protected int refId;
        protected boolean placed;
        protected Coordinate horRef;
        protected Coordinate verRef;

        protected void savePreviousPosition() {
            previousPositions.add(new Coordinate(position));
        }

        public <T extends Block> T copy(T copy) {
            copy.setPosition(new Coordinate(this.getPosition()));
            copy.setPreviousPositions((ArrayList<Coordinate>) this.getPreviousPositions().clone());
            if (copy instanceof MovingBlock && this instanceof MovingBlock) {
                HashMap<Coordinate, Direction> allPreviousDirections = new HashMap<>();
                allPreviousDirections.putAll(((MovingBlock) this).getAllPreviousPositions());
                ((MovingBlock) copy).setAllPreviousPositions(allPreviousDirections);
                ((MovingBlock) copy).setCurrentDirection(((MovingBlock) this).getCurrentDirection());
                ((MovingBlock) copy).setLastDirection(((MovingBlock) this).getLastDirection());
                ((MovingBlock) copy).setDeadBlock(((MovingBlock) this).isDeadBlock());
                ((MovingBlock) copy).setPassedThroughPortal(((MovingBlock) this).isPassedThroughPortal());
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

        public int getRefId() {
            return this.refId;
        }

        public void setUsed(boolean isUsed) {
            this.isUsed = isUsed;
        }

        public boolean isUsed() {
            return this.isUsed;
        }

        public void setWritten(boolean isWritten) {
            this.isWritten = isWritten;
        }

        public boolean isWritten() {
            return this.isWritten;
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
        private boolean passedThroughPortal = false;

        public boolean isPassedThroughPortal() {
            return passedThroughPortal;
        }

        public void setPassedThroughPortal(boolean passedThroughPortal) {
            this.passedThroughPortal = passedThroughPortal;
        }

        public MovingBlock() {

        }

        @Override
        public String getBlockType() {
            return "Moving Block";
        }

        @Override
        public String printMapObject() {
            return "S";
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

    class RefBlock extends Block {

        public RefBlock(Coordinate coordinate) {
            setPosition(coordinate);
            this.refId = getNextRefId();
            addToReferences();
        }

        public void addToReferences() {
            if (references != null) {
                references.add(this);
            } else {
                references = new ArrayList<RefBlock>();
                references.add(this);
            }
        }

        public int getRefId() {
            return this.refId;
        }

        @Override
        public String getBlockType() {
            return "Reference Block";
        }

        @Override
        public String printMapObject() {
            return "~";
        }

        public void onTouch(MovingBlock block) {
//            block.move();
//            block.savePositionForPrinting();
//            I don't think we want to do anything here....
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

    class BreakableBlock extends RockBlock {
        private int turnBroken = 0;
        private int hits = 0;
        private boolean broken = false;

        public BreakableBlock(Coordinate coordinate) {
            super(coordinate);
        }

        @Override
        public void onTouch(MovingBlock block) {
            turnBroken = block.getPreviousPositions().size() + 1;
            hits++;
            if (hits == 3) {
                turnBroken = block.getPreviousPositions().size() + 1;
                smash();
            }
        }

        @Override
        public String getBlockType() {
            return "Breakable Block";
        }

        @Override
        public boolean canTravel(Direction direction) {
            return broken;
        }

        @Override
        public String printMapObject() {
            return "Q";
        }

        public void smash() {
            this.broken = true;
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

    class IceBlock extends RockBlock {

        public IceBlock(Coordinate coordinate) {
            super(coordinate);
        }

        @Override
        public void onTouch(MovingBlock block) {
            // Does nothing for the solver
        }

        @Override
        public String getBlockType() {
            return "Ice Block";
        }

        @Override
        public boolean canTravel(Direction direction) {
            return false;
        }

        @Override
        public String printMapObject() {
            return "I";
        }
    }

    class PortalBlock extends RockBlock {

        private Coordinate portalExit;

        public PortalBlock(Coordinate coordinate) {
            super(coordinate);
        }

        @Override
        public void onTouch(MovingBlock block) {
            Coordinate coordinate;
            switch (block.currentDirection) {
                case UP:
                    coordinate = new Coordinate(portalExit.getX(), portalExit.getY() + 1);
                    break;
                case DOWN:
                    coordinate = new Coordinate(portalExit.getX(), portalExit.getY() - 1);
                    break;
                case RIGHT:
                    coordinate = new Coordinate(portalExit.getX() - 1, portalExit.getY());
                    break;
                case LEFT:
                    coordinate = new Coordinate(portalExit.getX() + 1, portalExit.getY());
                    break;
                default:
                    coordinate = null;
            }
            if (map.get(coordinate) == null || !(map.get(coordinate) instanceof EmptyBlock)) { // If a side of the exit portal is covered by block or wall, don't travel through it
                return;
            }
            block.setPassedThroughPortal(true);
            block.setLastDirection(block.currentDirection);
            block.setPosition(new Coordinate(portalExit));
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
            block.savePositionForPrinting();
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

        public Coordinate getPortalExit() {
            return portalExit;
        }

        public void setPortalExit(Coordinate portalExit) {
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
    private String getHintDirection(Coordinate from, Coordinate to) {
        int fromX = from.getX();
        int fromY = from.getY();
        int toX = to.getX();
        int toY = to.getY();
        int xDiff = fromX - toX;
        int yDiff = fromY - toY;
        if (xDiff < 0 && yDiff == 0) {
            return "right";
        } else if (xDiff > 0 && yDiff == 0) {
            return "left";
        } else if (yDiff < 0 && xDiff == 0) {
            return "down";
        } else if (yDiff > 0 && xDiff == 0) {
            return "up";
        } else {
            System.out.println("getDirection else!");
            return "up";
        }
    }
    public int getNextRefId() {
        int id = references == null ? 1 : references.size() + 1;
        return id;
    }

}

