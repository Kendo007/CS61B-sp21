package gitlet;

import java.io.File;
import java.io.Serializable;
import java.util.Date;
import java.util.HashMap;

public class Branch implements Serializable {
    private static final long serialVersionUID = 6529685098267757690L;
    /** Name of the branch */
    private final String branchName;
    /** List of splits where the branch and its ancestors are split */
    private HashMap<String, String> splitPoints;
    /** Name of the last Commit in the given Branch */
    private final File HEAD;
    /** Folder where all the branches are saved */
    public static final File BRANCH_DIR = Utils.join(Repository.GITLET_DIR, "branches");
    /** File in which name of active branch is saved */
    public static final File ACTIVE_BRANCH = Utils.join(Repository.GITLET_DIR, "activeBranch");
    /** Reference to head commits of all branches */
    public static final File HEADS_DIR = Utils.join(Repository.GITLET_DIR, "heads");

    public Branch(String name, HashMap<String, String> points) {
        branchName = name;
        splitPoints = points;
        HEAD = Utils.join(HEADS_DIR, branchName);

        Utils.writeObject(HEAD, getHeadActive());
        this.saveBranch();
    }

    /**
     * Returns name of the active branch
     */
    public static String getActiveBranchName() {
        return Utils.readObject(ACTIVE_BRANCH, String.class);
    }

    /**
     * Returns current active branch
     */
    public static Branch getActiveBranch() {
        File f = Utils.join(BRANCH_DIR, getActiveBranchName());

        return  Utils.readObject(f, Branch.class);
    }

    /**
     * Returns head of the given branch
     */
    public static String getHead(String branchName) {
        File latestCommit = Utils.join(HEADS_DIR, branchName);

        return Utils.readObject(latestCommit, String.class);
    }

    /**
     * Returns head of the active branch
     */
    public static String getHeadActive() {
        return getHead(getActiveBranchName());
    }

    public void createNewBranch(String name) {
        splitPoints.put(name, getHead(branchName));

        new Branch(name, splitPoints);
    }

    public void addCommit(String msg, Date d, HashMap<String, String> temp) {
        Commit c = new Commit(d, getHead(branchName), msg, temp);

        String shaOfc = Utils.sha1(c.toString());
        File newCommit = Utils.join(Commit.COMMITS_DIR, shaOfc);
        Utils.writeObject(newCommit, c);

        Utils.writeObject(HEAD, shaOfc);
        this.saveBranch();
    }

    /**
     * Saves the current branch in branches folder
     */
    public void saveBranch() {
        File f = Utils.join(BRANCH_DIR, branchName);
        Utils.writeObject(f, this);
    }
}
