package examplefuncsplayer;

import battlecode.common.*;

public abstract class Rat {
    protected static Bug2Navigator nav = new Bug2Navigator();
    protected int turnCount = 0;
    protected int[][] memoryMap = null;
    protected static int waitTime = 0;
    static MapLocation targetLocation = null;
    protected enum StaticTileTypes {
        UNKNOWN,
        DIRT,
        FREE,
        CHEESE,
        WALL,
        MINE,
    }

    public abstract void run(RobotController rc);

    public static void executeMovement(RobotController rc, MapLocation targetLocation) throws GameActionException {
        Bug2Navigator.Action action = nav.nextAction(rc, rc.getLocation(), targetLocation, true);
        System.out.println("action " + action.type + " " + action.dir);
        switch (action.type) {
            case MOVE:
                if (rc.canMove(action.dir)) {
                    rc.turn(action.dir);
                    rc.moveForward();
                }
                waitTime=0;
                break;
            case TURN:
                if (rc.canTurn(action.dir)) {
                    rc.turn(action.dir);
                }
                waitTime=0;
                break;
            case DELETE_DIRT:
                if (rc.canRemoveDirt(rc.getLocation().add(action.dir))) {
                    rc.removeDirt(rc.getLocation().add(action.dir));
                }
                waitTime=0;
                break;
            case WAIT, OCCUPIED, FINISHED:
                waitTime++;
                if (waitTime > 3){
                    if (rc.canTurn(rc.getDirection().rotateRight())) {
                        rc.turn(rc.getDirection().rotateRight());
                    }
                    nav.reset();
                    waitTime = 0;
                }
                break;
        }
    }

    public void moveToTarget(RobotController rc, MapLocation target) throws GameActionException {
        while (!rc.getLocation().equals(target)) {
            if (!rc.isMovementReady() || !rc.isActionReady()) {
                break;
            }
            executeMovement(rc, target);
        }
        if(rc.getLocation().equals(target)){
            targetLocation = null;
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
        while (nextLocation.x >= 1 && nextLocation.x <= mapWidth &&
                nextLocation.y >= 1 && nextLocation.y <= mapHeight) {
            furthestLocation = nextLocation;
            nextLocation = furthestLocation.add(awayDir);
        }
        return furthestLocation;
    }
}
