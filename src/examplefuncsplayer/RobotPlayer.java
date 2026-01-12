package examplefuncsplayer;

import battlecode.common.*;

import java.util.*;

public class RobotPlayer {

    public static Rat myRat = null;
    @SuppressWarnings("unused")
    public static void run(RobotController rc) throws GameActionException {
        rc.getType();
        if (myRat == null){
            if (rc.getType() == UnitType.RAT_KING){
                myRat = new KingRat();
            }else{
                myRat = new ChildRat();
            }
        }
        while (true) {
            if (myRat.run(rc)) {
							if (rc.getType() == UnitType.RAT_KING) {
									myRat = new KingRat();
							} else {
									myRat = new ChildRat();
							}
						}
						
						Clock.yield();
        }
    }
}
