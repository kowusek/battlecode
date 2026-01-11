package examplefuncsplayer;

import battlecode.common.*;

public class Bug2Navigator {

    private boolean followingWall = false;
    private int hitDist = 0;

    public static class Action {
        public enum ActionType {
            MOVE, TURN, DELETE_DIRT, WAIT, OCCUPIED, FINISHED
        }

        public final ActionType type;
        public final Direction dir;

        public Action(ActionType type, Direction dir) {
            this.type = type;
            this.dir = dir;
        }
    }

    private MapLocation startLoc = null;
    private MapLocation targetLoc = null;
    private Direction wallDir = Direction.CENTER;
    private MapLocation wallPosition = null;

    public Action nextAction(RobotController rc, MapLocation current, MapLocation goal, boolean deleteDirt)
            throws GameActionException {

        if (!goal.equals(targetLoc) || startLoc == null) {
            reset();
            targetLoc = goal;
            startLoc = current;
        }
        if (followingWall) {
            if (rc.canMove(rc.getLocation().directionTo(wallPosition))) {
                // System.out.println("reset because wall disappeared");
                reset();
                targetLoc = goal;
                startLoc = current;
            }
        }
        // We are at the destination
        if (current.distanceSquaredTo(goal) == 0) {
            // System.out.println("current.distanceSquaredTo(goal) == 0");
            return new Action(Action.ActionType.FINISHED, Direction.CENTER);
        }
        Direction directionToTarget = current.directionTo(goal);
        // targetPosition is occupied and next to us
        if (rc.canSenseLocation(goal) && rc.isLocationOccupied(goal)
                && rc.adjacentLocation(directionToTarget).distanceSquaredTo(goal) == 0) {
            return new Action(Action.ActionType.OCCUPIED, Direction.CENTER);

        }
        // System.out.println("directionToTarget " + directionToTarget);
        if (deleteDirt) {
            Direction dirtDirection = findAnyRemovableDirt(rc);
            if (dirtDirection != Direction.CENTER) {
                if (!rc.isActionReady()) {
                    return new Action(Action.ActionType.WAIT, Direction.CENTER);
                }
                // System.out.println("dirtDirection " + dirtDirection);
                if(rc.getAllCheese() > 100){
                    return new Action(Action.ActionType.DELETE_DIRT, dirtDirection);
                }
                //System.out.println("dirtDirection " + dirtDirection);
            }
        }
        if (!rc.isMovementReady() || !rc.isTurningReady()) {
            return new Action(Action.ActionType.WAIT, Direction.CENTER);
        }
        // move towards goal
        if (!followingWall && rc.canMove(directionToTarget)) {
            //System.out.println("!followingWall && rc.canMove(directionToTarget) " + directionToTarget);
            return new Action(Action.ActionType.MOVE, directionToTarget);
        }
        // start wall-following
        if (!followingWall) {
            followingWall = true;
            hitDist = current.distanceSquaredTo(targetLoc);
            wallDir = directionToTarget;
            wallPosition = rc.getLocation().add(directionToTarget);
            Direction dir = followWall(rc);
            //System.out.println("!followingWall");
            if (dir == Direction.CENTER){
                return new Action(Action.ActionType.WAIT, dir);
            }
            return new Action(Action.ActionType.MOVE, dir);
        }
        // Bug2 exit condition: back on M-line closer to goal
        // System.out.println("current.distanceSquaredTo(targetLoc) " +
        // current.distanceSquaredTo(targetLoc));
        // System.out.println("hitDist " + hitDist);
        if (onMLine(startLoc, targetLoc, current)
                && current.distanceSquaredTo(targetLoc) <= hitDist
                && rc.canMove(directionToTarget)) {
            followingWall = false;
            wallDir = Direction.CENTER;
            wallPosition = null;
            // System.out.println("onMLine(startLoc, goal, current)");
            return new Action(Action.ActionType.MOVE, directionToTarget);
        }
        // following wall
        Direction move = followWall(rc);
        if (move == Direction.CENTER) {
            // System.out.println("move == Direction.CENTER");
            return new Action(Action.ActionType.TURN, rc.getDirection().rotateLeft());
        }
        // System.out.println("Action.ActionType.MOVE, mova " + move);
        return new Action(Action.ActionType.MOVE, move);
    }

    // ===== Wall Following =====
    private Direction followWall(RobotController rc) {
        Direction d = wallDir;
        for (int i = 0; i < 8; i++) {
            if (rc.canMove(d)) {
                wallDir = d.rotateRight(); // keep wall on right
                wallPosition = rc.getLocation().add(wallDir);
                // System.out.println("followWall " + wallDir);
                return d;
            }
            d = d.rotateLeft();
        }
        return Direction.CENTER;
    }

    // ===== M-line Check =====
    private boolean onMLine(MapLocation start, MapLocation goal, MapLocation cur) {
        // System.out.println("start goal, cur");
        // System.out.println(start + " " + goal + " " + cur);
        int dx1 = goal.x - start.x;
        int dy1 = goal.y - start.y;
        int dx2 = cur.x - start.x;
        int dy2 = cur.y - start.y;
        int cross = dx1 * dy2 - dy1 * dx2;
        return Math.abs(cross) <= Math.max(Math.abs(dx1), Math.abs(dy1));
    }

    private Direction findAnyRemovableDirt(RobotController rc) throws GameActionException {
        Direction[] directions;
        if (rc.getType() == UnitType.BABY_RAT) {
            directions = new Direction[] {
                    rc.getDirection(),
                    rc.getDirection().rotateLeft(),
                    rc.getDirection().rotateRight()
            };
        } else {
            directions = new Direction[] {
                    rc.getDirection(),
                    rc.getDirection().rotateLeft(),
                    rc.getDirection().rotateLeft().rotateLeft(),
                    rc.getDirection().rotateLeft().rotateLeft().rotateLeft(),
                    rc.getDirection().rotateLeft().rotateLeft().rotateLeft().rotateLeft(),
                    rc.getDirection().rotateRight(),
                    rc.getDirection().rotateRight().rotateRight(),
                    rc.getDirection().rotateRight().rotateRight().rotateRight()
            };
        }
        for (Direction dir : directions) {
            MapLocation loc = rc.getLocation().add(dir);
            if (loc.x < 0 || loc.y < 0 || loc.x >= rc.getMapWidth() || loc.y >= rc.getMapHeight())
                continue;
            // System.out.println("loc" + loc);
            if (rc.senseMapInfo(loc).isDirt())
                return dir;
        }
        return Direction.CENTER;
    }

    public void reset() {
        followingWall = false;
        hitDist = 0;
        startLoc = null;
        wallDir = Direction.CENTER;
        wallPosition = null;
    }
}
