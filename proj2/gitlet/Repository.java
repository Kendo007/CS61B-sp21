package gitlet;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;

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
     * All the files that are added to be committed
     */
    public static final File STAGING_AREA = join(GITLET_DIR, "stagingArea");
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
        if (Repository.GITLET_DIR.exists()) {
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
        Branch.BRANCH_DIR.mkdir();
        Commit.COMMITS_DIR.mkdir();

        Branch b = new Branch("master", null);
        Utils.writeObject(Branch.ACTIVE_BRANCH, "master");

        Commit initialCommit = new Commit("initial commit");
        Utils.writeObject(Commit.LATEST_COMMIT, initialCommit);
        b.addCommit();
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

        Commit temp = readObject(Commit.LATEST_COMMIT, Commit.class);
        String currFileSha = Utils.sha1(readContents(currFile));

        Commit latestCommit = Branch.getActiveBranch().getLatestCommit();
        String storedFileSha = latestCommit.getSha(fileName);

        if (currFileSha.equals(storedFileSha)) {
            temp.removeFile(fileName, currFileSha);
        } else {
            temp.putFile(fileName, currFileSha);
        }

        writeObject(Commit.LATEST_COMMIT, temp);
    }

    public static void commitFiles(String msg) {
        Branch br = Branch.getActiveBranch();

        Commit temp = readObject(Commit.LATEST_COMMIT, Commit.class);
        Commit latestCommit = br.getLatestCommit();

        if (temp.isSame(latestCommit)) {
            System.out.println("No changes added to the commit.");
            System.exit(0);
        }

        br.addCommit();
    }
}