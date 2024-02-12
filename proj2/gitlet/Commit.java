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
    /** Stores the second Parent of a merge */
    private String secondParent = null;
    /** Folder in which commits are stored */
    public static final File COMMITS_DIR = Utils.join(Repository.GITLET_DIR, "commits");
    /** All the files that should be tracked and untracked in the next commit */
    public static final File LATEST_MAP = Utils.join(Repository.STAGING_AREA, "nextMap");

    public Commit(Date d, String shaOParent, String msg, HashMap<String, String> files) {
        TIMESTAMP = d;
        PARENT_COMMIT = shaOParent;
        MSG = msg;
        FILES_IN_COMMIT = files;
    }

    public Commit(Date d, String shaOParent, String secondParent, String msg, HashMap<String, String> files) {
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

    private static class firstXCompare implements Comparator<String> {
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
        if (halfCommit.length() == 40 || halfCommit.length() < 6) {
            return halfCommit;
        }

        List<String> l = Utils.plainFilenamesIn(COMMITS_DIR);

        int index = Collections.binarySearch(l, halfCommit, new firstXCompare());
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

        if (secondParent != null) {
            sb.append("\nMerge: ");
            sb.append(PARENT_COMMIT, 0, 6);
            sb.append(secondParent, 0, 6);
        }

        sb.append("\n");
        sb.append(MSG);

        return sb.toString();
    }
}