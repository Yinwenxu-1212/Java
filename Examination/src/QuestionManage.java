import java.awt.event.*;

import type.MultiChoice;
import util.DatabaseConnector;
import util.GetId;
import util.QuestionDetails;
import type.ListenQuestion;
import type.QuestionAndAnswer;
import type.SingleChoice;

import javax.swing.*;
import javax.swing.GroupLayout;
import javax.swing.table.DefaultTableModel;
import java.sql.*;

/**
 * @author 11219
 */
public class QuestionManage extends JFrame {
    private String subject;
    private String type;
    private JScrollPane scrollPane1;
    private JTable table1;
    private JPanel panel1;
    private JLabel label1;
    private JComboBox comboBox1;
    private JLabel label2;
    private JComboBox comboBox2;
    private JButton button1;
    private JButton button2;
    private JButton button3;
    public QuestionManage() {
        initComponents();
        presentSubject();
        presentType();
        comboBox1.addActionListener(e -> comboBox1ActionPerformed(e));
        // 在构造函数中调用一次以确保初始化时能显示表格数据
        comboBox1ActionPerformed(null);
        table1.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    showQuestionDetails();
                }
            }
        });


    }


    private void showQuestionDetails() {
        int selectedRow = table1.getSelectedRow();
        String subject = comboBox1.getSelectedItem().toString();
        String type = comboBox2.getSelectedItem().toString();
        if (selectedRow != -1) {
            int questionId = GetId.getQuestionID((String) table1.getValueAt(selectedRow, 0));
            if (questionId != -1) {
                QuestionDetails questionDetails = new QuestionDetails(questionId, subject, type);
                questionDetails.setVisible(true);
            }
        }
    }
    private int getSelectedQuestionId() {
        int selectedRow = table1.getSelectedRow();
        if (selectedRow != -1) {
            return GetId.getQuestionID((String)table1.getValueAt(selectedRow, 0));
        }
        return -1;
    }
    private void comboBox1ActionPerformed(ActionEvent e) {
        DefaultTableModel model = presentQuestion();
        if (model.getRowCount() == 0) {
            // 如果查询结果为空，则创建一个包含"不存在相关题目"消息的默认表格模型
            model = new DefaultTableModel(new Object[][]{{"不存在相关题目"}}, new Object[]{"消息"});
        }
        table1.setModel(model);
    }
    private void presentSubject(){
        try (Connection connection = DatabaseConnector.connect()) {
            String query = "SELECT subject FROM subjects ";
            try (PreparedStatement statement = connection.prepareStatement(query)) {
                ResultSet resultSet = statement.executeQuery();
                comboBox1.removeAllItems();
                while (resultSet.next()) {
                    comboBox1.addItem(resultSet.getString("subject"));
                }
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "数据库连接错误：" + ex.getMessage());
        }
    }

    private void presentType(){
        try (Connection connection = DatabaseConnector.connect()) {
            String query = "SELECT name FROM question_types ";
            try (PreparedStatement statement = connection.prepareStatement(query)) {
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

    private void performQuestion(ActionEvent e) {
        table1.setModel(presentQuestion());
    }
    private DefaultTableModel presentQuestion() {
        try (Connection connection = DatabaseConnector.connect()) {
            String query = "";
            type = (String)comboBox2.getSelectedItem();
            int typeId = GetId.getTypeID(type);
            System.out.println(typeId);
            int subjectId = GetId.getSubjectId((String)comboBox1.getSelectedItem());
            System.out.println(subjectId);
            if(type.equals("单项选择题") || type.equals("听力题") || type.equals("多项选择题")){
                query = "SELECT " +
                        "    questions.content AS '题干', " +
                        "    questions.difficulty AS '难度', " +
                        "    questions.score AS '分数', " +
                        "    topics.name AS '知识点', " +
                        "    MAX(CASE WHEN options.option_name = 'A' THEN options.option_text END) AS '选项A', " +
                        "    MAX(CASE WHEN options.option_name = 'B' THEN options.option_text END) AS '选项B', " +
                        "    MAX(CASE WHEN options.option_name = 'C' THEN options.option_text END) AS '选项C', " +
                        "    MAX(CASE WHEN options.option_name = 'D' THEN options.option_text END) AS '选项D', " +
                        "    GROUP_CONCAT(CASE WHEN options.is_correct = 1 THEN options.option_name END ORDER BY options.option_name ASC) AS '答案' " +
                        "FROM " +
                        "    questions " +
                        "JOIN topics ON questions.topic_id = topics.id " +
                        "JOIN subjects ON topics.subject_id = subjects.id " +
                        "JOIN options ON questions.id = options.question_id " +
                        "WHERE " +
                        "    topics.subject_id = ? AND questions.question_type_id = ? " +
                        "GROUP BY " +
                        "    questions.id, questions.content, questions.difficulty, questions.score, topics.name";

            } else if(type.equals("问答题")){
                query = "SELECT " +
                        "    questions.content AS '题干', " +
                        "    questions.difficulty AS '难度', " +
                        "    questions.score AS '分数', " +
                        "    topics.name AS '知识点', " +
                        "    answers.correct_answer_text AS '答案' " +
                        "FROM " +
                        "    questions " +
                        "JOIN " +
                        "    answers ON questions.id = answers.question_id " +
                        "JOIN " +
                        "    topics ON questions.topic_id = topics.id " +
                        "WHERE " +
                        "    topics.subject_id = ? AND questions.question_type_id = ?";
            }
            try (PreparedStatement statement = connection.prepareStatement(query)) {
                statement.setInt(1, subjectId);
                statement.setInt(2, typeId);
                System.out.println("SQL Query: " + query);  // 输出SQL查询语句，用于调试
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


    private void deleteQuestion(ActionEvent e) {
        int questionId = getSelectedQuestionId();
        if (questionId != -1) {
            int confirm = JOptionPane.showConfirmDialog(this, "确定要删除这个问题吗？", "确认删除", JOptionPane.YES_NO_OPTION);
            if (confirm == JOptionPane.YES_OPTION) {
                // 执行删除操作
                try (Connection connection = DatabaseConnector.connect()) {
                    if(((String)comboBox2.getSelectedItem()).equals("问答题")) {
                        // 删除 answers 表中的记录
                        String deleteAnswersQuery = "DELETE FROM answers WHERE question_id=?";
                        try (PreparedStatement deleteAnswersStatement = connection.prepareStatement(deleteAnswersQuery)) {
                            deleteAnswersStatement.setInt(1, questionId);
                            deleteAnswersStatement.executeUpdate();
                        }
                    }
                    else if(comboBox2.getSelectedItem().equals("单项选择题")||
                            comboBox2.getSelectedItem().equals("听力题")||
                            comboBox2.getSelectedItem().equals("多项选择题")){
                        // 删除 options 表中的记录
                        String deleteOptionsQuery = "DELETE FROM options WHERE question_id=?";
                        try (PreparedStatement deleteOptionsStatement = connection.prepareStatement(deleteOptionsQuery)) {
                            deleteOptionsStatement.setInt(1, questionId);
                            deleteOptionsStatement.executeUpdate();
                        }
                    }
                    // 删除 questions 表中的记录
                    String deleteQuestionQuery = "DELETE FROM questions WHERE id=?";
                    try (PreparedStatement deleteQuestionStatement = connection.prepareStatement(deleteQuestionQuery)) {
                        deleteQuestionStatement.setInt(1, questionId);
                        int rowsAffected = deleteQuestionStatement.executeUpdate();
                        if (rowsAffected > 0) {
                            JOptionPane.showMessageDialog(this, "问题删除成功");
                            comboBox1ActionPerformed(null); // 刷新表格数据
                        } else {
                            JOptionPane.showMessageDialog(this, "问题删除失败");
                        }
                    }
                } catch (SQLException ex) {
                    ex.printStackTrace();
                    JOptionPane.showMessageDialog(this, "数据库连接错误：" + ex.getMessage());
                }
            }
        } else {
            JOptionPane.showMessageDialog(this, "请选择要删除的问题");
        }
    }

    private void updateQuestion(ActionEvent e) {
        type = (String)comboBox2.getSelectedItem();
        subject = (String)comboBox1.getSelectedItem();
        if(comboBox2.getSelectedItem().equals("单项选择题")){
            if(getSelectedQuestionId()==-1){
                JOptionPane.showMessageDialog(this, "请选择要修改的问题");
            }
            else {
                SingleChoice singleChoice = new SingleChoice(subject, type, getSelectedQuestionId());
                System.out.println(getSelectedQuestionId());
                singleChoice.setVisible(true);
            }
        }
        else if(comboBox2.getSelectedItem().equals("听力题")){
            if(getSelectedQuestionId()==-1){
                JOptionPane.showMessageDialog(this, "请选择要修改的问题");
            }
            else {
                ListenQuestion listenQuestion = new ListenQuestion(subject, type, getSelectedQuestionId());
                System.out.println(getSelectedQuestionId());
                listenQuestion.setVisible(true);
            }
        }
        else if (comboBox2.getSelectedItem().equals("问答题")) {
            if(getSelectedQuestionId()==-1){
                JOptionPane.showMessageDialog(this, "请选择要修改的问题");
            }
            else {
                QuestionAndAnswer questionAndAnswer = new QuestionAndAnswer(subject, type, getSelectedQuestionId());
                System.out.println(getSelectedQuestionId());
                questionAndAnswer.setVisible(true);
            }
        }
        else if (comboBox2.getSelectedItem().equals("多项选择题")) {
            if(getSelectedQuestionId()==-1){
                JOptionPane.showMessageDialog(this, "请选择要修改的问题");
            }
            else {
                MultiChoice multiChoice = new MultiChoice(
                        subject, type, getSelectedQuestionId());
                System.out.println(getSelectedQuestionId());
                multiChoice.setVisible(true);
            }
        }
    }

    private void performDetails(ActionEvent e) {
        showQuestionDetails();
    }

    private void initComponents() {
        scrollPane1 = new JScrollPane();
        table1 = new JTable();
        panel1 = new JPanel();
        label1 = new JLabel();
        comboBox1 = new JComboBox();
        label2 = new JLabel();
        comboBox2 = new JComboBox();
        button1 = new JButton();
        button2 = new JButton();
        button3 = new JButton();

        //======== this ========
        setTitle("\u9898\u76ee\u7ba1\u7406");
        var contentPane = getContentPane();

        //======== scrollPane1 ========
        {
            scrollPane1.setViewportView(table1);
        }

        //======== panel1 ========
        {

            //---- label1 ----
            label1.setText("\u5b66\u79d1\uff1a");

            //---- label2 ----
            label2.setText("\u9898\u578b\uff1a");

            //---- comboBox2 ----
            comboBox2.addActionListener(e -> performQuestion(e));

            //---- button1 ----
            button1.setText("\u4fee\u6539");
            button1.addActionListener(e -> updateQuestion(e));

            //---- button2 ----
            button2.setText("\u5220\u9664");
            button2.addActionListener(e -> deleteQuestion(e));

            //---- button3 ----
            button3.setText("\u8be6\u60c5");
            button3.addActionListener(e -> performDetails(e));

            GroupLayout panel1Layout = new GroupLayout(panel1);
            panel1.setLayout(panel1Layout);
            panel1Layout.setHorizontalGroup(
                    panel1Layout.createParallelGroup()
                            .addGroup(panel1Layout.createSequentialGroup()
                                    .addGap(18, 18, 18)
                                    .addGroup(panel1Layout.createParallelGroup()
                                            .addGroup(panel1Layout.createSequentialGroup()
                                                    .addGroup(panel1Layout.createParallelGroup(GroupLayout.Alignment.TRAILING)
                                                            .addComponent(label1)
                                                            .addComponent(label2))
                                                    .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                                                    .addGroup(panel1Layout.createParallelGroup()
                                                            .addComponent(comboBox1, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                                                            .addComponent(comboBox2, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)))
                                            .addGroup(panel1Layout.createSequentialGroup()
                                                    .addGap(12, 12, 12)
                                                    .addGroup(panel1Layout.createParallelGroup()
                                                            .addComponent(button3)
                                                            .addGroup(panel1Layout.createParallelGroup(GroupLayout.Alignment.TRAILING)
                                                                    .addComponent(button2)
                                                                    .addComponent(button1)))))
                                    .addContainerGap(16, Short.MAX_VALUE))
            );
            panel1Layout.setVerticalGroup(
                    panel1Layout.createParallelGroup()
                            .addGroup(panel1Layout.createSequentialGroup()
                                    .addGap(23, 23, 23)
                                    .addGroup(panel1Layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                                            .addComponent(label1)
                                            .addComponent(comboBox1, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
                                    .addGap(12, 12, 12)
                                    .addGroup(panel1Layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                                            .addComponent(label2)
                                            .addComponent(comboBox2, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
                                    .addGap(25, 25, 25)
                                    .addComponent(button1)
                                    .addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED)
                                    .addComponent(button2)
                                    .addGap(18, 18, 18)
                                    .addComponent(button3)
                                    .addContainerGap(GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
            );
        }

        GroupLayout contentPaneLayout = new GroupLayout(contentPane);
        contentPane.setLayout(contentPaneLayout);
        contentPaneLayout.setHorizontalGroup(
                contentPaneLayout.createParallelGroup()
                        .addGroup(GroupLayout.Alignment.TRAILING, contentPaneLayout.createSequentialGroup()
                                .addContainerGap()
                                .addComponent(panel1, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(scrollPane1, GroupLayout.DEFAULT_SIZE, 592, Short.MAX_VALUE))
        );
        contentPaneLayout.setVerticalGroup(
                contentPaneLayout.createParallelGroup()
                        .addGroup(contentPaneLayout.createSequentialGroup()
                                .addContainerGap(27, Short.MAX_VALUE)
                                .addGroup(contentPaneLayout.createParallelGroup()
                                        .addGroup(contentPaneLayout.createSequentialGroup()
                                                .addComponent(scrollPane1, GroupLayout.PREFERRED_SIZE, 306, GroupLayout.PREFERRED_SIZE)
                                                .addContainerGap(GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                                        .addGroup(contentPaneLayout.createSequentialGroup()
                                                .addComponent(panel1, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                                .addContainerGap(GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))))
        );
        pack();
        setLocationRelativeTo(getOwner());
    }

}
