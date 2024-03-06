package byow.Core;

import byow.TileEngine.TETile;

import static byow.Core.Engine.*;
import static byow.TileEngine.Tileset.*;
import static byow.TileEngine.Tileset.WALL;

public class Position {
    protected int x;
    protected int y;

    Position(int x, int y) {
        this.x = x;
        this.y = y;
    }

    /** Shifts the current position with the given coordinates
     * @return A new shifted position
     */
    public Position shift(int dx, int dy) {
        int nx = x + dx, ny = y + dy;

        if (nx >= WIDTH || ny >= HEIGHT) {
            throw new IllegalArgumentException("Out of the Current World Position");
        }

        return new Position(nx, ny);
    }

    public boolean equals(Position p) {
        return p.x == x && p.y == y;
    }

    /** A helper function used to construct the path between points */
    private static void connectPointsHelper(TETile[][] world, Position p1, Position p2) {
        for (int i = p2.x; i < p1.x; ++i) {
            if (world[i][p2.y - 1] == NOTHING) {
                world[i][p2.y - 1] = WALL;
            }

            world[i][p2.y] = FLOOR;

            if (world[i][p2.y + 1] == NOTHING) {
                world[i][p2.y + 1] = WALL;
            }
        }

        int end = Math.max(p1.y, p2.y);
        for (int i = Math.min(p1.y, p2.y); i <= end; ++i) {
            if (world[p2.x - 1][i] == NOTHING) {
                world[p2.x - 1][i] = WALL;
            }

            world[p2.x][i] = FLOOR;

            if (world[p2.x + 1][i] == NOTHING) {
                world[p2.x + 1][i] = WALL;
            }
        }
    }

    /** Connects two points in the given world and constructs a path between them */
    public static void connectPoints(TETile[][] world, Position p1, Position p2) {
        int lessX = Math.min(p1.x, p2.x);
        Position lessP = new Position(lessX, Math.min(p1.y, p2.y));

        if (lessP.equals(p1) || lessP.equals(p2)) {
            lessP = new Position(lessX, Math.max(p1.y, p2.y));
        }

        connectPointsHelper(world, p1, lessP);
        connectPointsHelper(world, p2, lessP);
    }
}
