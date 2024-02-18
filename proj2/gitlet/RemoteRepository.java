package gitlet;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.HashSet;
import java.util.List;

import static gitlet.Utils.*;
import static gitlet.Commit.*;

public class RemoteRepository extends Repository implements Serializable {
    public static final File REMOTE = Utils.join(Repository.GITLET_DIR, "remote");

    private String myName;
    private final File MY_LOCATION;
    private final File COMMITS_DIR;
    private final File HEAD;
    private final File LAST_COMMIT;
    private final File OBJECTS_DIR;

    /**
     * Saves a file pointer to the given path remote repo if it exists
     * @param givenPath the path given as string
     * @return file pointer of the path
     */
    private File fileToRemote(String givenPath) {
        try {
            File f = join(Repository.CWD, givenPath);
            return new File(f.getCanonicalPath());
        } catch (IOException e) {
            throw error("Some internal error occurred");
        }
    }

    RemoteRepository(String remoteName, String remotePath) {
        File remoteInfo = join(REMOTE, remoteName);

        if (remoteInfo.exists()) {
            System.out.println("A remote with that name already exists.");
            System.exit(0);
        }

        myName = remoteName;
        MY_LOCATION = fileToRemote(remotePath);
        COMMITS_DIR = join(MY_LOCATION, "commits");
        HEAD = join(MY_LOCATION, "HEAD");
        LAST_COMMIT = join(MY_LOCATION, "lastCommit");
        OBJECTS_DIR = join(MY_LOCATION, "objects");
        writeObject(remoteInfo, this);
    }

    public static void removeRemote(String remoteName) {
        if (!join(REMOTE, remoteName).delete()) {
            System.out.println("A remote with that name does not exist.");
            System.exit(0);
        }

        File deleteRemote = join(Branch.LAST_COMMIT, remoteName);
        List<String> l = plainFilenamesIn(deleteRemote);

        if (l != null) {
            for (String i : l) {
                File d = join(deleteRemote, i);
                d.delete();
            }
        }

        deleteRemote.delete();
    }

    /**
     * Returns the file pointer to the remote repo of the given name
     * @param remoteName name of the repo
     * @return file pointer to the repo
     */
    public static RemoteRepository getRemoteDir(String remoteName) {
        File remoteInfo = join(REMOTE, remoteName);

        if (!remoteInfo.exists()) {
            System.out.println("A remote with that name does not exist.");
            System.exit(0);
        }

        RemoteRepository remoteDir = readObject(remoteInfo, RemoteRepository.class);

        if (!remoteDir.MY_LOCATION.exists()) {
            System.out.println("Remote directory not found.");
            System.exit(0);
        }

        return remoteDir;
    }

    private String getRemoteActiveBranch() {
        return readObject(HEAD, String.class);
    }

    private String getRemoteHead() {
        return readObject(join(LAST_COMMIT, getRemoteActiveBranch()), String.class);
    }

    /**
     * Returns a set of all commits in active branch
     */
    private HashSet<String> getAllCommits(boolean copy, String remoteBranchName) {
        String remoteHeadSha = getRemoteHead();
        String myHeadSha = Branch.getHeadActive();
        Commit c = getCommit(myHeadSha);

        String temp = myHeadSha;

        HashSet<String> hs = new HashSet<>();
        boolean found = false;

        while (c != null) {
            if (remoteHeadSha.equals(myHeadSha)) {
                found = true;
                break;
            }

            if (copy) {
                writeObject(COMMITS_DIR, c);
            } else {
                hs.add(myHeadSha);
            }

            myHeadSha = c.getParent();
            c = getCommit(myHeadSha);
        }

        if (!found && !copy) {
            System.out.println("Please pull down remote changes before pushing.");
            System.exit(0);
        }

        writeObject(join(LAST_COMMIT, remoteBranchName), temp);
        writeObject(HEAD, remoteBranchName);
        return hs;
    }

    private void copyEverything(File src, File dest) {
        dest.mkdir();
        String[] files = src.list();

        for (String i : files) {
            File srcF = join(src, i);
            File destF = join(dest, i);

            if (srcF.isDirectory()) {
                copyEverything(srcF, destF);
            } else if (!destF.exists()) {
                writeContents(destF, (Object) readContents(srcF));
            }
        }
    }

    public void push(String remoteBranchName) {
        File remoteInfo = join(LAST_COMMIT, remoteBranchName);

        if (!remoteInfo.exists()) {
            getAllCommits(true, remoteBranchName);
        } else {
            HashSet<String> hs = getAllCommits(false, remoteBranchName);

            for (String i : hs) {
                writeObject(join(COMMITS_DIR, i), Commit.getCommit(i));
            }
        }

        copyEverything(Repository.OBJECTS_DIR, this.OBJECTS_DIR);
    }


    public void fetch(String remoteBranchName) {
        File f = join(LAST_COMMIT, remoteBranchName);

        if (!f.exists()) {
            System.out.println("That remote does not have that branch.");
            System.exit(0);
        }

        copyEverything(COMMITS_DIR, Commit.COMMITS_DIR);
        copyEverything(OBJECTS_DIR, Repository.OBJECTS_DIR);

        String newBranchName = myName + "/" + remoteBranchName;
        File remoteBranchDir = join(Branch.LAST_COMMIT, myName);
        remoteBranchDir.mkdir();
        try {
            Files.copy(LAST_COMMIT.toPath().resolve(remoteBranchName),
                    Branch.LAST_COMMIT.toPath().resolve(newBranchName),
                    StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException ignored) {
            return;
        }
    }

    public void pull(String remoteBranchName) {
        fetch(remoteBranchName);
        mergebranch(myName + "/" + remoteBranchName);
    }
}
