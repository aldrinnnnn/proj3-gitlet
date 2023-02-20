package gitlet;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.TreeSet;
import java.util.Set;
import java.util.List;

public class Initialize {
    /** File path for CWD. */
    static final File CWD = new File(System.getProperty("user.dir"));
    /** File path for .gitlet folder. */
    static final File GITLET = new File(".gitlet");
    /** File path for COMMITS. */
    static final File COMMITS = Utils.join(GITLET, "commits");
    /** File path for BLOBS. */
    static final File BLOBS = Utils.join(GITLET, "blobs");
    /** File path for STAGING. */
    static final File STAGING = Utils.join(GITLET, "staging");
    /** File path for BRANCH. */
    static final File BRANCH = Utils.join(GITLET, "branch");
    /** File path for HEAD. */
    static final File HEAD = Utils.join(GITLET, "head");
    /** File path for ACTIVE. */
    static final File ACTIVE = Utils.join(GITLET, "active");
    /** File path for MASTER. */
    static final File MASTER = Utils.join(BRANCH, "master");

    public void init() {
        if (!GITLET.exists()) {
            GITLET.mkdir();
            COMMITS.mkdir();
            BLOBS.mkdir();
            BRANCH.mkdir();
            try {
                HEAD.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
            try {
                MASTER.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
            try {
                ACTIVE.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
            try {
                STAGING.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
            Commit initial = new Commit("initial commit", null);
            initial.toFile();
            Staging stagingArea = new Staging();
            stagingArea.toFile();
            Branch master = new Branch("master", initial.getHash());
            Utils.writeContents(ACTIVE, master.getName());
            Utils.writeContents(MASTER, initial.getHash());
            Utils.writeContents(HEAD, initial.getHash());
        } else {
            System.out.println("A Gitlet version-control system "
                    + "already exists in the current directory.");
            System.exit(0);
        }
    }
    public void add(String fileName) {
        Staging stagingArea = Utils.readObject(STAGING, Staging.class);
        if (stagingArea.getRemStage().contains(fileName)) {
            stagingArea.getRemStage().remove(fileName);
            stagingArea.toFile();
        } else {
            if (Utils.join(CWD, fileName).exists()) {
                Blob b = new Blob(fileName,
                        Utils.readContentsAsString(Utils.join(CWD, fileName)));
                File file = Utils.join(BLOBS, b.getHash());
                try {
                    file.createNewFile();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                if (stagingArea.getBlob(fileName) == null) {
                    stagingArea.add(fileName, b.getHash());
                } else if (stagingArea.getBlob(fileName).compareTo(b.getHash())
                        != 0) {
                    stagingArea.add(fileName, b.getHash());
                }
                if (Utils.readObject(Utils.join(COMMITS,
                        Utils.readContentsAsString(HEAD)),
                        Commit.class).getFile(fileName) != null) {
                    if (stagingArea.getBlob(fileName).compareTo(
                            Utils.readObject(Utils.join(COMMITS,
                                    Utils.readContentsAsString(HEAD)),
                                    Commit.class).getFile(fileName)) == 0) {
                        stagingArea.getAddStage().remove(fileName);
                    }
                }
                Utils.writeObject(file, b);
                stagingArea.toFile();
            } else {
                System.out.println("File does not exist.");
                System.exit(0);
            }
        }
    }
    public void rm(String fileName) {
        Staging stagingArea = Utils.readObject(STAGING, Staging.class);
        Commit curr = Utils.readObject(Utils.join(COMMITS,
                Utils.readContentsAsString(HEAD)), Commit.class);
        if (stagingArea.getBlob(fileName) != null) {
            stagingArea.getAddStage().remove(fileName);
        } else if (curr.getFile(fileName) != null) {
            stagingArea.remove(fileName);
            Utils.restrictedDelete(Utils.join(CWD, fileName));
        } else {
            System.out.println("No reason to remove the file.");
            System.exit(0);
        }
        stagingArea.toFile();
    }
    public void commit(String message) {
        HashMap<String, String> parentFiles
                = Utils.readObject(Utils.join(COMMITS,
                Utils.readContentsAsString(HEAD)),
                Commit.class).getFileNames();
        Staging stagingArea = Utils.readObject(STAGING, Staging.class);
        HashMap<String, String> aStage = stagingArea.getAddStage();
        HashSet<String> rStage = stagingArea.getRemStage();
        if (!aStage.isEmpty() || !rStage.isEmpty()) {
            for (Map.Entry<String, String> pair : aStage.entrySet()) {
                parentFiles.put(pair.getKey(), pair.getValue());
            }
            for (String file : rStage) {
                parentFiles.remove(file);
            }
            Commit c = new Commit(message,
                    Utils.readContentsAsString(HEAD));
            c.setFileNames(parentFiles);
            aStage.clear();
            rStage.clear();
            c.toFile();
            stagingArea.toFile();
            Utils.writeContents(HEAD, c.getHash());
            Utils.writeContents(Utils.join(BRANCH,
                    Utils.readContentsAsString(ACTIVE)), c.getHash());
        } else {
            System.out.println("No changes added to the commit.");
            System.exit(0);
        }
    }
    public void log() {
        Commit curr = Utils.readObject(Utils.join(COMMITS,
                Utils.readContentsAsString(HEAD)), Commit.class);
        while (curr.getParent() != null) {
            System.out.println("===\ncommit " + curr.getHash() + "\nDate: "
                    + curr.getDate() + "\n" + curr.getMessage() + "\n");
            curr = Utils.readObject(Utils.join(COMMITS, curr.getParent()),
                    Commit.class);
        }
        System.out.println("===\ncommit " + curr.getHash() + "\nDate: "
                + curr.getDate() + "\n" + curr.getMessage() + "\n");
    }
    public void global() {
        ArrayList<String> commits
                = new ArrayList<>(Utils.plainFilenamesIn(COMMITS));
        for (int i = 0; i < commits.size(); i++) {
            Commit c = Utils.readObject(Utils.join(COMMITS,
                    commits.get(i)), Commit.class);
            System.out.println("===\ncommit " + c.getHash()
                    + "\nDate: " + c.getDate() + "\n" + c.getMessage() + "\n");
        }
    }
    public void checkout(String dash, String fileName) {
        Commit head = Utils.readObject(Utils.join(COMMITS,
                Utils.readContentsAsString(HEAD)), Commit.class);
        if (head.getFile(fileName) != null) {
            Blob blobHead = Utils.readObject(Utils.join(BLOBS,
                    head.getFile(fileName)), Blob.class);
            File file = Utils.join(CWD, fileName);
            Utils.writeContents(file, blobHead.getContents());
        } else {
            System.out.println("File does not exist in that commit.");
            System.exit(0);
        }
    }
    public void checkout(String commitID, String dash, String fileName) {
        Commit commit = null;
        if (commitID.length() == 8) {
            ArrayList<String> commits
                    = new ArrayList<String>(Utils.plainFilenamesIn(COMMITS));
            for (int i = 0; i < commits.size(); i++) {
                Commit c = Utils.readObject(Utils.join(COMMITS,
                        commits.get(i)), Commit.class);
                if (c.getHash().substring(0, 8).equals(commitID)) {
                    commit = c;
                }
            }
        }
        if (commit != null || Utils.join(COMMITS, commitID).exists()) {
            if (commit == null) {
                commit = Utils.readObject(Utils.join(COMMITS,
                        commitID), Commit.class);
            }
            if (commit.getFile(fileName) != null) {
                String hash = commit.getFile(fileName);
                Blob blob = Utils.readObject(Utils.join(BLOBS,
                        hash), Blob.class);
                File file = Utils.join(CWD, fileName);
                Utils.writeContents(file, blob.getContents());
            } else {
                System.out.println("File does not exist in that commit.");
                System.exit(0);
            }
        } else {
            System.out.println("No commit with that id exists.");
            System.exit(0);
        }
    }
    public void checkout(String bName) {
        Commit head = Utils.readObject(Utils.join(COMMITS,
                Utils.readContentsAsString(HEAD)), Commit.class);
        if (Utils.join(BRANCH, bName).exists()) {
            if (!bName.equals(Utils.readContentsAsString(ACTIVE))) {
                ArrayList<String> cwd
                        = new ArrayList<>(Utils.plainFilenamesIn(CWD));
                Commit check = Utils.readObject(Utils.join(COMMITS,
                        Utils.readContentsAsString(Utils.join(BRANCH,
                                bName))), Commit.class);
                Staging stagingArea = Utils.readObject(STAGING, Staging.class);
                ArrayList<String> untracked = untracked();
                if (!untracked.isEmpty()) {
                    System.out.println("There is an untracked file in the "
                            + "way; delete it, or add and commit it first.");
                    System.exit(0);
                }
                for (int i = 0; i < cwd.size(); i++) {
                    String contents
                            = Utils.readContentsAsString(Utils.join(CWD,
                            cwd.get(i)));
                    Blob b = new Blob(cwd.get(i), contents);
                    if (b.getHash().equals(head.getFile(cwd.get(i)))
                            && check.getFile(cwd.get(i)) == null) {
                        Utils.join(CWD, cwd.get(i)).delete();
                    }
                }
                for (Map.Entry<String, String> pair
                        : check.getFileNames().entrySet()) {
                    Blob blob = Utils.readObject(Utils.join(BLOBS,
                            pair.getValue()), Blob.class);
                    File file = Utils.join(CWD, pair.getKey());
                    Utils.writeContents(file, blob.getContents());
                }
                stagingArea.getRemStage().clear();
                stagingArea.getAddStage().clear();
                stagingArea.toFile();
                Utils.writeContents(ACTIVE, bName);
                Utils.writeContents(HEAD, check.getHash());
                Utils.writeContents(Utils.join(BRANCH,
                        Utils.readContentsAsString(ACTIVE)), check.getHash());
            } else {
                System.out.println("No need to checkout the current branch.");
                System.exit(0);
            }
        } else {
            System.out.println("No such branch exists.");
            System.exit(0);
        }
    }
    public void find(String message) {
        ArrayList<String> commits
                = new ArrayList<String>(Utils.plainFilenamesIn(COMMITS));
        Boolean signal = true;
        for (int i = 0; i < commits.size(); i++) {
            Commit c = Utils.readObject(Utils.join(COMMITS,
                    commits.get(i)), Commit.class);
            if (c.getMessage().equals(message)) {
                System.out.println(c.getHash());
                signal = false;
            }
        }
        if (signal) {
            System.out.println("Found no commit with that message.");
            System.exit(0);
        }
    }
    public void branch(String bName) {
        if (!Utils.join(BRANCH, bName).exists()) {
            Branch b = new Branch(bName, Utils.readContentsAsString(HEAD));
            b.toFile();
        } else {
            System.out.println("A branch with that name already exists.");
            System.exit(0);
        }
    }
    public void rmBranch(String bName) {
        if (Utils.join(BRANCH, bName).exists()) {
            if (!bName.equals(Utils.readContentsAsString(ACTIVE))) {
                Utils.join(BRANCH, bName).delete();
            } else {
                System.out.println("Cannot remove the current branch.");
                System.exit(0);
            }
        } else {
            System.out.println("A branch with that name does not exist.");
            System.exit(0);
        }
    }
    public ArrayList<String> untracked() {
        Commit head = Utils.readObject(Utils.join(COMMITS,
                Utils.readContentsAsString(HEAD)), Commit.class);
        ArrayList<String> cwd = new ArrayList<>(Utils.plainFilenamesIn(CWD));
        ArrayList<String> untracked = new ArrayList<>();
        Staging stagingArea = Utils.readObject(STAGING, Staging.class);
        for (int i = 0; i < cwd.size(); i++) {
            String file = cwd.get(i);
            if (head.getFile(file) == null) {
                untracked.add(file);
            }
        }
        for (int i = 0; i < untracked.size(); i++) {
            if (stagingArea.getAddStage().containsKey(untracked.get(i))
                    || stagingArea.getRemStage().contains(untracked.get(i))) {
                untracked.remove(i);
            }
        }
        return untracked;
    }
    public void status() {
        ArrayList<String> branches
                = new ArrayList<>(Utils.plainFilenamesIn(BRANCH));
        Staging stagingArea = Utils.readObject(STAGING, Staging.class);
        HashMap<String, String> stage = stagingArea.getAddStage();
        Set<String> set = new HashSet<>();
        for (Map.Entry<String, String> pair : stage.entrySet()) {
            set.add(pair.getKey());
        }
        Set<String> sort = new TreeSet<>(set);
        List<String> finalAdd = new ArrayList<>(sort);
        HashSet<String> rem = stagingArea.getRemStage();
        Set<String> sortR = new TreeSet<>(rem);
        List<String> finalRem = new ArrayList<>(sortR);
        System.out.println("=== Branches ===");
        for (int i = 0; i < branches.size(); i++) {
            if (branches.get(i).equals(Utils.readContentsAsString(ACTIVE))) {
                System.out.println("*" + branches.get(i));
            } else {
                System.out.println(branches.get(i));
            }
        }
        System.out.print("\n");
        System.out.println("=== Staged Files ===");
        for (int i = 0; i < finalAdd.size(); i++) {
            System.out.println(finalAdd.get(i));
        }
        System.out.print("\n");
        System.out.println("=== Removed Files ===");
        for (int i = 0; i < finalRem.size(); i++) {
            System.out.println(finalRem.get(i));
        }
        System.out.print("\n");
        System.out.println("=== Modifications Not Staged For Commit ===\n");
        System.out.println("=== Untracked Files ===");
    }
    public void reset(String hash) {
        if (Utils.join(COMMITS, hash).exists()) {
            Staging stagingArea = Utils.readObject(STAGING, Staging.class);
            Commit check = Utils.readObject(Utils.join(COMMITS, hash),
                    Commit.class);
            Commit head = Utils.readObject(Utils.join(COMMITS,
                    Utils.readContentsAsString(HEAD)), Commit.class);
            ArrayList<String> cwd
                    = new ArrayList<>(Utils.plainFilenamesIn(CWD));
            for (int i = 0; i < cwd.size(); i++) {
                String contents = Utils.readContentsAsString(Utils.join(CWD,
                        cwd.get(i)));
                Blob b = new Blob(cwd.get(i), contents);
                if (check.getFileNames().containsKey(cwd.get(i))) {
                    if (!b.getHash().equals(head.getFile(cwd.get(i)))) {
                        System.out.println("There is an untracked file "
                                + "in the way; delete it, or add and"
                                + " commit it first.");
                        System.exit(0);
                    }
                }
            }
            for (int i = 0; i < cwd.size(); i++) {
                String contents = Utils.readContentsAsString(Utils.join(CWD,
                        cwd.get(i)));
                Blob b = new Blob(cwd.get(i), contents);
                if (b.getHash().equals(head.getFile(cwd.get(i)))
                        && check.getFile(cwd.get(i)) == null) {
                    Utils.join(CWD, cwd.get(i)).delete();
                }
            }
            for (Map.Entry<String, String> pair
                    : check.getFileNames().entrySet()) {
                Blob blob = Utils.readObject(Utils.join(BLOBS,
                        pair.getValue()), Blob.class);
                File file = Utils.join(CWD, blob.getName());
                Utils.writeContents(file, blob.getContents());
            }
            stagingArea.getRemStage().clear();
            stagingArea.getAddStage().clear();
            stagingArea.toFile();
            Utils.writeContents(HEAD, hash);
            Utils.writeContents(Utils.join(BRANCH,
                    Utils.readContentsAsString(ACTIVE)), hash);
        } else {
            System.out.println("No commit with that id exists.");
            System.exit(0);
        }
    }
    public void merge(String bName) {
        Staging stagingArea = Utils.readObject(STAGING, Staging.class);
        ArrayList<String> untracked = untracked();
        if (!Utils.readContentsAsString(ACTIVE).equals(bName)) {
            if (Utils.join(BRANCH, bName).exists()) {
                if (stagingArea.getAddStage().isEmpty()
                        && stagingArea.getRemStage().isEmpty()) {
                    if (!untracked.isEmpty()) {
                        System.out.println("There is an untracked file in the "
                                + "way; delete it, or"
                                + " add and commit it first.");
                        System.exit(0);
                    }
                } else {
                    System.out.println("You have uncommitted changes.");
                    System.exit(0);
                }
            } else {
                System.out.println("A branch with that name does not exist.");
                System.exit(0);
            }
        } else {
            System.out.println("Cannot merge a branch with itself.");
            System.exit(0);
        }
    }
}
