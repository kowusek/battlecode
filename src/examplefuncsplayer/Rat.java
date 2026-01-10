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
}
