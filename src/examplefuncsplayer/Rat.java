package examplefuncsplayer;
import battlecode.common.*;

public abstract class Rat {
    protected Bug2Navigator nav = new Bug2Navigator();
    protected int turnCount = 0;
    protected int[][] memoryMap = null;
    protected final Direction[] directions = {
            Direction.NORTH,
            Direction.NORTHEAST,
            Direction.EAST,
            Direction.SOUTHEAST,
            Direction.SOUTH,
            Direction.SOUTHWEST,
            Direction.WEST,
            Direction.NORTHWEST,
    };
    protected enum StaticTileTypes {
        DIRT,
        FREE,
        CHEESE,
        UNKNOWN,
        WALL,
        MINE,
    }
    public abstract void run(RobotController rc);
}
