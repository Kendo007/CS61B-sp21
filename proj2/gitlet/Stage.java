package gitlet;

import java.io.File;
import java.util.HashMap;
import java.util.TreeSet;

import static gitlet.Utils.*;

/**
 * This class is a static class. It odes not have any object of his own
 * It is mainly used to manipulate the staging area of the repo
 */
public class Stage {
    /** Folder where staged files are stored */
    public static final File STAGING_AREA = join(Repository.GITLET_DIR, "staging");
    /** All the files that are ready to be committed */
    public static final File STAGED_ADD = join(STAGING_AREA, "add");
    /** All the files that are ready to be removed */
    public static final File STAGED_REMOVED = join(STAGING_AREA, "remove");
    /** All the files that should be tracked and untracked in the next commit */
    public static final File LATEST_MAP = Utils.join(STAGING_AREA, "nextMap");
    public static TreeSet<String> stageAdd;
    public static TreeSet<String> stageRemove;
    public static HashMap<String, String> nextMap;

    /** Loads full staging area in the private members */
    public static void loadFullStage() {
        stageAdd = readObject(STAGED_ADD, TreeSet.class);
        stageRemove = readObject(STAGED_REMOVED, TreeSet.class);
        nextMap = readObject(LATEST_MAP, HashMap.class);
    }

    public static void loadStageMap() {
        nextMap = readObject(LATEST_MAP, HashMap.class);
    }

    public static void saveStageArea() {
        writeObject(STAGED_ADD, stageAdd);
        writeObject(STAGED_REMOVED, stageRemove);
    }

    public static void saveStageMap() {
        writeObject(LATEST_MAP, nextMap);
    }

    public static void saveFullStage() {
        saveStageMap();
        saveStageArea();
    }

    public static void newArea() {
        writeObject(STAGED_ADD, new TreeSet<>());
        writeObject(STAGED_REMOVED, new TreeSet<>());
    }
}
