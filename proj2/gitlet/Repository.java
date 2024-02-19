package gitlet;

import java.io.File;
import java.io.IOException;
import java.nio.file.StandardCopyOption;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;

import static gitlet.Utils.*;
import static gitlet.Branch.*;
import static gitlet.Commit.*;
import static gitlet.Stage.*;

/**
 * Represents a gitlet repository.
 * Executes all the commands of the repo
 *  does at a high level.
 *
 * @author Kheyanshu Garg
 */
public class Repository {
    /** The current working directory. */
    public static final File CWD = new File(System.getProperty("user.dir"));
    /** The .gitlet directory. */
    public static final File GITLET_DIR = join(CWD, ".gitlet");
    /** Stores temporary files for add */
    public static final File TEMP = join(STAGING_AREA, "temp");
    /** files are stored in this folder */
    public static final File OBJECTS_DIR = join(GITLET_DIR, "objects");

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
            System.out.println("A Gitlet version-control system "
                    + "already exists in the current directory.");
            return;
        }

        // Setting up various empty directories
        GITLET_DIR.mkdir();
        OBJECTS_DIR.mkdir();
        COMMITS_DIR.mkdir();
        LAST_COMMIT.mkdir();
        RemoteRepository.REMOTE.mkdir();

        // Setting up branches
        writeObject(HEAD, "master");
        writeObject(join(LAST_COMMIT, "master"), null);
        addCommit("initial commit", new Date(0), new HashMap<>());

        // Setting up Staging Area
        STAGING_AREA.mkdir();
        TEMP.mkdir();
        newArea();
        writeObject(LATEST_MAP, new HashMap<>());
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

        Object currFileContents = (Object) readContents(currFile);
        String currFileSha = sha1Object(currFileContents, fileName);

        Commit latestCommit = getCommit(getHeadActive());
        String storedFileSha = latestCommit.getSha(fileName);

        loadFullStage();
        if (currFileSha.equals(storedFileSha)) {
            stageAdd.remove(fileName);
            stageRemove.remove(fileName);
        } else {
            stageAdd.add(fileName);
            stageRemove.remove(fileName);

            if (nextMap.containsKey(fileName)) {
                File unnecessary = join(TEMP, nextMap.get(fileName));
                unnecessary.delete();
            }

            writeContents(join(TEMP, currFileSha), currFileContents);
        }

        nextMap.put(fileName, currFileSha);
        saveFullStage();
    }

    public static void commitFiles(String msg) {
        validateGitletRepo();

        if (msg.isEmpty()) {
            System.out.println("Please enter a commit message.");
            System.exit(0);
        }

        loadFullStage();
        if (stageAdd.isEmpty() && stageRemove.isEmpty()) {
            System.out.println("No changes added to the commit.");
            System.exit(0);
        }

        List<String> tempFiles = plainFilenamesIn(TEMP);
        if (tempFiles != null) {
            for (String i : tempFiles) {
                File startDir = join(OBJECTS_DIR, i.substring(0, 1));
                startDir.mkdir();
                try {
                    Files.move(TEMP.toPath().resolve(i), startDir.toPath().resolve(i),
                            StandardCopyOption.REPLACE_EXISTING);
                } catch (IOException ignored) {
                    return;
                }
            }
        }

        addCommit(msg, new Date(), nextMap);
        newArea();
    }

    public static void removeFile(String fileName) {
        validateGitletRepo();

        Commit latestCommit = getCommit(getHeadActive());
        loadFullStage();

        if (latestCommit.getSha(fileName) != null && !stageRemove.contains(fileName)) {
            File f = new File(CWD, fileName);
            f.delete();

            File unnecessary = join(TEMP, nextMap.get(fileName));
            unnecessary.delete();

            nextMap.remove(fileName);
            stageAdd.remove(fileName);
            stageRemove.add(fileName);
        } else if (stageAdd.contains(fileName)) {
            File unnecessary = join(TEMP, nextMap.get(fileName));
            unnecessary.delete();

            nextMap.remove(fileName);
            stageAdd.remove(fileName);
        } else {
            System.out.println("No reason to remove the file.");
            return;
        }

        saveFullStage();
    }

    private static void printCommit(String shaOfi, Commit c) {
        System.out.print("===\ncommit ");
        System.out.println(shaOfi);
        System.out.println(c);
        System.out.println();
    }

    public static void printLog() {
        validateGitletRepo();
        String shaOfi = getHeadActive();
        Commit i = getCommit(shaOfi);
        int c = 0;

        while (i != null) {
            ++c;
            printCommit(shaOfi, i);

            shaOfi = i.getParent();
            i = getCommit(shaOfi);
        }
    }

    public static void globalLog() {
        validateGitletRepo();
        List<String> l = plainFilenamesIn(COMMITS_DIR);

        for (String i : l) {
            printCommit(i, getCommit(i));
        }
    }

    public static void findCommits(String msg) {
        validateGitletRepo();
        String[] files = COMMITS_DIR.list();
        Commit c;
        boolean found = false;

        for (String i : files) {
            c = getCommit(i);

            if (msg.equals(c.getMsg())) {
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
    private static TreeSet<String> untrackedFiles(List<String> filesInDir,
                                                  HashMap<String, String> temp,
                                                  boolean wantList) {
        TreeSet<String> untracked = new TreeSet<>();

        for (String i : filesInDir) {
            if (!temp.containsKey(i)) {
                if (!wantList) {
                    System.out.println("There is an untracked file in the way; "
                            + "delete it, or add and commit it first.");
                    System.exit(0);
                }

                untracked.add(i);
            }
        }

        return untracked;
    }

    public static void printStatus() {
        validateGitletRepo();
        loadFullStage();

        System.out.println("=== Branches ===");
        TreeSet<String> branchList = new TreeSet<>();
        File[] files = LAST_COMMIT.listFiles();

        for (File i : files) {
            if (i.isDirectory()) {
                String[] l = i.list();
                String name = i.getName() + "/";

                for (String j : l) {
                    branchList.add(name + j);
                }
            } else {
                branchList.add(i.getName());
            }
        }

        String activeName = getActiveBranchName();
        System.out.println("*" + activeName);
        for (String i : branchList) {
            if (!i.equals(activeName)) {
                System.out.println(i);
            }
        }

        System.out.println("\n=== Staged Files ===");
        for (String i : stageAdd) {
            System.out.println(i);
        }

        System.out.println("\n=== Removed Files ===");
        for (String i : stageRemove) {
            System.out.println(i);
        }

        System.out.println("\n=== Modifications Not Staged For Commit ===");
        TreeSet<String> modified = new TreeSet<>();

        for (Map.Entry<String, String> i : nextMap.entrySet()) {
            File inCWD = join(CWD, i.getKey());

            if (inCWD.exists()) {
                String inCWDSha = sha1Object(readContents(inCWD), i.getKey());

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
        List<String> dr = plainFilenamesIn(CWD);
        TreeSet<String> untracked = untrackedFiles(dr, nextMap, true);

        for (String i : untracked) {
            System.out.println(i);
        }

        System.out.println();
    }

    public static void createBranch(String branchName) {
        validateGitletRepo();
        List<String> l = plainFilenamesIn(LAST_COMMIT);

        if (l.contains(branchName)) {
            System.out.println("A branch with that name already exists.");
            System.exit(0);
        }

        createNewBranch(branchName);
    }

    /**
     * Replaces the files in CWD with file that has the given sha
     */
    protected static void writeFileCWD(String fileName, String shaOfFile) {
        try {
            Files.copy(Paths.get(OBJECTS_DIR.getAbsolutePath(),
                            shaOfFile.substring(0, 1), shaOfFile),
                    CWD.toPath().resolve(fileName),
                    StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException ignored) {
            return;
        }
    }

    /**
     * Checkout the file in the commit to the working directory
     */
    public static void checkOutFile(String shaOfC, String fileName) {
        Commit c = getCommit(shaOfC);
        String shaOfFile = c.getSha(fileName);

        if (shaOfFile == null) {
            System.out.println("File does not exist in that commit.");
            System.exit(0);
        }

        writeFileCWD(fileName, shaOfFile);
    }

    /**
     * Writes everything from the given commit to the Working directory
     */
    private static void copyFromREPO(String shaOfCommit) {
        List<String> dr = plainFilenamesIn(CWD);
        loadStageMap();

        if (!(dr == null)) {
            untrackedFiles(dr, nextMap, false);

            for (String i : dr) {
                File delete = join(CWD, i);
                delete.delete();
            }
        }

        Commit branchCommit = getCommit(shaOfCommit);
        for (Map.Entry<String, String> i : branchCommit.getFilesInCommit().entrySet()) {
            writeFileCWD(i.getKey(), i.getValue());
        }

        nextMap = branchCommit.getFilesInCommit();
        saveStageMap();
        newArea();
    }

    /**
     * Checks out to the given branch and replaces everything
     */
    public static void checkOutBranch(String branchName) {
        validateGitletRepo();

        if (branchName.equals(getActiveBranchName())) {
            System.out.println("No need to checkout the current branch.");
            System.exit(0);
        }

        File branchFile = join(LAST_COMMIT, branchName);
        if (!branchFile.exists()) {
            System.out.println("No such branch exists.");
            System.exit(0);
        }

        copyFromREPO(getHead(branchName));
        writeObject(HEAD, branchName);
    }

    /**
     * Removes the given branch from the branch list does not affect any
     * associated commit
     */
    public static void removeBranch(String branchName) {
        validateGitletRepo();

        if (branchName.equals(getActiveBranchName())) {
            System.out.println("Cannot remove the current branch.");
            System.exit(0);
        }

        File branchFile = join(LAST_COMMIT, branchName);
        if (!branchFile.exists()) {
            System.out.println("A branch with that name does not exist.");
            System.exit(0);
        }

        File branchHEAD = new File(LAST_COMMIT, branchName);
        branchFile.delete();
        branchHEAD.delete();
    }

    /**
     * Resets the active branch to the given commit
     */
    public static void reset(String shaOfCommit) {
        validateGitletRepo();

        String fullCommit = getFullCommit(shaOfCommit);
        copyFromREPO(fullCommit);

        File head = join(LAST_COMMIT, getActiveBranchName());
        writeObject(head, fullCommit);
    }

    /**
     * Merges the given branch name into the current active branch
     */
    public static void mergebranch(String branchName) {
        validateGitletRepo();

        // Checking for non-existing branch
        File branchFile = join(LAST_COMMIT, branchName);
        if (!branchFile.exists()) {
            System.out.println("A branch with that name does not exist.");
            System.exit(0);
        }

        // Checking if branch is same as active branch
        String activeBranch = getActiveBranchName();
        if (activeBranch.equals(branchName)) {
            System.out.println("Cannot merge a branch with itself.");
            System.exit(0);
        }

        // Checking for untracked files
        loadFullStage();
        List<String> dr = plainFilenamesIn(CWD);
        if (!(dr == null)) {
            untrackedFiles(dr, nextMap, false);
        }

        // Checking for uncommitted changes
        if (!stageAdd.isEmpty() || !stageRemove.isEmpty()) {
            System.out.println("You have uncommitted changes.");
            System.exit(0);
        }

        Branch.mergeBranch(activeBranch, branchName);
    }
}
