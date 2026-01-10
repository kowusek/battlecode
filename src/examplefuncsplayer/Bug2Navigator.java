package examplefuncsplayer;

import battlecode.common.*;
import java.util.LinkedList;

public class Bug2Navigator {

    private boolean followingWall = false;
    private int hitDist = 0;
    public static class Action {
        public enum ActionType { MOVE, TURN, DELETE_DIRT, NONE }
        public final ActionType type;
        public final Direction dir;

        public Action(ActionType type, Direction dir) {
            this.type = type;
            this.dir = dir;
        }
    }
    private MapLocation startLoc = null;
    private MapLocation goalLoc = null;
    private Direction wallDir = Direction.CENTER;
    private MapLocation lastVisitedLocation = null;
    // Keep track of visited locations while wall-following to detect loops
    private final LinkedList<MapLocation> visitedLocations = new LinkedList<>();
    private static final int MAX_VISITED_HISTORY = 50;

    // ===== Main Entry =====
    public Action nextAction(RobotController rc, MapLocation current, MapLocation goal) throws GameActionException {

        // Reset if new goal
        if (!goal.equals(goalLoc)) {
            reset();
            goalLoc = goal;
            startLoc = current;
            lastVisitedLocation = null;
        }
        Direction toGoal = current.directionTo(goal);
        if (toGoal == Direction.CENTER){
            System.out.println("toGoal == Direction.CENTER");
            return new Action(Action.ActionType.NONE, Direction.CENTER);
        }
        // ===== Check for dirt in all surrounding tiles first =====
        //Direction dirtDir = findAnyRemovableDirt(rc);
        if(rc.canRemoveDirt(rc.getLocation().add(rc.getDirection()))){
            return new Action(Action.ActionType.DELETE_DIRT, rc.getDirection());
        }
//        if (dirtDir != Direction.CENTER) {
//            // Remove dirt to unblock path
//            return new Action(Action.ActionType.DELETE_DIRT, dirtDir);
//        }
        // ===== Direct move toward goal =====
        if (!followingWall && rc.canMove(toGoal)) {
            return new Action(Action.ActionType.MOVE, toGoal);
        }

        // ===== Not following wall yet? Start wall-following =====
        if (!followingWall) {
            followingWall = true;
            hitDist = current.distanceSquaredTo(goal);
            wallDir = toGoal.rotateRight();
            visitedLocations.clear();
            visitedLocations.add(current);
            return new Action(Action.ActionType.MOVE, followWall(rc));
        }

        // ===== FOLLOWING WALL =====
        Direction move = followWall(rc);

        // ===== Loop detection =====
//        MapLocation nextLoc = current.add(move);
//        if (visitedLocations.contains(nextLoc)) {
//            // We are about to revisit a location → try removing any dirt around
//            Direction escapeDirt = findAnyRemovableDirt(rc, current);
//            if (escapeDirt != Direction.CENTER) return escapeDirt;
//        }

        // Record current location
        //visitedLocations.add(current);
        //if (visitedLocations.size() > MAX_VISITED_HISTORY) visitedLocations.removeFirst();

        // ===== Bug2 exit condition: back on M-line closer to goal =====
        if (onMLine(startLoc, goal, current)
                && current.distanceSquaredTo(goal) < hitDist
                && rc.canMove(toGoal)) {

            followingWall = false;
            //visitedLocations.clear();
            return new Action(Action.ActionType.MOVE, toGoal);
        }
        if (move == Direction.CENTER){
            return new Action(Action.ActionType.TURN, rc.getDirection().rotateLeft());
        }
        return new Action(Action.ActionType.MOVE, move);
    }

    // ===== Wall Following =====
    private Direction followWall(RobotController rc) {
        Direction d = wallDir;
        for (int i = 0; i < 4; i++) {
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

    private Direction findAnyRemovableDirt(RobotController rc) throws GameActionException {
        for (Direction dir : Direction.values()) {
            if (dir == Direction.CENTER) continue;
            MapLocation loc = rc.getLocation().add(dir);
            if (!rc.onTheMap(loc)) continue;
            if (rc.canRemoveDirt(loc)) return dir;
        }
        return Direction.CENTER;
    }
    // ===== Reset navigator =====
    public void reset() {
        followingWall = false;
        hitDist = 0;
        startLoc = null;
        goalLoc = null;
        wallDir = Direction.CENTER;
        visitedLocations.clear();
    }
}
