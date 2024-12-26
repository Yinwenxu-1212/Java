import entity.Question2;
import entity.Question1;
import util.DatabaseConnector;
import util.Instruction;
import util.QuestionUploader;

import java.awt.event.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.swing.*;
import javax.swing.GroupLayout;

import static util.GetId.getSubjectId;

/**
 * @author 11219
 */
public class TemplateInput extends JFrame {
    private List<String> questionTypes = new ArrayList<>();
    private JScrollPane scrollPane1;
    private JTextArea textArea1;
    private JScrollPane scrollPane2;
    private JTextArea textArea2;
    private JPanel panel1;
    private JLabel label1;
    private JButton button1;
    private JButton button2;
    private JPanel panel2;
    private JLabel label2;
    private JButton button3;
    private static final Pattern  answer = Pattern.compile("答案：([ABCDabcd],*)+");
    public TemplateInput() {
        initComponents();
    }

    public static String recognizeQuestionType(String question) {
        question = question.trim();
        if (question.endsWith("音频路径：null")){
            Matcher matcher = answer.matcher(question);
            if (matcher.find()) {
                String group = matcher.group();
                String[] split = group.split(",");
                if (split.length == 1) {
                    return "单项选择题";
                }
                return "多项选择题";
            }
            return "问答题";
        }else {
            return "听力题";
        }
    }

    private void instruction(ActionEvent e) {
        Instruction instruction = new Instruction();
        instruction.setVisible(true);
    }

    private void checkbutton(ActionEvent e) {
        String allText = textArea1.getText();
        String[] questions = allText.split("(?<=\\n\\n)"); // 使用空行分隔题目

        StringBuilder resultBuilder = new StringBuilder();

        for (String question : questions) {
            String type = recognizeQuestionType(question);
            questionTypes.add(type);
            resultBuilder.append("【").append(type).append("】").append("\n");
            resultBuilder.append(question);
        }

        textArea2.setText(resultBuilder.toString());
    }

    private int insertQuestion(Connection connection, String content, String difficulty, String topic, int score, String type, String imagePath, String audioPath) throws SQLException {
        // 上传带有音频路径的问题
        int questionId = QuestionUploader.uploadQuestion(connection, content, difficulty, topic, score, type, imagePath, audioPath);
        return questionId;
    }
    private void addToDatabase(ActionEvent e) {
        String allText = textArea2.getText();
        String[] questions = allText.split("(?<=\\n\\n)"); // 使用空行分隔题目
        for (int i = 0; i < questions.length; i++) {
            if(questionTypes.get(i).equals("问答题")) {
                List<Question2> questions1 = Question2.parseQuestions(questions[i]);
                for (Question2 question : questions1) {
                    String type = question.getType();
                    int score = question.getScore();
                    String content = question.getContent();
                    String topic = question.getTopic();
                    String answer = question.getAnswer();
                    String imagePath = question.getImagePath();
                    String audioPath = question.getAudioPath();
                    String subject = question.getSubject();
                    String difficulty = question.getDifficulty();
                    try (Connection connection = DatabaseConnector.connect()) {
                        if(!doesSubjectExist(connection, subject)){
                            QuestionUploader.uploadSubject(connection, subject);
                        }
                        if(!doesTopicExist(connection, subject, topic)){
                            QuestionUploader.uploadTopic(connection, subject, topic);
                        }
                        int questionId = insertQuestion(connection, content,difficulty, topic, score, type, imagePath, audioPath);
                        QuestionUploader.saveAnswer(connection, questionId, answer);
                        JOptionPane.showMessageDialog(this, "导入成功！");
                    }catch (SQLException ex) {
                        ex.printStackTrace();
                        JOptionPane.showMessageDialog(this, "数据库连接错误：" + ex.getMessage());
                    }
                }
            }
            else{
                List<Question1> questions2 = Question1.parseQuestions(questions[i]);
                for (Question1 question : questions2) {
                    String type = question.getType();
                    int score = question.getScore();
                    String content = question.getContent();
                    String topic = question.getTopic();
                    List<String> answers = question.getAnswers();
                    String imagePath = question.getImagePath();
                    String audioPath = question.getAudioPath();
                    String subject = question.getSubject();
                    String difficulty = question.getDifficulty();
                    Map<String, String> options = question.getOptions();
                    try (Connection connection = DatabaseConnector.connect()) {
                        if(!doesSubjectExist(connection, subject)){
                            QuestionUploader.uploadSubject(connection, subject);
                        }
                        if(!doesTopicExist(connection, subject, topic)){
                            QuestionUploader.uploadTopic(connection, subject, topic);
                        }
                        int questionId = insertQuestion(connection, content,difficulty, topic, score, type, imagePath, audioPath);
                        QuestionUploader.uploadOptions(connection, questionId, options.get("A"), options.get("B"), options.get("C"), options.get("D"),
                                isCorrectOption("A", answers),
                                isCorrectOption("B", answers),
                                isCorrectOption("C", answers),
                                isCorrectOption("D", answers));
                        JOptionPane.showMessageDialog(this, "导入成功！");
                    }catch (SQLException ex) {
                        ex.printStackTrace();
                        JOptionPane.showMessageDialog(this, "数据库连接错误：" + ex.getMessage());
                    }
                }
            }
        }
    }

    private boolean isCorrectOption(String A, List<String> B) {
        for (int i = 0; i < B.size(); i++) {
            if (A.equals(B.get(i))) {
                return true;
            }
        }
        return false;
    }

    private boolean doesTopicExist(Connection connection, String subject, String topicName) throws SQLException {
        String query = "SELECT COUNT(*) FROM topics WHERE subject_id = ? AND name = ?";
        try (PreparedStatement preparedStatement = connection.prepareStatement(query)) {
            int subjectId = getSubjectId(subject);

            preparedStatement.setInt(1, subjectId);
            preparedStatement.setString(2, topicName);

            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                resultSet.next();
                int count = resultSet.getInt(1);
                return count > 0;
            }
        }
    }

    private boolean doesSubjectExist(Connection connection, String subject) throws SQLException {
        String query = "SELECT COUNT(*) FROM subjects WHERE subject = ?";
        try (PreparedStatement preparedStatement = connection.prepareStatement(query)) {
            preparedStatement.setString(1, subject);
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                resultSet.next();
                int count = resultSet.getInt(1);
                return count > 0;
            }
        }
    }
    private void initComponents() {
        scrollPane1 = new JScrollPane();
        textArea1 = new JTextArea();
        scrollPane2 = new JScrollPane();
        textArea2 = new JTextArea();
        panel1 = new JPanel();
        label1 = new JLabel();
        button1 = new JButton();
        button2 = new JButton();
        panel2 = new JPanel();
        label2 = new JLabel();
        button3 = new JButton();

        //======== this ========
        setTitle("\u6a21\u677f\u5f55\u5165");
        var contentPane = getContentPane();

        //======== scrollPane1 ========
        {
            scrollPane1.setViewportView(textArea1);
        }

        //======== scrollPane2 ========
        {
            scrollPane2.setViewportView(textArea2);
        }

        //======== panel1 ========
        {

            //---- label1 ----
            label1.setText("1.\u5f55\u9898");
            label1.setFont(label1.getFont().deriveFont(label1.getFont().getSize() + 5f));

            //---- button1 ----
            button1.setText("\u4f7f\u7528\u8bf4\u660e");
            button1.addActionListener(e -> instruction(e));

            //---- button2 ----
            button2.setText("\u5f55\u5165");
            button2.addActionListener(e -> checkbutton(e));

            GroupLayout panel1Layout = new GroupLayout(panel1);
            panel1.setLayout(panel1Layout);
            panel1Layout.setHorizontalGroup(
                    panel1Layout.createParallelGroup()
                            .addGroup(panel1Layout.createSequentialGroup()
                                    .addContainerGap()
                                    .addComponent(label1)
                                    .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                    .addComponent(button2)
                                    .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                                    .addComponent(button1)
                                    .addContainerGap())
            );
            panel1Layout.setVerticalGroup(
                    panel1Layout.createParallelGroup()
                            .addGroup(panel1Layout.createSequentialGroup()
                                    .addContainerGap()
                                    .addComponent(label1)
                                    .addContainerGap(GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                            .addGroup(panel1Layout.createSequentialGroup()
                                    .addGroup(panel1Layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                                            .addComponent(button2)
                                            .addComponent(button1))
                                    .addGap(0, 0, Short.MAX_VALUE))
            );
        }

        //======== panel2 ========
        {

            //---- label2 ----
            label2.setText("2.\u68c0\u67e5\u5e76\u5bfc\u5165");
            label2.setFont(label2.getFont().deriveFont(label2.getFont().getSize() + 5f));

            //---- button3 ----
            button3.setText("\u5bfc\u5165");
            button3.addActionListener(e -> addToDatabase(e));

            GroupLayout panel2Layout = new GroupLayout(panel2);
            panel2.setLayout(panel2Layout);
            panel2Layout.setHorizontalGroup(
                    panel2Layout.createParallelGroup()
                            .addGroup(panel2Layout.createSequentialGroup()
                                    .addContainerGap()
                                    .addComponent(label2)
                                    .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED, 263, Short.MAX_VALUE)
                                    .addComponent(button3)
                                    .addContainerGap())
            );
            panel2Layout.setVerticalGroup(
                    panel2Layout.createParallelGroup()
                            .addGroup(GroupLayout.Alignment.TRAILING, panel2Layout.createSequentialGroup()
                                    .addContainerGap(GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                    .addComponent(label2)
                                    .addContainerGap())
                            .addGroup(panel2Layout.createSequentialGroup()
                                    .addComponent(button3)
                                    .addGap(0, 0, Short.MAX_VALUE))
            );
        }

        GroupLayout contentPaneLayout = new GroupLayout(contentPane);
        contentPane.setLayout(contentPaneLayout);
        contentPaneLayout.setHorizontalGroup(
                contentPaneLayout.createParallelGroup()
                        .addGroup(contentPaneLayout.createSequentialGroup()
                                .addContainerGap()
                                .addGroup(contentPaneLayout.createParallelGroup()
                                        .addComponent(scrollPane1, GroupLayout.PREFERRED_SIZE, 452, GroupLayout.PREFERRED_SIZE)
                                        .addComponent(panel1, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                                .addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED)
                                .addGroup(contentPaneLayout.createParallelGroup(GroupLayout.Alignment.LEADING, false)
                                        .addComponent(scrollPane2, GroupLayout.PREFERRED_SIZE, 452, GroupLayout.PREFERRED_SIZE)
                                        .addComponent(panel2, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
                                .addGap(57, 57, 57))
        );
        contentPaneLayout.setVerticalGroup(
                contentPaneLayout.createParallelGroup()
                        .addGroup(contentPaneLayout.createSequentialGroup()
                                .addContainerGap()
                                .addGroup(contentPaneLayout.createParallelGroup()
                                        .addComponent(panel2, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                                        .addComponent(panel1, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
                                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(contentPaneLayout.createParallelGroup(GroupLayout.Alignment.LEADING, false)
                                        .addComponent(scrollPane1, GroupLayout.PREFERRED_SIZE, 381, GroupLayout.PREFERRED_SIZE)
                                        .addComponent(scrollPane2, GroupLayout.PREFERRED_SIZE, 381, GroupLayout.PREFERRED_SIZE))
                                .addContainerGap(GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        pack();
        setLocationRelativeTo(getOwner());
    }

}
