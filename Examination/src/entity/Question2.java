package entity;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Question2 {
    private String type;
    private int score;
    private String content;
    private String difficulty;
    private String subject;
    private String topic;
    private String answer;
    private String imagePath;
    private String audioPath;

    public Question2(String type, String content, String difficulty, String subject, String topic, String answer, String imagePath, String audioPath, int score) {
        this.type = type;
        this.content = content;
        this.difficulty = difficulty;
        this.subject = subject;
        this.topic = topic;
        this.answer = answer;
        this.imagePath = imagePath;
        this.audioPath = audioPath;
        this.score = score;
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

    public String getAnswer() {
        return answer;
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

    public String getType(){
        return type;
    }
    public static List<Question2> parseQuestions(String text) {
        List<Question2> questions = new ArrayList<>();
        // 更新正则表达式，以提取分数
        Pattern pattern = Pattern.compile("\\【(.*?)】\\n(\\d+)\\.（(\\d+)分）(.*?)答案：(.*?)\\n学科：(.*?)\\n知识点：(.*?)\\n难度：(.*?)\\n图片路径：(.*?)\\n音频路径：(.*?)(?:\\n|$)", Pattern.DOTALL);

        Matcher matcher = pattern.matcher(text);

        int start = 0;
        while (matcher.find(start)) {
            // 提取分数字符串并转换为整数
            String type = matcher.group(1);
            String scoreStr = matcher.group(3);
            int score = Integer.parseInt(scoreStr);

            // 提取其他信息
            String content = matcher.group(4).trim();
            String answer = matcher.group(5).trim();
            String subject = matcher.group(6).trim();
            String topic = matcher.group(7).trim();
            String difficulty = matcher.group(8).trim();
            String imagePath = matcher.group(9).trim();
            String audioPath = matcher.group(10).trim();

            // 创建 Question2 对象，并将分数传递给构造函数
            Question2 question = new Question2(type, content, difficulty, subject, topic, answer, imagePath, audioPath, score);
            questions.add(question);

            start = matcher.end();
        }

        return questions;
    }
}
