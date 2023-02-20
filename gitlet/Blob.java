package gitlet;

import java.io.Serializable;

public class Blob implements Serializable {
    /** blob name. */
    private String _name;
    /** blob contents. */
    private String _contents;

    public Blob(String name, String contents) {
        _name = name;
        _contents = contents;
    }

    public String getName() {
        return _name;
    }

    public String getHash() {
        return Utils.sha1(_name, _contents);
    }

    public String getContents() {
        return _contents;
    }
}
