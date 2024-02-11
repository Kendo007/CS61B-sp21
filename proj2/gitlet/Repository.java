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

        String currFileSha = Utils.sha1Object(readContents(currFile));

        HashMap<String, String> temp = readObject(Commit.LATEST_MAP, HashMap.class);
        Commit latestCommit = Commit.getCommit(Branch.getHeadActive());
        String storedFileSha = latestCommit.getSha(fileName);

        TreeSet<String> stage = Utils.readObject(STAGED_ADD, TreeSet.class);

        if (currFileSha.equals(storedFileSha)) {
            stage.remove(fileName);
        } else {
            stage.add(fileName);
        }

        temp.put(fileName, currFileSha);
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

            String shaOfb = Utils.sha1Object(b);
            File stored = Utils.join(OBJECTS_DIR, shaOfb.substring(0, 1));
            stored.mkdir();

            f = Utils.join(stored, shaOfb);
            Utils.writeContents(f, b);
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

            temp.remove(fileName);
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

    /**
     * Returns a Set of Strings which are in list but not in map
     *
     * @param filesInDir All the files in the CWD
     * @param temp The staging area hashmap
     * @return Untracked files in the directpry
     */
    private static TreeSet<String> untrackedFiles(List<String> filesInDir, HashMap<String, String> temp,
                                                  boolean wantList) {
        TreeSet<String> untracked = new TreeSet<>();

        for (String i : filesInDir) {
            if (!temp.containsKey(i)) {
                if (!wantList) {
                    return null;
                }

                untracked.add(i);
            }
        }

        return untracked;
    }

    public static void printStatus() {
        validateGitletRepo();

        System.out.println("=== Branches ===");
        List<String> branchList = Utils.plainFilenamesIn(Branch.BRANCH_DIR);
        String activeName = Branch.getActiveBranchName();

        System.out.println("*" + activeName);
        for (String i : branchList) {
            if (!i.equals(activeName)) {
                System.out.println(i);
            }
        }

        System.out.println("\n=== Staged Files ===");
        TreeSet<String> stage = Utils.readObject(STAGED_ADD, TreeSet.class);

        for (String i : stage) {
            System.out.println(i);
        }

        System.out.println("\n=== Removed Files ===");
        TreeSet<String> stage_remove = Utils.readObject(STAGED_REMOVED, TreeSet.class);

        for (String i : stage_remove) {
            System.out.println(i);
        }

        System.out.println("\n=== Modifications Not Staged For Commit ===");
        HashMap<String, String> temp = Utils.readObject(Commit.LATEST_MAP, HashMap.class);
        TreeSet<String> modified = new TreeSet<>();

        for (Map.Entry<String, String> i : temp.entrySet()) {
            File inCWD = Utils.join(CWD, i.getKey());

            if (inCWD.exists()) {
                String inCWDSha = Utils.sha1Object(Utils.readContents(inCWD));

                // Checking if staged contents are same as in working directory
                if (!inCWDSha.equals(i.getValue())) {
                    modified.add(i.getKey() + " (modified)");
                }
            } else {
                modified.add(i.getKey() + " (deleted)");
            }
        }

        for (String i : modified) {
            System.out.println(i);
        }

        System.out.println("\n=== Untracked Files ===");
        List<String> dr = Utils.plainFilenamesIn(CWD);
        TreeSet<String> untracked = untrackedFiles(dr, temp, true);

        for (String i : untracked) {
            System.out.println(i);
        }

        System.out.println();
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

    /**
     * Returns file Pointer of the file with the given sha
     */
    private static void writeFileCWD(String fileName, String shaOfFile) {
        File f = Utils.join(OBJECTS_DIR, shaOfFile.substring(0, 1), shaOfFile);
        File fileInWD = Utils.join(CWD, fileName);

        Utils.writeContents(fileInWD, Utils.readContents(f));
    }

    public static void checkOutFile(String shaOfC, String fileName) {
        Commit c = Commit.getCommit(shaOfC);
        String shaOfFile = c.getSha(fileName);

        if (shaOfFile == null) {
            System.out.println("File does not exist in that commit.");
            System.exit(0);
        }

        writeFileCWD(fileName, shaOfFile);
    }

    private static void copyFromREPO(String shaOfCommit) {
        List<String> dr = Utils.plainFilenamesIn(CWD);
        HashMap<String, String> temp = Utils.readObject(Commit.LATEST_MAP, HashMap.class);
        Commit branchCommit = Commit.getCommit(shaOfCommit);

        if (!(dr == null)) {
            if (untrackedFiles(dr, temp, false) == null) {
                System.out.println("There is an untracked file in the way; delete it, or add and commit it first.");
                System.exit(0);
            }

            for (String i : dr) {
                File delete = Utils.join(CWD, i);
                delete.delete();
            }
        }

        HashMap<String, String> commitFiles = branchCommit.getFilesInCommit();

        for (Map.Entry<String, String> i : commitFiles.entrySet()) {
            writeFileCWD(i.getKey(), i.getValue());
        }

        Utils.writeObject(STAGED_ADD, new TreeSet<>());
        Utils.writeObject(STAGED_REMOVED, new TreeSet<>());
        Utils.writeObject(Commit.LATEST_MAP, commitFiles);
    }

    public static void checkOutBranch(String branchName) {
        validateGitletRepo();

        if (branchName.equals(Branch.getActiveBranchName())) {
            System.out.println("No need to checkout the current branch.");
            System.exit(0);
        }

        File branchFile = Utils.join(Branch.BRANCH_DIR, branchName);
        if (!branchFile.exists()) {
            System.out.println("No such branch exists.");
            System.exit(0);
        }

        copyFromREPO(Branch.getHead(branchName));
        Utils.writeObject(Branch.ACTIVE_BRANCH, branchName);
    }

    public static void removeBranch(String branchName) {
        validateGitletRepo();

        if (branchName.equals(Branch.getActiveBranchName())) {
            System.out.println("Cannot remove the current branch.");
            System.exit(0);
        }

        File branchFile = Utils.join(Branch.BRANCH_DIR, branchName);
        if (!branchFile.exists()) {
            System.out.println("A branch with that name does not exist.");
            System.exit(0);
        }

        File branchHEAD = new File(Branch.HEADS_DIR, branchName);
        branchFile.delete();
        branchHEAD.delete();
    }

    public static void reset(String shaOfCommit) {
        validateGitletRepo();

        String fullCommit = Commit.getFullCommit(shaOfCommit);
        copyFromREPO(fullCommit);

        File Head = Utils.join(Branch.HEADS_DIR, Branch.getActiveBranchName());
        Utils.writeObject(Head, fullCommit);
    }
}