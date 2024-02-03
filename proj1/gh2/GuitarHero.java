package gh2;

import edu.princeton.cs.algs4.StdAudio;
import edu.princeton.cs.algs4.StdDraw;

public class GuitarHero {

    /**
     * A client that uses the synthesizer package to replicate a plucked guitar string sound
     */
    private static final String keyboard = "q2we4r5ty7u8i9op-[=zxdcfvgbnjmk,.;/' ";
    private static final GuitarString[] strings = new GuitarString[37];

    public static void main(String[] args) {

        for (int i = 0; i < keyboard.length(); ++i) {
            double concert = 440 * Math.pow(2, (i - 24) / 12.0);
            strings[i] = new GuitarString(concert);
        }

        while (true) {
            int index;
            /* check if the user has typed a key; if so, process it */
            if (StdDraw.hasNextKeyTyped()) {
                char key = StdDraw.nextKeyTyped();
                index = keyboard.indexOf(key);

                if (index < 0) { continue; }

                strings[index].pluck();
            }

            /* compute the superposition of samples */
            double sample = 0.0;
            for (int i = 0; i < keyboard.length(); ++i) {
                sample += strings[i].sample();
            }

            /* play the sample on standard audio */
            StdAudio.play(sample);

            /* advance the simulation of each guitar string by one step */
            for (int i = 0; i < keyboard.length(); ++i) {
                strings[i].tic();
            }
        }
    }
}
