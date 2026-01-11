package examplefuncsplayer;

import battlecode.common.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class KingRat extends Rat {
    protected int desiredRatCost = 40;
    static MapLocation targetLocation = null;
    static int mySharedArrayOffset = -1;
    
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

    @Override
    public void run(RobotController rc) {
        try {
            if (rng == null){
                rng = new Random(rc.getID());
            }
            writeLocationToSharedArray(rc, rc.getLocation());
            //senseNearbyCats(rc);
            buildRat(rc);
            targetLocation = determineTargetLocation(rc);
            //System.out.println("targetLocation "+ targetLocation);
            if (targetLocation != null) {
                System.out.println("moveToTarget " + targetLocation);
                moveToTarget(rc, targetLocation);
            }
            // System.out.println("cheese" + rc.getAllCheese() + "turn " + turnCount);
        } catch (GameActionException e) {
            System.out.println("GameActionException");
            e.printStackTrace();
        } catch (Exception e) {
            System.out.println("Exception");
            e.printStackTrace();
        } finally {
            Clock.yield();
        }
    }

    public void buildRat(RobotController rc) throws GameActionException {
        if (rc.getCurrentRatCost() > desiredRatCost) {
            return;
        }
        
        // King is 3x3, so we need to spawn rats around the perimeter
        // Try all directions in random order
        Direction[] shuffledDirections = directions.clone();
        for (int i = shuffledDirections.length - 1; i > 0; i--) {
            int j = rng.nextInt(i + 1);
            Direction temp = shuffledDirections[i];
            shuffledDirections[i] = shuffledDirections[j];
            shuffledDirections[j] = temp;
        }
        
        MapLocation kingCenter = rc.getLocation();
        for (Direction dir : shuffledDirections) {
            // Move 2 tiles in the direction to get outside the 3x3 area
            MapLocation buildLocation = kingCenter.add(dir).add(dir);
            if (rc.canBuildRat(buildLocation)) {
                rc.buildRat(buildLocation);
                return;
            }
        }
    }

    public void writeLocationToSharedArray(RobotController rc, MapLocation location)
            throws GameActionException {
        // Find an empty slot if we don't have one yet
        if (mySharedArrayOffset == -1) {
            for (int i = 0; i < 5; i++) {
                int offset = i * 2;
                int x = rc.readSharedArray(offset);
                System.out.print("Read 10th slot in shared mem: " + rc.readSharedArray(10));
                int y = rc.readSharedArray(offset + 1);
                // Empty slot found (uninitialized = 0,0)
                if (x == 0 && y == 0) {
                    mySharedArrayOffset = offset;
                    break;
                }
            }
        }
        // Write location to our reserved slot
        if (mySharedArrayOffset != -1) {
            rc.writeSharedArray(mySharedArrayOffset, location.x);
            rc.writeSharedArray(mySharedArrayOffset + 1, location.y);
        }
    }

    public MapLocation determineTargetLocation(RobotController rc) {
        MapLocation target = targetLocation;
        MapLocation runLocation = runAwayFromCats(rc);
        if( runLocation != null){
            System.out.println("runAwayFromCats runLocation " + runLocation);
            target = runLocation;
        }
        return target;
    }

    public MapLocation runAwayFromCats(RobotController rc) {
        MapLocation myLocation = rc.getLocation();
        MapLocation catLocation = null;
				int directionX = 0;
				int directionY = 0;
				boolean foundCat = false;

				// Find direction where there are no seen cats
        for (RobotInfo robot : rc.senseNearbyRobots()) {
            if (robot.type != UnitType.CAT) {
                continue;
            }
						
						catLocation = robot.getLocation();
						directionX += myLocation.x - catLocation.x;
						directionY += myLocation.y - catLocation.y;
						foundCat = true;
        }
				if (!foundCat) {
						return null;
				}
				
				int locX = directionX + myLocation.x;
				int locY = directionY + myLocation.y;
				
				System.out.print("Running away to " + locX + " " + locY);
        return new MapLocation(locX, locY);
    }
}
