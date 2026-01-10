package examplefuncsplayer;

import battlecode.common.*;

public class KingRat extends Rat {
    protected static boolean notBuild = false;
    static MapLocation targetLocation = null;

    @Override
    public void run(RobotController rc) {
        try {
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
        if (!notBuild && rc.canBuildRat(buildLocation)) {
            rc.buildRat(buildLocation);
            notBuild = true;
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

    public MapLocation runAwayFromCats(RobotController rc) {
        MapLocation myLocation = rc.getLocation();
        MapLocation nearestLocation = null;
        int minDistance = 100000;

        for (RobotInfo robot : rc.senseNearbyRobots()) {
            if (robot.type != UnitType.CAT) {
                continue;
            }
            int distance = myLocation.distanceSquaredTo(robot.getLocation());
            if (distance < minDistance) {
                minDistance = distance;
                nearestLocation = robot.getLocation();
            }
        }

        if (nearestLocation != null) {
            MapLocation furthestLocation = findFurthestLocationAwayFrom(rc, nearestLocation);
            System.out.print("Running away to " + furthestLocation);
            return furthestLocation;
        }
        return null;
    }
}
