package examplefuncsplayer;

import battlecode.common.*;

import java.util.Random;

public class ChildRat extends Rat{
    public static enum ChildRatState {
        INITIALIZE,
        GO_TO_LOCATION,
        ATTACK,
    }
    public int waitTime = 0;
    public static ChildRatState currentChildRatState = ChildRatState.INITIALIZE;

    @Override
    public void run(RobotController rc) {
        try {
            if (memoryMap == null){
                initMemoryMap(rc);
            }
            updateMemoryMap(rc);
            //debugMemoryMap(rc);
            switch (currentChildRatState){
                case INITIALIZE:
                    currentChildRatState = ChildRatState.GO_TO_LOCATION;
                case GO_TO_LOCATION:
                    goToLocation(rc);
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

    private void goToLocation(RobotController rc) throws GameActionException {
        MapLocation loc1 = new MapLocation(0, 0);
        MapLocation loc2 = new MapLocation(57, 25);
        // Randomly pick one
        MapLocation targetLocation = (rc.getID() % 2 == 0) ? loc1 : loc2;
        Bug2Navigator.Action action = nav.nextAction(rc, rc.getLocation(), targetLocation, true);
        System.out.println("action " + action.type + " " + action.dir);
        switch (action.type) {
            case MOVE:
                if (rc.canMove(action.dir)) {
                    rc.turn(action.dir);
                    rc.moveForward();
                }
                waitTime = 0;
                break;
            case TURN:
                if (rc.canTurn(action.dir)) {
                    rc.turn(action.dir);
                }
                waitTime = 0;
                break;
            case DELETE_DIRT:
                if (rc.canRemoveDirt(rc.getLocation().add(action.dir))) {
                    rc.removeDirt(rc.getLocation().add(action.dir));
                    nav.reset();
                }
                waitTime = 0;
                break;
            case OCCUPIED:
                waitTime = 0;
                break;
            case FINISHED:
                waitTime = 0;
                break;
            case WAIT:
                // do nothing
                waitTime++;
                if (waitTime > 3){
                    nav.reset();
                }
                break;
        }
    }

    private void updateMemoryMap(RobotController rc) {
        MapInfo[] sensed = rc.senseNearbyMapInfos();
        //RobotInfo[] nearbyEnemies = rc.senseNearbyRobots(rc.getType().getVisionRadiusSquared(), rc.getTeam().opponent());
        //RobotInfo[] nearbyCats = rc.senseNearbyRobots(rc.getType().getVisionRadiusSquared(), Team.NEUTRAL);
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
        // Initially unknown
        for (int x = 0; x < mapWidth; x++) {
            for (int y = 0; y < mapHeight; y++) {
                memoryMap[x][y] = StaticTileTypes.UNKNOWN.ordinal();
            }
        }
    }

    private void debugMemoryMap(RobotController rc) {
        int mapWidth = rc.getMapWidth();
        int mapHeight = rc.getMapHeight();
        System.out.println();
        for (int y = 0; y <  mapHeight; y++) {  // print top to bottom
            for (int x = 0; x < mapWidth; x++) {
                System.out.print("x "+ x + " y " + y);
                switch (memoryMap[x][y]) {
                    case 0: System.out.print("D "); break;  // DIRT
                    case 1: System.out.print(". "); break;  // FREE
                    case 2: System.out.print("C "); break;  // CHEESE
                    case 3: System.out.print("? "); break;  // UNKNOWN
                    case 4: System.out.print("X "); break;  // WALL
                    case 5: System.out.print("M "); break;  // MINE
                }
            }
            System.out.println();
        }
    }
}
