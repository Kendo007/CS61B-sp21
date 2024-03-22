package byow.lab13;

import static byow.Core.RandomUtils.*;
import edu.princeton.cs.introcs.StdDraw;

import java.awt.Color;
import java.awt.Font;
import java.util.Random;

public class MemoryGame {
    /** The width of the window of this game. */
    private final int width;
    /** The height of the window of this game. */
    private final int height;
    /** The current round the user is on. */
    private int round;
    /** The Random object used to randomly generate Strings. */
    private final Random rand;
    /** Whether or not the game is over. */
    private boolean gameOver;
    /** Whether or not it is the player's turn. Used in the last section of the
     * spec, 'Helpful UI'. */
    private boolean playerTurn;
    /** The characters we generate random Strings from. */
    private final int OFFSET = 1;
    private static final char[] CHARACTERS = "abcdefghijklmnopqrstuvwxyz".toCharArray();
    /** Encouraging phrases. Used in the last section of the spec, 'Helpful UI'. */
    private static final String[] ENCOURAGEMENT = {"You can do this!", "I believe in you!",
                                                   "You got this!", "You're a star!", "Go Bears!",
                                                   "Too easy for you!", "Wow, so impressive!"};

    public static void main(String[] args) {
        if (args.length < 1) {
            System.out.println("Please enter a seed");
            return;
        }

        long seed = Long.parseLong(args[0]);
        MemoryGame game = new MemoryGame(40, 40, seed);
        game.startGame();
    }

    public MemoryGame(int width, int height, long seed) {
        /* Sets up StdDraw so that it has a width by height grid of 16 by 16 squares as its canvas
         * Also sets up the scale so the top left is (0,0) and the bottom right is (width, height)
         */
        this.width = width;
        this.height = height;
        this.rand = new Random(seed);
        StdDraw.setCanvasSize(this.width * 16, this.height * 16);
        Font font = new Font("Monaco", Font.BOLD, 30);
        StdDraw.setFont(font);
        StdDraw.setPenColor(Color.WHITE);
        StdDraw.setXscale(0, this.width);
        StdDraw.setYscale(0, this.height);
        StdDraw.clear(Color.BLACK);
        StdDraw.enableDoubleBuffering();
    }

    /** Generates random string of letters of length n */
    public String generateRandomString(int n) {
        StringBuilder s = new StringBuilder();

        for (int i = 0; i < n; ++i) {
            s.append(CHARACTERS[uniform(rand, CHARACTERS.length)]);
        }

        return s.toString();
    }

    /** Displays all the information on the screen */
    public void drawFrame(String s) {
        StdDraw.clear(Color.BLACK);

        // If game is not over, display relevant game information at the top of the screen
        StdDraw.setFont(new Font("Monaco", Font.PLAIN ,22));
        StdDraw.textLeft(OFFSET, height - OFFSET, "Round: " + round);

        String currentTask = playerTurn ? "Type!" : "Watch!";
        StdDraw.text(width / 2, height - OFFSET, currentTask);
        StdDraw.textRight(width - OFFSET, height - OFFSET, ENCOURAGEMENT[uniform(rand, ENCOURAGEMENT.length)]);

        StdDraw.line(0, height - OFFSET - 1, width, height - OFFSET - 1);

        // Take the string and display it in the center of the screen
        StdDraw.setFont(new Font("Monaco", Font.BOLD, 30));
        StdDraw.text((double) width / 2, (double) height / 2, s);
        StdDraw.show();
    }

    public void flashSequence(String letters) {
        // Display each character in letters, making sure to blank the screen between letters
        for (int i = 0; i < letters.length(); ++i) {
            drawFrame(Character.toString(letters.charAt(i)));
            StdDraw.pause(1000);

            if (i == letters.length() - 1) {
                playerTurn = true;
            }

            drawFrame("");
            StdDraw.pause(500);
        }
    }

    public String solicitNCharsInput(int n) {
        // Read n letters of player input
        int count = 0;
        StringBuilder s = new StringBuilder();

        while (count < n) {
            if (StdDraw.hasNextKeyTyped()) {
                s.append(StdDraw.nextKeyTyped());
                drawFrame(s.toString());
                ++count;
            }
        }

        return s.toString();
    }

    public void startGame() {
        // Set any relevant variables before the game starts
        gameOver = playerTurn = false;
        round = 0;

        String s;
        // Establish Engine loop
        while (!gameOver) {
            playerTurn = false;
            s = generateRandomString(++round);

            drawFrame("Round: " + round);
            StdDraw.pause(500);

            flashSequence(s);

            gameOver = !s.equals(solicitNCharsInput(round));
        }

        drawFrame("Game Over! You made it to round: " + round);
    }

}
