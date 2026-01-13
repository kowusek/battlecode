package stateful;

import battlecode.common.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class KingRat extends Rat {
		static final int MAX_KINGS = 5;

    static final int KING_VARIABLES = 4;
    static final int KING_STATE_INDEX = 2;
    static final int KING_EPOCH_INDEX = 3;

		static final int GLOBAL_EPOCH_INDEX = KING_VARIABLES * MAX_KINGS;

    protected int desiredRatCost = 40;
    static MapLocation targetLocation = null;
    static int sharedArrayIndex = 0;
    static int creationEpoch = 0;
    static int turnsWaitedForDead = 0;
    
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

    public KingRat(RobotController rc) throws GameActionException {
        this.rng = new Random(rc.getID());

        for (this.sharedArrayIndex = 0; this.sharedArrayIndex < this.MAX_KINGS; this.sharedArrayIndex++) {
            int x = rc.readSharedArray(this.sharedArrayIndex * KING_VARIABLES);
            int y = rc.readSharedArray(this.sharedArrayIndex * KING_VARIABLES + 1);
            if (x == 0 && y == 0) {
								this.writeEpoch(rc);
								this.writeLocationToSharedArray(rc, rc.getLocation());
                break;
            }
        }

        if (this.sharedArrayIndex >= MAX_KINGS) {
						// Increase epoch
						this.creationEpoch = rc.readSharedArray(this.GLOBAL_EPOCH_INDEX) + 1;
            rc.writeSharedArray(this.GLOBAL_EPOCH_INDEX, this.creationEpoch);
        }
    }

		private void writeEpoch(RobotController rc) throws GameActionException {
				int currentEpoch = rc.readSharedArray(this.GLOBAL_EPOCH_INDEX) + 1;
				rc.writeSharedArray(this.sharedArrayIndex * this.KING_VARIABLES + KING_EPOCH_INDEX, currentEpoch);
		}

		private void checkAliveness(RobotController rc) throws GameActionException {
				if (this.sharedArrayIndex < MAX_KINGS) {
						this.writeEpoch(rc);
						return;
				}

				if (this.turnsWaitedForDead < 1) {
						this.turnsWaitedForDead++;
						return;
				}

				for (int i = 0; i < this.MAX_KINGS; i++) {
						int kingsEpoch = rc.readSharedArray(i * this.KING_VARIABLES + KING_EPOCH_INDEX);
						if (kingsEpoch >= this.creationEpoch) {
								continue;
						}

						this.sharedArrayIndex = i;
						this.writeLocationToSharedArray(rc, rc.getLocation());
						this.writeEpoch(rc);
						return;
				}
		}

    @Override
    public boolean run(RobotController rc) {
        try {
						checkAliveness(rc);

            buildRat(rc);

            targetLocation = determineTargetLocation(rc);
            if (targetLocation != null) {
                System.out.println("moveToTarget " + targetLocation);
                moveToTarget(rc, targetLocation);
            }
        } catch (GameActionException e) {
            System.out.println("GameActionException");
            e.printStackTrace();
        } catch (Exception e) {
            System.out.println("Exception");
            e.printStackTrace();
        }
				return false;
    }

    public void buildRat(RobotController rc) throws GameActionException {
        if (rc.getCurrentRatCost() > desiredRatCost) {
            return;
        }
				
				if (rc.getGlobalCheese() < 500 && rng.nextInt(100) > 30) {
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
        rc.writeSharedArray(this.sharedArrayIndex, location.x);
        rc.writeSharedArray(this.sharedArrayIndex + 1, location.y);
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
						System.out.print("Direction after finding a cat: " + directionX + " " + directionY);
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
