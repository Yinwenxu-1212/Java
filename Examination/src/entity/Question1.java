package entity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Question1 {
    private String type;
    private int score;
    private String content;
    private String difficulty;
    private String subject;
    private String topic;
    private List<String> answers;
    private String imagePath;
    private String audioPath;
    private Map<String, String> options; // 选项的名称和值

    public Question1(String type, String content, String difficulty, String subject, String topic, List<String> answers, String imagePath, String audioPath, int score, Map<String, String> options) {
        this.type = type;
        this.content = content;
        this.difficulty = difficulty;
        this.subject = subject;
        this.topic = topic;
        this.answers = answers;
        this.imagePath = imagePath;
        this.audioPath = audioPath;
        this.score = score;
        this.options = options;
    }

    public String getContent() {
        return content;
    }

    public String getDifficulty() {
        return difficulty;
    }

    public String getSubject() {
        return subject;
    }

    public String getTopic() {
        return topic;
    }

    public List<String> getAnswers() {
        return answers;
    }

    public String getImagePath() {
        return imagePath;
    }

    public String getAudioPath() {
        return audioPath;
    }

    public int getScore() {
        return score;
    }

    public String getType() {
        return type;
    }
    public Map<String, String> getOptions() {
        return options;
    }
    public static List<Question1> parseQuestions(String text) {
        List<Question1> questions = new ArrayList<>();

        Pattern pattern = Pattern.compile("【(.*?)】\\n(\\d+\\.（(\\d+)分）)(.*?)\\n((?:[A-Z]\\. .*?\n)+)答案：(.*?)\\n学科：(.*?)\\n知识点：(.*?)\\n难度：(.*?)\\n图片路径：(.*?)\\n音频路径：(.*?)(?:\\n|$)", Pattern.DOTALL);
        Matcher matcher = pattern.matcher(text);
        int start = 0;
        while (matcher.find(start)) {
            String type = matcher.group(1);
            String scoreStr = matcher.group(3);
            int score = Integer.parseInt(scoreStr);
            String content = matcher.group(4).trim();
            String optionsString = matcher.group(5).trim();
            String answerString = matcher.group(6).trim();
            String[] answerArray = answerString.split(",");
            List<String> answers = new ArrayList<>();
            for (String answer : answerArray) {
                answers.add(answer.trim());
            }
            String subject = matcher.group(7).trim();
            String topic = matcher.group(8).trim();
            String difficulty = matcher.group(9).trim();
            String imagePath = matcher.group(10).trim();
            String audioPath = matcher.group(11).trim();

            // 使用内部匹配，匹配选项
            Pattern optionsPattern = Pattern.compile("([A-Z])\\. (.*?)(?:\\n|$)");
            Matcher optionsMatcher = optionsPattern.matcher(optionsString);

            // 存储选项的变量
            Map<String, String> options = new HashMap<>();

            while (optionsMatcher.find()) {
                String optionKey = optionsMatcher.group(1);
                String optionValue = optionsMatcher.group(2);
                options.put(optionKey, optionValue);
            }
            Question1 question = new Question1(type, content, difficulty, subject, topic, answers, imagePath, audioPath, score, options);
            questions.add(question);

            start = matcher.end();
        }
        return questions;
    }
}
