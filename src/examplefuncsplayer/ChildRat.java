package examplefuncsplayer;

import battlecode.common.*;
import java.util.*;

public class ChildRat extends Rat {
    public static enum ChildRatState {
        INITIALIZE,
        GO_TO_LOCATION,
        ATTACK,
    }

    static final Random rng = new Random(6147);
    static MapLocation targetLocation = null;
    int[][] memoryMap = null;
    public static ChildRatState currentChildRatState = ChildRatState.INITIALIZE;

    @Override
    public void run(RobotController rc) {
        try {
            if (memoryMap == null) {
                initMemoryMap(rc);
            }
            updateMemoryMap(rc);
            // debugPrintMap(rc);
            targetLocation = determineTargetLocation(rc);
            moveToTarget(rc, targetLocation);

        } catch (GameActionException e) {
            System.out.println("GameActionException");
            e.printStackTrace();
        } catch (Exception e) {
            System.out.println("Exception");
            e.printStackTrace();
        } finally {
            Clock.yield();
        }

    }

    public MapLocation determineTargetLocation(RobotController rc) throws GameActionException {
        MapLocation target = targetLocation;

        MapLocation cheeseLocation = findNearestCheese(rc);
        if (cheeseLocation != null) {
            target = cheeseLocation;
        }

        MapLocation runLocation = runAwayFromOtherRats(rc);
        if (runLocation != null) {
            target = runLocation;
        }

        if (target != null && !rc.canPickUpCheese(target)) {
            target = runToRandomLocation(rc); // run to king
        }

        if (target == null) {
            target = runToRandomLocation(rc);
        }

        return target;
    }

    public static MapLocation runToRandomLocation(RobotController rc) {
        int mapWidth = rc.getMapWidth();
        int mapHeight = rc.getMapHeight();
        int randX = rng.nextInt(mapWidth);
        int randY = rng.nextInt(mapHeight);
        return new MapLocation(randX, randY);
    }

    public MapLocation runAwayFromOtherRats(RobotController rc) {
        MapLocation myLocation = rc.getLocation();
        MapLocation nearestLocation = null;
        int minDistance = 100000;

        for (RobotInfo rat : rc.senseNearbyRobots()) {
            int distance = myLocation.distanceSquaredTo(rat.getLocation());
            if (distance < minDistance) {
                minDistance = distance;
                nearestLocation = rat.getLocation();
            }
        }

        if (nearestLocation != null) {
            MapLocation furthestLocation = findFurthestLocationAwayFrom(rc, nearestLocation);
            System.out.print("Running away to " + furthestLocation);
            return furthestLocation;
        }
        return null;
    }

    public MapLocation findNearestCheese(RobotController rc) {
        int mapWidth = rc.getMapWidth();
        int mapHeight = rc.getMapHeight();
        for (int x = 0; x < mapWidth; x++) {
            for (int y = 0; y < mapHeight; y++) {
                if (memoryMap[x][y] == StaticTileTypes.CHEESE.ordinal()) {
                    return new MapLocation(x, y);
                }
            }
        }
        return null;
    }

    private void updateMemoryMap(RobotController rc) {
        MapInfo[] sensed = rc.senseNearbyMapInfos();/* */
        for (MapInfo info : sensed) {
            MapLocation loc = info.getMapLocation();
            int x = loc.x;
            int y = loc.y;
            if (info.isDirt()) {
                memoryMap[x][y] = StaticTileTypes.DIRT.ordinal();
            } else if (info.isWall()) {
                memoryMap[x][y] = StaticTileTypes.WALL.ordinal();
            } else if (info.hasCheeseMine()) {
                memoryMap[x][y] = StaticTileTypes.MINE.ordinal();
            } else if (info.getCheeseAmount() > 0) {
                memoryMap[x][y] = StaticTileTypes.CHEESE.ordinal();
            } else {
                memoryMap[x][y] = StaticTileTypes.FREE.ordinal();
            }
        }
    }

    private void initMemoryMap(RobotController rc) {
        int mapWidth = rc.getMapWidth();
        int mapHeight = rc.getMapHeight();
        memoryMap = new int[mapWidth][mapHeight];
        // Initially unknown
        for (int x = 0; x < mapWidth; x++) {
            for (int y = 0; y < mapHeight; y++) {
                memoryMap[x][y] = StaticTileTypes.UNKNOWN.ordinal();
            }
        }
    }

    private void debugMemoryMap(RobotController rc) {
        int mapWidth = rc.getMapWidth();
        int mapHeight = rc.getMapHeight();
        System.out.println();
        for (int y = 0; y < mapHeight; y++) { // print top to bottom
            for (int x = 0; x < mapWidth; x++) {
                System.out.print("x " + x + " y " + y);
                switch (memoryMap[x][y]) {
                    case 0:
                        System.out.print("D ");
                        break; // DIRT
                    case 1:
                        System.out.print(". ");
                        break; // FREE
                    case 2:
                        System.out.print("C ");
                        break; // CHEESE
                    case 3:
                        System.out.print("? ");
                        break; // UNKNOWN
                    case 4:
                        System.out.print("X ");
                        break; // WALL
                    case 5:
                        System.out.print("M ");
                        break; // MINE
                }
            }
            System.out.println();
        }
    }
}
