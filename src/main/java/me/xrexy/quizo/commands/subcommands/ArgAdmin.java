package me.xrexy.quizo.commands.subcommands;

import me.xrexy.quizo.Quizo;
import me.xrexy.quizo.commands.CommandInterface;
import me.xrexy.quizo.questions.Question;
import me.xrexy.quizo.questions.QuestionAPI;
import me.xrexy.quizo.utils.Utils;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicInteger;

public class ArgAdmin implements CommandInterface {
    public static class AdminInventories implements Listener {
        private static final Quizo quizo = Quizo.getInstance();
        private static final QuestionAPI questionAPI = quizo.getQuestionAPI();
        private static final ItemStack blackGlassPane = new ItemStack(Material.STAINED_GLASS_PANE, 1, (byte) 15);

        private static final String initTitle = Utils.colorize("&0MANAGE QUESTIONS");
        private static final String startTitle = "Loaded " + questionAPI.getQuestions().size() + " questions";

        public static Inventory getInitInventory() {
            Inventory init = Bukkit.createInventory(null, 27, initTitle);

            ItemMeta glassMeta = blackGlassPane.getItemMeta();
            glassMeta.setDisplayName("");
            blackGlassPane.setItemMeta(glassMeta);

            for (int i = 0; i <= 26; i++) {
                init.setItem(i, blackGlassPane);
            }

            ItemStack cancelItem = new ItemStack(Material.STAINED_CLAY, 1, (byte) 14);
            {
                ItemMeta cancelItemMeta = cancelItem.getItemMeta();
                assert cancelItemMeta != null;

                ArrayList<String> lore = new ArrayList<>();
                lore.add(" ");
                lore.add(questionAPI.isRunning ? Utils.colorize("&cClick to cancel current question!") : Utils.colorize("&aThere is no question to cancel."));

                cancelItemMeta.setDisplayName(Utils.colorize("&4&lCANCEL QUESTION"));
                cancelItemMeta.setLore(lore);

                cancelItem.setItemMeta(cancelItemMeta);
            }

            byte colorID = (byte) (questionAPI.isRunning ? 4 : 5);
            ItemStack startItem = new ItemStack(Material.STAINED_GLASS_PANE, 1, colorID);
            {
                ItemMeta startItemMeta = startItem.getItemMeta();
                assert startItemMeta != null;

                ArrayList<String> lore = new ArrayList<>();
                lore.add(" ");
                lore.add(questionAPI.isRunning ? Utils.colorize("&fThere is already an question running!") : Utils.colorize("&fClick to select a new question."));

                startItemMeta.setDisplayName(questionAPI.isRunning ? Utils.colorize("&e&lQUESTION ACTIVE") : Utils.colorize("&a&lNEW QUESTION"));
                startItemMeta.setLore(lore);

                startItem.setItemMeta(startItemMeta);
            }

            init.setItem(14, cancelItem);
            init.setItem(12, startItem);
            return init;
        }

        private static Inventory getStartMenu() {
            Inventory inventory = Bukkit.createInventory(null, 54, startTitle);

            AtomicInteger i = new AtomicInteger();
            ItemStack questionItem = new ItemStack(Material.SNOW_BALL);
            ItemMeta questionMeta = questionItem.getItemMeta();
            ArrayList<String> lore = new ArrayList<>();
            questionAPI.getQuestions().forEach(question -> {
                lore.add(Utils.colorize("&fCorrect answer: &a" + question.getCorrectAnswer()));
                lore.add("");
                lore.add(Utils.colorize("&fIncorrect answers:"));
                question.getWrongAnswers().forEach(wrongAnswer -> {
                    lore.add(Utils.colorize(" &c" + wrongAnswer));
                });
                lore.add("");
                lore.add(Utils.colorize("&8Title: ") + question.getTitle());
                questionMeta.setLore(lore);
                lore.clear();

                questionMeta.setDisplayName(Utils.colorize("&e" + question.getQuestion()));
                questionItem.setItemMeta(questionMeta);
                inventory.setItem(i.get(), questionItem);
                i.getAndIncrement();
            });
            return inventory;
        }

        @EventHandler
        public void inventoryClick(InventoryClickEvent e) {
            Inventory inventory = e.getClickedInventory();
            int slot = e.getRawSlot();
            Player player = (Player) e.getWhoClicked();

            if (inventory.getTitle() != null && inventory.getTitle().equalsIgnoreCase(initTitle)) {
                if (slot == 14) { // cancel item slot
                    if (!questionAPI.isRunning) {
                        Utils.sendRaw(player, Utils.process("%prefix% &cNo active question"));
                        player.closeInventory();
                        return;
                    }
                    questionAPI.cancelCurrentQuestion();

                    Utils.sendMessage(player, "messages.cancel.sender");
                    if (quizo.getConfig().getBoolean("messages.cancel.global-toggle")) {
                        Bukkit.getOnlinePlayers().forEach(onlinePlayer -> {
                            Utils.sendRaw(onlinePlayer, Utils.process(Utils.getString("messages.cancel.global").replace("%player%", player.getName())));
                        });
                    }
                }
                if (slot == 12) { // start menu slot
                    if (questionAPI.isRunning) {
                        Utils.sendRaw(player, Utils.process("%prefix% &eAn question is already running"));
                        player.closeInventory();
                        return;
                    }

                    Bukkit.getScheduler().runTaskLater(quizo, () -> {
                        player.openInventory(getStartMenu());
                    }, 1L); // waits a tick to make sure previous inventory is closed
                }
                player.closeInventory();
                e.setCancelled(true);
            }
            if (inventory.getTitle() != null && inventory.getTitle().equalsIgnoreCase(startTitle)) {
                Question question = questionAPI.getQuestions().get(slot);

                if (question != null) {
                    questionAPI.pushQuestion(question);
                    Utils.sendRaw(player, Utils.process("%prefix% &aYou started the question &e\"" + question.getQuestion() + "&e\""));
                    player.closeInventory();
                }
                e.setCancelled(true);
            }
        }
    }

    @Override
    public String getCommand() {
        return "admin";
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String[] args) {
        Player player = (Player) sender;

        player.openInventory(AdminInventories.getInitInventory());

        return true;
    }
}