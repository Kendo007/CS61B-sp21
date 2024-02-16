package gitlet;

import java.io.Serial;
import java.io.Serializable;
import java.io.File;
import java.util.*;

/** Represents a gitlet commit object.
 * Also used for returning commits from the commits directory
 * or getting files SHA from the filesCommits
 *  does at a high level.
 *
 *  @author Kheyanshu Garg
 */
public class Commit implements Serializable {
    @Serial
    private static final long serialVersionUID = 65296850946917690L;

    private static final int SHA_LENGTH = 40;
    /** The message of this Commit. */
    private final String MSG;
    /** Reference to all the files in the commit */
    private final HashMap<String, String> FILES_IN_COMMIT;
    /** Date at which commit was made */
    private final Date TIMESTAMP;
    /** Sha of the parent commit */
    private final String PARENT_COMMIT;
    /** Stores the second Parent of a merge */
    private String secondParent = null;
    /** Folder in which commits are stored */
    public static final File COMMITS_DIR = Utils.join(Repository.GITLET_DIR, "commits");

    public Commit(Date d, String shaOParent, String msg, HashMap<String, String> files) {
        TIMESTAMP = d;
        PARENT_COMMIT = shaOParent;
        MSG = msg;
        FILES_IN_COMMIT = files;
    }

    public Commit(Date d, String shaOParent, String secondParent,
                  String msg, HashMap<String, String> files) {
        this(d, shaOParent, msg, files);
        this.secondParent = secondParent;
    }

    /** Returns the Sha of the given filename
     *
     * @param filename name of the file
     * @return stored sha in the map
     */
    public String getSha(String filename) {
        return FILES_IN_COMMIT.get(filename);
    }

    private static class FirstXCompare implements Comparator<String> {
        @Override
        public int compare(String s1, String s2) {
            int limit = Math.min(s1.length(), s2.length());

            int i = 0;
            while (i < limit) {
                char ch1 = s1.charAt(i);
                char ch2 = s2.charAt(i);
                if (ch1 != ch2) {
                    return ch1 - ch2;
                }
                ++i;
            }
            return 0;
        }
    }

    public static String getFullCommit(String halfCommit) {
        if (halfCommit.length() >= SHA_LENGTH || halfCommit.length() < 6) {
            return halfCommit;
        }

        List<String> l = Utils.plainFilenamesIn(COMMITS_DIR);

        int index = Collections.binarySearch(l, halfCommit, new FirstXCompare());
        if (index >= 0) {
            return l.get(index);
        }

        System.out.println("No commit with that id exists.");
        System.exit(0);

        return null;
    }

    /**
     * @param shaOfCommit sha of the commit you want
     * @return commit object related to the sha
     */
    public static Commit getCommit(String shaOfCommit) {
        if (shaOfCommit == null) {
            return null;
        }

        File f = Utils.join(COMMITS_DIR, getFullCommit(shaOfCommit));

        try {
            return Utils.readObject(f, Commit.class);
        } catch (IllegalArgumentException ignored) {
            System.out.println("No commit with that id exists.");
            System.exit(0);
        }

        return null;
    }

    public String getMsg() {
        return MSG;
    }

    public String getParent() {
        return PARENT_COMMIT;
    }

    public String getSecondParent() {
        return secondParent;
    }

    public HashMap<String, String> getFilesInCommit() {
        return FILES_IN_COMMIT;
    }

    /** Used to convert the given commit to a string
     *
     * @return String of commit
     */
    public String toString() {
        StringBuilder sb = new StringBuilder();

        Locale ind = new Locale("en", "IN");
        Formatter formatter = new Formatter(sb, ind);

        if (secondParent != null) {
            sb.append("Merge: ");
            sb.append(PARENT_COMMIT, 0, 7);
            sb.append(" ");
            sb.append(secondParent, 0, 7);
            sb.append("\n");
        }

        sb.append("Date: ");
        formatter.format("%ta %tb %td %tT %tY %tz", TIMESTAMP, TIMESTAMP, TIMESTAMP,
                TIMESTAMP, TIMESTAMP, TIMESTAMP);
        sb.append("\n");
        sb.append(MSG);

        return sb.toString();
    }
}
