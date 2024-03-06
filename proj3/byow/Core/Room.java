package byow.Core;

import byow.TileEngine.TERenderer;
import byow.TileEngine.TETile;

import static byow.Core.Engine.*;
import static byow.Core.Position.*;
import static byow.TileEngine.Tileset.*;

public class Room {
    /** Increase this number to increase the probability of creating paths */
    private static final int PATHS_PROB = 3;
    /** Position of the top-left corner of the room */
    private final Position position;
    /** width of the room */
    private final int width;
    /** height of the room */
    private final int height;

    Room(Position p, int w, int h) {
        width = w;
        height = h;
        position = p;
    }

    Room(Position p, int w, int h, TETile[][] world, Room lastRoom) {
        this(p, w, h);
        addRoom(world, lastRoom);
    }

    /**
     * Adds walls around the current room in the given world also merges the given room if there
     * is an existing room in the way and also creates a path between two disjoint rooms
     */
    private void addWalls(TETile[][] world, Room lastRoom) {
        int x, y, overwrite = 0;

        // Adds walls if and only if it is not merging with any other room
        for (int i = position.y; i < position.y + height; i += (height - 1)) {
            for (int j = 0; j < width; ++j) {
                x = position.x + j;

                if (world[x][i] == NOTHING) {
                    world[x][i] = WALL;
                } else if (world[x][i] == WALL) {
                    if ((i == position.y && world[x][i - 1] == FLOOR)
                            || (i != position.y && world[x][i + 1] == FLOOR)) {
                        world[x][i] = FLOOR;
                    }
                } else {
                    ++overwrite;
                }
            }
        }

        for (int i = position.x; i < position.x + width; i += (width - 1)) {
            for (int j = 0; j < height; ++j) {
                y = position.y + j;

                if (world[i][y] == NOTHING) {
                    world[i][y] = WALL;
                } else if (world[i][y] == WALL) {
                    if ((i == position.x && world[i - 1][y] == FLOOR)
                            || (i != position.x && world[i + 1][y] == FLOOR)) {
                        world[i][y] = FLOOR;
                    }
                } else {
                    ++overwrite;
                }
            }
        }

        if (overwrite < PATHS_PROB && lastRoom != null) {
            this.connectRoom(lastRoom, world);
        }
    }

    /**
     * Adds the room to the given world
     */
    public void addRoom(TETile[][] world, Room lastRoom) {
        addWalls(world, lastRoom);

        for (int i = 1; i < width - 1; ++i) {
            for (int j = 1; j < height - 1; ++j) {
                world[position.x + i][position.y + j] = FLOOR;
            }
        }
    }

    /** Returns the center position of the room */
    public Position getCenter() {
        return new Position(position.x + width / 2, position.y + height / 2);
    }

    /** Returns the area of the room */
    public int getArea() {
        return width * height;
    }

    /** Creates a path between the current and the given room
     * @param otherRoom The other room which you want to connect
     */
    public void connectRoom(Room otherRoom, TETile[][] world) {
        Position p1 = this.getCenter();
        Position p2 = otherRoom.getCenter();

        connectPoints(world, p1, p2);
    }

    public static void main(String[] args) {
        TERenderer ter = new TERenderer();
        ter.initialize(WIDTH, HEIGHT);

        TETile[][] world = new TETile[WIDTH][HEIGHT];
        /*
        for (int i = 0; i < WIDTH; ++i) {
            for (int j = 0; j < HEIGHT; ++j) {
                world[i][j] = NOTHING;
            }
        }

        Room r1 = new Room(new Position(10, 25), 10, 15, world, null);
        Room r2 = new Room(new Position(20, 45), 13, 5, world, r1);
        Room r3 = new Room(new Position(30, 30), 15, 20, world, r2);
        */
        ter.renderFrame(world);
    }
}
