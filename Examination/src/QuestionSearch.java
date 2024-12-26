import java.awt.*;
import java.awt.event.*;

import org.apache.batik.swing.JSVGCanvas;
import org.apache.batik.swing.svg.SVGUserAgentAdapter;
import util.*;

import javax.swing.*;
import javax.swing.GroupLayout;
import javax.swing.table.DefaultTableModel;
import java.net.URL;
import java.sql.*;
/**
 * @author 11219
 */
public class QuestionSearch extends JFrame {
    private boolean isPlaying = false;

    private JPanel panel1;
    private JButton button1;
    private JButton button2;
    private JButton detailsButton;
    private JTextField textField1;
    private JScrollPane scrollPane1;
    private JTable table1;
    public QuestionSearch() {
        initComponents();
        table1.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                scrollPane1MouseClicked(e);
            }
        });
        scrollPane1.setViewportView(table1);
    }

    private void questionDetails(MouseEvent e) {
        if (e.getClickCount() == 2) {
            int row = table1.rowAtPoint(e.getPoint());
            int column = table1.columnAtPoint(e.getPoint());

            if (row != -1 && column != -1) {
                // 处理双击事件
                showQuestionDetails();
            }
        }
    }

    private void showQuestionDetails() {
        int selectedRow = table1.getSelectedRow();
        if (selectedRow != -1) {
            String subject = (String) table1.getValueAt(selectedRow, 5);
            String type = (String) table1.getValueAt(selectedRow, 3);
            int questionId = GetId.getQuestionID((String) table1.getValueAt(selectedRow, 0));
            if (questionId != -1) {
                QuestionDetails questionDetails = new QuestionDetails(questionId, subject, type);
                questionDetails.setVisible(true);
            }
        }
    }

    private void toggleAudio(String audioPath) {
        if (isPlaying) {
            stopAudio(audioPath);
            isPlaying = false;
        } else {
            playAudio(audioPath);
            isPlaying = true;
        }
    }

    private DefaultTableModel searchQuestion(String keyword) {
        try (Connection connection = DatabaseConnector.connect()) {
            String query = "SELECT questions.content AS '题干', " +
                    "questions.difficulty AS '难度', " +
                    "questions.score AS '分数', " +
                    "question_types.name AS '题型', " +
                    "topics.name AS '知识点', " +
                    "subjects.subject AS '学科', " +
                    "questions.image_path AS '图片路径', " +
                    "questions.audio_path AS '音频路径' " +
                    "FROM questions " +
                    "JOIN topics ON questions.topic_id = topics.id " +
                    "JOIN subjects ON subjects.id = topics.subject_id " +
                    "JOIN question_types ON questions.question_type_id = question_types.id " +
                    "WHERE questions.content LIKE ? OR topics.name LIKE ?";
            try (PreparedStatement statement = connection.prepareStatement(query)) {
                statement.setString(1, "%" + keyword + "%");
                statement.setString(2, "%" + keyword + "%");
                try (ResultSet resultSet = statement.executeQuery()) {
                    DefaultTableModel model = new DefaultTableModel();
                    ResultSetMetaData metaData = resultSet.getMetaData();
                    int columnCount = metaData.getColumnCount();
                    // 添加列名
                    for (int columnIndex = 1; columnIndex <= columnCount; columnIndex++) {
                        model.addColumn(metaData.getColumnLabel(columnIndex));
                    }
                    // 添加数据
                    while (resultSet.next()) {
                        Object[] rowData = new Object[columnCount];
                        for (int columnIndex = 1; columnIndex <= columnCount; columnIndex++) {
                            rowData[columnIndex - 1] = resultSet.getObject(columnIndex);
                        }
                        model.addRow(rowData);
                    }
                    return model;
                }
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "数据库连接错误：" + ex.getMessage());
            return new DefaultTableModel();
        }
    }
    private void stopAudio(String audioPath) {
        if (isPlaying) {
            if (audioPath.toLowerCase().endsWith(".wav")) {
                AudioPlayer.stopAudio();
            } else if (audioPath.toLowerCase().endsWith(".mp3")) {
                MP3Player.stopMP3();
            }
            isPlaying = false;
        }
    }
    private void searchQuestion(ActionEvent e) {
        String keyword = textField1.getText();
        DefaultTableModel model = searchQuestion(keyword);
        table1.setModel(model);
    }

    private void scrollPane1MouseClicked(MouseEvent e) {
        int column = table1.columnAtPoint(e.getPoint());
        int row = table1.rowAtPoint(e.getPoint());

        if (row >= 0 && column == table1.getColumnCount() - 1) {
            Object value = table1.getValueAt(row, column);
            if (value instanceof String) {
                String audioPath = (String) value;
                toggleAudio(audioPath);
            }
        }
        if (row >= 0 && column == table1.getColumnCount() - 2) {
            Object value = table1.getValueAt(row, column);
            if (value != null) {
                if ((!value.equals("null")) && value instanceof String) {
                    String imagePath = (String) value;
                    showImage(imagePath);
                }
            }
        }
    }

    private void playAudio(String audioPath) {
        stopAudio(audioPath); // 停止当前音频
        isPlaying = true;
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
    private void showImage(String imagePath) {
        try {
            // 获取文件扩展名
            String extension = imagePath.substring(imagePath.lastIndexOf('.') + 1).toLowerCase();

            if ("svg".equals(extension)) {
                // 如果是 SVG 文件，使用 JSVGCanvas 显示
                showSvgImage(imagePath);
            } else {
                // 如果是普通图片文件，使用 JOptionPane 显示缩放后的图标
                showNormalImage(imagePath);
            }
        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "显示图片时发生错误：" + e.getMessage(), "错误", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void showSvgImage(String svgFilePath) {
        try {
            JSVGCanvas svgCanvas = new JSVGCanvas(new SVGUserAgentAdapter(), true, false);
            svgCanvas.setURI(String.valueOf(new URL("file:///" + svgFilePath)));
            JFrame frame = new JFrame("SVG Viewer");
            frame.setSize(800, 600);
            frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
            frame.getContentPane().add(new JScrollPane(svgCanvas));
            frame.setVisible(true);
        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "显示 SVG 图片时发生错误：" + e.getMessage(), "错误", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void showNormalImage(String imagePath) {
        try {
            ImageIcon originalIcon = new ImageIcon(imagePath);

            // 设置缩放比例，这里设置为原始图片的 50%
            int scaledWidth = (int) (originalIcon.getIconWidth() * 0.5);
            int scaledHeight = (int) (originalIcon.getIconHeight() * 0.5);

            // 创建缩放后的图标
            ImageIcon scaledIcon = new ImageIcon(originalIcon.getImage().getScaledInstance(scaledWidth, scaledHeight, Image.SCALE_DEFAULT));

            // 使用 JOptionPane 显示缩放后的图标
            JOptionPane.showMessageDialog(this, scaledIcon, "图片预览", JOptionPane.PLAIN_MESSAGE);
        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "显示图片时发生错误：" + e.getMessage(), "错误", JOptionPane.ERROR_MESSAGE);
        }
    }


    private void perform(ActionEvent e) {
        JOptionPane.showMessageDialog(this,"点击表格中的图片路径可以查看图片，点击音频路径可以播放音频，双击可以停止, 选择题目点击详情按钮可以查看详情");
    }

    private void initComponents() {

        panel1 = new JPanel();
        button1 = new JButton();
        button2 = new JButton();
        textField1 = new JTextField();
        scrollPane1 = new JScrollPane();
        table1 = new JTable();
        detailsButton = new JButton();

        //======== this ========
        setTitle("\u9898\u76ee\u641c\u7d22");
        var contentPane = getContentPane();

        //======== panel1 ========
        {

            //---- button1 ----
            button1.setText("\u641c\u7d22");
            button1.addActionListener(e -> searchQuestion(e));

            detailsButton.setText("详情");
            detailsButton.addActionListener(e -> showQuestionDetails());
            panel1.add(detailsButton);

            //---- button2 ----
            button2.setText("\u63d0\u793a");
            button2.addActionListener(e -> perform(e));

            GroupLayout panel1Layout = new GroupLayout(panel1);
            panel1.setLayout(panel1Layout);
            panel1Layout.setHorizontalGroup(
                    panel1Layout.createParallelGroup()
                            .addGroup(panel1Layout.createSequentialGroup()
                                    .addContainerGap(96, Short.MAX_VALUE)
                                    .addComponent(textField1, GroupLayout.PREFERRED_SIZE, 309, GroupLayout.PREFERRED_SIZE)
                                    .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED, 58, Short.MAX_VALUE)
                                    .addComponent(button1)
                                    .addGap(18, 18, Short.MAX_VALUE)
                                    .addComponent(detailsButton)
                                    .addGap(18, 18, Short.MAX_VALUE)
                                    .addComponent(button2)
                                    .addContainerGap(81, Short.MAX_VALUE))
            );
            panel1Layout.setVerticalGroup(
                    panel1Layout.createParallelGroup()
                            .addGroup(GroupLayout.Alignment.TRAILING, panel1Layout.createSequentialGroup()
                                    .addContainerGap(14, Short.MAX_VALUE)
                                    .addGroup(panel1Layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                                            .addComponent(button1)
                                            .addComponent(detailsButton)
                                            .addComponent(button2)
                                            .addComponent(textField1, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
                                    .addContainerGap(GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
            );
        }

        //======== scrollPane1 ========
        {
            scrollPane1.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    scrollPane1MouseClicked(e);
                    questionDetails(e);
                }
            });
            scrollPane1.setViewportView(table1);
        }

        GroupLayout contentPaneLayout = new GroupLayout(contentPane);
        contentPane.setLayout(contentPaneLayout);
        contentPaneLayout.setHorizontalGroup(
                contentPaneLayout.createParallelGroup()
                        .addComponent(panel1, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(scrollPane1, GroupLayout.DEFAULT_SIZE, 718, Short.MAX_VALUE)
        );
        contentPaneLayout.setVerticalGroup(
                contentPaneLayout.createParallelGroup()
                        .addGroup(contentPaneLayout.createSequentialGroup()
                                .addComponent(panel1, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addComponent(scrollPane1, GroupLayout.DEFAULT_SIZE, 322, Short.MAX_VALUE))
        );
        pack();
        setLocationRelativeTo(getOwner());
    }

}
