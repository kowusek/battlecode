package examplefuncsplayer;
import battlecode.common.*;

public abstract class Rat {
    protected Bug2Navigator nav = new Bug2Navigator();
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
}
