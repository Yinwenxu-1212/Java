/*
 * Created by JFormDesigner on Thu Nov 30 23:30:54 CST 2023
 */

package type;

import util.*;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.Set;

/**
 * @author 11219
 */
public class MultiChoice extends JFrame {
    private int preId;
    private String subject;
    private String type;
    private JPanel panel1;
    private JLabel label3;
    private JTextField textField2;
    private JLabel label4;
    private JLabel label5;
    private JLabel label6;
    private JLabel label7;
    private JTextField textField3;
    private JTextField textField4;
    private JTextField textField5;
    private JTextField textField6;
    private JLabel label8;
    private JButton button1;
    private JButton button2;
    private JLabel label9;
    private JTextField textField7;
    private JLabel label10;
    private JComboBox<String> comboBox2;
    private JLabel label11;
    private JComboBox comboBox3;
    private JPanel checkBoxPanel;
    public MultiChoice(String subject, String type, int preId) {
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
                comboBox3.removeAllItems();
                while (resultSet.next()) {
                    comboBox3.addItem(resultSet.getString("name"));
                }
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "数据库连接错误：" + ex.getMessage());
        }
    }
    private void insertQuestion(Connection connection, String content, String difficulty, String topic, int score, String type, String imagePath, String audioPath) throws SQLException {
        int questionId = QuestionUploader.uploadQuestion(connection, content, difficulty, topic, score, type, imagePath, audioPath);
        QuestionUploader.uploadMultiOptions(connection, questionId, textField3.getText(), textField4.getText(), textField5.getText(), textField6.getText(), getSelectedOptions());
    }

    private void updateQuestion(Connection connection, int questionId, String content, String difficulty, String topic, int score, String type, String imagePath, String audioPath) throws SQLException {
        // 更新问题
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
                // 更新选项
                QuestionUploader.updateMultiOptions(connection, questionId, textField3.getText(), textField4.getText(), textField5.getText(), textField6.getText(), getSelectedOptions());
                JOptionPane.showMessageDialog(this, "问题更新成功");
            } else {
                JOptionPane.showMessageDialog(this, "问题更新失败");
            }
        }
    }
    private void uploadQuestionAndOptions(ActionEvent e) {
        String content = textField2.getText();
        String difficulty = (String) comboBox2.getSelectedItem();
        String topic = (String) comboBox3.getSelectedItem();
        int score = Integer.parseInt(textField7.getText());

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
                JOptionPane.showMessageDialog(this, "问题和选项添加成功！");
            }
            this.dispose();
        } catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "数据库连接错误：" + ex.getMessage());
        }
    }
    private Set<String> getSelectedOptions() {
        Set<String> selectedOptions = new HashSet<>();
        for (Component component : checkBoxPanel.getComponents()) {
            if (component instanceof JCheckBox) {
                JCheckBox checkBox = (JCheckBox) component;
                if (checkBox.isSelected()) {
                    selectedOptions.add(checkBox.getText());
                }
            }
        }
        return selectedOptions;
    }

    private void initComponents() {
        panel1 = new JPanel();
        label3 = new JLabel();
        textField2 = new JTextField();
        label4 = new JLabel();
        label5 = new JLabel();
        label6 = new JLabel();
        label7 = new JLabel();
        textField3 = new JTextField();
        textField4 = new JTextField();
        textField5 = new JTextField();
        textField6 = new JTextField();
        label8 = new JLabel();
        checkBoxPanel = new JPanel();
        checkBoxPanel.setLayout(new FlowLayout());
        checkBoxPanel.add(new JCheckBox("A"));
        checkBoxPanel.add(new JCheckBox("B"));
        checkBoxPanel.add(new JCheckBox("C"));
        checkBoxPanel.add(new JCheckBox("D"));
        button1 = new JButton();
        button2 = new JButton();
        label9 = new JLabel();
        textField7 = new JTextField();
        label10 = new JLabel();
        comboBox2 = new JComboBox<>();
        label11 = new JLabel();
        comboBox3 = new JComboBox();

        //======== this ========
        setTitle("多项选择题");
        var contentPane = getContentPane();

        //======== panel1 ========
        {

            //---- label3 ----
            label3.setText("\u9898\u5e72\uff1a");

            //---- label4 ----
            label4.setText("A");

            //---- label5 ----
            label5.setText("B");

            //---- label6 ----
            label6.setText("C");

            //---- label7 ----
            label7.setText("D");

            GroupLayout panel1Layout = new GroupLayout(panel1);
            panel1.setLayout(panel1Layout);
            panel1Layout.setHorizontalGroup(
                panel1Layout.createParallelGroup()
                    .addGroup(panel1Layout.createSequentialGroup()
                        .addGap(43, 43, 43)
                        .addGroup(panel1Layout.createParallelGroup()
                            .addComponent(label3)
                            .addGroup(GroupLayout.Alignment.TRAILING, panel1Layout.createParallelGroup()
                                .addComponent(label5, GroupLayout.PREFERRED_SIZE, 17, GroupLayout.PREFERRED_SIZE)
                                .addComponent(label4)
                                .addComponent(label6, GroupLayout.PREFERRED_SIZE, 14, GroupLayout.PREFERRED_SIZE)
                                .addComponent(label7)))
                        .addGap(18, 18, 18)
                        .addGroup(panel1Layout.createParallelGroup()
                            .addComponent(textField6, GroupLayout.PREFERRED_SIZE, 449, GroupLayout.PREFERRED_SIZE)
                            .addComponent(textField5, GroupLayout.PREFERRED_SIZE, 449, GroupLayout.PREFERRED_SIZE)
                            .addComponent(textField4, GroupLayout.PREFERRED_SIZE, 449, GroupLayout.PREFERRED_SIZE)
                            .addComponent(textField3, GroupLayout.PREFERRED_SIZE, 449, GroupLayout.PREFERRED_SIZE)
                            .addComponent(textField2, GroupLayout.PREFERRED_SIZE, 449, GroupLayout.PREFERRED_SIZE))
                        .addContainerGap(47, Short.MAX_VALUE))
            );
            panel1Layout.setVerticalGroup(
                panel1Layout.createParallelGroup()
                    .addGroup(panel1Layout.createSequentialGroup()
                        .addGap(21, 21, 21)
                        .addGroup(panel1Layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                            .addComponent(label3)
                            .addComponent(textField2, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
                        .addGap(18, 18, 18)
                        .addGroup(panel1Layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                            .addComponent(textField3, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                            .addComponent(label4))
                        .addGap(18, 18, 18)
                        .addGroup(panel1Layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                            .addComponent(textField4, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                            .addComponent(label5))
                        .addGap(18, 18, 18)
                        .addGroup(panel1Layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                            .addComponent(textField5, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                            .addComponent(label6))
                        .addGap(18, 18, 18)
                        .addGroup(panel1Layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                            .addComponent(textField6, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                            .addComponent(label7))
                        .addContainerGap(10, Short.MAX_VALUE))
            );
        }

        //---- label8 ----
        label8.setText("\u7b54\u6848\uff1a");

        //---- button1 ----
        button1.setText("\u786e\u5b9a");
        button1.addActionListener(e -> uploadQuestionAndOptions(e));

        //---- button2 ----
        button2.setText("\u753b\u56fe");  // 设置按钮文本
        button2.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // 在这里调用 Painting 类进行画图操作
                Painting painting = new Painting();
                painting.setVisible(true);
            }
        });

        //---- label9 ----
        label9.setText("\u5206\u503c\uff1a");

        //---- label10 ----
        label10.setText("\u96be\u5ea6\uff1a");

        //---- comboBox2 ----
        comboBox2.setModel(new DefaultComboBoxModel<>(new String[] {
            "\u6613",
            "\u4e2d",
            "\u96be"
        }));

        //---- label11 ----
        label11.setText("\u77e5\u8bc6\u70b9\uff1a");

        GroupLayout contentPaneLayout = new GroupLayout(contentPane);
        contentPane.setLayout(contentPaneLayout);
        contentPaneLayout.setHorizontalGroup(
                contentPaneLayout.createParallelGroup()
                        .addGroup(contentPaneLayout.createSequentialGroup()
                                .addGap(43, 43, 43)
                                .addComponent(label8)
                                .addGap(18, 18, 18)
                                .addComponent(checkBoxPanel, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addComponent(button2)  // 将 button2 放在 button1 的左边
                                .addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED)
                                .addComponent(button1)
                                .addGap(29, 29, 29))
                        .addGroup(contentPaneLayout.createSequentialGroup()
                                .addGap(42, 42, 42)
                                .addComponent(label9)
                                .addGap(18, 18, 18)
                                .addComponent(textField7, GroupLayout.PREFERRED_SIZE, 84, GroupLayout.PREFERRED_SIZE)
                                .addGap(18, 18, 18)
                                .addComponent(label10)
                                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(comboBox2, GroupLayout.PREFERRED_SIZE, 92, GroupLayout.PREFERRED_SIZE)
                                .addGap(54, 54, 54)
                                .addComponent(label11)
                                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(comboBox3, GroupLayout.PREFERRED_SIZE, 95, GroupLayout.PREFERRED_SIZE)
                                .addContainerGap(58, Short.MAX_VALUE))
                        .addComponent(panel1, GroupLayout.Alignment.TRAILING, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );

        contentPaneLayout.setVerticalGroup(
                contentPaneLayout.createParallelGroup()
                        .addGroup(contentPaneLayout.createSequentialGroup()
                                .addGap(16, 16, 16)
                                .addGroup(contentPaneLayout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                                        .addComponent(label9)
                                        .addComponent(label10)
                                        .addComponent(textField7, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                                        .addComponent(comboBox2, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                                        .addComponent(label11)
                                        .addComponent(comboBox3, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
                                .addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED)
                                .addComponent(panel1, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                                .addGap(24, 24, 24)
                                .addGroup(contentPaneLayout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                                        .addComponent(label8)
                                        .addComponent(checkBoxPanel, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                                        .addComponent(button2)  // 将 button2 放在 button1 的左边
                                        .addComponent(button1))
                                .addContainerGap(33, Short.MAX_VALUE)
                        )
        );
        pack();
        setLocationRelativeTo(getOwner());
    }
}
