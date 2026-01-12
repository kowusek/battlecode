package examplefuncsplayer;

import battlecode.common.*;
import java.util.*;

import java.util.Random;

public class ChildRat extends Rat {

    static MapLocation targetLocation = null;
    static MapLocation ratKingLocation = null;
    static MapLocation mineLocation = null;
    List<MapLocation> mineLocations = new ArrayList<>();

    @Override
		// Zwraca czy zmienić stan
    public boolean run(RobotController rc) {
        try {
            if (rng == null){
                rng = new Random(rc.getID());
            }
            if (memoryMap == null) {
                initMemoryMap(rc);
            }
            updateMemoryMap(rc);
            ratKingLocation = findNearestRatKing(rc);
            senseNearbyCats(rc);
            senseNearbyMouses(rc);
            if (rc.canBecomeRatKing()) {
                // rc.becomeRatKing();
								// return true;
            }
            if (nearestEnemyMouseLocation != null && rc.canAttack(nearestEnemyMouseLocation)) {
								System.out.println("Attak!");
                rc.attack(nearestEnemyMouseLocation, rc.getRawCheese());
            }
            if (nearestCatLocation != null) {
                if(rc.canPlaceCatTrap(rc.getLocation().add(rc.getLocation().directionTo(nearestCatLocation)))){
                    rc.placeCatTrap(rc.getLocation().add(rc.getLocation().directionTo(nearestCatLocation)));
                }
                if(rc.canAttack(nearestCatLocation)){
										System.out.println("Attak cat!");
                    rc.attack(nearestCatLocation, rc.getRawCheese());
                }
            }
            handleCheeseLogic(rc);
            targetLocation = determineTargetLocation(rc);
            moveToTarget(rc, targetLocation);

        } catch (GameActionException e) {
            System.out.println("GameActionException");
            e.printStackTrace();
        } catch (Exception e) {
            System.out.println("Exception");
            e.printStackTrace();
        }
				return false;
    }

    public void handleCheeseLogic(RobotController rc) throws GameActionException {
        if (rc.canPickUpCheese(rc.getLocation())) {
            rc.pickUpCheese(rc.getLocation());
        }
        if (ratKingLocation != null && rc.canTransferCheese(ratKingLocation, rc.getRawCheese())) {
            rc.transferCheese(ratKingLocation, rc.getRawCheese());
        }
    }

    public MapLocation determineTargetLocation(RobotController rc) throws GameActionException {
        MapLocation target = targetLocation;
				
				// Jak mam ser to wracam
        if (rc.getRawCheese() >= 40){
            return ratKingLocation;
        }
				
				// WPP jak potrzebujemy bardzo sera to szukam z okolicznych kopalni
        if (rc.getGlobalCheese() < 1000 && !mineLocations.isEmpty() && !(
							target != null && target.x >= 0 && target.x < rc.getMapWidth() &&
																target.y >= 0 && target.y < rc.getMapHeight() &&
							this.memoryMap[target.x][target.y] == StaticTileTypes.CHEESE.ordinal())) {
						MapLocation mine = getTheClosestMine(rc);
						if (!rc.getLocation().isWithinDistanceSquared(mine, 36)) {
								return mine;
						}
        }

				/// WPP Jak widzę ser to do niego idę
        MapLocation cheeseLocation = findNearestStaticTileType(rc, StaticTileTypes.CHEESE);
        if (cheeseLocation != null && rc.getRawCheese() < 20) {
						return cheeseLocation;
        }
				
				/// WPP odchodzę od innych szczurów
        MapLocation runLocation = runAwayFromOtherRats(rc);
        if (runLocation != null) {
						// TODO Random roaming
            return runAwayFromKings(rc);
        }
        if (target == null || rc.getLocation().isWithinDistanceSquared(target, 4)) {
            return runAwayFromKings(rc);
        }
        return target;
    }

    private MapLocation getTheClosestMine(RobotController rc) {
        int minDistance = Integer.MAX_VALUE;
        MapLocation nearestMine = null;
        for (MapLocation mine: mineLocations){
            int dist = mine.distanceSquaredTo(rc.getLocation());
            if (dist < minDistance) {
                minDistance = dist;
                nearestMine = mine;
            }
        }
        return nearestMine;
    }

		protected static MapLocation runAwayFromKings(RobotController rc) throws GameActionException{
				int mapWidth = rc.getMapWidth();
				int mapHeight = rc.getMapHeight();
				int i = 0;
				int sumX = 0;
				int sumY = 0;
				while (true) {
						int kingX = rc.readSharedArray(i);
						if (kingX == 0) {
								break;
						}
						int kingY = rc.readSharedArray(i + 1);
						sumX += (mapWidth - kingX);
						sumY += (mapHeight - kingY);
						i += 2;
				}
        int randX = rng.nextInt(mapWidth);
        int randY = rng.nextInt(mapHeight);
				if (rng.nextInt(2) == 1) {
						randX = rng.nextInt(10) + (sumX / (i / 2)) - 5;
				} else {
						randY = rng.nextInt(10) + (sumY / (i / 2)) - 5;
				}
				
				return new MapLocation(randX, randY);
		}

    public static MapLocation runToRandomLocation(RobotController rc) {
        int mapWidth = rc.getMapWidth();
        int mapHeight = rc.getMapHeight();
        int randX = rng.nextInt(mapWidth);
        int randY = rng.nextInt(mapHeight);
        //System.out.println("randX " + randX + "randY" + randY);
        return new MapLocation(randX, randY);
    }

    public MapLocation runAwayFromOtherRats(RobotController rc) {
        MapLocation myLocation = rc.getLocation();
        MapLocation nearestLocation = null;
        int minDistance = 10;
        for (RobotInfo rat : rc.senseNearbyRobots()) {
            if (rat.getTeam() != rc.getTeam()) continue;
            int distance = myLocation.distanceSquaredTo(rat.getLocation());
            if (distance < minDistance) {
                minDistance = distance;
                nearestLocation = rat.getLocation();
            }
        }
        if (nearestLocation != null) {
            MapLocation furthestLocation = findFurthestLocationAwayFrom(rc, nearestLocation);
            // System.out.print("Running away to " + furthestLocation);
            return furthestLocation;
        }
        return null;
    }
    public MapLocation findNearestRatKing(RobotController rc) throws GameActionException {
        MapLocation myLocation = rc.getLocation();
        MapLocation nearestKing = null;
        int minDistance = Integer.MAX_VALUE;
        for (int i = 0; i < 5; i++) {
            int offset = i * 2;
            int x = rc.readSharedArray(offset);
            int y = rc.readSharedArray(offset + 1);

            // Skip if coordinates are 0,0 (uninitialized)
            if (x == 0 && y == 0) {
                continue;
            }
            MapLocation kingLocation = new MapLocation(x, y);
            int distance = myLocation.distanceSquaredTo(kingLocation);
            if (distance < minDistance) {
                minDistance = distance;
                nearestKing = kingLocation;
            }
        }
        return nearestKing;
    }

    public MapLocation runAwayFromRatKing(RobotController rc) {
        MapLocation myLocation = rc.getLocation();
        MapLocation nearestLocation = null;
        int minDistance = 10;
        for (RobotInfo rat : rc.senseNearbyRobots()) {
            if (rat.getType() != UnitType.RAT_KING && rat.team != rc.getTeam()) {
                continue;
            }
            int distance = myLocation.distanceSquaredTo(rat.getLocation());
            if (distance < minDistance) {
                minDistance = distance;
                nearestLocation = rat.getLocation();
            }
        }
        if (nearestLocation != null) {
            MapLocation furthestLocation = findFurthestLocationAwayFrom(rc, nearestLocation);
            // System.out.print("Running away to " + furthestLocation);
            return furthestLocation;
        }
        return null;
    }
    protected MapLocation findNearestStaticTileType(RobotController rc, StaticTileTypes staticTileType) {
        int myX = rc.getLocation().x;
        int myY = rc.getLocation().y;

        int minDistance = Integer.MAX_VALUE;
        int radius = 4;
        MapLocation nearestTileType = null;
        int width = rc.getMapWidth();
        int height = rc.getMapHeight();

        for (int dx = -radius; dx <= radius; dx++) {
            int x = myX + dx;
            if (x < 0 || x >= width) continue;

            for (int dy = -radius; dy <= radius; dy++) {
                int y = myY + dy;
                if (y < 0 || y >= height) continue;

                if (memoryMap[x][y] == staticTileType.ordinal()) {
                    int dist = dx * dx + dy * dy;

                    if (dist < minDistance) {
                        minDistance = dist;
                        nearestTileType = new MapLocation(x, y);
                    }
                }
            }
        }
        return nearestTileType;
    }
    private void updateMemoryMap(RobotController rc) {
        MapInfo[] sensed = rc.senseNearbyMapInfos();
        for (MapInfo info : sensed) {
            MapLocation loc = info.getMapLocation();
            int x = loc.x;
            int y = loc.y;
            if (info.isDirt()){
                memoryMap[x][y] = StaticTileTypes.DIRT.ordinal();
            }else if (info.isWall()){
                memoryMap[x][y] = StaticTileTypes.WALL.ordinal();
            }
            else if (info.hasCheeseMine()){
                memoryMap[x][y] = StaticTileTypes.MINE.ordinal();
                mineLocations.add(new MapLocation(x, y));
            }
            else if (info.getCheeseAmount()> 0){
                memoryMap[x][y] = StaticTileTypes.CHEESE.ordinal();
            }else {
                memoryMap[x][y] = StaticTileTypes.FREE.ordinal();
            }
        }
    }

    private void initMemoryMap(RobotController rc) {
        int mapWidth = rc.getMapWidth();
        int mapHeight = rc.getMapHeight();
        memoryMap = new int[mapWidth][mapHeight];
    }
}
