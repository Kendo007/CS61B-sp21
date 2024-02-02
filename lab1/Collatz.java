/**
 * Class that prints the Collatz sequence starting from a given number.
 *
 * @author Kheyanshu Garg
 */
public class Collatz {

    /**
     * Returns a number of Collatz sequence based on the given number eg 5 -> 16, 12 -> 6
     */
    public static int nextNumber(int n) {
        if (n % 2 != 0)
            return 3 * n + 1;
        else
            return n / 2;
    }

    public static void main(String[] args) {
        int n = 5;
        System.out.print(n + " ");
        while (n != 1) {
            n = nextNumber(n);
            System.out.print(n + " ");
        }
        System.out.println();
    }
}

