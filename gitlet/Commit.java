package gitlet;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;

public class Commit implements Serializable {
    /** Commit message. */
    private String _logMessage;
    /** Commit date. */
    private String _date;
    /** Commit parentHash. */
    private String _parentHash;
    /** Commit hashmap of file names and hash. */
    private HashMap<String, String> _fileNames;

    public Commit(String message, String parent) {
        _logMessage = message;
        _parentHash = parent;
        _fileNames = new HashMap<>();
        SimpleDateFormat simple = new
                SimpleDateFormat("EEE MMM dd HH:mm:ss yyyy Z");
        if (parent == null) {
            _date = simple.format(new Date(0));
        } else {
            _date = simple.format(new Date());
        }
    }

    public void toFile() {
        File savedCommit = new File(".gitlet/commits/" + this.getHash());
        try {
            savedCommit.createNewFile();
        } catch (IOException e) {
            e.printStackTrace();
        }
        Utils.writeObject(savedCommit, this);

    }

    public String getMessage() {
        return _logMessage;
    }

    public String getDate() {
        return _date;
    }

    public String getParent() {
        return _parentHash;
    }

    public String getHash() {
        return Utils.sha1(_date, _logMessage);
    }

    public void setFileNames(HashMap<String, String> files) {
        _fileNames = files;
    }

    public HashMap<String, String> getFileNames() {
        return _fileNames;
    }

    public String getFile(String fileName) {
        return _fileNames.get(fileName);
    }
}
