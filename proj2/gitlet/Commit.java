package gitlet;

import java.io.Serializable;
import java.io.File;
import java.util.Date;
import java.util.Formatter;
import java.util.HashMap;
import java.util.Locale;

/** Represents a gitlet commit object.
 *  TODO: It's a good idea to give a description here of what else this Class
 *  does at a high level.
 *
 *  @author Kheyanshu Garg
 */
public class Commit implements Serializable {
    private static final long serialVersionUID = 65296850946917690L;
    /** The message of this Commit. */
    private String message;
    /** Reference to all the files in the commit */
    private HashMap<String, String> filesInCommit;
    /** Date at which commit was made */
    private Date commitDate;
    /** Sha of the parent commit */
    private String parentCommit;
    /** Folder in which commits are stored */
    public static final File COMMITS_DIR = Utils.join(Repository.GITLET_DIR, "commits");
    public static final File LATEST_MAP = Utils.join(Repository.GITLET_DIR, "latestcommit");

    public Commit(Date d, String shaOParent, String msg, HashMap<String, String> files) {
        commitDate = d;
        parentCommit = shaOParent;
        message = msg;
        filesInCommit = files;
    }

    /** Returns the Sha of the given filename
     *
     * @param filename name of the file
     * @return stored sha in the map
     */
    public String getSha(String filename) {
        return filesInCommit.get(filename);
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
        return Utils.readObject(f, Commit.class);
    }

    public String getMsg() {
        return message;
    }

    public String getParent() {
        return parentCommit;
    }

    public HashMap<String, String> getFilesInCommit() {
        return filesInCommit;
    }

    public String toString() {
        StringBuilder sb = new StringBuilder("Date: ");

        Locale IND = new Locale("en", "IN");
        Formatter formatter = new Formatter(sb, IND);

        formatter.format("%ta %tb %td %tT %tY %tz", commitDate, commitDate, commitDate,
                commitDate, commitDate, commitDate);
        sb.append("\n");
        sb.append(message);

        return sb.toString();
    }
}