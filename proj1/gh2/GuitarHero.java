package gh2;

import edu.princeton.cs.algs4.StdAudio;
import edu.princeton.cs.algs4.StdDraw;

public class GuitarHero {

    /**
     * A client that uses the synthesizer package to replicate a plucked guitar string sound
     */
    private static final String KEYBOARD = "q2we4r5ty7u8i9op-[=zxdcfvgbnjmk,.;/' ";
    private static final GuitarString[] STRINGS = new GuitarString[37];

    public static void main(String[] args) {

        for (int i = 0; i < KEYBOARD.length(); ++i) {
            double concert = 440 * Math.pow(2, (i - 24) / 12.0);
            STRINGS[i] = new GuitarString(concert);
        }

        while (true) {
            int index;
            /* check if the user has typed a key; if so, process it */
            if (StdDraw.hasNextKeyTyped()) {
                char key = StdDraw.nextKeyTyped();
                index = KEYBOARD.indexOf(key);

                if (index < 0) {
                    continue;
                }

                STRINGS[index].pluck();
            }

            /* compute the superposition of samples */
            double sample = 0.0;
            for (int i = 0; i < KEYBOARD.length(); ++i) {
                sample += STRINGS[i].sample();
            }

            /* play the sample on standard audio */
            StdAudio.play(sample);

            /* advance the simulation of each guitar string by one step */
            for (int i = 0; i < KEYBOARD.length(); ++i) {
                STRINGS[i].tic();
            }
        }
    }
}
