package me.xrexy.quizo;

import me.xrexy.quizo.files.FileAPI;
import me.xrexy.quizo.questions.QuestionAPI;
import org.bukkit.plugin.java.JavaPlugin;

public final class Quizo extends JavaPlugin {
    private static Quizo instance;
    private FileAPI fileAPI;
    private QuestionAPI questionAPI;

    @Override
    public void onEnable() {
        Quizo.instance = this;
        this.fileAPI = new FileAPI();

        this.questionAPI = new QuestionAPI();

        new PluginLoader().load();
    }

    public FileAPI getFileAPI() {
        return fileAPI;
    }

    public QuestionAPI getQuestionAPI() {
        return questionAPI;
    }

    public static Quizo getInstance() {
        return instance;
    }
}
