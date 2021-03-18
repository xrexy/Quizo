package me.xrexy.quizo.commands.subcommands;

import me.xrexy.quizo.Quizo;
import me.xrexy.quizo.commands.CommandInterface;
import me.xrexy.quizo.questions.QuestionAPI;
import me.xrexy.quizo.utils.Utils;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class ArgGuess implements CommandInterface {
    private final Quizo quizo = Quizo.getInstance();
    private final QuestionAPI questionAPI = quizo.getQuestionAPI();

    @Override
    public String getCommand() {
        return "guess";
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String[] args) {
        Player player = (Player) sender;

        if (!questionAPI.isRunning) {
            Utils.sendMessage(player, "messages.quiz.not-running");
            return true;
        }

        if (args.length > 0) {
            if (args.length > 1) {
                try {
                    int guessedID = Integer.parseInt(args[1]);

                    questionAPI.onGuess(player, guessedID);
                } catch (Exception e) {
                    Utils.sendMessage(player, "messages.guess.invalid-guess");
                }
            } else {
                Utils.sendMessage(player, "messages.invalid-args");
            }
        }

        return true;
    }
}
