package me.xrexy.quizo;

import me.xrexy.quizo.commands.CommandManager;
import me.xrexy.quizo.commands.QuizoCommand;
import me.xrexy.quizo.commands.subcommands.ArgAdmin;
import me.xrexy.quizo.commands.subcommands.ArgCancel;
import me.xrexy.quizo.commands.subcommands.ArgGuess;
import me.xrexy.quizo.commands.subcommands.ArgTest;
import me.xrexy.quizo.files.FileAPI;
import me.xrexy.quizo.questions.QuestionAPI;
import org.bukkit.Bukkit;
import org.bukkit.command.PluginCommand;
import org.bukkit.plugin.PluginManager;

public class PluginLoader {
    private final Quizo quizo = Quizo.getInstance();
    private final FileAPI fileAPI = quizo.getFileAPI();
    private final QuestionAPI questionAPI = quizo.getQuestionAPI();
    private final PluginManager pluginManager = Bukkit.getPluginManager();

    public void load() {
        loadFiles();
        loadCommands();

        // loads all questions in QuestionAPI
        questionAPI.load();
        pluginManager.registerEvents(new ArgAdmin.AdminInventories(), quizo);

        Bukkit.getScheduler().runTaskLater(quizo, questionAPI::pushQuestion, quizo.getConfig().getInt("delay-on-enable") * 20L);
    }

    void loadCommands() {
        QuizoCommand quizoCommand = new QuizoCommand();
        PluginCommand quizoMainCommand = Bukkit.getPluginCommand(quizoCommand.getCommand());

        if (quizoMainCommand == null) {
            Bukkit.getLogger().severe(String.format("[%s] - Disabled, couldn't load managemaps command!", quizo.getDescription().getName()));
            quizo.getServer().getPluginManager().disablePlugin(quizo);
            return;
        }

        CommandManager quizoCommandManager = new CommandManager(quizoCommand.getCommand());
        quizoMainCommand.setExecutor(quizoCommandManager);
        // TODO tab completer

        quizoCommandManager.register(quizoCommand.getCommand(), quizoCommand);

        ArgTest test = new ArgTest();
        quizoCommandManager.register(test.getCommand(), test);

        ArgGuess guess = new ArgGuess();
        quizoCommandManager.register(guess.getCommand(), guess);

        ArgCancel cancel = new ArgCancel();
        quizoCommandManager.register(cancel.getCommand(), cancel);

        ArgAdmin admin = new ArgAdmin();
        quizoCommandManager.register(admin.getCommand(), admin);
    }

    void loadFiles() {
        quizo.getConfig().options().copyDefaults(true);
        quizo.saveDefaultConfig();

        fileAPI.registerFile("questions.yml", true);
    }
}
