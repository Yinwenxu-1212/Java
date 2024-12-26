import util.DatabaseConnector;

import java.awt.event.*;
import java.sql.*;
import javax.swing.*;
import javax.swing.GroupLayout;
import javax.swing.table.*;

/**
 * @author 11219
 */
public class UserManage extends JFrame {

    private JScrollPane scrollPane1;
    private JTable table1;
    private JComboBox<String> comboBox1;
    private JButton button1;
    private JButton button2;
    private JButton button3;
    private JButton button4;
    private int userId;

    public UserManage(int userId) {
        initComponents();
        this.userId = userId;
        String selectedUserType = (String) comboBox1.getSelectedItem();

        if(isAdmin(userId) == 1) {
            // 设置按钮的可见性
            if ("管理员".equals(selectedUserType)) {
                button1.setVisible(false);
                button2.setVisible(false);
                button3.setVisible(false);
                button4.setVisible(true);
            } else if ("组卷人".equals(selectedUserType)) {
                button1.setVisible(true);
                button2.setVisible(true);
                button3.setVisible(true);
                button4.setVisible(false);
            }
        }else {
            comboBox1.setVisible(false);
            button4.setVisible(true);
            button1.setVisible(false);
            button2.setVisible(false);
            button3.setVisible(false);
        }

        // 更新表格数据
        loadUserData();
    }

    private void selectAdmin(ActionEvent e) {
        String selectedUserType = (String) comboBox1.getSelectedItem();
        if ("管理员".equals(selectedUserType)) {
            // 加载管理员信息的逻辑
            int admin = 1;
            table1.setModel(presentTable(admin));
        } else if ("组卷人".equals(selectedUserType)) {
            // 加载组卷人信息的逻辑
            int admin = 0;
            table1.setModel(presentTable(admin));
        }
    }

    private void updatePermission(ActionEvent e) {
        String username = JOptionPane.showInputDialog(this, "请输入用户名：");
        if (username == null || username.isEmpty()) {
            return; // 用户取消或未输入，直接返回
        }

        try (Connection connection = DatabaseConnector.connect()) {
            String updateQuery = "UPDATE login SET is_admin = ? WHERE name = ?";
            try (PreparedStatement updateStatement = connection.prepareStatement(updateQuery)) {
                updateStatement.setInt(1, 1);
                updateStatement.setString(2, username);
                int rowsAffected = updateStatement.executeUpdate();

                if (rowsAffected > 0) {
                    JOptionPane.showMessageDialog(this, "权限更新成功！");
                    // 刷新表格
                    loadUserData();
                } else {
                    JOptionPane.showMessageDialog(this, "未找到指定用户或权限更新失败！");
                }
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "数据库连接错误：" + ex.getMessage());
        }
    }
    private int isAdmin(int userId){
        try (Connection connection = DatabaseConnector.connect()) {
            String query = "SELECT is_admin FROM login WHERE id = ?";
            try (PreparedStatement statement = connection.prepareStatement(query)) {
                statement.setInt(1, userId);
                try (ResultSet resultSet = statement.executeQuery()) {
                    if (resultSet.next()) {
                        return resultSet.getInt("is_admin");
                    }
                    // 处理没有结果的情况
                    return -1;
                }
            }
        }catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "数据库连接错误：" + ex.getMessage());
        }
        return -1;
    }
    private void deleteUser(ActionEvent e){
        String username = JOptionPane.showInputDialog(this, "请输入删除的用户：");
        if (username == null || username.isEmpty()) {
            return; // 用户取消或未输入，直接返回
        }
        try (Connection connection = DatabaseConnector.connect()) {
            String deleteQuery = "DELETE FROM login WHERE name = ?";
            try (PreparedStatement statement = connection.prepareStatement(deleteQuery)) {
                statement.setString(1, username); // 获取用户名
                int rowsAffected = statement.executeUpdate();
                if (rowsAffected > 0) {
                    JOptionPane.showMessageDialog(this, "删除成功");
                    // 刷新表格
                    loadUserData();
                } else {
                    JOptionPane.showMessageDialog(this, "用户不存在");
                }
            }
        }catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "数据库连接错误：" + ex.getMessage());
        }
    }
    private void loadUserData() {
        String selectedUserType = (String) comboBox1.getSelectedItem();
        int isAdmin = "管理员".equals(selectedUserType) ? 1 : 0;
        table1.setModel(presentTable(isAdmin));
    }

    private void updateUserInfo(ActionEvent e) {

        String newPassword = JOptionPane.showInputDialog(this, "请输入新的密码：");
        // 检查用户是否取消输入
        if (newPassword == null) {
            return;
        }

        // 更新数据库中的用户信息
        try (Connection connection = DatabaseConnector.connect()) {
            String updateQuery = "UPDATE login SET password = ? WHERE id = ?";
            try (PreparedStatement updateStatement = connection.prepareStatement(updateQuery)) {
                updateStatement.setString(1, newPassword);
                updateStatement.setInt(2, userId);

                int rowsAffected = updateStatement.executeUpdate();

                if (rowsAffected > 0) {
                    JOptionPane.showMessageDialog(this, "密码修改成功！");
                    // 刷新表格
                    loadUserData();
                } else {
                    JOptionPane.showMessageDialog(this, "未找到指定用户或用户信息更新失败！");
                }
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "数据库连接错误：" + ex.getMessage());
        }
        // 更新表格数据
        loadUserData();
    }
    private DefaultTableModel presentTable(int isadmin) {
        if(isAdmin(userId)==1) {
            try (Connection connection = DatabaseConnector.connect()) {
                if (isadmin == 0) {
                    String query = "SELECT * FROM login WHERE is_admin = ?";
                    try (PreparedStatement statement = connection.prepareStatement(query)) {
                        statement.setInt(1, isadmin);
                        try (ResultSet resultSet = statement.executeQuery()) {
                            DefaultTableModel model = new DefaultTableModel();
                            ResultSetMetaData metaData = resultSet.getMetaData();
                            int columnCount = metaData.getColumnCount();

                            // 添加列名
                            for (int columnIndex = 1; columnIndex <= columnCount; columnIndex++) {
                                model.addColumn(metaData.getColumnName(columnIndex));
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
                } else {
                    String query = "SELECT * FROM login WHERE is_admin = ? AND id = ?";
                    try (PreparedStatement statement = connection.prepareStatement(query)) {
                        statement.setInt(1, isadmin);
                        statement.setInt(2, userId);
                        try (ResultSet resultSet = statement.executeQuery()) {
                            DefaultTableModel model = new DefaultTableModel();
                            ResultSetMetaData metaData = resultSet.getMetaData();
                            int columnCount = metaData.getColumnCount();

                            // 添加列名
                            for (int columnIndex = 1; columnIndex <= columnCount; columnIndex++) {
                                model.addColumn(metaData.getColumnName(columnIndex));
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
                }
            } catch (SQLException ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(this, "数据库连接错误：" + ex.getMessage());
                return new DefaultTableModel();
            }
        }
        else{
            try (Connection connection = DatabaseConnector.connect()) {
                    String query = "SELECT * FROM login WHERE id = ?";
                    try (PreparedStatement statement = connection.prepareStatement(query)) {
                        statement.setInt(1, userId);
                        try (ResultSet resultSet = statement.executeQuery()) {
                            DefaultTableModel model = new DefaultTableModel();
                            ResultSetMetaData metaData = resultSet.getMetaData();
                            int columnCount = metaData.getColumnCount();

                            // 添加列名
                            for (int columnIndex = 1; columnIndex <= columnCount; columnIndex++) {
                                model.addColumn(metaData.getColumnName(columnIndex));
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
                }catch (SQLException ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(this, "数据库连接错误：" + ex.getMessage());
                return new DefaultTableModel();
            }
        }
    }

    private void addUser(ActionEvent e) {
        String newName = JOptionPane.showInputDialog(this, "请输入用户名：");
        // 检查用户名是否已存在
        if (isUserExists(newName)) {
            JOptionPane.showMessageDialog(this, "用户已存在！");
            return;
        }
        String newPassword = JOptionPane.showInputDialog(this, "请输入密码：");

        // 检查用户是否取消输入
        if (newName == null || newPassword == null) {
            return;
        }

        // 检查用户名是否已存在
        if (isUserExists(newName)) {
            JOptionPane.showMessageDialog(this, "用户已存在！");
            return;
        }

        // 将新用户信息插入数据库
        try (Connection connection = DatabaseConnector.connect()) {
            String insertQuery = "INSERT INTO login (name, password, is_admin) VALUES (?, ?, 0)";
            try (PreparedStatement insertStatement = connection.prepareStatement(insertQuery)) {
                insertStatement.setString(1, newName);
                insertStatement.setString(2, newPassword);

                int rowsInserted = insertStatement.executeUpdate();

                if (rowsInserted > 0) {
                    JOptionPane.showMessageDialog(this, "用户添加成功！");
                    // 刷新表格
                    loadUserData();
                } else {
                    JOptionPane.showMessageDialog(this, "用户添加失败！");
                }
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "数据库连接错误：" + ex.getMessage());
        }
    }

    private boolean isUserExists(String username) {
        try (Connection connection = DatabaseConnector.connect()) {
            String query = "SELECT COUNT(*) FROM login WHERE name = ?";
            try (PreparedStatement statement = connection.prepareStatement(query)) {
                statement.setString(1, username);
                try (ResultSet resultSet = statement.executeQuery()) {
                    if (resultSet.next()) {
                        int count = resultSet.getInt(1);
                        return count > 0;
                    }
                }
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "数据库连接错误：" + ex.getMessage());
        }
        return false;
    }


    private void updateAdmin(ActionEvent e) {
        updatePermission(e);
    }

    private void comboBox1ItemStateChanged(ItemEvent e) {
        String selectedUserType = (String) comboBox1.getSelectedItem();

        // 设置按钮的可见性
        if ("管理员".equals(selectedUserType)) {
            button1.setVisible(false);
            button2.setVisible(false);
            button3.setVisible(false);
            button4.setVisible(true);
        } else if ("组卷人".equals(selectedUserType)) {
            button1.setVisible(true);
            button2.setVisible(true);
            button3.setVisible(true);
            button4.setVisible(false);
        }

        // 更新表格数据
        loadUserData();
    }
    private void initComponents() {
        scrollPane1 = new JScrollPane();
        table1 = new JTable();
        comboBox1 = new JComboBox<>();
        button1 = new JButton();
        button2 = new JButton();
        button3 = new JButton();
        button4 = new JButton();

        //======== this ========
        setName("frame0");
        setTitle("\u7528\u6237\u7ba1\u7406");
        var contentPane = getContentPane();

        //======== scrollPane1 ========
        {

            //---- table1 ----
            table1.setModel(new DefaultTableModel(
                    new Object[][] {
                    },
                    new String[] {
                            "id", "name", "password", "is_admin"
                    }
            ));
            scrollPane1.setViewportView(table1);
        }

        //---- comboBox1 ----
        comboBox1.setModel(new DefaultComboBoxModel<>(new String[] {
                "\u7ba1\u7406\u5458",
                "\u7ec4\u5377\u4eba"
        }));
        comboBox1.addActionListener(e -> selectAdmin(e));
        comboBox1.addItemListener(e -> comboBox1ItemStateChanged(e));

        //---- button1 ----
        button1.setText("\u6dfb\u52a0\u7528\u6237");
        button1.addActionListener(e -> addUser(e));

        //---- button2 ----
        button2.setText("\u5220\u9664\u7528\u6237");
        button2.addActionListener(e -> deleteUser(e));

        //---- button3 ----
        button3.setText("\u66f4\u6539\u6743\u9650");
        button3.addActionListener(e -> updateAdmin(e));

        //---- button4 ----
        button4.setText("修改密码");
        button4.addActionListener(e -> updateUserInfo(e));

        GroupLayout contentPaneLayout = new GroupLayout(contentPane);
        contentPane.setLayout(contentPaneLayout);
        contentPaneLayout.setHorizontalGroup(
                contentPaneLayout.createParallelGroup()
                        .addGroup(contentPaneLayout.createSequentialGroup()
                                .addGap(78, 78, 78)
                                .addGroup(contentPaneLayout.createParallelGroup()
                                        .addComponent(button2)
                                        .addComponent(button1)
                                        .addComponent(button3)
                                        .addComponent(button4)
                                        .addComponent(comboBox1, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
                                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED, 79, Short.MAX_VALUE)
                                .addComponent(scrollPane1, GroupLayout.PREFERRED_SIZE, 488, GroupLayout.PREFERRED_SIZE))
        );
        contentPaneLayout.setVerticalGroup(
                contentPaneLayout.createParallelGroup()
                        .addGroup(contentPaneLayout.createSequentialGroup()
                                .addGap(44, 44, 44)
                                .addComponent(comboBox1, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                                .addGap(64, 64, 64)
                                .addComponent(button1)
                                .addGap(18, 18, 18)
                                .addComponent(button2)
                                .addGap(18, 18, 18)
                                .addComponent(button3)
                                .addGap(18, 18, 18)
                                .addComponent(button4)
                                .addContainerGap(47, Short.MAX_VALUE))
                        .addComponent(scrollPane1, GroupLayout.Alignment.TRAILING, GroupLayout.DEFAULT_SIZE, 329, Short.MAX_VALUE)
        );
        pack();
        setLocationRelativeTo(getOwner());
    }

}
