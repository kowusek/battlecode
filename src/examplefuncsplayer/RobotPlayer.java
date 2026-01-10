package examplefuncsplayer;

import battlecode.common.*;

import java.util.*;

import java.util.stream.Stream;


/**
 * RobotPlayer is the class that describes your main robot strategy.
 * The run() method inside this class is like your main function: this is what we'll call once your robot
 * is created!
 */
public class RobotPlayer {
    /**
     * We will use this variable to count the number of turns this robot has been alive.
     * You can use static variables like this to save any information you want. Keep in mind that even though
     * these variables are static, in Battlecode they aren't actually shared between your robots.
     */
    static int turnCount = 0;
    public static boolean notBuild = false;
    static int[][] memoryMap = null;
    /**
     * A random number generator.
     * We will use this RNG to make some random moves. The Random class is provided by the java.util.Random
     * import at the top of this file. Here, we *seed* the RNG with a constant number (6147); this makes sure
     * we get the same sequence of numbers every time this code is run. This is very useful for debugging!
     */
    static final Random rng = new Random(6147);
    static Bug2Navigator nav = new Bug2Navigator();

    /** Array containing all the possible movement directions. */
    static final Direction[] directions = {
            Direction.NORTH,
            Direction.NORTHEAST,
            Direction.EAST,
            Direction.SOUTHEAST,
            Direction.SOUTH,
            Direction.SOUTHWEST,
            Direction.WEST,
            Direction.NORTHWEST,
    };
    public static enum StaticTileTypes {
        DIRT,
        FREE,
        CHEESE,
        UNKNOWN,
        WALL,
        MINE,
    }

    /**
     * run() is the method that is called when a robot is instantiated in the Battlecode world.
     * It is like the main function for your robot. If this method returns, the robot dies!
     *
     * @param rc  The RobotController object. You use it to perform actions from this robot, and to get
     *            information on its current status. Essentially your portal to interacting with the world.
     **/
    @SuppressWarnings("unused")
    public static void run(RobotController rc) throws GameActionException {
        // Hello world! Standard output is very useful for debugging.
        // Everything you say here will be directly viewable in your terminal when you run a match!
        System.out.println("I'm alive");

        // You can also use indicators to save debug notes in replays.
        rc.setIndicatorString("Hello world!");
        rc.getType();
        while (true) {
            turnCount += 1;  // We have now been alive for one more turn!
            if (rc.getType() == UnitType.RAT_KING) {
                MapLocation buildLocation = rc.getLocation().add(Direction.NORTH).add(Direction.NORTH);
                //System.out.println("outside " + buildLocation + " "+ rc.canBuildRat(buildLocation) );
                if (!notBuild && rc.canBuildRat(buildLocation)) {
                    //System.out.println("inside");
                    rc.buildRat(buildLocation);
                    notBuild = true;
                }
                //System.out.println("cheese" + rc.getAllCheese() + "turn " + turnCount);
                Clock.yield();
            } else {
                runBabyRat(rc);
            }
        }
    }
    public static void runBabyRat(RobotController rc) throws GameActionException{
        try {
            int mapWidth = rc.getMapWidth();
            int mapHeight = rc.getMapHeight();
            if (memoryMap == null){
                memoryMap = new int[mapWidth][mapHeight];
// Initially unknown
                for (int x = 0; x < mapWidth; x++) {
                    for (int y = 0; y < mapHeight; y++) {
                        memoryMap[x][y] = StaticTileTypes.UNKNOWN.ordinal();
                    }
                }
            }
            MapInfo[] sensed = rc.senseNearbyMapInfos();
            //RobotInfo[] nearbyEnemies = rc.senseNearbyRobots(rc.getType().getVisionRadiusSquared(), rc.getTeam().opponent());
            //RobotInfo[] nearbyCats = rc.senseNearbyRobots(rc.getType().getVisionRadiusSquared(), Team.NEUTRAL);
            for (MapInfo info : sensed) {
                MapLocation loc = info.getMapLocation();
                int x = loc.x;
                int y = loc.y;
                if (info.isDirt()){
                    memoryMap[x][y] = StaticTileTypes.DIRT.ordinal();
                }else if (info.isWall()){
                    memoryMap[x][y] = StaticTileTypes.WALL.ordinal();
                }
                else if (info.hasCheeseMine()){
                    memoryMap[x][y] = StaticTileTypes.MINE.ordinal();
                }
                else if (info.getCheeseAmount()> 0){
                    memoryMap[x][y] = StaticTileTypes.CHEESE.ordinal();
                }else {
                    memoryMap[x][y] = StaticTileTypes.FREE.ordinal();
                }
            }
//            System.out.println();
//            for (int y = 0; y <  mapHeight; y++) {  // print top to bottom
//                for (int x = 0; x < mapWidth; x++) {
//                    System.out.print("x "+ x + " y " + y);
//                    switch (memoryMap[x][y]) {
//                        case 0: System.out.print("D "); break;  // DIRT
//                        case 1: System.out.print(". "); break;  // FREE
//                        case 2: System.out.print("C "); break;  // CHEESE
//                        case 3: System.out.print("? "); break;  // UNKNOWN
//                        case 4: System.out.print("X "); break;  // WALL
//                        case 5: System.out.print("M "); break;  // MINE
//                    }
//                }
//                System.out.println();
//            }
            MapLocation targetLocation = new MapLocation(0, 0);
            Bug2Navigator.Action action = nav.nextAction(rc, rc.getLocation(), targetLocation);
            System.out.println("action " + action.type + " " + action.dir);
            switch (action.type) {
                case MOVE:
                    if (rc.canMove(action.dir)) {
                        rc.turn(action.dir);
                        rc.moveForward();
                    }
                    break;
                case TURN:
                    if (rc.canTurn(action.dir)) {
                        rc.turn(action.dir);
                    }
                    break;
                case DELETE_DIRT:
                    if (rc.canRemoveDirt(rc.getLocation().add(action.dir))) {
                        rc.removeDirt(rc.getLocation().add(action.dir));
                    }
                    break;
                case NONE:
                    // do nothing
                    break;
            }

        }catch (GameActionException e) {
            System.out.println("GameActionException");
            e.printStackTrace();
        } catch (Exception e) {
            System.out.println("Exception");
            e.printStackTrace();
        } finally {
            Clock.yield();
        }

    }
}