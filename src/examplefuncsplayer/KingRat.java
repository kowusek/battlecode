package examplefuncsplayer;
import battlecode.common.*;

public class KingRat extends Rat{
    public int toBuild = 15;
    @Override
    public void run(RobotController rc) {
        try {
            MapLocation buildLocation = rc.getLocation().add(Direction.NORTH).add(Direction.NORTH);
            //System.out.println("outside " + buildLocation + " "+ rc.canBuildRat(buildLocation) );
            if (toBuild > 0 && rc.canBuildRat(buildLocation)) {
                //System.out.println("inside");
                rc.buildRat(buildLocation);
                toBuild--;
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
