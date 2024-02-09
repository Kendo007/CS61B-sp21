package gitlet;

import java.io.File;
import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

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
        Branch.BRANCH_DIR.mkdir();
        Commit.COMMITS_DIR.mkdir();

        Branch b = new Branch("master", null);
        Utils.writeObject(Branch.ACTIVE_BRANCH, "master");

        Commit initialCommit = new Commit();
        Utils.writeObject(Commit.LATEST_COMMIT, initialCommit);

        b.addCommit("initial commit", new Date(0));
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
        validateGitletRepo();
        Branch br = Branch.getActiveBranch();

        Commit temp = readObject(Commit.LATEST_COMMIT, Commit.class);
        Commit latestCommit = br.getLatestCommit();

        if (temp.isSame(latestCommit)) {
            System.out.println("No changes added to the commit.");
            System.exit(0);
        }

        for (HashMap.Entry<String, String> i : temp.getFilesInCommit().entrySet()) {
            if (i.getValue().equals(latestCommit.getSha(i.getKey()))) {
                continue;
            }

            File f = Utils.join(CWD, i.getKey());
            byte[] b = Utils.readContents(f);

            String shaOfb = Utils.sha1(b);
            File stored = Utils.join(OBJECTS_DIR, shaOfb.substring(0, 1));
            stored.mkdir();

            f = Utils.join(stored, shaOfb);
            Utils.writeObject(f, b);
        }

        br.addCommit(msg, new Date());
    }

    public static void removeFile(String fileName) {
        validateGitletRepo();
        Branch br = Branch.getActiveBranch();

        Commit temp = readObject(Commit.LATEST_COMMIT, Commit.class);
        Commit latestCommit = br.getLatestCommit();

        if (latestCommit.getSha(fileName) != null) {
            File f = new File(CWD, fileName);
            f.delete();
        } else if (temp.getSha(fileName) != null) {
            temp.removeFile(fileName);
        } else {
            System.out.println("No reason to remove the file.");
        }
    }

    public static void printLog() {
        Branch br = Branch.getActiveBranch();
        Commit i = br.getLatestCommit();
        String shaOfi = br.getShaOfLatest();

        while (i != null) {
            System.out.println("===");
            System.out.println("commit " + shaOfi);
            System.out.println(i);
            System.out.println();

            shaOfi = i.getParent();
            i = Commit.getCommit(shaOfi);
        }
    }
}