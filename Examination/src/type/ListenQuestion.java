package type;

import util.*;

import javax.swing.*;
import javax.swing.GroupLayout;
import java.awt.event.ActionEvent;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * @author 11219
 */
public class ListenQuestion extends JFrame {
    private int preId;
    // 在 ListenQuestion 类中添加一个变量来保存录音文件路径
    private String recordedAudioPath;
    private String subject;
    private String type;
    private JPanel panel1;
    private JTextField textField1;
    private JLabel label1;
    private JLabel label2;
    private JComboBox<String> comboBox1;
    private JLabel label3;
    private JComboBox comboBox2;
    private JPanel panel2;
    private JLabel label4;
    private JTextField textField2;
    private JLabel label5;
    private JLabel label6;
    private JLabel label7;
    private JLabel label8;
    private JTextField textField3;
    private JTextField textField4;
    private JTextField textField5;
    private JTextField textField6;
    private JButton button1;
    private JPanel panel3;
    private JLabel label9;
    private JComboBox<String> comboBox3;
    private JButton button2;
    private JButton button3;
    public ListenQuestion(String subject, String type, int preId) {
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
    private void insertQuestion(Connection connection, String content, String difficulty, String topic, int score, String type, String imagePath, String audioPath) throws SQLException {
        // 上传带有音频路径的问题
        int questionId = QuestionUploader.uploadQuestion(connection, content, difficulty, topic, score, type, imagePath, audioPath);

        // 上传选项
        QuestionUploader.uploadOptions(connection, questionId, textField3.getText(), textField4.getText(), textField5.getText(), textField6.getText(),
                isCorrectOption(comboBox3.getSelectedItem().toString().equals("A")),
                isCorrectOption(comboBox3.getSelectedItem().toString().equals("B")),
                isCorrectOption(comboBox3.getSelectedItem().toString().equals("C")),
                isCorrectOption(comboBox3.getSelectedItem().toString().equals("D")));
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
                QuestionUploader.updateOptions(connection, questionId, textField3.getText(), textField4.getText(), textField5.getText(), textField6.getText(),
                        isCorrectOption(comboBox3.getSelectedItem().toString().equals("A")),
                        isCorrectOption(comboBox3.getSelectedItem().toString().equals("B")),
                        isCorrectOption(comboBox3.getSelectedItem().toString().equals("C")),
                        isCorrectOption(comboBox3.getSelectedItem().toString().equals("D")));
                JOptionPane.showMessageDialog(this, "问题更新成功");
            } else {
                JOptionPane.showMessageDialog(this, "问题更新失败");
            }
        }
    }
    private void uploadQuestionAndOptions(ActionEvent e) {
        String content = textField2.getText();
        String difficulty = (String) comboBox1.getSelectedItem();
        String topic = (String) comboBox2.getSelectedItem();
        int score = Integer.parseInt(textField1.getText());

        // 使用 ImageUploader 类获取图像路径
        String imagePath = ImageUploader.uploadImage(this);

        // 使用 AudioUploader 类获取音频路径
        String audioPath;
        if (recordedAudioPath != null) {
            // 使用录音的路径
            audioPath = recordedAudioPath;
        } else {
            // 用户选择了上传本地音频
            audioPath = AudioUploader.uploadAudio(this);
        }

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

    private boolean isCorrectOption(boolean isSelected) {
        return isSelected;
    }

    private void recordAudio(ActionEvent e) {
        // 禁用开始录音按钮
        button2.setEnabled(false);

        SwingWorker<Void, Void> worker = new SwingWorker<>() {
            @Override
            protected Void doInBackground() throws Exception {
                try {
                    // 生成唯一的文件名，例如使用时间戳
                    String fileName = "output_" + System.currentTimeMillis() + ".wav";
                    // 调用 AudioRecorder 类开始录音
                    AudioRecorder.recordAudio("D:/project/Examination/audio/record/" + fileName);

                    // 保存录音文件路径
                    recordedAudioPath = "D:/project/Examination/audio/record/" + fileName;
                    System.out.println(recordedAudioPath);

                    // 在成功录音后，你可以在这里执行相关操作，例如显示文件路径
                    JOptionPane.showMessageDialog(ListenQuestion.this, "录音成功，音频文件保存路径：" + recordedAudioPath);
                } catch (Exception ex) {
                    ex.printStackTrace();
                    JOptionPane.showMessageDialog(ListenQuestion.this, "录音时发生错误：" + ex.getMessage());
                }
                return null;
            }

            @Override
            protected void done() {
                // 启用停止录音按钮
                button3.setEnabled(true);
            }
        };

        worker.execute();
    }

    private void stopRecording(ActionEvent e) {
        try {
            AudioRecorder.stopRecording();
        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "停止录音时发生错误：" + ex.getMessage());
        } finally {
            // 启用开始录音按钮
            button2.setEnabled(true);
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
        panel2 = new JPanel();
        label4 = new JLabel();
        textField2 = new JTextField();
        label5 = new JLabel();
        label6 = new JLabel();
        label7 = new JLabel();
        label8 = new JLabel();
        textField3 = new JTextField();
        textField4 = new JTextField();
        textField5 = new JTextField();
        textField6 = new JTextField();
        button1 = new JButton();
        panel3 = new JPanel();
        label9 = new JLabel();
        comboBox3 = new JComboBox<>();
        button2 = new JButton();
        button3 = new JButton();

        //======== this ========
        setTitle("听力题");
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

            //======== panel2 ========
            {

                //---- label4 ----
                label4.setText("\u9898\u5e72\uff1a");

                //---- label5 ----
                label5.setText("A");

                //---- label6 ----
                label6.setText("B");

                //---- label7 ----
                label7.setText("C");

                //---- label8 ----
                label8.setText("D");

                GroupLayout panel2Layout = new GroupLayout(panel2);
                panel2.setLayout(panel2Layout);
                panel2Layout.setHorizontalGroup(
                        panel2Layout.createParallelGroup()
                                .addGroup(panel2Layout.createSequentialGroup()
                                        .addGap(43, 43, 43)
                                        .addGroup(panel2Layout.createParallelGroup()
                                                .addComponent(label4)
                                                .addGroup(GroupLayout.Alignment.TRAILING, panel2Layout.createParallelGroup()
                                                        .addComponent(label6, GroupLayout.PREFERRED_SIZE, 17, GroupLayout.PREFERRED_SIZE)
                                                        .addComponent(label5)
                                                        .addComponent(label7, GroupLayout.PREFERRED_SIZE, 14, GroupLayout.PREFERRED_SIZE)
                                                        .addComponent(label8)))
                                        .addGap(18, 18, 18)
                                        .addGroup(panel2Layout.createParallelGroup(GroupLayout.Alignment.TRAILING, false)
                                                .addComponent(textField5, GroupLayout.Alignment.LEADING, GroupLayout.DEFAULT_SIZE, 409, Short.MAX_VALUE)
                                                .addComponent(textField4, GroupLayout.Alignment.LEADING, GroupLayout.DEFAULT_SIZE, 409, Short.MAX_VALUE)
                                                .addComponent(textField3, GroupLayout.Alignment.LEADING, GroupLayout.DEFAULT_SIZE, 409, Short.MAX_VALUE)
                                                .addComponent(textField2, GroupLayout.Alignment.LEADING, GroupLayout.DEFAULT_SIZE, 409, Short.MAX_VALUE)
                                                .addComponent(textField6, GroupLayout.DEFAULT_SIZE, 409, Short.MAX_VALUE))
                                        .addContainerGap(GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                );
                panel2Layout.setVerticalGroup(
                        panel2Layout.createParallelGroup()
                                .addGroup(panel2Layout.createSequentialGroup()
                                        .addGap(21, 21, 21)
                                        .addGroup(panel2Layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                                                .addComponent(label4)
                                                .addComponent(textField2, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
                                        .addGap(18, 18, 18)
                                        .addGroup(panel2Layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                                                .addComponent(label5)
                                                .addComponent(textField3, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
                                        .addGap(18, 18, 18)
                                        .addGroup(panel2Layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                                                .addComponent(label6)
                                                .addComponent(textField4, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
                                        .addGap(18, 18, 18)
                                        .addGroup(panel2Layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                                                .addComponent(label7)
                                                .addComponent(textField5, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
                                        .addGap(18, 18, 18)
                                        .addGroup(panel2Layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                                                .addComponent(label8)
                                                .addComponent(textField6, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
                                        .addContainerGap(10, Short.MAX_VALUE))
                );
            }

            GroupLayout panel1Layout = new GroupLayout(panel1);
            panel1.setLayout(panel1Layout);
            panel1Layout.setHorizontalGroup(
                    panel1Layout.createParallelGroup()
                            .addGroup(GroupLayout.Alignment.TRAILING, panel1Layout.createSequentialGroup()
                                    .addGap(50, 50, 50)
                                    .addComponent(label1)
                                    .addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED)
                                    .addComponent(textField1, GroupLayout.PREFERRED_SIZE, 79, GroupLayout.PREFERRED_SIZE)
                                    .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED, 20, Short.MAX_VALUE)
                                    .addComponent(label2)
                                    .addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED)
                                    .addComponent(comboBox1, GroupLayout.PREFERRED_SIZE, 92, GroupLayout.PREFERRED_SIZE)
                                    .addGap(22, 22, 22)
                                    .addComponent(label3)
                                    .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                                    .addComponent(comboBox2, GroupLayout.PREFERRED_SIZE, 95, GroupLayout.PREFERRED_SIZE)
                                    .addGap(48, 48, 48))
                            .addGroup(panel1Layout.createSequentialGroup()
                                    .addContainerGap()
                                    .addComponent(panel2, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                    .addContainerGap())
            );
            panel1Layout.setVerticalGroup(
                    panel1Layout.createParallelGroup()
                            .addGroup(panel1Layout.createSequentialGroup()
                                    .addGap(18, 18, 18)
                                    .addGroup(panel1Layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                                            .addComponent(comboBox2, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                                            .addComponent(label3)
                                            .addComponent(textField1, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                                            .addComponent(label1)
                                            .addComponent(comboBox1, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                                            .addComponent(label2))
                                    .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED, 12, Short.MAX_VALUE)
                                    .addComponent(panel2, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                                    .addContainerGap())
            );
        }

        //---- button1 ----
        button1.setText("\u786e\u5b9a");
        button1.addActionListener(e -> uploadQuestionAndOptions(e));

        //======== panel3 ========
        {

            //---- label9 ----
            label9.setText("\u7b54\u6848\uff1a");

            //---- comboBox3 ----
            comboBox3.setModel(new DefaultComboBoxModel<>(new String[] {
                    "A",
                    "B",
                    "C",
                    "D"
            }));

            GroupLayout panel3Layout = new GroupLayout(panel3);
            panel3.setLayout(panel3Layout);
            panel3Layout.setHorizontalGroup(
                    panel3Layout.createParallelGroup()
                            .addGroup(panel3Layout.createSequentialGroup()
                                    .addContainerGap()
                                    .addComponent(label9)
                                    .addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED)
                                    .addComponent(comboBox3, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                                    .addGap(0, 8, Short.MAX_VALUE))
            );
            panel3Layout.setVerticalGroup(
                    panel3Layout.createParallelGroup()
                            .addGroup(panel3Layout.createSequentialGroup()
                                    .addGroup(panel3Layout.createParallelGroup()
                                            .addComponent(comboBox3, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                                            .addComponent(label9))
                                    .addGap(0, 0, Short.MAX_VALUE))
            );
        }

        //---- button2 ----
        button2.setText("\u5f00\u59cb\u5f55\u97f3");
        button2.addActionListener(e -> recordAudio(e));

        //---- button3 ----
        button3.setText("\u505c\u6b62\u5f55\u97f3");
        button3.addActionListener(e -> stopRecording(e));

        GroupLayout contentPaneLayout = new GroupLayout(contentPane);
        contentPane.setLayout(contentPaneLayout);
        contentPaneLayout.setHorizontalGroup(
                contentPaneLayout.createParallelGroup()
                        .addGroup(contentPaneLayout.createSequentialGroup()
                                .addGroup(contentPaneLayout.createParallelGroup()
                                        .addGroup(contentPaneLayout.createSequentialGroup()
                                                .addContainerGap()
                                                .addComponent(panel1, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
                                        .addGroup(contentPaneLayout.createSequentialGroup()
                                                .addGap(75, 75, 75)
                                                .addComponent(panel3, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                                                .addGap(18, 18, 18)
                                                .addComponent(button2)
                                                .addGap(18, 18, 18)
                                                .addComponent(button3)
                                                .addGap(18, 18, 18)
                                                .addComponent(button1)))
                                .addContainerGap(GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        contentPaneLayout.setVerticalGroup(
                contentPaneLayout.createParallelGroup()
                        .addGroup(contentPaneLayout.createSequentialGroup()
                                .addContainerGap()
                                .addComponent(panel1, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(contentPaneLayout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                                        .addComponent(panel3, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                        .addComponent(button2)
                                        .addComponent(button3)
                                        .addComponent(button1))
                                .addContainerGap(22, Short.MAX_VALUE))
        );
        pack();
        setLocationRelativeTo(getOwner());
    }
}
