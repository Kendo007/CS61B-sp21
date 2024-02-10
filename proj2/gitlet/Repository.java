package gitlet;

import java.io.File;
import java.util.*;

import static gitlet.Utils.*;

/**
 * Represents a gitlet repository.
 *  TODO: It's a good idea to give a description here of what else this Class
 *  does at a high level.
 *
 * @author Kheyanshu Garg
 */
public class Repository {
    /**
     * The current working directory.
     */
    public static final File CWD = new File(System.getProperty("user.dir"));
    /**
     * The .gitlet directory.
     */
    public static final File GITLET_DIR = join(CWD, ".gitlet");
    /**
     * Folder where staged files are stored
     */
    public static final File STAGING_AREA = join(GITLET_DIR, "staging");
    /**
     * All the files that are ready to be committed
     */
    public static final File STAGED_ADD = join(STAGING_AREA, "add");
    /**
     * All the files that are ready to be removed
     */
    public static final File STAGED_REMOVED = join(STAGING_AREA, "remove");
    /**
     * files are stored in this folder
     */
    public static final File OBJECTS_DIR = Utils.join(GITLET_DIR, "objects");

    /**
     * Checks if gitlet repository exists
     * if yes then simply returns
     * else exits the program
     */
    public static void validateGitletRepo() {
        if (!Repository.GITLET_DIR.exists()) {
            System.out.println("Not in an initialized Gitlet directory.");
            System.exit(0);
        }
    }

    /**
     * Creates an empty gitlet repository
     * if already exists then prints an error msg than returns
     */
    public static void createRepository() {
        if (GITLET_DIR.exists()) {
            System.out.println("A Gitlet version-control system already exists in the current directory.");
            return;
        }

        GITLET_DIR.mkdir();
        OBJECTS_DIR.mkdir();
        STAGING_AREA.mkdir();
        Branch.HEADS_DIR.mkdir();
        Branch.BRANCH_DIR.mkdir();
        Commit.COMMITS_DIR.mkdir();

        Utils.writeObject(Branch.ACTIVE_BRANCH, "master");
        Utils.writeObject(Utils.join(Branch.HEADS_DIR, "master"), null);

        Branch b = new Branch("master", new HashMap<>());
        b.addCommit("initial commit", new Date(0), new HashMap<>());

        Utils.writeObject(STAGED_ADD, new TreeSet<>());
        Utils.writeObject(STAGED_REMOVED, new TreeSet<>());
        Utils.writeObject(Commit.LATEST_MAP, new HashMap<>());
    }

    /**
     * Adds a file to the staging area
     *
     * @param fileName filename to be added in staging area
     */
    public static void addFile(String fileName) {
        validateGitletRepo();

        File currFile = new File(CWD, fileName);
        if (!currFile.exists()) {
            System.out.println("File does not exist.");
            System.exit(0);
        }

        String currFileSha = Utils.sha1(readContents(currFile));

        HashMap<String, String> temp = readObject(Commit.LATEST_MAP, HashMap.class);
        Commit latestCommit = Commit.getCommit(Branch.getHeadActive());
        String storedFileSha = latestCommit.getSha(fileName);

        TreeSet<String> stage = Utils.readObject(STAGED_ADD, TreeSet.class);

        if (currFileSha.equals(storedFileSha)) {
            stage.remove(fileName);
        } else {
            temp.put(fileName, currFileSha);
            stage.add(fileName);
        }

        Utils.writeObject(Commit.LATEST_MAP, temp);
        Utils.writeObject(STAGED_ADD, stage);
    }

    public static void commitFiles(String msg) {
        validateGitletRepo();

        TreeSet<String> stage = Utils.readObject(STAGED_ADD, TreeSet.class);
        TreeSet<String> stage_remove = Utils.readObject(STAGED_REMOVED, TreeSet.class);
        if (stage.isEmpty() && stage_remove.isEmpty()) {
            System.out.println("No changes added to the commit.");
            System.exit(0);
        }

        for (String i : stage) {
            File f = Utils.join(CWD, i);
            byte[] b = Utils.readContents(f);

            String shaOfb = Utils.sha1(b);
            File stored = Utils.join(OBJECTS_DIR, shaOfb.substring(0, 1));
            stored.mkdir();

            f = Utils.join(stored, shaOfb);
            Utils.writeObject(f, b);
        }

        HashMap<String, String> temp = Utils.readObject(Commit.LATEST_MAP, HashMap.class);
        for (String i : stage_remove) {
            temp.remove(i);
        }

        Branch br = Branch.getActiveBranch();
        br.addCommit(msg, new Date(), temp);
        Utils.writeObject(STAGED_ADD, new TreeSet<>());
        Utils.writeObject(STAGED_REMOVED, new TreeSet<>());
    }

    public static void removeFile(String fileName) {
        validateGitletRepo();

        HashMap<String, String> temp = readObject(Commit.LATEST_MAP, HashMap.class);
        Commit latestCommit = Commit.getCommit(Branch.getHeadActive());
        TreeSet<String> stage = Utils.readObject(STAGED_ADD, TreeSet.class);
        TreeSet<String> stage_remove = Utils.readObject(STAGED_REMOVED, TreeSet.class);

        if (latestCommit.getSha(fileName) != null && !stage_remove.contains(fileName)) {
            File f = new File(CWD, fileName);
            f.delete();

            stage.remove(fileName);
            stage_remove.add(fileName);
        } else if (stage.contains(fileName)) {
            temp.remove(fileName);
            stage.remove(fileName);
        } else {
            System.out.println("No reason to remove the file.");
            return;
        }

        Utils.writeObject(Commit.LATEST_MAP, temp);
        Utils.writeObject(STAGED_ADD, stage);
        Utils.writeObject(STAGED_REMOVED, stage_remove);
    }

    private static void printCommit(String shaOfi, Commit c) {
        System.out.println("===");
        System.out.println("commit " + shaOfi);
        System.out.println(c);
        System.out.println();
    }

    public static void printLog() {
        validateGitletRepo();
        String shaOfi = Branch.getHeadActive();
        Commit i = Commit.getCommit(shaOfi);

        while (i != null) {
            printCommit(shaOfi, i);

            shaOfi = i.getParent();
            i = Commit.getCommit(shaOfi);
        }
    }

    public static void globalLog() {
        validateGitletRepo();
        List<String> l = Utils.plainFilenamesIn(Commit.COMMITS_DIR);

        for (String i : l) {
            printCommit(i, Commit.getCommit(i));
        }
    }

    public static void findCommits(String msg) {
        validateGitletRepo();
        List<String> l = Utils.plainFilenamesIn(Commit.COMMITS_DIR);
        Commit c;
        boolean found = false;

        for (String i : l) {
            c = Commit.getCommit(i);

            if (c.getMsg().equals(msg)) {
                System.out.println(i);
                found = true;
            }
        }

        if (!found) {
            System.out.println("Found no commit with that message.");
        }
    }

    public static void printStatus() {
        validateGitletRepo();

        System.out.println("===Branches===");
        List<String> branchList = Utils.plainFilenamesIn(Branch.BRANCH_DIR);
        String activeName = Branch.getActiveBranchName();

        System.out.println("*" + activeName);
        for (String i : branchList) {
            if (!i.equals(activeName)) {
                System.out.println(i);
            }
        }

        System.out.println("\n==Staged Files==");
        TreeSet<String> stage = Utils.readObject(STAGED_ADD, TreeSet.class);

        for (String i : stage) {
            System.out.println(i);
        }

        System.out.println("\n==Removed Files==");
        TreeSet<String> stage_remove = Utils.readObject(STAGED_REMOVED, TreeSet.class);

        for (String i : stage_remove) {
            System.out.println(i);
        }
    }

    public static void createBranch(String branchName) {
        validateGitletRepo();
        List<String> l = Utils.plainFilenamesIn(Branch.BRANCH_DIR);

        if (l.contains(branchName)) {
            System.out.println("A branch with that name already exists.");
            System.exit(0);
        }

        Branch br = Branch.getActiveBranch();
        br.createNewBranch(branchName);
    }
}