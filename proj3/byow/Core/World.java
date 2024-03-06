package byow.Core;

import byow.TileEngine.TERenderer;
import byow.TileEngine.TETile;
import byow.TileEngine.Tileset;

import java.util.Random;

import static byow.Core.Engine.*;
import static byow.Core.RandomUtils.*;

public class World {
    /**
     * Percentage of how much of the total world area is covered by rooms.
     * Increase this to get more rooms.
     */
    private static final int ROOM_PERCENT = 50;
    private static final int ROOM_AREA = (HEIGHT * WIDTH) * ROOM_PERCENT / 100;
    /** The minimum width of a room */
    private static final int MIN_WIDTH = 4;
    /** The maximum width of a room */
    private static final int MAX_WIDTH = WIDTH / 6;
    /** The minimum height of a room */
    private static final int MIN_HEIGHT = 4;
    /** The maximum height of a room */
    private static final int MAX_HEIGHT = HEIGHT / 6;

    /** Adds a random room to the given world */
    private static Room addRandomRoom(TETile[][] world, Random r, Room lastRoom) {
        int height = uniform(r, MIN_HEIGHT, MAX_HEIGHT);
        int width = uniform(r, MIN_WIDTH, MAX_WIDTH);
        int x = uniform(r, 1, WIDTH - width);
        int y = uniform(r, 1, HEIGHT - height);

        return new Room(new Position(x, y), width, height, world, lastRoom);
    }

    public static void createWorld(TETile[][] world, Random r) {
        int currArea = 0;
        Room lastRoom = null;

        // Fills the world with nothingness
        for (int i = 0; i < WIDTH; ++i) {
            for (int j = 0; j < HEIGHT; ++j) {
                world[i][j] = Tileset.NOTHING;
            }
        }

        while (currArea < ROOM_AREA) {
            lastRoom = addRandomRoom(world, r, lastRoom);
            currArea += lastRoom.getArea();
        }
    }

    public static void main(String[] args) {
        TERenderer ter = new TERenderer();
        ter.initialize(WIDTH, HEIGHT);

        TETile[][] world = new TETile[WIDTH][HEIGHT];

        createWorld(world, new Random(5));

        ter.renderFrame(world);
    }
}
