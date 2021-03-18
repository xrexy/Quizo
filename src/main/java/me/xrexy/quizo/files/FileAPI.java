package me.xrexy.quizo.files;

import me.xrexy.quizo.Quizo;
import me.xrexy.quizo.utils.FileUtils;
import me.xrexy.quizo.utils.Utils;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;

import java.io.IOException;
import java.util.HashMap;
import java.util.logging.Level;

public class FileAPI {
    private final HashMap<String, FileConfiguration> files = new HashMap<>();
    private final Quizo quizo = Quizo.getInstance();

    public void registerFile(String fileName, boolean isResource) {
        if (!files.containsKey(fileName)) {
            try {
                files.put(fileName, FileUtils.createFile(fileName, isResource));
            } catch (IOException | InvalidConfigurationException exception) {
                if(quizo.getConfig().getBoolean("debug")) exception.printStackTrace();

                Utils.log(Level.SEVERE, "Couldn't load file " + fileName);
            }
        }
    }

    public HashMap<String, FileConfiguration> getFiles() {
        return files;
    }

    public FileConfiguration getFile(String fileName) {
        return files.get(fileName);
    }
}
