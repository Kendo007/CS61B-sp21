package gitlet;

import java.io.File;
import java.io.IOException;

/** Driver class for Gitlet, a subset of the Git version-control system.
 *  @author Kheyanshu Garg
 */
public class Main {

    /** Usage: java gitlet.Main ARGS, where ARGS contains
     *  <COMMAND> <OPERAND1> <OPERAND2> ...
     */
    public static void main(String[] args) {
        if (args.length == 0) {
            System.out.println("Please enter a command.");
            System.exit(0);
        }

        String firstArg = args[0];
        switch(firstArg) {
            case "init":
                validateNumArgs(args, 1, "");
                Repository.createRepository();
                break;
            case "add":
                validateNumArgs(args, 2, "");
                Repository.addFile(args[1]);
                break;
            case "commit":
                validateNumArgs(args, 2, "Please enter a commit message.");
                Repository.commitFiles(args[1]);
                break;
            case "rm":
                validateNumArgs(args, 2, "");
                Repository.removeFile(args[1]);
                break;
            case "log":
                validateNumArgs(args, 1, "");
                Repository.printLog();
                break;
            case "global-log":
                validateNumArgs(args, 1, "");
                Repository.globalLog();
                break;
            case "find":
                validateNumArgs(args, 2, "");
                Repository.findCommits(args[1]);
                break;
            case "status":
                validateNumArgs(args, 1, "");
                Repository.printStatus();
                break;
            case "branch":
                validateNumArgs(args, 2, "");
                Repository.createBranch(args[1]);
                break;
            default:
                System.out.println("No command with that name exists.");
                System.exit(0);
        }
    }

    /**
     * @source lab6/capers/Main
     * Checks the number of arguments versus the expected number,
     * throws a RuntimeException if they do not match.
     *
     * @param args Argument array from command line
     * @param n    Number of expected arguments
     * @param error Error message you want to print
     */
    private static void validateNumArgs(String[] args, int n, String error) {
        if (args.length != n) {
            System.out.println(error);
            System.exit(0);
        }
    }
}