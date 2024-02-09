package gitlet;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

public class Branch implements Serializable {
    private static final long serialVersionUID = 6529685098267757690L;
    /** Name of the branch */
    private final String branchName;
    /** List of splits where the branch and its ancestors are split */
    private HashMap<String, String> splitPoints;
    /** Name of the current active branch */
    private final File THIS_BRANCH;
    /** Name of the last Commit in the given Branch */
    private String latestCommit = null;
    /** Folder where all the branches are saved */
    public static final File BRANCH_DIR = Utils.join(Repository.GITLET_DIR, "branches");
    /** File in which name of active branch is saved */
    public static final File ACTIVE_BRANCH = Utils.join(Repository.GITLET_DIR, "activeBranch");

    public Branch(String name, HashMap<String, String> points) {
        List<String> branchList = Utils.plainFilenamesIn(BRANCH_DIR);

        if (branchList != null && branchList.contains(name)) {
            System.out.println("A branch with that name already exists.");
            System.exit(0);
        }

        branchName = name;
        splitPoints = points;
        THIS_BRANCH  = Utils.join(BRANCH_DIR, branchName);

        this.saveBranch();
    }

    /**
     * @return Returns current active branch
     */
    public static Branch getActiveBranch() {
        String name = Utils.readObject(ACTIVE_BRANCH, String.class);
        File f = Utils.join(BRANCH_DIR, name);

        return  Utils.readObject(f, Branch.class);
    }

    /**
     * Returns sha of latest commit in the branch
     */
    public String getShaOfLatest() {
        return latestCommit;
    }

    public Commit getLatestCommit() {
        return Commit.getCommit(getShaOfLatest());
    }

    public void createNewBranch(String name) {
        HashMap<String, String> points;

        if (splitPoints != null) {
            points = new HashMap<>(splitPoints);
        } else {
            points = new HashMap<>();
        }

        points.put(name, latestCommit);
        Branch newBranch = new Branch(name, points);
    }

    public void addCommit(String msg, Date d) {
        Commit c = Utils.readObject(Commit.LATEST_COMMIT, Commit.class);
        c.setDate(d);
        c.setParent(latestCommit);
        c.setMsg(msg);

        String shaOfc = Utils.sha1(c.toString());
        File newCommit = Utils.join(Commit.COMMITS_DIR, shaOfc);
        Utils.writeObject(newCommit, c);

        latestCommit = shaOfc;
        this.saveBranch();
    }

    /**
     * Saves the current branch in branches folder
     */
    public void saveBranch() {
        Utils.writeObject(THIS_BRANCH, this);
    }
}
