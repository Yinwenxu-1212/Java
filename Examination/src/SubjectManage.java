import util.DatabaseConnector;

import java.awt.event.*;
import javax.swing.*;
import javax.swing.GroupLayout;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.*;
import java.sql.*;

/**
 * @author 11219
 */
public class SubjectManage extends JFrame {
    private JButton button1;
    private JButton button2;
    private JButton button3;
    private JScrollPane scrollPane1;
    private JTable table1;
    public SubjectManage() {
        initComponents();
        loadData();
    }
    private void addSubject(ActionEvent e) {
        String subject = JOptionPane.showInputDialog(this, "请输入添加的科目：");
        // 检查subject是否为null或空字符串
        if (subject == null || subject.trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "请输入有效的知识点！");
            return;
        }
        if (isSubjectExists(subject)) {
            JOptionPane.showMessageDialog(this, "目标学科已经存在，无法添加。");
            return;
        }
        try (Connection connection = DatabaseConnector.connect()) {
            String query = "INSERT INTO subjects (subject) VALUES (?)";
            try (PreparedStatement statement = connection.prepareStatement(query)) {
                statement.setString(1, subject);
                int rowsInserted = statement.executeUpdate();
                if (rowsInserted > 0) {
                    JOptionPane.showMessageDialog(this, "科目添加成功！");
                    loadData();
                } else {
                    JOptionPane.showMessageDialog(this, "科目添加失败！");
                }
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "数据库连接错误：" + ex.getMessage());
        }
    }

    private void deleteSubject(ActionEvent e) {
        // 提示用户确认删除
        int confirmResult = JOptionPane.showConfirmDialog(this,
                "删除科目将同时删除相关的知识点和题目。是否确认删除？",
                "确认删除",
                JOptionPane.YES_NO_OPTION);

        // 如果用户选择确认删除
        if (confirmResult == JOptionPane.YES_OPTION) {
            String subject = JOptionPane.showInputDialog(this, "请输入删除的科目：");

            try (Connection connection = DatabaseConnector.connect()) {
                // 获取该科目对应的知识点的ID
                String getSubjectIdQuery = "SELECT id FROM subjects WHERE subject = ?";
                int subjectId;
                try (PreparedStatement getSubjectIdStatement = connection.prepareStatement(getSubjectIdQuery)) {
                    getSubjectIdStatement.setString(1, subject);
                    try (ResultSet resultSet = getSubjectIdStatement.executeQuery()) {
                        if (resultSet.next()) {
                            subjectId = resultSet.getInt("id");

                            // 删除关联的题目表中的数据
                            String deleteQuestionsQuery = "DELETE FROM questions WHERE topic_id IN (SELECT id FROM topics WHERE subject_id = ?)";
                            try (PreparedStatement deleteQuestionsStatement = connection.prepareStatement(deleteQuestionsQuery)) {
                                deleteQuestionsStatement.setInt(1, subjectId);
                                deleteQuestionsStatement.executeUpdate();

                                // 删除知识点表中的数据
                                String deleteTopicsQuery = "DELETE FROM topics WHERE subject_id = ?";
                                try (PreparedStatement deleteTopicsStatement = connection.prepareStatement(deleteTopicsQuery)) {
                                    deleteTopicsStatement.setInt(1, subjectId);
                                    deleteTopicsStatement.executeUpdate();

                                    // 最后删除科目表中的记录
                                    String deleteSubjectQuery = "DELETE FROM subjects WHERE subject = ?";
                                    try (PreparedStatement deleteSubjectStatement = connection.prepareStatement(deleteSubjectQuery)) {
                                        deleteSubjectStatement.setString(1, subject);
                                        int rowsInserted = deleteSubjectStatement.executeUpdate();
                                        if (rowsInserted > 0) {
                                            JOptionPane.showMessageDialog(this, "科目删除成功！");
                                            loadData();
                                        } else {
                                            JOptionPane.showMessageDialog(this, "科目删除失败！");
                                        }
                                    }
                                }
                            }
                        } else {
                            JOptionPane.showMessageDialog(this, "未找到该科目！");
                        }
                    }
                }
            } catch (SQLException ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(this, "数据库连接错误：" + ex.getMessage());
            }
        }
    }

    private boolean isSubjectExists(String subject) {
        try (Connection connection = DatabaseConnector.connect()) {
            String query = "SELECT COUNT(*) FROM subjects WHERE subject = ?";
            try (PreparedStatement statement = connection.prepareStatement(query)) {
                statement.setString(1, subject);
                try (ResultSet resultSet = statement.executeQuery()) {
                    if (resultSet.next()) {
                        int count = resultSet.getInt(1);
                        return count > 0;
                    }
                }
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
            // 处理异常，可能是数据库连接问题等
        }
        return false;
    }
    private void updateSubject(ActionEvent e) {
        int selectedRow = table1.getSelectedRow();

        // 检查是否选中了单元格
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "请先选中要修改的单元格！");
            return;
        }

        // 获取科目名称
        String subject = (String) table1.getValueAt(selectedRow, 0);

        // 添加 PropertyChangeListener 监听模型属性变化
        table1.getModel().addTableModelListener(new TableModelListener() {
            @Override
            public void tableChanged(TableModelEvent e) {
                // 编辑完成后获取值
                if (e.getType() == TableModelEvent.UPDATE && e.getColumn() != TableModelEvent.ALL_COLUMNS) {
                    Object editedValue = table1.getValueAt(selectedRow, e.getColumn());
                    updateSubjects(subject, editedValue);
                    // 重新加载数据
                    loadData();
                }
            }
        });

        // 停止单元格的编辑
        table1.getCellEditor().stopCellEditing();
    }



    private void updateSubjects(String subject, Object editedValue) {
        if (isSubjectExists((String) editedValue)) {
            JOptionPane.showMessageDialog(this, "目标学科已经存在，无法更新。");
            return;
        }
        try (Connection connection = DatabaseConnector.connect()) {
            String query = "UPDATE subjects SET subject = ? WHERE subject = ?";
            try (PreparedStatement statement = connection.prepareStatement(query)) {
                statement.setObject(1, editedValue);
                statement.setString(2, subject);

                int rowsUpdated = statement.executeUpdate();
                if (rowsUpdated > 0) {
                    JOptionPane.showMessageDialog(this, "科目修改成功！");
                } else {
                    JOptionPane.showMessageDialog(this, "科目修改失败！");
                }
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "数据库连接错误：" + ex.getMessage());
        }
    }
    private void loadData() {
        try (Connection connection = DatabaseConnector.connect()) {
            String query = "SELECT subject FROM subjects";
            try (PreparedStatement statement = connection.prepareStatement(query);
                 ResultSet resultSet = statement.executeQuery()) {

                DefaultTableModel model = new DefaultTableModel();
                model.addColumn("学科");  // 设置列名

                while (resultSet.next()) {
                    Object[] rowData = new Object[1];
                    rowData[0] = resultSet.getObject("subject");
                    model.addRow(rowData);
                }
                table1.setModel(model);
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "数据库连接错误：" + ex.getMessage());
        }
    }
    private void initComponents() {

        button1 = new JButton();
        button2 = new JButton();
        button3 = new JButton();
        scrollPane1 = new JScrollPane();
        table1 = new JTable();

        //======== this ========
        setTitle("\u79d1\u76ee\u7ba1\u7406");
        var contentPane = getContentPane();

        //---- button1 ----
        button1.setText("\u6dfb\u52a0");
        button1.addActionListener(e -> addSubject(e));

        //---- button2 ----
        button2.setText("\u4fee\u6539");
        button2.addActionListener(e -> updateSubject(e));

        //---- button3 ----
        button3.setText("\u5220\u9664");
        button3.addActionListener(e -> deleteSubject(e));

        //======== scrollPane1 ========
        {
            scrollPane1.setViewportView(table1);
        }

        GroupLayout contentPaneLayout = new GroupLayout(contentPane);
        contentPane.setLayout(contentPaneLayout);
        contentPaneLayout.setHorizontalGroup(
                contentPaneLayout.createParallelGroup()
                        .addGroup(GroupLayout.Alignment.TRAILING, contentPaneLayout.createSequentialGroup()
                                .addGap(80, 80, 80)
                                .addGroup(contentPaneLayout.createParallelGroup(GroupLayout.Alignment.TRAILING)
                                        .addComponent(button1)
                                        .addComponent(button2)
                                        .addComponent(button3))
                                .addGap(46, 46, 46)
                                .addComponent(scrollPane1, GroupLayout.PREFERRED_SIZE, 74, GroupLayout.PREFERRED_SIZE)
                                .addContainerGap(50, Short.MAX_VALUE))
        );
        contentPaneLayout.setVerticalGroup(
                contentPaneLayout.createParallelGroup()
                        .addGroup(contentPaneLayout.createSequentialGroup()
                                .addGap(50, 50, 50)
                                .addGroup(contentPaneLayout.createParallelGroup(GroupLayout.Alignment.LEADING, false)
                                        .addComponent(scrollPane1, GroupLayout.DEFAULT_SIZE, 0, Short.MAX_VALUE)
                                        .addGroup(contentPaneLayout.createSequentialGroup()
                                                .addComponent(button1)
                                                .addGap(18, 18, 18)
                                                .addComponent(button2)
                                                .addGap(18, 18, 18)
                                                .addComponent(button3)))
                                .addContainerGap(68, Short.MAX_VALUE))
        );
        pack();
        setLocationRelativeTo(getOwner());
    }

}
