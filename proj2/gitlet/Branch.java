package gitlet;

import java.io.File;
import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.nio.file.StandardCopyOption;
import java.nio.file.Files;

import static gitlet.Commit.getCommit;

public class Branch {
    /**
     * File in which name of active branch is saved
     */
    public static final File HEAD = Utils.join(Repository.GITLET_DIR, "HEAD");
    /**
     * Reference to head commits of all branches
     */
    public static final File LAST_COMMIT = Utils.join(Repository.GITLET_DIR, "lastCommit");

    /**
     * Returns name of the active branch
     */
    public static String getActiveBranchName() {
        return Utils.readObject(HEAD, String.class);
    }

    /**
     * Returns head of the given branch
     */
    public static String getHead(String branchName) {
        File latestCommit = Utils.join(LAST_COMMIT, branchName);

        return Utils.readObject(latestCommit, String.class);
    }

    /**
     * Returns head of the active branch
     */
    public static String getHeadActive() {
        return getHead(getActiveBranchName());
    }

    public static void addCommit(String msg, Date d, HashMap<String, String> temp) {
        String activeBranch = getActiveBranchName();
        Commit c = new Commit(d, getHead(activeBranch), msg, temp);

        String shaOfc = Utils.sha1(c.toString());
        File newCommit = Utils.join(Commit.COMMITS_DIR, shaOfc);

        Utils.writeObject(newCommit, c);
        Utils.writeObject(Utils.join(LAST_COMMIT, activeBranch), shaOfc);
    }

    public static <Path> void createNewBranch(String newBranchName) {
        String activeBranchName = getActiveBranchName();

        java.nio.file.Path lastCommitPath = LAST_COMMIT.toPath();
        try {
            Files.copy(lastCommitPath.resolve(activeBranchName), lastCommitPath.resolve(newBranchName),
                    StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException ignored) {
            return;
        }
    }

    /** Gives all the files in the split Point
     *
     * @param branchOne Name of first branch
     * @param branchTwo Name of second branch
     * @return The Reference to files in the splitPoint
     */
    private static HashMap<String, String> getSplitPoint(String branchOne, String branchTwo) {
        LinkedHashSet<String> points = new LinkedHashSet<>();
        String shaOfi = getHead(branchOne);
        Commit i = getCommit(shaOfi);

        while (i != null) {
            points.add(shaOfi);

            shaOfi = i.getParent();
            i = getCommit(shaOfi);
        }

        String shaOfj = getHead(branchTwo);
        Commit j = getCommit(shaOfj);

        while (j != null) {
            if (points.contains(shaOfj)) {
                return j.getFilesInCommit();
            }

            shaOfj = j.getParent();
            j = getCommit(shaOfj);
        }

        return null;
    }

    public static void mergeBranch(String branchOne, String branchTwo) {
        // Files in head of split point and both branches
        Commit branchOneCommit = getCommit(getHead(branchOne));
        Commit branchTwoCommit = getCommit(getHead(branchOne));
        HashMap<String, String> branchOneFiles = branchOneCommit.getFilesInCommit();
        HashMap<String, String> branchTwoFiles = branchTwoCommit.getFilesInCommit();
        HashMap<String, String> splitPointFiles = getSplitPoint(branchOne, branchTwo);


    }
}
