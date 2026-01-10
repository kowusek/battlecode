package examplefuncsplayer;

import battlecode.common.*;
import java.util.*;

import java.util.Random;

public class ChildRat extends Rat {

    static MapLocation targetLocation = null;
    static MapLocation ratKingLocation = null;
    static MapLocation mineLocation = null;

    @Override
    public void run(RobotController rc) {
        try {
            ratKingLocation = findNearestRatKing(rc);
            updateMineCoordinates(rc);
            senseNearbyCats(rc);
            if (rc.canBecomeRatKing()) {
                rc.becomeRatKing();
            }
            if (nearestCatLocation != null) {
                rc.attack(nearestCatLocation);
            }

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
        if (mineLocation != null) {
            target = mineLocation;
        }
        MapLocation runLocation = runAwayFromRatKing(rc);
        if (runLocation != null) {
            target = runLocation;
        }
        MapLocation cheeseLocation = findNearestCheese(rc);
        if (cheeseLocation != null && !rc.isLocationOccupied(cheeseLocation)) {
            target = cheeseLocation;
        }
        if (target != null && rc.getRawCheese() != 0) {
            target = ratKingLocation;
        }
        if (target == null || rc.getLocation().equals(target)) {
            target = runToRandomLocation(rc);
        }
        return target;
    }

    public MapLocation findNearestCheese(RobotController rc) {
        MapInfo[] sensed = rc.senseNearbyMapInfos();
        for (MapInfo info : sensed) {
            if (info.getCheeseAmount() > 0) {
                return info.getMapLocation();
            }
        }
        return null;
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

    public MapLocation runAwayFromRatKing(RobotController rc) {
        MapLocation myLocation = rc.getLocation();
        MapLocation nearestLocation = null;
        int minDistance = 100000;
        for (RobotInfo rat : rc.senseNearbyRobots()) {
            if (rat.getType() != UnitType.RAT_KING && rat.team != rc.getTeam()) {
                continue;
            }
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

    private void updateMineCoordinates(RobotController rc) {
        MapInfo[] sensed = rc.senseNearbyMapInfos();
        for (MapInfo info : sensed) {
            if (info.hasCheeseMine()) {
                mineLocation = info.getMapLocation();
            }
        }
    }
}
