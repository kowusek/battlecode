package examplefuncsplayer;

import battlecode.common.*;
import java.util.*;

import java.util.Random;

public class ChildRat extends Rat {

    static final Random rng = new Random(6147);
    static MapLocation targetLocation = null;
    static MapLocation ratKingLocation = null;
    static MapLocation mineLocation = null;

    @Override
    public void run(RobotController rc) {
        try {
            if (memoryMap == null) {
                initMemoryMap(rc);
            }
            updateMemoryMap(rc);
            ratKingLocation = findNearestRatKing(rc);
            handleCheeseLogic(rc);
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

    public void handleCheeseLogic(RobotController rc) throws GameActionException {
        if (rc.canPickUpCheese(rc.getLocation())) {
            rc.pickUpCheese(rc.getLocation());
        }
        if (ratKingLocation != null && rc.canTransferCheese(ratKingLocation, rc.getRawCheese())) {
            rc.transferCheese(ratKingLocation, rc.getRawCheese());
        }
    }

    public MapLocation determineTargetLocation(RobotController rc) throws GameActionException {
        MapLocation target = targetLocation;
        MapLocation mineLocation = findNearestStaticTileType(rc, StaticTileTypes.MINE);
        if (mineLocation != null && rc.getRawCheese() < 20) {
            target = mineLocation;
        }
        MapLocation cheeseLocation = findNearestStaticTileType(rc, StaticTileTypes.CHEESE);
        if (cheeseLocation != null && rc.getRawCheese() < 20) {
            target = cheeseLocation;
        }
        MapLocation runLocation = runAwayFromOtherRats(rc);
        if (runLocation != null) {
            target = runLocation;
        }
        if (target != null && rc.getRawCheese() != 0) {
            target = ratKingLocation;
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

    public MapLocation findNearestRatKing(RobotController rc) throws GameActionException {
        MapLocation myLocation = rc.getLocation();
        MapLocation nearestKing = null;
        int minDistance = Integer.MAX_VALUE;
        for (int i = 0; i < 5; i++) {
            int offset = i * 2;
            int x = rc.readSharedArray(offset);
            int y = rc.readSharedArray(offset + 1);

            // Skip if coordinates are 0,0 (uninitialized)
            if (x == 0 && y == 0) {
                continue;
            }
            MapLocation kingLocation = new MapLocation(x, y);
            int distance = myLocation.distanceSquaredTo(kingLocation);
            if (distance < minDistance) {
                minDistance = distance;
                nearestKing = kingLocation;
            }
        }
        return nearestKing;
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
            // System.out.print("Running away to " + furthestLocation);
            return furthestLocation;
        }
        return null;
    }
    protected MapLocation findNearestStaticTileType(RobotController rc, StaticTileTypes staticTileType) {
        int myX = rc.getLocation().x;
        int myY = rc.getLocation().y;

        int minDistance = Integer.MAX_VALUE;
        int radius = 5;
        MapLocation nearestTileType = null;
        int width = rc.getMapWidth();
        int height = rc.getMapHeight();

        for (int dx = -radius; dx <= radius; dx++) {
            int x = myX + dx;
            if (x < 0 || x >= width) continue;

            for (int dy = -radius; dy <= radius; dy++) {
                int y = myY + dy;
                if (y < 0 || y >= height) continue;

                if (memoryMap[x][y] == staticTileType.ordinal()) {
                    int dist = dx * dx + dy * dy;

                    if (dist < minDistance) {
                        minDistance = dist;
                        nearestTileType = new MapLocation(x, y);
                    }
                }
            }
        }
        return nearestTileType;
    }
    private void updateMemoryMap(RobotController rc) {
        MapInfo[] sensed = rc.senseNearbyMapInfos();
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
    }

    private void initMemoryMap(RobotController rc) {
        int mapWidth = rc.getMapWidth();
        int mapHeight = rc.getMapHeight();
        memoryMap = new int[mapWidth][mapHeight];
    }
}
