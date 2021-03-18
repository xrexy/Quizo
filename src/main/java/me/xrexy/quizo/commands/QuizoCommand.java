package me.xrexy.quizo.commands;

import me.xrexy.quizo.utils.Utils;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class QuizoCommand implements CommandInterface {
    @Override
    public String getCommand() {
        return "quizo";
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String[] args) {
        Utils.sendMultilineMessage((Player) sender, "messages.help");
        return true;
    }
}
