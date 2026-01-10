package examplefuncsplayer;

import battlecode.common.*;

public class Bug2Navigator {

    private boolean followingWall = false;
    private int hitDist = 0;

    public static class Action {
        public enum ActionType {
            MOVE, TURN, DELETE_DIRT, WAIT, OCCUPIED
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

    public Action nextAction(RobotController rc, MapLocation current, MapLocation goal) throws GameActionException {

        if (!goal.equals(targetLoc) || startLoc == null) {
            reset();
            targetLoc = goal;
            startLoc = current;
        }
        if (!rc.isMovementReady() || !rc.isTurningReady() || !rc.isActionReady()) {
            System.out.println("!rc.isMovementReady() || !rc.isTurningReady() || !rc.isActionReady()");
            return new Action(Action.ActionType.WAIT, Direction.CENTER);
        }
        // Direction to goal
        Direction directionToTarget = current.directionTo(goal);
        System.out.println("directionToTarget " + directionToTarget);
        // We are at the destination
        if (current.distanceSquaredTo(goal) == 0) {
            System.out.println("current.distanceSquaredTo(goal) == 0");
            return new Action(Action.ActionType.WAIT, Direction.CENTER);
        }

        Direction dirtDirection = findAnyRemovableDirt(rc);
        if (dirtDirection != Direction.CENTER) {
            System.out.println("dirtDirection " + dirtDirection);
            if (rc.getGlobalCheese() > 200 && rc.isActionReady()) {
                reset();
                return new Action(Action.ActionType.DELETE_DIRT, dirtDirection);
            } else if (!rc.isActionReady()) {
                return new Action(Action.ActionType.WAIT, Direction.CENTER);
            }
        }
        // move towards goal
        if (!followingWall && rc.canMove(directionToTarget)) {
            System.out.println("!followingWall && rc.canMove(directionToTarget)");
            return new Action(Action.ActionType.MOVE, directionToTarget);
        }
        // start wall-following
        if (!followingWall) {
            followingWall = true;
            hitDist = current.distanceSquaredTo(targetLoc);
            wallDir = directionToTarget;
            System.out.println("!followingWall");
            return new Action(Action.ActionType.MOVE, followWall(rc));
        }
        // Bug2 exit condition: back on M-line closer to goal
        if (onMLine(startLoc, targetLoc, current)
                && current.distanceSquaredTo(targetLoc) <= hitDist
                && rc.canMove(directionToTarget)) {
            followingWall = false;
            System.out.println("onMLine(startLoc, goal, current)");
            return new Action(Action.ActionType.MOVE, directionToTarget);
        }
        // following wall
        Direction move = followWall(rc);
        if (move == Direction.CENTER) {
            System.out.println("move == Direction.CENTER");
            return new Action(Action.ActionType.TURN, rc.getDirection().rotateLeft());
        }
        return new Action(Action.ActionType.MOVE, move);
    }

    // ===== Wall Following =====
    private Direction followWall(RobotController rc) {
        Direction d = wallDir;
        for (int i = 0; i < 8; i++) {
            if (rc.canMove(d)) {
                wallDir = d.rotateRight(); // keep wall on right
                return d;
            }
            d = d.rotateLeft();
        }
        return Direction.CENTER;
    }

    // ===== M-line Check =====
    private boolean onMLine(MapLocation start, MapLocation goal, MapLocation cur) {
        int dx1 = goal.x - start.x;
        int dy1 = goal.y - start.y;
        int dx2 = cur.x - start.x;
        int dy2 = cur.y - start.y;
        int cross = dx1 * dy2 - dy1 * dx2;
        return Math.abs(cross) <= Math.max(Math.abs(dx1), Math.abs(dy1));
    }

    private Direction findAnyRemovableDirt(RobotController rc) {
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
            if (rc.canRemoveDirt(loc))
                return dir;
        }
        return Direction.CENTER;
    }

    // reset
    private void reset() {
        followingWall = false;
        hitDist = 0;
        startLoc = null;
        wallDir = Direction.CENTER;
    }
}
