package byow.lab12;

import byow.TileEngine.TERenderer;
import byow.TileEngine.TETile;
import byow.TileEngine.Tileset;

import java.util.Random;

/**
 * Draws a world consisting of hexagonal regions.
 */
public class HexWorld {
    /** Width of the world */
    private static final int WIDTH = 80;
    /** Height of the world */
    private static final int HEIGHT = 60;
    /** size of the hexagon */
    private static final int S = 4;
    /** size of tessellate */
    private static final int TES_SIZE = 4;
    private static final long SEED = 94976118;
    private static final Random RANDOM = new Random(SEED);

    /**
     * Returns a RANDOM tile
     */
    private static TETile randomTile() {
        int tileNum = RANDOM.nextInt(7);

        return switch (tileNum) {
            case 0 -> Tileset.WALL;
            case 1 -> Tileset.FLOWER;
            case 2 -> Tileset.GRASS;
            case 3 -> Tileset.WATER;
            case 4 -> Tileset.SAND;
            case 5 -> Tileset.MOUNTAIN;
            case 6 -> Tileset.TREE;
            default -> Tileset.FLOOR;
        };
    }

    /**
     * Draws a single Hexagon starting from the given x and y coordinates
     */
    public static void addHexagon(int x, int y, TETile[][] world) {
        int n = 3*S - 2;
        int temp = x;
        TETile t = randomTile();

        // Adds the upper half of the hexagon
        for (int i = S; i <= n; i += 2, ++y, x = --temp) {
            for (int j = i; j > 0; --j, ++x) {
                world[x][y] = t;
            }
        }

        temp = ++x;

        // Adds the lower half of the hexagon
        for (int i = n; i >= S; i -= 2, ++y, x = ++temp) {
            for (int j = i; j > 0; --j, ++x) {
                world[x][y] = t;
            }
        }
    }

    /** Draws a single column of columnSize number of hexagons on the given x-axis */
    public static void drawColumn(int x, int y, int columnSize, TETile[][] world) {
        for (int i = 0; i < columnSize; ++i, y += 2* S) {
            addHexagon(x, y, world);
        }
    }

    /**
     * Draws the complete side using recursion of the passed x and y coordinates
     * @param side Pass True for left side and False for right side.
     */
    public static void drawSide(int x, int y, int columnSize, TETile[][] world, boolean side) {
        if (columnSize < TES_SIZE)
            return;

        if (side) {
            x -= 2*S - 1;
        } else {
            x += 2*S - 1;
        }

        drawColumn(x, y + S, columnSize, world);
        drawSide(x, y + S, columnSize - 1, world, side);
    }

    /**
     * Builds a tessellate of hexagon shapes
     * @param world the array in which tile locations should be stored
     */
    public static void createWorld(TETile[][] world) {
        // Creates a world of nothingness
        for (int i = 0; i < WIDTH; ++i) {
            for (int j = 0; j < HEIGHT; ++j) {
                world[i][j] = Tileset.NOTHING;
            }
        }

        // To get the tessellate in center of the world
        int initialTess = 2*TES_SIZE - 1;
        int initialX = (WIDTH - S)/2, initialY = (HEIGHT/2 - (initialTess * S));

        drawColumn(initialX, initialY, initialTess, world);
        drawSide(initialX, initialY, initialTess - 1, world, true);
        drawSide(initialX, initialY, initialTess - 1, world, false);
    }

    public static void main(String[] args) {
        TERenderer ter = new TERenderer();
        ter.initialize(WIDTH, HEIGHT);

        TETile[][] world = new TETile[WIDTH][HEIGHT];
        try {
            createWorld(world);
        } catch (ArrayIndexOutOfBoundsException e) {
            System.out.println("Please Increase the size of the world!");
        }

        ter.renderFrame(world);
    }
}
