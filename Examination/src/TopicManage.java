import util.DatabaseConnector;
import util.GetId;

import java.awt.*;
import java.awt.event.*;
import java.sql.*;
import javax.swing.*;
import javax.swing.GroupLayout;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.DefaultTableModel;

/**
 * @author 11219
 */
public class TopicManage extends JFrame {

    private JScrollPane scrollPane1;
    private JTable table1;
    private JLabel label1;
    private JComboBox comboBox1;
    private JButton button1;
    private JButton button2;
    private JButton button3;
    public TopicManage() {
        initComponents();
        presentSubject();
    }

    private boolean isTopicExists(String topic) {
        try (Connection connection = DatabaseConnector.connect()) {
            String query = "SELECT COUNT(*) FROM topics WHERE name = ?";
            try (PreparedStatement statement = connection.prepareStatement(query)) {
                statement.setString(1, topic);
                try (ResultSet resultSet = statement.executeQuery()) {
                    if (resultSet.next()) {
                        int count = resultSet.getInt(1);
                        return count > 0;
                    }
                }
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        return false;
    }
    private void addTopic(ActionEvent e) {
        String topic = JOptionPane.showInputDialog(this, "请输入添加的知识点：");
        // 检查topic是否为null或空字符串
        if (topic == null || topic.trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "请输入有效的知识点！");
            return;
        }
        // 检查知识点是否已经存在
        if (isTopicExists(topic)){
            JOptionPane.showMessageDialog(this, "该知识点已经存在！");
            return;
        }
        try (Connection connection = DatabaseConnector.connect()) {
            String query = "INSERT INTO topics (name,subject_id) VALUES (?,?)";
            try (PreparedStatement statement = connection.prepareStatement(query)) {
                statement.setString(1, topic);
                statement.setInt(2, GetId.getSubjectId((String) comboBox1.getSelectedItem()));
                int rowsInserted = statement.executeUpdate();
                if (rowsInserted > 0) {
                    JOptionPane.showMessageDialog(this, "知识点添加成功！");
                    table1.setModel(topicPresent((String) comboBox1.getSelectedItem()));
                } else {
                    JOptionPane.showMessageDialog(this, "知识点添加失败！");
                }
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "数据库连接错误：" + ex.getMessage());
        }
    }

    private void updateTopic(ActionEvent e) {
        int selectedRow = table1.getSelectedRow();

        // 检查是否选中了单元格
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "请先选中要修改的单元格！");
            return;
        }
        String topic = (String) table1.getValueAt(selectedRow, 0);
        // 添加 PropertyChangeListener 监听模型属性变化
        table1.getModel().addTableModelListener(new TableModelListener() {
            @Override
            public void tableChanged(TableModelEvent e) {
                // 编辑完成后获取值
                if (e.getType() == TableModelEvent.UPDATE && e.getColumn() != TableModelEvent.ALL_COLUMNS) {
                    Object editedValue = table1.getValueAt(selectedRow, e.getColumn());
                    updateTopics(topic, editedValue);
                    System.out.println(topic + "   " + editedValue);
                    // 重新加载数据
                    table1.setModel(topicPresent((String) comboBox1.getSelectedItem()));
                }
            }
        });

        // 停止单元格的编辑
        table1.getCellEditor().stopCellEditing();
    }

    private void updateTopics(String topic, Object editedValue) {
        if (isTopicExists((String) editedValue)) {
            JOptionPane.showMessageDialog(this, "该知识点已经存在！");
            return;
        }
        try (Connection connection = DatabaseConnector.connect()) {
            String query = "UPDATE topics SET name = ? WHERE name = ?";
            try (PreparedStatement statement = connection.prepareStatement(query)) {
                statement.setObject(1, editedValue);
                statement.setString(2, topic);

                int rowsUpdated = statement.executeUpdate();
                if (rowsUpdated > 0) {
                    JOptionPane.showMessageDialog(this, "知识点修改成功！");
                } else {
                    JOptionPane.showMessageDialog(this, "知识点修改失败！");
                }
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "数据库连接错误：" + ex.getMessage());
        }
    }
    private void deleteTopic(ActionEvent e) {
        int choice = JOptionPane.showConfirmDialog(this,
                "删除知识点将同时删除相关的题目。是否确认删除？",
                "确认删除",
                JOptionPane.YES_NO_OPTION);

        // 检查用户的选择
        if (choice == JOptionPane.YES_OPTION) {
            String topic = JOptionPane.showInputDialog(this, "请输入删除的知识点：");
            // 用户确认删除，继续删除知识点和相关题目
            try (Connection connection = DatabaseConnector.connect()) {
                // 开启事务
                connection.setAutoCommit(false);

                // 删除与知识点关联的题目
                String deleteQuestionsQuery = "DELETE FROM questions WHERE topic_id IN (SELECT id FROM topics WHERE name = ?)";
                try (PreparedStatement questionsStatement = connection.prepareStatement(deleteQuestionsQuery)) {
                    questionsStatement.setString(1, topic);
                    questionsStatement.executeUpdate();
                }

                // 删除知识点
                String deleteTopicQuery = "DELETE FROM topics WHERE name = ?";
                try (PreparedStatement topicStatement = connection.prepareStatement(deleteTopicQuery)) {
                    topicStatement.setString(1, topic);
                    int rowsDeleted = topicStatement.executeUpdate();
                    if (rowsDeleted > 0) {
                        JOptionPane.showMessageDialog(this, "知识点删除成功！");
                        table1.setModel(topicPresent((String) comboBox1.getSelectedItem()));
                    } else {
                        JOptionPane.showMessageDialog(this, "知识点删除失败！");
                    }
                }

                // 提交事务
                connection.commit();

                // 更新表格
                table1.setModel(topicPresent((String) comboBox1.getSelectedItem()));
            } catch (SQLException ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(this, "数据库连接错误：" + ex.getMessage());

                // 出现异常，回滚事务
                try (Connection connection = DatabaseConnector.connect()) {
                    connection.rollback();
                } catch (SQLException rollbackException) {
                    rollbackException.printStackTrace();
                }
            }
        }
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


    private void comboBox1ItemStateChanged(ItemEvent e) {
        String selectedUserType = (String) comboBox1.getSelectedItem();
        table1.setModel(topicPresent(selectedUserType));
        label1.setFont(new Font("楷体", Font.PLAIN, 16));
        label1.setText(selectedUserType + "的知识点");
    }
    private DefaultTableModel topicPresent(String selecteditem){
        try (Connection connection = DatabaseConnector.connect()) {
            String query = "SELECT name FROM topics WHERE subject_id = ?";
            try (PreparedStatement statement = connection.prepareStatement(query)) {
                statement.setInt(1, GetId.getSubjectId(selecteditem));
                try (ResultSet resultSet = statement.executeQuery()) {
                    DefaultTableModel model = new DefaultTableModel(new Object[]{"知识点"}, 0);
                    while (resultSet.next()) {
                        Object[] rowData = new Object[]{resultSet.getString("name")};
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

    private void initComponents() {
        scrollPane1 = new JScrollPane();
        table1 = new JTable();
        label1 = new JLabel();
        comboBox1 = new JComboBox();
        button1 = new JButton();
        button2 = new JButton();
        button3 = new JButton();

        //======== this ========
        setTitle("\u77e5\u8bc6\u70b9\u7ba1\u7406");
        var contentPane = getContentPane();

        //======== scrollPane1 ========
        {
            scrollPane1.setViewportView(table1);
        }

        //---- label1 ----
        label1.setText("text");
        label1.setFont(label1.getFont().deriveFont(label1.getFont().getSize() + 4f));

        //---- comboBox1 ----
        comboBox1.addItemListener(e -> comboBox1ItemStateChanged(e));

        //---- button1 ----
        button1.setText("\u6dfb\u52a0");
        button1.addActionListener(e -> addTopic(e));

        //---- button2 ----
        button2.setText("\u4fee\u6539");
        button2.addActionListener(e -> updateTopic(e));

        //---- button3 ----
        button3.setText("\u5220\u9664");
        button3.addActionListener(e -> deleteTopic(e));

        GroupLayout contentPaneLayout = new GroupLayout(contentPane);
        contentPane.setLayout(contentPaneLayout);
        contentPaneLayout.setHorizontalGroup(
                contentPaneLayout.createParallelGroup()
                        .addGroup(contentPaneLayout.createSequentialGroup()
                                .addGap(48, 48, 48)
                                .addGroup(contentPaneLayout.createParallelGroup(GroupLayout.Alignment.TRAILING)
                                        .addComponent(comboBox1, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                                        .addComponent(button3)
                                        .addComponent(button2)
                                        .addComponent(button1))
                                .addGap(48, 48, 48)
                                .addGroup(contentPaneLayout.createParallelGroup()
                                        .addComponent(scrollPane1, GroupLayout.PREFERRED_SIZE, 89, GroupLayout.PREFERRED_SIZE)
                                        .addComponent(label1))
                                .addContainerGap(60, Short.MAX_VALUE))
        );
        contentPaneLayout.setVerticalGroup(
                contentPaneLayout.createParallelGroup()
                        .addGroup(GroupLayout.Alignment.TRAILING, contentPaneLayout.createSequentialGroup()
                                .addContainerGap(28, Short.MAX_VALUE)
                                .addGroup(contentPaneLayout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                                        .addComponent(comboBox1, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                                        .addComponent(label1))
                                .addGap(18, 18, 18)
                                .addGroup(contentPaneLayout.createParallelGroup()
                                        .addGroup(contentPaneLayout.createSequentialGroup()
                                                .addGap(10, 10, 10)
                                                .addComponent(button1)
                                                .addGap(18, 18, 18)
                                                .addComponent(button2)
                                                .addGap(18, 18, 18)
                                                .addComponent(button3))
                                        .addComponent(scrollPane1, GroupLayout.PREFERRED_SIZE, 144, GroupLayout.PREFERRED_SIZE))
                                .addGap(59, 59, 59))
        );
        pack();
        setLocationRelativeTo(getOwner());

    }
}
