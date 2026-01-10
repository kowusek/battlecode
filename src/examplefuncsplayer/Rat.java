package examplefuncsplayer;

import battlecode.common.*;

public abstract class Rat {
    protected static Bug2Navigator nav = new Bug2Navigator();
    protected int turnCount = 0;
    protected int[][] memoryMap = null;

    protected enum StaticTileTypes {
        DIRT,
        FREE,
        CHEESE,
        UNKNOWN,
        WALL,
        MINE,
    }

    public abstract void run(RobotController rc);

    public static void executeMovement(RobotController rc, MapLocation targetLocation) throws GameActionException {
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
            case WAIT, OCCUPIED:
                // do nothing
                break;
        }
    }

    public void moveToTarget(RobotController rc, MapLocation target) throws GameActionException {
        while (rc.getLocation() != target &&
                rc.isMovementReady() &&
                rc.isTurningReady()) {
            executeMovement(rc, target);
        }
    }

    public MapLocation findFurthestLocationAwayFrom(RobotController rc, MapLocation nearestLocation) {
        MapLocation myLocation = rc.getLocation();
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
        return furthestLocation;
    }
}
