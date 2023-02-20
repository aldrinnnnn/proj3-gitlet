package gitlet;

import java.io.File;
import java.io.Serializable;
import java.util.HashMap;
import java.util.HashSet;

public class Staging implements Serializable {
    /** Stage for addition. */
    private HashMap<String, String> _addStage;
    /** Stage for removal. */
    private HashSet<String> _remStage;

    public Staging() {
        _addStage = new HashMap<>();
        _remStage = new HashSet<>();
    }

    public void add(String fileName, String hash) {
        _addStage.put(fileName, hash);
    }

    public void remove(String fileName) {
        _remStage.add(fileName);
    }

    public String getBlob(String fileName) {
        return _addStage.get(fileName);
    }

    public void toFile() {
        File savedStage = new File(".gitlet/staging");
        Utils.writeObject(savedStage, this);
    }

    public HashMap<String, String> getAddStage() {
        return _addStage;
    }

    public HashSet<String> getRemStage() {
        return _remStage;
    }
}
