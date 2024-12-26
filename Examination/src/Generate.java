import util.ImagePanel;

import java.awt.event.*;
import javax.swing.*;
import javax.swing.GroupLayout;

/**
 * @author 11219
 */
public class Generate extends JFrame {
    private JMenuBar menuBar1;
    private JButton button1;
    private JButton button2;
    private JButton button3;
    private JButton button4;
    private ImagePanel imagePanel;
    private int userId;
    public Generate(int userId) {
        initComponents();
        this.userId = userId;
    }

    private void correctPassword(ActionEvent e) {
        UserManage userManage = new UserManage(userId);
        JInternalFrame internalFrame = new JInternalFrame("密码修改", true, true, true, true);
        internalFrame.getContentPane().add(userManage.getContentPane());
        internalFrame.setSize(userManage.getSize());
        internalFrame.setVisible(true);
        imagePanel.add(internalFrame);
    }
    private void manualGenerate(ActionEvent e) {
        ManualGenerate manualGenerate = new ManualGenerate();
        JInternalFrame internalFrame = new JInternalFrame("手动组卷", true, true, true, true);
        internalFrame.getContentPane().add(manualGenerate.getContentPane());
        internalFrame.setSize(manualGenerate.getSize());
        internalFrame.setVisible(true);
        imagePanel.add(internalFrame);
    }

    private void autoGenerate(ActionEvent e) {
        AutoGenerate autoGenerate = new AutoGenerate();
        JInternalFrame internalFrame = new JInternalFrame("自动组卷", true, true, true, true);
        internalFrame.getContentPane().add(autoGenerate.getContentPane());
        internalFrame.setSize(autoGenerate.getSize());
        internalFrame.setVisible(true);
        imagePanel.add(internalFrame);
    }
    private void depart(ActionEvent e) {
        System.exit(0);
    }

    private void initComponents() {
        menuBar1 = new JMenuBar();
        button1 = new JButton();
        button2 = new JButton();
        button3 = new JButton();
        button4 = new JButton();
        imagePanel = new ImagePanel("D:\\project\\Examination\\image\\sky.jpg");

        //======== this ========
        setTitle("\u7ec4\u5377\u4eba\u9875\u9762");
        var contentPane = getContentPane();

        //======== menuBar1 ========
        {

            //---- button4 ----
            button4.setText("密码修改");
            button4.addActionListener(e -> correctPassword(e));
            menuBar1.add(button4);

            //---- button1 ----
            button1.setText("手动组卷");
            button1.addActionListener(e -> manualGenerate(e));
            menuBar1.add(button1);

            //---- button2 ----
            button2.setText("自动组卷");
            button2.addActionListener(e -> autoGenerate(e));
            menuBar1.add(button2);

            //---- button3 ----
            button3.setText("退出");
            button3.addActionListener(e -> depart(e));
            menuBar1.add(button3);
        }
        setJMenuBar(menuBar1);

        GroupLayout contentPaneLayout = new GroupLayout(contentPane);
        contentPane.setLayout(contentPaneLayout);
        contentPaneLayout.setHorizontalGroup(
                contentPaneLayout.createParallelGroup()
                        .addComponent(imagePanel, GroupLayout.DEFAULT_SIZE, 838, Short.MAX_VALUE)
        );
        contentPaneLayout.setVerticalGroup(
                contentPaneLayout.createParallelGroup()
                        .addComponent(imagePanel, GroupLayout.DEFAULT_SIZE, 463, Short.MAX_VALUE)
        );
        pack();
        setLocationRelativeTo(getOwner());
    }

}
