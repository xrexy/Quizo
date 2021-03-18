package me.xrexy.quizo.questions;

import me.xrexy.quizo.Quizo;
import me.xrexy.quizo.files.FileAPI;
import me.xrexy.quizo.utils.FileUtils;
import me.xrexy.quizo.utils.Utils;
import net.md_5.bungee.chat.ComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;

public class QuestionAPI {
    private ArrayList<Question> questions = new ArrayList<>();
    private final Quizo quizo = Quizo.getInstance();
    private final FileAPI fileAPI = quizo.getFileAPI();
    private final HashMap<Integer, String> currentAnswers = new HashMap<>();
    private final HashMap<Integer, String> timestamps = new HashMap<>();
    private final FileConfiguration config = quizo.getConfig();

    public boolean isRunning, timestampsEnabled = false;
    public int taskID, correctAnswerID, correctAnswersCount;
    public final ArrayList<UUID> guessed = new ArrayList<>();

    public void load() {
        questions = FileUtils.getQuestions(fileAPI.getFile("questions.yml"));

        try {
            ConfigurationSection timestampsSection = config.getConfigurationSection("messages.quiz.timestamps");
            if (timestampsSection == null) {
                Utils.log(Level.WARNING, "Section \"messages.quiz.timestamps\" is either missing or empty, disabling timestamps feature!");
                timestampsEnabled = false;
                return;
            }

            for (String key : timestampsSection.getKeys(false)) {
                timestamps.put(Integer.parseInt(key), timestampsSection.getString(key));
            }
            timestampsEnabled = true;
        } catch (Exception e) {
            if (config.getBoolean("debug")) e.printStackTrace();
            timestampsEnabled = false;
            Utils.log(Level.WARNING, "Something went wrong while enabling timestamps, disabling for now!");
        }
    }

    private Question getRandom() {
        int random = ThreadLocalRandom.current().nextInt(questions.size());
        return questions.get(random);
    }

    public void pushQuestion() {
        pushQuestion(getRandom());
    }

    public void pushQuestion(Question question) {
        if (isRunning) {
            // TODO SEND "messages.quiz.already-running" to player
            return;
        }

        currentAnswers.clear();
        correctAnswersCount = 0;

        StringBuilder message = new StringBuilder("[\"\",");
        isRunning = true;

        List<String> quizStart = config.getStringList("messages.quiz.format");

        for (String s : quizStart) {
            if (s.trim().length() == 0) {
                s = "\n";
            }
//            if (quizStartCount++ == quizStart.size() - 1) { // last element
//                message.append("{ \"text\": \"" + Utils.process(s, question) + "\"}]");
//                continue;
//            }
            message.append("{ \"text\": \"" + Utils.process(s, question) + "\"},");
        }

        List<String> wrongAnswers = new ArrayList<>(question.getWrongAnswers());

        String answer;
        int random;
        int answersCount = config.getInt("answers-count");
        if (answersCount > question.getWrongAnswers().size()) {
            answersCount = question.getWrongAnswers().size();
        }
        int finalAnswersCount = answersCount;

        for (int i = 1; i <= finalAnswersCount; i++) {
            random = ThreadLocalRandom.current().nextInt(wrongAnswers.size());
            answer = wrongAnswers.get(random);
            wrongAnswers.remove(random);

            currentAnswers.put(i, answer);
        }

        // old value is replaced
        correctAnswerID = 1 + ThreadLocalRandom.current().nextInt(currentAnswers.size() - 1);
        currentAnswers.put(correctAnswerID, question.getCorrectAnswer());

        final String[] hoverText = new String[finalAnswersCount + 1];
        hoverText[0] = config.getString("messages.quiz.answer-hover");
        // {"text":"1","clickEvent":{"action":"run_command","value":"/quizo suggest id"},"hoverEvent":{"action":"show_text","value":[{"text":"hover text"}]}}
        currentAnswers.forEach((id, finalAnswer) -> {
            finalAnswer = Utils.processAnswer(Utils.getString("messages.quiz.answer-format"), finalAnswer, id);

            hoverText[id] = Utils.process(hoverText[0], question, finalAnswer, id);

            if (id == finalAnswersCount) { // last element
                message.append("{\"text\":\"" + finalAnswer + "\",\"clickEvent\":{\"action\":\"run_command\",\"value\":\"/quizo guess " + id + "\"},\"hoverEvent\":{\"action\":\"show_text\",\"value\":[{\"text\":\"" + hoverText[id] + "\"}]}}]");
            } else {
                if (config.getBoolean("messages.quiz.answer-line-break")) {
                    finalAnswer += "\n";
                }
                message.append("{\"text\":\"" + (finalAnswer + "\n") + "\",\"clickEvent\":{\"action\":\"run_command\",\"value\":\"/quizo guess " + id + "\"},\"hoverEvent\":{\"action\":\"show_text\",\"value\":[{\"text\":\"" + hoverText[id] + "\"}]}},");
            }
        });

        // parses json to BaseComponent[] also replaces '&' with '§' so chatcolor can work
        Bukkit.spigot().broadcast(ComponentSerializer.parse(message.toString().replace("&", "§")));

        // starts timer till game ends
        if (timestampsEnabled) {
            AtomicInteger time = new AtomicInteger(config.getInt("messages.quiz.time-limit"));
            taskID = Bukkit.getScheduler().scheduleSyncRepeatingTask(quizo, () -> {
                if (timestamps.containsKey(time.get())) {
                    Bukkit.broadcastMessage(Utils.process(timestamps.get(time.get())));
                }
                time.getAndDecrement();
                if (time.get() <= 0) {
                    endQuestion(question, question.getCorrectAnswer(), correctAnswerID);
                }
            }, 0L, 20L);
        } else { // ends right when timer ends, no timestamps
            taskID = Bukkit.getScheduler().scheduleSyncDelayedTask(quizo, () -> {
                endQuestion(question, question.getCorrectAnswer(), correctAnswerID);
            }, config.getInt("messages.quiz.time-limit") * 20L);
        }
    }

    private void startTimerUntilNextQuestion() {
        taskID = Bukkit.getScheduler().runTaskLater(quizo, this::pushQuestion, config.getInt("delay-between-questions") * 20L).getTaskId();
    }

    public void onGuess(Player player, int guessedID) {
        if (guessed.contains(player.getUniqueId())) {
            Utils.sendMessage(player, "messages.guess.already-guessed");
            return;
        }
        guessed.add(player.getUniqueId());

        if (guessedID == correctAnswerID) {
            correctAnswersCount++;
            Utils.sendMessage(player, "messages.guess.correct");
            List<String> rewards = config.getStringList("messages.quiz.rewards");
            rewards.forEach(reward -> {
                reward = reward.replace("/", "").replace("%player%", player.getName());
                try {
                    Bukkit.dispatchCommand(Bukkit.getConsoleSender(), reward);
                } catch (Exception e) {
                    if (config.getBoolean("debug")) {
                        e.printStackTrace();
                    }
                }
            });

            if (config.getBoolean("messages.guess.correct-global-toggle")) {
                Bukkit.getOnlinePlayers().forEach(onlinePlayer -> {
                    Utils.sendRaw(onlinePlayer, Utils.process(Utils.getString("messages.guess.correct-global").replace("%player%", player.getName())));
                });
            }
        } else {
            Utils.sendMessage(player, "messages.guess.incorrect-answer");
        }
    }

    public void cancelCurrentQuestion() {
        cancelCurrentTask();
        isRunning = false;
        guessed.clear();
    }

    private void cancelCurrentTask() {
        Bukkit.getScheduler().cancelTask(taskID);
    }

    private void endQuestion(Question question, String answer, int answerID) {
        Bukkit.broadcastMessage(Utils.process(Utils.getString("messages.quiz.end-message"), question, answer, answerID, guessed.size(), correctAnswersCount));
        isRunning = false;
        cancelCurrentTask();
        guessed.clear();
        correctAnswersCount = 0;

        startTimerUntilNextQuestion();
    }

    public ArrayList<Question> getQuestions() {
        return questions;
    }
}