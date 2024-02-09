package gitlet;

import java.io.Serializable;
import java.io.File;
import java.util.Date;
import java.util.HashMap;

/** Represents a gitlet commit object.
 *  TODO: It's a good idea to give a description here of what else this Class
 *  does at a high level.
 *
 *  @author Kheyanshu Garg
 */
public class Commit implements Serializable {
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
    public static final File LATEST_COMMIT = Utils.join(Repository.GITLET_DIR, "latestcommit");

    public Commit(String msg) {
        commitDate = new Date();
        message = msg;
        parentCommit = Branch.getActiveBranch().getShaOfLatest();
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
        File f = Utils.join(COMMITS_DIR, shaOfCommit);
        return Utils.readObject(f, Commit.class);
    }

    public void removeFile(String fileName, String currFileSha) {
        filesInCommit.remove(fileName, currFileSha);
    }

    public void putFile(String fileName, String currFileSha) {
        filesInCommit.put(fileName, currFileSha);
    }

    public boolean isSame(Commit other) {
        return filesInCommit.equals(other.filesInCommit);
    }
}