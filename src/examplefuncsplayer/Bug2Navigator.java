package examplefuncsplayer;

import battlecode.common.*;

public class Bug2Navigator {

    private boolean followingWall = false;
    private int hitDist = 0;

    private MapLocation startLoc = null;
    private MapLocation goalLoc = null;

    private Direction wallDir = Direction.CENTER;

    // ===== Main Entry =====
    public Direction nextMove(
            RobotController rc,
            MapLocation current,
            MapLocation goal
    ) {

        // New goal → reset
        if (goalLoc == null || !goal.equals(goalLoc)) {
            reset();
            goalLoc = goal;
            startLoc = current;
        }

        // ✅ If already adjacent to goal (rat king), STOP
        if (current.isAdjacentTo(goal)) {
            followingWall = false;
            return Direction.CENTER;
        }

        Direction toGoal = current.directionTo(goal);

        // ===== NOT FOLLOWING WALL =====
        if (!followingWall) {
            if (toGoal != Direction.CENTER && rc.canMove(toGoal)) {
                return toGoal;
            }

            // Hit obstacle
            followingWall = true;
            hitDist = current.distanceSquaredTo(goal);
            wallDir = toGoal.rotateRight(); // right-hand rule
            return followWall(rc);
        }

        // ===== FOLLOWING WALL =====
        Direction move = followWall(rc);

        // ✅ Correct Bug2 exit condition
        if (onMLine(startLoc, goal, current)
                && current.distanceSquaredTo(goal) < hitDist
                && toGoal != Direction.CENTER
                && rc.canMove(toGoal)) {

            followingWall = false;
            return toGoal;
        }

        return move;
    }

    // ===== Wall Following =====
    private Direction followWall(RobotController rc) {

        Direction d = wallDir;
        for (int i = 0; i < 4; i++) {
            if (rc.canMove(d)) {
                wallDir = d.rotateRight();
                return d;
            }
            d = d.rotateLeft();
        }
        return Direction.CENTER;
    }

    // ===== M-line Check =====
    private boolean onMLine(
            MapLocation start,
            MapLocation goal,
            MapLocation cur
    ) {
        int dx1 = goal.x - start.x;
        int dy1 = goal.y - start.y;
        int dx2 = cur.x - start.x;
        int dy2 = cur.y - start.y;

        int cross = dx1 * dy2 - dy1 * dx2;

        // Allow small error due to grid discretization
        return Math.abs(cross) <= Math.max(Math.abs(dx1), Math.abs(dy1));
    }

    public void reset() {
        followingWall = false;
        hitDist = 0;
        startLoc = null;
        wallDir = Direction.CENTER;
    }
}
