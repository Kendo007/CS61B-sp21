package gitlet;

import java.io.File;
import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.nio.file.StandardCopyOption;
import java.nio.file.Files;
import java.util.Map;

import static gitlet.Commit.getCommit;
import static gitlet.Repository.*;
import static gitlet.Stage.nextMap;
import static gitlet.Utils.*;

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
        addCommit(msg, d, temp, null);
    }

    public static void addCommit(String msg, Date d, HashMap<String, String> temp,
                                 String secondParent) {
        String activeBranch = getActiveBranchName();
        Commit c;
        if (secondParent == null) {
            c = new Commit(d, getHead(activeBranch), msg, temp);
        } else {
            c = new Commit(d, getHead(activeBranch), secondParent, msg, temp);
        }

        String shaOfc = Utils.sha1(c.toString());
        File newCommit = Utils.join(Commit.COMMITS_DIR, shaOfc);

        Utils.writeObject(newCommit, c);
        Utils.writeObject(Utils.join(LAST_COMMIT, activeBranch), shaOfc);
    }

    public static void createNewBranch(String newBranchName) {
        String activeBranchName = getActiveBranchName();

        java.nio.file.Path lastCommitPath = LAST_COMMIT.toPath();
        try {
            Files.copy(lastCommitPath.resolve(activeBranchName),
                    lastCommitPath.resolve(newBranchName),
                    StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException ignored) {
            return;
        }
    }

    /**
     * This function creates a set of Strings which contains all the sha in given branch
     *
     * @param commitSha the sha of the given branch
     * @param s         set to which strings should be added
     */
    private static void createGraph(String commitSha, HashSet<String> s) {
        if (commitSha == null || s.contains(commitSha)) {
            return;
        }

        s.add(commitSha);
        Commit c = getCommit(commitSha);

        if (c.getSecondParent() != null) {
            createGraph(c.getSecondParent(), s);
        }
        createGraph(c.getParent(), s);
    }

    private static class Point {
        int weight;
        String commitID;

        Point(int w, String id) {
            weight = w;
            commitID = id;
        }
    }

    /**
     * Searches for the common point from the given graph and other commit
     *\
     */
    private static Point search(String commitSha, int weight,
                                HashSet<String> graph,
                                HashSet<String> searched) {
        if (searched.contains(commitSha)) {
            return null;
        }

        searched.add(commitSha);
        if (graph.contains(commitSha)) {
            return new Point(weight, commitSha);
        }

        Commit c = getCommit(commitSha);
        if (c.getSecondParent() != null) {
            Point first = search(c.getParent(), weight + 1, graph, searched);
            Point second = search(c.getSecondParent(), weight + 1, graph, searched);

            if (second != null && second.weight < first.weight) {
                return second;
            } else {
                return first;
            }
        } else {
            return search(c.getParent(), weight + 1, graph, searched);
        }
    }

    /**
     * Gives all the files in the split Point
     *
     * @param branchOneSha Last commit of first branch
     * @param branchTwoSha Last Commit of second branch
     * @return The Reference to files in the splitPoint
     */
    private static String getSplitPoint(String branchOneSha, String branchTwoSha) {
        HashSet<String> graph = new HashSet<>();
        createGraph(branchOneSha, graph);

        Point p = search(branchTwoSha, 0, graph, new HashSet<>());
        return p.commitID;
    }

    /**
     * Reads the saved file into the memory or returns an empty string if the file
     * does not exist.
     */
    private static String readString(String fileSha) {
        if (fileSha == null) {
            return "";
        }

        File f = Utils.join(Repository.OBJECTS_DIR, fileSha.substring(0, 1), fileSha);
        return Utils.readContentsAsString(f);
    }

    /**
     * Solves the merge conflict between two files
     */
    private static void conflict(String fileName, String headFileSha, String otherFileSha,
                                 boolean conflicted) {
        if (headFileSha != null && headFileSha.equals(otherFileSha)) {
            return;
        }

        if (!conflicted) {
            System.out.println("Encountered a merge conflict.");
        }

        String newFileSha = Utils.sha1Object("<<<<<<< HEAD\n", readString(headFileSha), "=======\n",
                readString(otherFileSha), ">>>>>>>", fileName);

        File dir = join(Repository.OBJECTS_DIR, newFileSha.substring(0, 1));
        dir.mkdir();

        writeContents(join(dir, newFileSha),
                "<<<<<<< HEAD\n", readString(headFileSha), "=======\n",
                readString(otherFileSha), ">>>>>>>");
        writeContents(join(Repository.CWD, fileName),
                "<<<<<<< HEAD\n", readString(headFileSha), "=======\n",
                readString(otherFileSha), ">>>>>>>");

        nextMap.put(fileName, newFileSha);
    }

    private static void deleteinCWD(String fileName) {
        join(Repository.CWD, fileName).delete();
    }

    /**
     * Merges two branches into a single one with a lot of if conditions
     */
    public static void mergeBranch(String branchOne, String branchTwo) {
        // Files in head of split point and both branches
        String branchOneSha = getHead(branchOne), branchTwoSha = getHead(branchTwo);
        String splitPointSha = getSplitPoint(branchOneSha, branchTwoSha);

        if (splitPointSha.equals(branchTwoSha)) {
            System.out.println("Given branch is an ancestor of the current branch.");
            System.exit(0);
        }

        if (splitPointSha.equals(branchOneSha)) {
            System.out.println("Current branch fast-forwarded.");
            reset(branchTwoSha);

            return;
        }

        HashMap<String, String> headFiles = nextMap;
        HashMap<String, String> otherFiles = getCommit(branchTwoSha).getFilesInCommit();
        HashMap<String, String> splitPointFiles = getCommit(splitPointSha).getFilesInCommit();

        String shaInSplit, shaInHead, shaInOther;
        boolean conflicted = false;
        for (Map.Entry<String, String> i : splitPointFiles.entrySet()) {
            shaInSplit = i.getValue();
            shaInHead = headFiles.get(i.getKey());
            shaInOther = otherFiles.get(i.getKey());

            if (shaInSplit.equals(shaInHead) && !shaInSplit.equals(shaInOther)) {
                if (shaInOther == null) {
                    headFiles.remove(i.getKey());
                    deleteinCWD(i.getKey());
                } else {
                    headFiles.put(i.getKey(), shaInOther);
                    writeFileCWD(i.getKey(), shaInOther);
                }
            } else if (shaInSplit.equals(shaInOther)) {
                if (shaInHead == null) {
                    deleteinCWD(i.getKey());
                }
            } else if (shaInHead == shaInOther) {
                deleteinCWD(i.getKey());
            } else {
                conflict(i.getKey(), shaInHead, shaInOther, conflicted);
                conflicted = true;
            }

            otherFiles.remove(i.getKey());
        }

        for (Map.Entry<String, String> i : otherFiles.entrySet()) {
            shaInHead = headFiles.get(i.getKey());
            shaInOther = i.getValue();

            if (shaInHead == null) {
                headFiles.put(i.getKey(), shaInOther);
                writeFileCWD(i.getKey(), i.getValue());
            } else {
                conflict(i.getKey(), shaInHead, shaInOther, conflicted);
                conflicted = true;
            }
        }

        addCommit("Merged " + branchTwo + " into " + branchOne + ".", new Date(),
                headFiles, branchTwoSha);

        Stage.saveStageMap();
    }
}
