package examplefuncsplayer;
import battlecode.common.*;

public class KingRat extends Rat{
    protected static boolean notBuild = false;
    @Override
    public void run(RobotController rc) {
        try {
            MapLocation buildLocation = rc.getLocation().add(Direction.NORTH).add(Direction.NORTH);
            //System.out.println("outside " + buildLocation + " "+ rc.canBuildRat(buildLocation) );
            if (!notBuild && rc.canBuildRat(buildLocation)) {
                //System.out.println("inside");
                rc.buildRat(buildLocation);
                notBuild = true;
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
