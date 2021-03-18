package me.xrexy.quizo.utils;

import me.xrexy.quizo.Quizo;
import me.xrexy.quizo.questions.Question;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

public class FileUtils {
    private static final Quizo quizo = Quizo.getInstance();

    public static ArrayList<Question> getQuestions(FileConfiguration config) {
        ArrayList<Question> output = new ArrayList<>();

        ConfigurationSection questionsSection = config.getConfigurationSection("questions");
        if (questionsSection == null) {
            return output;
        }

        for (String key : questionsSection.getKeys(false)) {
            output.add(new Question(
                    key,
                    questionsSection.getString(key + ".question"),
                    questionsSection.getString(key + ".answers.correct"),
                    questionsSection.getStringList(key + ".answers.wrong")
            ));
        }

        return output;
    }

    public static FileConfiguration createFile(String fileName, boolean isResource) throws IOException, InvalidConfigurationException {
        File file = new File(quizo.getDataFolder(), fileName);
        FileConfiguration config = new YamlConfiguration();

        if (!file.exists()) {
            file.getParentFile().mkdirs();

            if (!isResource) {
                try {
                    file.createNewFile();
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            } else {
                quizo.saveResource(fileName, quizo.getResource(fileName) == null);
            }
        }
        config.load(file);
        return config;
    }
}
