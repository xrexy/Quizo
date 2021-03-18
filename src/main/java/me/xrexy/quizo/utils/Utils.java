package me.xrexy.quizo.utils;

import me.xrexy.quizo.Quizo;
import me.xrexy.quizo.questions.Question;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;

public class Utils {
    private static class Placeholder {

        private final String placeholder, replacement;

        public Placeholder(String placeholder, String replacement) {
            this.placeholder = placeholder;
            this.replacement = replacement;
        }

        public String getPlaceholder() {
            return placeholder;
        }

        public String getReplacement() {
            return replacement;
        }

        public static String processPlaceholders(String message, Placeholder... placeholders) {
            for (Placeholder p : placeholders)
                message = message.replace(p.getPlaceholder(), p.getReplacement());

            return colorize(message);
        }

        public static ArrayList<String> processPlaceholders(List<String> messages, Placeholder... placeholders) {
            ArrayList<String> output = new ArrayList<>();
            for (String line : messages) {
                for (Placeholder p : placeholders)
                    line = line.replace(p.getPlaceholder(), p.getReplacement());

                output.add(colorize(line));
            }
            return output;
        }
    }

    private static final Quizo quizo = Quizo.getInstance();
    private static final FileConfiguration config = quizo.getConfig();

    public static void log(Level level, String message) {
        quizo.getLogger().log(level, colorize(message));
    }

    public static String colorize(String msg) {
        return ChatColor.translateAlternateColorCodes('&', msg);
    }

    public static void sendRaw(Player p, String msg) {
        p.sendMessage(colorize(msg));
    }

    public static void sendMessage(Player p, String path) {
        p.sendMessage(process(getString(path)));
    }

    public static String getString(String path) {
        return config.getString(path);
    }

    public static String process(String toProcess) {
        return colorize(toProcess.replace("%prefix%", getString("prefix")));
    }

    public static String process(String toProcess, Question question) {
        return colorize(Placeholder.processPlaceholders(toProcess,
                new Placeholder("%prefix%", config.getString("prefix")),
                new Placeholder("%question%", question.getQuestion()),
                new Placeholder("%answer%", question.getCorrectAnswer()),
                new Placeholder("%title%", question.getTitle()),
                new Placeholder("%time-limit%", config.getInt("messages.quiz.time-limit") + "")
        ));
    }

    public static String process(String toProcess, Question question, String answer, int id) {
        return colorize(Placeholder.processPlaceholders(toProcess,
                new Placeholder("%prefix%", config.getString("prefix")),
                new Placeholder("%question%", question.getQuestion()),
                new Placeholder("%answer%", question.getCorrectAnswer()),
                new Placeholder("%title%", question.getTitle()),
                new Placeholder("%time-limit%", config.getInt("messages.quiz.time-limit") + ""),
                new Placeholder("%answer%", answer),
                new Placeholder("%id%", id + "")

        ));
    }
    public static String process(String toProcess, Question question, String answer, int id, int guessedCount, int correctGuessesCount) {
        return colorize(Placeholder.processPlaceholders(toProcess,
                new Placeholder("%prefix%", config.getString("prefix")),
                new Placeholder("%question%", question.getQuestion()),
                new Placeholder("%answer%", question.getCorrectAnswer()),
                new Placeholder("%title%", question.getTitle()),
                new Placeholder("%time-limit%", config.getInt("messages.quiz.time-limit") + ""),
                new Placeholder("%answer%", answer),
                new Placeholder("%id%", id + ""),
                new Placeholder("%total-answers%", guessedCount + ""),
                new Placeholder("%correct-answers%", correctGuessesCount + "")

        ));
    }

    public static String processAnswer(String toProcess, String answer, int id) {
        return colorize(Placeholder.processPlaceholders(toProcess,
                new Placeholder("%prefix%", config.getString("prefix")),
                new Placeholder("%answer%", answer),
                new Placeholder("%id%", id + ""),
                new Placeholder("%time-limit%", config.getInt("messages.quit.time-limit") + "")
        ));
    }

    public static void sendMultilineMessage(Player p, String path) {
        StringBuilder message = new StringBuilder();
        for (String s : config.getStringList(path))
            message.append(colorize(process(s)));
        p.sendMessage(message.toString());
    }
}
