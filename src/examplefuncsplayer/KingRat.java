package examplefuncsplayer;

import battlecode.common.*;

public class KingRat extends Rat {
    protected static boolean notBuild = false;
    static MapLocation targetLocation = null;

    @Override
    public void run(RobotController rc) {
        try {
            MapLocation buildLocation = rc.getLocation().add(Direction.NORTH).add(Direction.NORTH);
            // System.out.println("outside " + buildLocation + " "+
            // rc.canBuildRat(buildLocation) );
            if (!notBuild && rc.canBuildRat(buildLocation)) {
                // System.out.println("inside");
                rc.buildRat(buildLocation);
                notBuild = true;
            }
            MapLocation runLocation = runAwayFromCats(rc);
            if (runLocation != null) {
                targetLocation = runLocation;
            }
            if (targetLocation != null) {
                while (rc.getLocation() != targetLocation &&
                        rc.isMovementReady() &&
                        rc.isTurningReady()) {
                    executeMovement(rc, targetLocation);
                }
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
            Direction awayDir = nearestLocation.directionTo(myLocation);
            MapLocation furthestLocation = myLocation;
            int mapWidth = rc.getMapWidth();
            int mapHeight = rc.getMapHeight();

            // Keep moving in the away direction until we hit a boundary or obstacle
            MapLocation nextLocation = furthestLocation.add(awayDir);
            while (nextLocation.x >= 0 && nextLocation.x < mapWidth &&
                    nextLocation.y >= 0 && nextLocation.y < mapHeight) {
                furthestLocation = nextLocation;
                nextLocation = furthestLocation.add(awayDir);
            }
            System.out.print("Running away to " + furthestLocation);
            return furthestLocation;
        }
        return null;
    }
}
