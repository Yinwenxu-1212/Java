package util;

import org.apache.batik.swing.JSVGCanvas;
import org.apache.batik.swing.svg.SVGUserAgentAdapter;

import javax.swing.*;
import javax.swing.text.BadLocationException;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;
import java.awt.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class QuestionDetails extends JFrame {
    private JTextPane textPane;
    private String subject;
    private String topic;
    private int questionId;
    private String content;
    private String optionA;
    private String optionB;
    private String optionC;
    private String optionD;
    private String answer;
    private String difficulty;
    private int score;
    private String imagePath;
    private String audioPath;
    private String questionType;
    private JButton buttonPlayAudio;
    private JButton buttonStop;

    public QuestionDetails(int questionId, String subject, String type) {
        this.questionId = questionId;
        this.subject = subject;
        this.questionType = type;
        getDetail();
        initComponents();
        insertContent();

        if ("听力题".equals(questionType)) {
            buttonPlayAudio.setVisible(true);
            buttonPlayAudio.addActionListener(e -> playAudio());
        } else {
            buttonPlayAudio.setVisible(false);
            buttonStop.setVisible(false);
        }
    }
    private void playAudio() {
        if (audioPath != null && !audioPath.isEmpty()) {
            if (audioPath.toLowerCase().endsWith(".wav")) {
                // 如果是 WAV 文件，使用原来的方式播放
                AudioPlayer.playAudio(audioPath);
            } else if (audioPath.toLowerCase().endsWith(".mp3")) {
                // 如果是 MP3 文件，使用 MP3Player 播放
                MP3Player.playMP3(audioPath);
            } else {
                JOptionPane.showMessageDialog(this, "不支持的音频文件格式");
            }
        } else {
            JOptionPane.showMessageDialog(this, "该题目没有关联的音频文件");
        }
    }
    private void stopAudio() {
        if (audioPath.toLowerCase().endsWith(".wav")) {
            AudioPlayer.stopAudio();
        } else if (audioPath.toLowerCase().endsWith(".mp3")) {
            MP3Player.stopMP3();
        }
    }

    private void getDetail() {
        try (Connection connection = DatabaseConnector.connect()) {
            if (questionType.equals("单项选择题") || questionType.equals("听力题")||questionType.equals("多项选择题")) {
                String query = "SELECT content, difficulty, score, audio_path, " +
                        "image_path,topics.name FROM questions JOIN topics ON " +
                        "topics.id = questions.topic_id  WHERE questions.id = ?";
                try (PreparedStatement statement = connection.prepareStatement(query)) {
                    statement.setInt(1, questionId);
                    ResultSet resultSet = statement.executeQuery();
                    if (resultSet.next()) {
                        content = resultSet.getString("content");
                        difficulty = resultSet.getString("difficulty");
                        score = resultSet.getInt("score");
                        audioPath = resultSet.getString("audio_path");
                        imagePath = resultSet.getString("image_path");
                        topic = resultSet.getString("name");
                    }
                }
            }
            else if(questionType.equals("问答题")){
                String query = "SELECT content, difficulty, score, audio_path, image_path, " +
                        "answers.correct_answer_text, topics.name FROM questions JOIN topics ON " +
                        "topics.id = questions.topic_id  JOIN answers ON questions.id = answers.question_id  " +
                        "WHERE questions.id = ?";
                try (PreparedStatement statement = connection.prepareStatement(query)) {
                    statement.setInt(1, questionId);
                    ResultSet resultSet = statement.executeQuery();
                    if (resultSet.next()) {
                        content = resultSet.getString("content");
                        difficulty = resultSet.getString("difficulty");
                        score = resultSet.getInt("score");
                        audioPath = resultSet.getString("audio_path");
                        imagePath = resultSet.getString("image_path");
                        answer = resultSet.getString("correct_answer_text");
                        topic = resultSet.getString("name");
                    }
                    System.out.println(answer);
                }
            }
            if (questionType.equals("单项选择题")||questionType.equals("听力题")||questionType.equals("多项选择题")) {
                String query = "SELECT option_name, option_text, is_correct FROM options WHERE question_id = ?";
                try (PreparedStatement statement = connection.prepareStatement(query)) {
                    statement.setInt(1, questionId);
                    ResultSet resultSet = statement.executeQuery();
                    int i = 1;
                    answer = "";
                    while (resultSet.next()) {
                        i++;
                        String optionName = resultSet.getString("option_name");
                        String optionText = resultSet.getString("option_text");
                        int isCorrect = resultSet.getInt("is_correct");

                        // 根据选项名设置对应的属性值
                        switch (optionName) {
                            case "A":
                                optionA = optionText;
                                break;
                            case "B":
                                optionB = optionText;
                                break;
                            case "C":
                                optionC = optionText;
                                break;
                            case "D":
                                optionD = optionText;
                                break;
                        }

                        if (isCorrect == 1) {
                            answer += optionName;
                        }
                    }
                    System.out.println(answer);
                }
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "数据库连接错误：" + ex.getMessage());
        }
    }
    private void insertContent() {
        try {
            // 获取文档
            StyledDocument doc = textPane.getStyledDocument();
            SimpleAttributeSet style = new SimpleAttributeSet();
            StyleConstants.setFontSize(style, 14);
            StyleConstants.setBold(style, false);

            // 插入题干
            doc.insertString(doc.getLength(), "(" + difficulty + ")" + content + "(" + score + "分)" + "\n", style);

            // 插入选项（如果是选择题）
            if ("单项选择题".equals(questionType) || "多项选择题".equals(questionType) || "听力题".equals(questionType)) {
                doc.insertString(doc.getLength(), "A. " + optionA + "\n", style);
                doc.insertString(doc.getLength(), "B. " + optionB + "\n", style);
                doc.insertString(doc.getLength(), "C. " + optionC + "\n", style);
                doc.insertString(doc.getLength(), "D. " + optionD + "\n", style);
            }
            // 插入图片
            insertImage(doc);
            doc.insertString(doc.getLength(), "\n", null);
            // 插入答案
            int documentLength = doc.getLength(); // 获取插入图片后的文档长度
            doc.insertString(documentLength, "答案: ", style);
            insertAnswer(doc, style);
        } catch (BadLocationException e) {
            e.printStackTrace();
        }
    }

    private void insertAnswer(StyledDocument doc, SimpleAttributeSet style) throws BadLocationException {
        // 处理多选题的多个答案
        if (questionType.equals("多项选择题")) {
            for (int i = 0; i < answer.length(); i++) {
                doc.insertString(doc.getLength(), answer.charAt(i) + " ", style);
            }
        } else {
            // 使用样式设置答案
            doc.insertString(doc.getLength(), " " + answer, style);
        }
    }


    // 用于插入图片
    private void insertImage(StyledDocument doc) throws BadLocationException {
        if (imagePath != null && !imagePath.isEmpty() && !imagePath.equals("null")) {
            // 获取当前段落的结束位置
            int endPosition = doc.getLength();

            // 判断是否为SVG文件
            if (imagePath.toLowerCase().endsWith(".svg")) {
                // 创建一个空行
                doc.insertString(endPosition, "\n", null);

                // 创建 SVGCanvas 用于显示 SVG 图像
                JSVGCanvas svgCanvas = new JSVGCanvas(new SVGUserAgentAdapter(), true, true);
                svgCanvas.setURI("file:///" + imagePath);

                // 将图像插入到当前段落的结束位置之前
                textPane.insertComponent(svgCanvas);
            } else {
                // 加载原始图像
                ImageIcon originalIcon = new ImageIcon(imagePath);
                Image originalImage = originalIcon.getImage();

                // 计算新的宽度和高度
                int newWidth = 2 * getWidth() / 3;
                int newHeight = (int) (newWidth * ((double) originalImage.getHeight(null) / originalImage.getWidth(null)));

                // 调整图像大小
                Image resizedImage = originalImage.getScaledInstance(newWidth, newHeight, Image.SCALE_SMOOTH);

                // 创建一个空行
                doc.insertString(endPosition, "\n", null);

                // 将图像插入到当前段落的结束位置之前
                textPane.insertIcon(new ImageIcon(resizedImage));
            }

            doc.insertString(endPosition, "\n", null);
        }
    }


    private void initComponents() {
        textPane = new JTextPane();
        textPane.setEditable(false);  // 设置为只读模式
        JScrollPane scrollPane = new JScrollPane(textPane);

        // 设置 JTextPane 样式
        StyledDocument doc = textPane.getStyledDocument();
        SimpleAttributeSet style = new SimpleAttributeSet();
        StyleConstants.setFontSize(style, 14);
        StyleConstants.setBold(style, false);

        // 将 JTextPane 放入滚动窗口
        scrollPane.setPreferredSize(new Dimension(600, 400));
        add(scrollPane);

        buttonPlayAudio = new JButton("播放音频");
        buttonStop = new JButton("停止播放");

        buttonStop.addActionListener(e -> stopAudio());
        setTitle("题目详情");
        GroupLayout layout = new GroupLayout(getContentPane());
        getContentPane().setLayout(layout);

        layout.setAutoCreateGaps(true);
        layout.setAutoCreateContainerGaps(true);

        layout.setHorizontalGroup(
                layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                        .addComponent(scrollPane)
                        .addGroup(layout.createSequentialGroup()
                                .addComponent(buttonPlayAudio)
                                .addComponent(buttonStop))
        );

        layout.setVerticalGroup(
                layout.createSequentialGroup()
                        .addComponent(scrollPane)
                        .addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                                .addComponent(buttonPlayAudio)
                                .addComponent(buttonStop))
        );


        pack();
        setLocationRelativeTo(null);
    }
}