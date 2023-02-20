package gitlet;

import java.io.File;
import java.io.Serializable;

public class Branch implements Serializable {
    /** branch name. */
    private String _name;
    /** branch's commit pointer. */
    private String _pointer;

    public Branch(String name, String commit) {
        _name = name;
        _pointer = commit;
    }
    public String getName() {
        return _name;
    }
    public String getPointer() {
        return _pointer;
    }
    public String getHash() {
        return Utils.sha1(_name, _pointer);
    }

    public void toFile() {
        File savedBranch = new File(".gitlet/branch/" + _name);
        Utils.writeContents(savedBranch, _pointer);
    }
}
