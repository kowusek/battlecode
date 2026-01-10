package examplefuncsplayer;

import battlecode.common.*;

public class KingRat extends Rat {
    protected int toBuild = 9999;
    static MapLocation targetLocation = null;
    static int mySharedArrayOffset = -1;
    static MapLocation nearestCatLocation = null;

    @Override
    public void run(RobotController rc) {
        try {
            writeLocationToSharedArray(rc, rc.getLocation());
            senseNearbyRobots(rc);
            buildRat(rc);
            targetLocation = determineTargetLocation(rc);
            if (targetLocation != null) {
                moveToTarget(rc, targetLocation);
            }
            // System.out.println("cheese" + rc.getAllCheese() + "turn " + turnCount);
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

    public void buildRat(RobotController rc) throws GameActionException {
        MapLocation buildLocation = rc.getLocation().add(Direction.NORTH).add(Direction.NORTH);
        if (toBuild > 0 && rc.canBuildRat(buildLocation)) {
            // System.out.println("inside");
            rc.buildRat(buildLocation);
            toBuild--;
        }
    }

    public void writeLocationToSharedArray(RobotController rc, MapLocation location)
            throws GameActionException {
        // Find an empty slot if we don't have one yet
        if (mySharedArrayOffset == -1) {
            for (int i = 0; i < 5; i++) {
                int offset = i * 2;
                int x = rc.readSharedArray(offset);
                System.out.print("Read 10th slot in shared mem: " + rc.readSharedArray(10));
                int y = rc.readSharedArray(offset + 1);
                // Empty slot found (uninitialized = 0,0)
                if (x == 0 && y == 0) {
                    mySharedArrayOffset = offset;
                    break;
                }
            }
        }
        // Write location to our reserved slot
        if (mySharedArrayOffset != -1) {
            rc.writeSharedArray(mySharedArrayOffset, location.x);
            rc.writeSharedArray(mySharedArrayOffset + 1, location.y);
        }
    }

    public MapLocation determineTargetLocation(RobotController rc) {
        MapLocation target = targetLocation;
        MapLocation runLocation = runAwayFromCats(rc);
        if (runLocation != null) {
            target = runLocation;
        }

        return target;
    }

    public void senseNearbyRobots(RobotController rc) throws GameActionException {
        boolean seenCat = false;
        for (RobotInfo robot : rc.senseNearbyRobots()) {
            if (robot.type == UnitType.CAT) {
                nearestCatLocation = robot.getLocation();
                seenCat = true;
            }
        }
        if (!seenCat) {
            nearestCatLocation = null;
        }
    }

    public MapLocation runAwayFromCats(RobotController rc) {
        if (nearestCatLocation != null) {
            MapLocation furthestLocation = findFurthestLocationAwayFrom(rc, nearestCatLocation);
            return furthestLocation;
        }
        return null;
    }
}
