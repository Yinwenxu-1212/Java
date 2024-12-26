import entity.Question1;
import entity.Question2;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import util.DatabaseConnector;
import util.QuestionUploader;

import java.awt.*;
import java.awt.event.*;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
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
import javax.swing.filechooser.FileNameExtensionFilter;

import static util.GetId.getSubjectId;
/**
 * @author 11219
 */
public class DirectInput extends JFrame {
    private List<String> questionTypes = new ArrayList<>();
    private JPanel panel1;
    private JLabel label1;
    private JButton button1;
    private JPanel panel2;
    private JLabel label2;
    private JButton button2;
    private JButton button3;
    private JScrollPane scrollPane1;
    private JTextArea textArea1;
    private static final Pattern answer = Pattern.compile("答案：([ABCDabcd],*)+");
    public DirectInput() {
        initComponents();
    }

    private void downloadWordTemplate(ActionEvent e) {
        // 指定源Word文档模板路径
        Path sourcePath = Path.of("D:\\project\\Examination\\example\\doc导入示例.doc");

        // 创建文件选择器
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("选择保存Word文档的位置");
        fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);

        // 显示文件选择器对话框
        int result = fileChooser.showSaveDialog(this);

        // 如果用户选择了目录并点击了“保存”
        if (result == JFileChooser.APPROVE_OPTION) {
            // 获取选择的目录
            Path destinationPath = fileChooser.getSelectedFile().toPath().resolve("output.doc");

            try {
                // 将Word文档复制到选择的位置
                Files.copy(sourcePath, destinationPath, StandardCopyOption.REPLACE_EXISTING);

                // 可选地，使用默认关联的应用程序打开文件
                Desktop.getDesktop().open(destinationPath.toFile());

            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
    }

    private void uploadFile(ActionEvent e) {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("选择上传的Word文档");
        fileChooser.setFileFilter(new FileNameExtensionFilter("Word文档 (*.docx)", "docx"));

        int result = fileChooser.showOpenDialog(this);

        if (result == JFileChooser.APPROVE_OPTION) {
            Path selectedFile = fileChooser.getSelectedFile().toPath();
            try (FileInputStream fis = new FileInputStream(selectedFile.toFile())) {
                XWPFDocument document = new XWPFDocument(fis);
                StringBuilder fileContent = new StringBuilder();

                // 迭代文档中的段落
                for (var paragraph : document.getParagraphs()) {
                    fileContent.append(paragraph.getText()).append("\n");
                }

                // 将内容显示在 textArea1 中
                textArea1.setText(fileContent.toString());
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
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
    private int insertQuestion(Connection connection, String content, String difficulty, String topic, int score, String type, String imagePath, String audioPath) throws SQLException {
        // 上传带有音频路径的问题
        int questionId = QuestionUploader.uploadQuestion(connection, content, difficulty, topic, score, type, imagePath, audioPath);
        return questionId;
    }
    private void convertType() {
        String allText = textArea1.getText();
        String[] questions = allText.split("(?<=\\n\\n)"); // 使用空行分隔题目

        StringBuilder resultBuilder = new StringBuilder();

        for (String question : questions) {
            String type = recognizeQuestionType(question);
            questionTypes.add(type);
            resultBuilder.append("【").append(type).append("】").append("\n");
            resultBuilder.append(question);
        }

        textArea1.setText(resultBuilder.toString());
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
            // 假设你已经有一个方法来获取subject_id
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

    private void addToDataBase(ActionEvent e) {
        convertType();
        String allText = textArea1.getText();
        String[] questions = allText.split("(?<=\\n\\n)"); // 使用空行分隔题目
        for (int i = 0; i < questions.length; i++) {
            if(questionTypes.get(i).equals("问答题")) {
                java.util.List<Question2> questions1 = Question2.parseQuestions(questions[i]);
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
                java.util.List<Question1> questions2 = Question1.parseQuestions(questions[i]);
                for (Question1 question : questions2) {
                    String type = question.getType();
                    int score = question.getScore();
                    String content = question.getContent();
                    String topic = question.getTopic();
                    java.util.List<String> answers = question.getAnswers();
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
    private void initComponents() {
        panel1 = new JPanel();
        label1 = new JLabel();
        button1 = new JButton();
        panel2 = new JPanel();
        label2 = new JLabel();
        button2 = new JButton();
        button3 = new JButton();
        scrollPane1 = new JScrollPane();
        textArea1 = new JTextArea();

        //======== this ========
        setTitle("\u76f4\u63a5\u5f55\u5165");
        var contentPane = getContentPane();

        //======== panel1 ========
        {
            panel1.setLayout(new FlowLayout(FlowLayout.LEADING));

            //---- label1 ----
            label1.setText("\u7b2c\u4e00\u6b65\uff1a\u4e0a\u4f20\u524d\u8bf7\u4e0b\u8f7d\u76f4\u63a5\u5f55\u5165\u7684\u6a21\u677f\uff0c\u6309\u7167\u6a21\u677f\u7684\u8981\u6c42\u5c06\u5185\u5bb9\u5f55\u5165\u5230\u6a21\u677f\u4e2d");
            panel1.add(label1);

            //---- button1 ----
            button1.setText("\u4e0b\u8f7dword\u6a21\u677f");
            button1.addActionListener(e -> downloadWordTemplate(e));
            panel1.add(button1);
        }

        //======== panel2 ========
        {
            panel2.setLayout(new FlowLayout(FlowLayout.LEFT));

            //---- label2 ----
            label2.setText("\u7b2c\u4e8c\u6b65\uff1a\u4e0a\u4f20\u6587\u4ef6");
            panel2.add(label2);

            //---- button2 ----
            button2.setText("\u4e0a\u4f20");
            button2.addActionListener(e -> uploadFile(e));
            panel2.add(button2);

            //---- button3 ----
            button3.setText("\u5bfc\u5165");
            button3.addActionListener(e -> addToDataBase(e));
            panel2.add(button3);
        }

        //======== scrollPane1 ========
        {
            scrollPane1.setViewportView(textArea1);
        }

        GroupLayout contentPaneLayout = new GroupLayout(contentPane);
        contentPane.setLayout(contentPaneLayout);
        contentPaneLayout.setHorizontalGroup(
                contentPaneLayout.createParallelGroup()
                        .addGroup(contentPaneLayout.createSequentialGroup()
                                .addContainerGap()
                                .addGroup(contentPaneLayout.createParallelGroup(GroupLayout.Alignment.TRAILING)
                                        .addGroup(GroupLayout.Alignment.LEADING, contentPaneLayout.createSequentialGroup()
                                                .addComponent(panel1, GroupLayout.PREFERRED_SIZE, 421, GroupLayout.PREFERRED_SIZE)
                                                .addGap(0, 0, Short.MAX_VALUE))
                                        .addComponent(panel2, GroupLayout.Alignment.LEADING, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                        .addComponent(scrollPane1))
                                .addContainerGap())
        );
        contentPaneLayout.setVerticalGroup(
                contentPaneLayout.createParallelGroup()
                        .addGroup(contentPaneLayout.createSequentialGroup()
                                .addContainerGap()
                                .addComponent(panel1, GroupLayout.PREFERRED_SIZE, 62, GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(panel2, GroupLayout.PREFERRED_SIZE, 40, GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(scrollPane1, GroupLayout.PREFERRED_SIZE, 166, GroupLayout.PREFERRED_SIZE)
                                .addContainerGap(GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        pack();
        setLocationRelativeTo(getOwner());
    }
}
