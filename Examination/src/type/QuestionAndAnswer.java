package type;

import util.*;

import java.awt.event.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import javax.swing.*;
import javax.swing.GroupLayout;

/**
 * @author 11219
 */
public class QuestionAndAnswer extends JFrame {
    private int preId;
    private String subject;
    private String type;
    private JPanel panel1;
    private JTextField textField1;
    private JLabel label1;
    private JLabel label2;
    private JComboBox<String> comboBox1;
    private JLabel label3;
    private JComboBox comboBox2;
    private JLabel label4;
    private JTextField textField2;
    private JLabel label5;
    private JTextField textField3;
    private JButton button1;
    private JButton button2;
    public QuestionAndAnswer(String subject, String type, int preId) {
        this.subject = subject;
        this.type = type;
        this.preId = preId;
        initComponents();
        presentTopic();
    }

    private void presentTopic(){
        try (Connection connection = DatabaseConnector.connect()) {
            String query = "SELECT name FROM topics WHERE subject_id = ? ";
            try (PreparedStatement statement = connection.prepareStatement(query)) {
                statement.setInt(1, GetId.getSubjectId(subject));
                ResultSet resultSet = statement.executeQuery();
                comboBox2.removeAllItems();
                while (resultSet.next()) {
                    comboBox2.addItem(resultSet.getString("name"));
                }
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "数据库连接错误：" + ex.getMessage());
        }
    }

    // 插入问题和答案
    private void insertQuestion(Connection connection, String content, String difficulty, String topic, int score, String type, String imagePath, String audioPath) throws SQLException {
        int questionId = QuestionUploader.uploadQuestion(connection, content, difficulty, topic, score, type, imagePath, audioPath);

        QuestionUploader.saveAnswer(connection, questionId, textField3.getText());
    }

    // 更新问题和答案
    private void updateQuestion(Connection connection, int questionId, String content, String difficulty, String topic, int score, String type, String imagePath, String audioPath) throws SQLException {
        String updateQuestionQuery = "UPDATE questions SET content=?, difficulty=?, score=?, topic_id=?, image_path=?, audio_path=? WHERE id=?";
        try (PreparedStatement updateQuestionStatement = connection.prepareStatement(updateQuestionQuery)) {
            updateQuestionStatement.setString(1, content);
            updateQuestionStatement.setString(2, difficulty);
            updateQuestionStatement.setInt(3, score);
            updateQuestionStatement.setInt(4, GetId.getTopicId(topic));
            updateQuestionStatement.setString(5, imagePath);
            updateQuestionStatement.setString(6, audioPath);
            updateQuestionStatement.setInt(7, questionId);
            int rowsAffected = updateQuestionStatement.executeUpdate();
            if (rowsAffected > 0) {
                // 更新答案
                QuestionUploader.updateAnswer(connection, questionId, textField3.getText());
                JOptionPane.showMessageDialog(this, "问题和答案更新成功");
            } else {
                JOptionPane.showMessageDialog(this, "问题和答案更新失败");
            }
        }
    }
    private void uploadQuestionAndAnswers(ActionEvent e) {
        String content = textField2.getText();
        String difficulty = (String) comboBox1.getSelectedItem();
        String topic = (String) comboBox2.getSelectedItem();
        int score = Integer.parseInt(textField1.getText());

        // 使用 ImageUploader 类获取图像路径
        String imagePath = ImageUploader.uploadImage(this);

        // 使用 AudioUploader 类获取音频路径
        String audioPath = null;

        try (Connection connection = DatabaseConnector.connect()) {
            int existingQuestionId = preId;
            if (existingQuestionId != -1) {
                // 问题已存在，执行更新操作
                updateQuestion(connection, existingQuestionId, content, difficulty, topic, score, type, imagePath, audioPath);
            } else {
                // 问题不存在，执行插入操作
                insertQuestion(connection, content, difficulty, topic, score, type, imagePath, audioPath);
                JOptionPane.showMessageDialog(this, "问题和答案添加成功！");
            }
            this.dispose();
        } catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "数据库连接错误：" + ex.getMessage());
        }
    }

    private void initComponents() {

        panel1 = new JPanel();
        textField1 = new JTextField();
        label1 = new JLabel();
        label2 = new JLabel();
        comboBox1 = new JComboBox<>();
        label3 = new JLabel();
        comboBox2 = new JComboBox();
        label4 = new JLabel();
        textField2 = new JTextField();
        label5 = new JLabel();
        textField3 = new JTextField();
        button1 = new JButton();
        button2 = new JButton();

        //======== this ========
        setTitle("问答题");
        var contentPane = getContentPane();

        //======== panel1 ========
        {

            //---- label1 ----
            label1.setText("\u5206\u503c\uff1a");

            //---- label2 ----
            label2.setText("\u96be\u5ea6\uff1a");

            //---- comboBox1 ----
            comboBox1.setModel(new DefaultComboBoxModel<>(new String[] {
                    "\u6613",
                    "\u4e2d",
                    "\u96be"
            }));

            //---- label3 ----
            label3.setText("\u77e5\u8bc6\u70b9\uff1a");

            //---- label4 ----
            label4.setText("\u9898\u5e72\uff1a");

            //---- label5 ----
            label5.setText("\u7b54\u6848\uff1a");

            GroupLayout panel1Layout = new GroupLayout(panel1);
            panel1.setLayout(panel1Layout);
            panel1Layout.setHorizontalGroup(
                    panel1Layout.createParallelGroup()
                            .addGroup(panel1Layout.createSequentialGroup()
                                    .addGap(44, 44, 44)
                                    .addGroup(panel1Layout.createParallelGroup()
                                            .addGroup(panel1Layout.createSequentialGroup()
                                                    .addComponent(label5)
                                                    .addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED)
                                                    .addComponent(textField3, GroupLayout.DEFAULT_SIZE, 458, Short.MAX_VALUE))
                                            .addGroup(panel1Layout.createSequentialGroup()
                                                    .addComponent(label4)
                                                    .addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED)
                                                    .addComponent(textField2, GroupLayout.DEFAULT_SIZE, 458, Short.MAX_VALUE))
                                            .addGroup(panel1Layout.createSequentialGroup()
                                                    .addComponent(label1)
                                                    .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                                                    .addComponent(textField1, GroupLayout.PREFERRED_SIZE, 79, GroupLayout.PREFERRED_SIZE)
                                                    .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED, 48, Short.MAX_VALUE)
                                                    .addComponent(label2)
                                                    .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                                                    .addComponent(comboBox1, GroupLayout.PREFERRED_SIZE, 92, GroupLayout.PREFERRED_SIZE)
                                                    .addGap(54, 54, 54)
                                                    .addComponent(label3)
                                                    .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                                                    .addComponent(comboBox2, GroupLayout.PREFERRED_SIZE, 95, GroupLayout.PREFERRED_SIZE)))
                                    .addGap(48, 48, 48))
            );
            panel1Layout.setVerticalGroup(
                    panel1Layout.createParallelGroup()
                            .addGroup(panel1Layout.createSequentialGroup()
                                    .addGap(18, 18, 18)
                                    .addGroup(panel1Layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                                            .addComponent(textField1, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                                            .addComponent(comboBox2, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                                            .addComponent(label3)
                                            .addComponent(label1)
                                            .addComponent(label2)
                                            .addComponent(comboBox1, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
                                    .addGap(18, 18, 18)
                                    .addGroup(panel1Layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                                            .addComponent(label4)
                                            .addComponent(textField2, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
                                    .addGap(18, 18, 18)
                                    .addGroup(panel1Layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                                            .addComponent(label5)
                                            .addComponent(textField3, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
                                    .addContainerGap(26, Short.MAX_VALUE))
            );
        }

        //---- button1 ----
        button1.setText("\u753b\u56fe");
        button1.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // 在这里调用 Painting 类进行画图操作
                Painting painting = new Painting();
                painting.setVisible(true);
            }
        });

        //---- button2 ----
        button2.setText("\u786e\u5b9a");
        button2.addActionListener(e -> uploadQuestionAndAnswers(e));

        // 在这里添加 button1 和 button2 的布局设置
        GroupLayout contentPaneLayout = new GroupLayout(contentPane);
        contentPane.setLayout(contentPaneLayout);
        contentPaneLayout.setHorizontalGroup(
                contentPaneLayout.createParallelGroup()
                        .addComponent(panel1, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addGroup(contentPaneLayout.createSequentialGroup()
                                .addGap(250, 250, 250)
                                .addComponent(button1)
                                .addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED)
                                .addComponent(button2)
                                .addContainerGap(270, Short.MAX_VALUE))
        );

        contentPaneLayout.setVerticalGroup(
                contentPaneLayout.createParallelGroup()
                        .addGroup(contentPaneLayout.createSequentialGroup()
                                .addComponent(panel1, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED)
                                .addGroup(contentPaneLayout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                                        .addComponent(button1)
                                        .addComponent(button2))
                                .addGap(0, 41, Short.MAX_VALUE))
        );

        pack();
        setLocationRelativeTo(getOwner());
    }
}
