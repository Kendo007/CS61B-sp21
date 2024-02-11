package gitlet;

import java.io.Serial;
import java.io.Serializable;
import java.io.File;
import java.util.*;

/** Represents a gitlet commit object.
 *  TODO: It's a good idea to give a description here of what else this Class
 *  does at a high level.
 *
 *  @author Kheyanshu Garg
 */
public class Commit implements Serializable {
    @Serial
    private static final long serialVersionUID = 65296850946917690L;
    /** The message of this Commit. */
    private final String MSG;
    /** Reference to all the files in the commit */
    private final HashMap<String, String> FILES_IN_COMMIT;
    /** Date at which commit was made */
    private final Date TIMESTAMP;
    /** Sha of the parent commit */
    private final String PARENT_COMMIT;
    /** Folder in which commits are stored */
    public static final File COMMITS_DIR = Utils.join(Repository.GITLET_DIR, "commits");
    public static final File LATEST_MAP = Utils.join(Repository.GITLET_DIR, "latestcommit");

    public Commit(Date d, String shaOParent, String msg, HashMap<String, String> files) {
        TIMESTAMP = d;
        PARENT_COMMIT = shaOParent;
        MSG = msg;
        FILES_IN_COMMIT = files;
    }

    /** Returns the Sha of the given filename
     *
     * @param filename name of the file
     * @return stored sha in the map
     */
    public String getSha(String filename) {
        return FILES_IN_COMMIT.get(filename);
    }

    private static class firstSixCompare implements Comparator<String> {
        private final int SIZE;
        firstSixCompare(int s) {
            SIZE = s;
        }

        @Override
        public int compare(String s, String t1) {
            return s.substring(0, SIZE).compareTo(t1.substring(0, SIZE));
        }
    }

    public static String getFullCommit(String halfCommit) {
        List<String> l = Utils.plainFilenamesIn(COMMITS_DIR);

        int index = Collections.binarySearch(l, halfCommit, new firstSixCompare(halfCommit.length()));
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

        File f = Utils.join(COMMITS_DIR, shaOfCommit);

        if (shaOfCommit.length() < 40 && shaOfCommit.length() > 5) {
            String fullCommit = getFullCommit(shaOfCommit);
            f = Utils.join(COMMITS_DIR, fullCommit);
        }

        if (!f.exists()) {
            System.out.println("No commit with that id exists.");
            System.exit(0);
        }

        return Utils.readObject(f, Commit.class);
    }

    public String getMsg() {
        return MSG;
    }

    public String getParent() {
        return PARENT_COMMIT;
    }

    public HashMap<String, String> getFilesInCommit() {
        return FILES_IN_COMMIT;
    }

    public String toString() {
        StringBuilder sb = new StringBuilder("Date: ");

        Locale IND = new Locale("en", "IN");
        Formatter formatter = new Formatter(sb, IND);

        formatter.format("%ta %tb %td %tT %tY %tz", TIMESTAMP, TIMESTAMP, TIMESTAMP,
                TIMESTAMP, TIMESTAMP, TIMESTAMP);
        sb.append("\n");
        sb.append(MSG);

        return sb.toString();
    }
}