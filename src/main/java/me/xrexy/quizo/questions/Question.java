package me.xrexy.quizo.questions;

import java.util.List;

public class Question {
    private final String title;
    private final String question;
    private final String correctAnswer;
    private final List<String> wrongAnswers;

    public Question(String title, String question, String correctAnswer, List<String> wrongAnswers) {
        this.title = title;
        this.question = question;
        this.correctAnswer = correctAnswer;
        this.wrongAnswers = wrongAnswers;
    }

    public String getTitle() {
        return title;
    }

    public String getQuestion() {
        return question;
    }

    public String getCorrectAnswer() {
        return correctAnswer;
    }

    public List<String> getWrongAnswers() {
        return wrongAnswers;
    }

    @Override
    public String toString() {
        return "Question {" +
                "title='" + title + '\'' +
                ", question='" + question + '\'' +
                ", correctAnswer='" + correctAnswer + '\'' +
                ", wrongAnswers=" + wrongAnswers +
                '}';
    }
}
