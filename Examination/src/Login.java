import entity.User;
import util.DatabaseConnector;
import util.GetId;

import java.awt.event.*;
import java.sql.*;
import java.util.Arrays;
import javax.swing.*;
import javax.swing.GroupLayout;

/**
 * @author 11219
 */
public class Login extends JFrame {
    private JPanel panel1;
    private JLabel label1;
    private JLabel label2;
    private JLabel label3;
    private JTextField textField1;
    private JPasswordField passwordField1;
    private JPanel panel2;
    private JButton button3;
    private JButton button2;
    private JButton button1;
    public Login() {
        initComponents();
    }
    private void register(MouseEvent e) {
        new Register().setVisible(true);
    }

    private void handleLoginResult(User user) {
        if (user.getId() == -1) {
            JOptionPane.showMessageDialog(this, "用户名不存在，请重试！");
        } else if (user.getId() == -2) {
            JOptionPane.showMessageDialog(this, "密码错误，请重试！");
        } else if (user.getId() == -3) {
            JOptionPane.showMessageDialog(this, "数据库连接错误，请稍后重试！");
        } else {
            JOptionPane.showMessageDialog(this, "登录成功！");

            this.dispose();
        }
    }
    private User checkLogin(String username, String password) {
        try (Connection connection = DatabaseConnector.connect()) {
            String query = "SELECT * FROM login WHERE name = ?";
            try (PreparedStatement statement = connection.prepareStatement(query)) {
                statement.setString(1, username);

                try (ResultSet resultSet = statement.executeQuery()) {
                    // 如果结果集中没有匹配的记录，说明用户名不存在
                    if (!resultSet.next()) {
                        textField1.setText("");
                        passwordField1.setText("");
                        return new User(-1, "", "", false);
                    }

                    // 检查密码是否正确
                    String storedPassword = resultSet.getString("password");
                    if (password.equals(storedPassword)) {
                        // 用户名和密码匹配成功，返回User对象
                        return new User(
                                resultSet.getInt("id"),
                                resultSet.getString("name"),
                                resultSet.getString("password"),
                                resultSet.getBoolean("is_admin")
                        );
                    } else {
                        textField1.setText("");
                        passwordField1.setText("");
                        // 密码错误
                        return new User(-2, "", "", false);
                    }
                }
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
            // 数据库连接错误
            return new User(-3, "", "", false);
        }
    }

    private void depart(ActionEvent e) {
        System.exit(0);
    }

    // 清空密码字段数组的方法
    private void clearPassword(char[] passwordChars) {
        Arrays.fill(passwordChars, '0');
    }

    private void login(MouseEvent e) {
        // 获取用户输入的用户名和密码
        String username = textField1.getText();
        char[] passwordChars = passwordField1.getPassword();
        String password = new String(passwordChars);

        // 连接数据库，查询用户信息
        User user = checkLogin(username, password);

        // 处理登录结果
        handleLoginResult(user);

        // 清空密码字段数组
        clearPassword(passwordChars);

        // 根据用户权限跳转到不同界面
        if (user.isAdmin()) {
            new Admin(GetId.getUserId(username)).setVisible(true);
        } else if (!user.isAdmin() && user.getId() != -1 && user.getId() != -2) {
            new Generate(GetId.getUserId(username)).setVisible(true);
        }
    }

    private void initComponents() {
        panel1 = new JPanel();
        label1 = new JLabel();
        label2 = new JLabel();
        label3 = new JLabel();
        textField1 = new JTextField();
        passwordField1 = new JPasswordField();
        panel2 = new JPanel();
        button3 = new JButton();
        button2 = new JButton();
        button1 = new JButton();

        //======== this ========
        setTitle("\u9898\u5e93\u4e0e\u7ec4\u5377\u7cfb\u7edf");
        var contentPane = getContentPane();

        //======== panel1 ========
        {
            panel1.setBackground(UIManager.getColor("BookmarkMnemonicAvailable.background"));

            //---- label1 ----
            label1.setText("\u9898\u5e93\u4e0e\u7ec4\u5377\u7cfb\u7edf");
            label1.setFont(label1.getFont().deriveFont(label1.getFont().getSize() + 6f));
            label1.setIcon(new ImageIcon("D:\\project\\Examination\\image\\\u8bd5\u5377.png"));

            GroupLayout panel1Layout = new GroupLayout(panel1);
            panel1.setLayout(panel1Layout);
            panel1Layout.setHorizontalGroup(
                    panel1Layout.createParallelGroup()
                            .addGroup(GroupLayout.Alignment.TRAILING, panel1Layout.createSequentialGroup()
                                    .addContainerGap(143, Short.MAX_VALUE)
                                    .addComponent(label1)
                                    .addGap(119, 119, 119))
            );
            panel1Layout.setVerticalGroup(
                    panel1Layout.createParallelGroup()
                            .addGroup(GroupLayout.Alignment.TRAILING, panel1Layout.createSequentialGroup()
                                    .addContainerGap(35, Short.MAX_VALUE)
                                    .addComponent(label1)
                                    .addGap(30, 30, 30))
            );
        }

        //---- label2 ----
        label2.setText("\u7528\u6237\u540d\uff1a");
        label2.setLabelFor(textField1);
        label2.setIcon(new ImageIcon("D:\\project\\Examination\\image\\\u7528\u6237.png"));

        //---- label3 ----
        label3.setText("\u5bc6\u7801\uff1a");
        label3.setLabelFor(passwordField1);
        label3.setIcon(new ImageIcon("D:\\project\\Examination\\image\\\u5bc6\u7801.png"));

        //======== panel2 ========
        {
            panel2.setBackground(UIManager.getColor("BookmarkMnemonicAvailable.background"));

            //---- button3 ----
            button3.setText("\u767b\u5f55");
            button3.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    login(e);
                }
            });

            //---- button2 ----
            button2.setText("\u9000\u51fa");
            button2.addActionListener(e -> depart(e));

            //---- button1 ----
            button1.setText("\u6ce8\u518c");
            button1.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    register(e);
                }
            });

            GroupLayout panel2Layout = new GroupLayout(panel2);
            panel2.setLayout(panel2Layout);
            panel2Layout.setHorizontalGroup(
                    panel2Layout.createParallelGroup()
                            .addGroup(panel2Layout.createSequentialGroup()
                                    .addContainerGap()
                                    .addComponent(button3)
                                    .addGap(45, 45, 45)
                                    .addComponent(button2)
                                    .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED, 48, Short.MAX_VALUE)
                                    .addComponent(button1))
            );
            panel2Layout.setVerticalGroup(
                    panel2Layout.createParallelGroup()
                            .addGroup(panel2Layout.createSequentialGroup()
                                    .addContainerGap()
                                    .addGroup(panel2Layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                                            .addComponent(button3)
                                            .addComponent(button1)
                                            .addComponent(button2))
                                    .addContainerGap(GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
            );
        }

        GroupLayout contentPaneLayout = new GroupLayout(contentPane);
        contentPane.setLayout(contentPaneLayout);
        contentPaneLayout.setHorizontalGroup(
                contentPaneLayout.createParallelGroup()
                        .addGroup(contentPaneLayout.createSequentialGroup()
                                .addComponent(panel1, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                                .addGap(0, 0, Short.MAX_VALUE))
                        .addGroup(contentPaneLayout.createSequentialGroup()
                                .addGap(77, 77, 77)
                                .addGroup(contentPaneLayout.createParallelGroup()
                                        .addComponent(label2)
                                        .addComponent(label3))
                                .addGap(27, 27, 27)
                                .addGroup(contentPaneLayout.createParallelGroup(GroupLayout.Alignment.LEADING, false)
                                        .addComponent(textField1, GroupLayout.DEFAULT_SIZE, 204, Short.MAX_VALUE)
                                        .addComponent(passwordField1, GroupLayout.DEFAULT_SIZE, 204, Short.MAX_VALUE))
                                .addContainerGap(63, Short.MAX_VALUE))
                        .addGroup(GroupLayout.Alignment.TRAILING, contentPaneLayout.createSequentialGroup()
                                .addContainerGap(58, Short.MAX_VALUE)
                                .addComponent(panel2, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                                .addGap(53, 53, 53))
        );
        contentPaneLayout.setVerticalGroup(
                contentPaneLayout.createParallelGroup()
                        .addGroup(contentPaneLayout.createSequentialGroup()
                                .addComponent(panel1, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                                .addGap(18, 18, 18)
                                .addGroup(contentPaneLayout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                                        .addComponent(label2)
                                        .addComponent(textField1, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
                                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(contentPaneLayout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                                        .addComponent(passwordField1, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                                        .addComponent(label3, GroupLayout.PREFERRED_SIZE, 29, GroupLayout.PREFERRED_SIZE))
                                .addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED)
                                .addComponent(panel2, GroupLayout.PREFERRED_SIZE, 40, GroupLayout.PREFERRED_SIZE)
                                .addContainerGap(24, Short.MAX_VALUE))
        );
        pack();
        setLocationRelativeTo(getOwner());
    }
}
