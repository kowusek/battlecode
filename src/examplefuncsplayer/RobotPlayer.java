package examplefuncsplayer;

import battlecode.common.*;

import java.util.*;

import java.util.stream.Stream;


/**
 * RobotPlayer is the class that describes your main robot strategy.
 * The run() method inside this class is like your main function: this is what we'll call once your robot
 * is created!
 */
public class RobotPlayer {
    /**
     * We will use this variable to count the number of turns this robot has been alive.
     * You can use static variables like this to save any information you want. Keep in mind that even though
     * these variables are static, in Battlecode they aren't actually shared between your robots.
     */
    static int turnCount = 0;
    public static boolean notBuild = false;
    /**
     * A random number generator.
     * We will use this RNG to make some random moves. The Random class is provided by the java.util.Random
     * import at the top of this file. Here, we *seed* the RNG with a constant number (6147); this makes sure
     * we get the same sequence of numbers every time this code is run. This is very useful for debugging!
     */
    static final Random rng = new Random(6147);
    static Bug2Navigator nav = new Bug2Navigator();

    /** Array containing all the possible movement directions. */
    static final Direction[] directions = {
            Direction.NORTH,
            Direction.NORTHEAST,
            Direction.EAST,
            Direction.SOUTHEAST,
            Direction.SOUTH,
            Direction.SOUTHWEST,
            Direction.WEST,
            Direction.NORTHWEST,
    };

    /**
     * run() is the method that is called when a robot is instantiated in the Battlecode world.
     * It is like the main function for your robot. If this method returns, the robot dies!
     *
     * @param rc  The RobotController object. You use it to perform actions from this robot, and to get
     *            information on its current status. Essentially your portal to interacting with the world.
     **/
    @SuppressWarnings("unused")
    public static void run(RobotController rc) throws GameActionException {
        // Hello world! Standard output is very useful for debugging.
        // Everything you say here will be directly viewable in your terminal when you run a match!
        System.out.println("I'm alive");

        // You can also use indicators to save debug notes in replays.
        rc.setIndicatorString("Hello world!");
        rc.getType();
        while (true) {
            turnCount += 1;  // We have now been alive for one more turn!
            if (rc.getType() == UnitType.RAT_KING) {
                MapLocation buildLocation = rc.getLocation().add(Direction.NORTH).add(Direction.NORTH);
                //System.out.println("outside " + buildLocation + " "+ rc.canBuildRat(buildLocation) );
                if (!notBuild && rc.canBuildRat(buildLocation)) {
                    //System.out.println("inside");
                    rc.buildRat(buildLocation);
                    notBuild = true;
                }
                Clock.yield();
            } else {
                runBabyRat(rc);
            }
        }
    }
    public static void runBabyRat(RobotController rc) throws GameActionException{
        try {
            MapLocation targetLocation = new MapLocation(0, 0);
            Direction d = nav.nextMove(rc, rc.getLocation(), targetLocation);

            if (d != Direction.CENTER && rc.canTurn(d) && rc.canMove(d)) {
                rc.turn(d);
                rc.moveForward();
            }

        }catch (GameActionException e) {
            System.out.println("GameActionException");
            e.printStackTrace();
        } catch (Exception e) {
            System.out.println("Exception");
            e.printStackTrace();
        } finally {
            Clock.yield();
        }

    }
}