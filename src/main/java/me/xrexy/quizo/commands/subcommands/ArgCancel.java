package me.xrexy.quizo.commands.subcommands;

import me.xrexy.quizo.Quizo;
import me.xrexy.quizo.commands.CommandInterface;
import me.xrexy.quizo.questions.QuestionAPI;
import me.xrexy.quizo.utils.Utils;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class ArgCancel implements CommandInterface {
    private final Quizo quizo = Quizo.getInstance();
    private final QuestionAPI questionAPI = quizo.getQuestionAPI();

    @Override
    public String getCommand() {
        return "cancel";
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String[] args) {
        Player player = (Player) sender;

        if (!questionAPI.isRunning) {
            Utils.sendMessage(player, "messages.quiz.not-running");
            return true;
        }

        questionAPI.cancelCurrentQuestion();

        Utils.sendMessage(player, "messages.cancel.sender");
        if (quizo.getConfig().getBoolean("messages.cancel.global-toggle")) {
            Bukkit.getOnlinePlayers().forEach(onlinePlayer -> {
                Utils.sendRaw(onlinePlayer, Utils.process(Utils.getString("messages.cancel.global").replace("%player%", player.getName())));
            });
        }
        return true;
    }
}
