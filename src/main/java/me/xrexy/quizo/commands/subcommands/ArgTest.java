package me.xrexy.quizo.commands.subcommands;

import me.xrexy.quizo.Quizo;
import me.xrexy.quizo.commands.CommandInterface;
import me.xrexy.quizo.questions.QuestionAPI;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class ArgTest implements CommandInterface {
    private final Quizo quizo = Quizo.getInstance();
    private final QuestionAPI questionAPI = quizo.getQuestionAPI();

    @Override
    public String getCommand() {
        return "test";
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String[] args) {
        Player player = (Player) sender;

        questionAPI.pushQuestion();

        return true;
    }
}
