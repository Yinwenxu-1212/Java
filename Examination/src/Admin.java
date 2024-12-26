import util.ImagePanel;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.GroupLayout;

/**
 * @author 11219
 */
public class Admin extends JFrame {
    private JMenuBar menuBar1;
    private JMenu menu1;
    private JMenu menu2;
    private JMenuItem menuItem1;
    private JMenuItem menuItem2;
    private JMenu menu3;
    private JMenuItem menuItem3;
    private JMenuItem menuItem7;
    private JMenuItem menuItem6;
    private JMenuItem menuItem4;
    private JMenuItem menuItem5;
    private JMenuItem menuItem8;
    private JButton button1;
    private ImagePanel imagePanel;
    private int userId;
    public Admin(int userId) {
        initComponents();
        this.userId = userId;
    }

    private void userManage(MouseEvent e) {
        UserManage usermanage = new UserManage(userId);
        JInternalFrame internalFrame = new JInternalFrame("用户管理", true, true, true, true);
        internalFrame.getContentPane().add(usermanage.getContentPane());
        internalFrame.setSize(usermanage.getSize());
        internalFrame.setVisible(true);
        imagePanel.add(internalFrame);
    }

    private void subjectManage(ActionEvent e) {
        SubjectManage subjectManage = new SubjectManage();
        subjectManage.setVisible(true);
    }

    private void topicManage(ActionEvent e) {
        TopicManage knowledgeManage = new TopicManage();
        knowledgeManage.setVisible(true);
    }

    private void singleInput(ActionEvent e) {
        SingleInput singleInput = new SingleInput();
        singleInput.setVisible(true);
    }

    private void depart(ActionEvent e) {
        System.exit(0);
    }

    private void questionManage(ActionEvent e) {
        QuestionManage questionManage = new QuestionManage();
        JInternalFrame internalFrame = new JInternalFrame("题目管理", true, true, true, true);
        internalFrame.getContentPane().add(questionManage.getContentPane());
        internalFrame.setSize(questionManage.getSize());
        internalFrame.setVisible(true);
        imagePanel.add(internalFrame);
    }

    private void questionSearch(ActionEvent e) {
        QuestionSearch questionSearch = new QuestionSearch();
        JInternalFrame internalFrame = new JInternalFrame("题目搜索", true, true, true, true);
        internalFrame.getContentPane().add(questionSearch.getContentPane());
        internalFrame.setSize(questionSearch.getSize());
        internalFrame.setVisible(true);
        imagePanel.add(internalFrame);
    }

    private void questionOutput(ActionEvent e) {
        ManualGenerate manualGenerate = new ManualGenerate();
        JInternalFrame internalFrame = new JInternalFrame("题目导出", true, true, true, true);
        internalFrame.getContentPane().add(manualGenerate.getContentPane());
        internalFrame.setSize(manualGenerate.getSize());
        internalFrame.setVisible(true);
        imagePanel.add(internalFrame);
    }
    private void templateInput(ActionEvent e) {
        TemplateInput templateInput = new TemplateInput();
        templateInput.setVisible(true);
    }

    private void directInput(ActionEvent e) {
        DirectInput directInput = new DirectInput();
        directInput.setVisible(true);
    }

    private void initComponents() {
        menuBar1 = new JMenuBar();
        menu1 = new JMenu();
        menu2 = new JMenu();
        menuItem1 = new JMenuItem();
        menuItem2 = new JMenuItem();
        menu3 = new JMenu();
        menuItem3 = new JMenuItem();
        menuItem7 = new JMenuItem();
        menuItem6 = new JMenuItem();
        menuItem4 = new JMenuItem();
        menuItem5 = new JMenuItem();
        menuItem8 = new JMenuItem();
        button1 = new JButton();
        imagePanel = new ImagePanel("D:\\project\\Examination\\image\\sky.jpg");

        //======== this ========
        setTitle("\u7ba1\u7406\u5458\u9875\u9762");
        setBackground(new Color(0x0a6fb2));
        var contentPane = getContentPane();

        //======== menuBar1 ========
        {

            //======== menu1 ========
            {
                menu1.setText("\u7528\u6237\u7ba1\u7406");
                menu1.addMouseListener(new MouseAdapter() {
                    @Override
                    public void mouseClicked(MouseEvent e) {
                        userManage(e);
                    }
                });
            }
            menuBar1.add(menu1);

            //======== menu2 ========
            {
                menu2.setText("\u9898\u5e93\u7ba1\u7406");

                //---- menuItem1 ----
                menuItem1.setText("\u79d1\u76ee\u7ba1\u7406");
                menuItem1.addActionListener(e -> subjectManage(e));
                menu2.add(menuItem1);

                //---- menuItem2 ----
                menuItem2.setText("\u77e5\u8bc6\u70b9\u7ba1\u7406");
                menuItem2.addActionListener(e -> topicManage(e));
                menu2.add(menuItem2);

                //======== menu3 ========
                {
                    menu3.setText("\u9898\u76ee\u5f55\u5165");

                    //---- menuItem3 ----
                    menuItem3.setText("\u5355\u9898\u5f55\u5165");
                    menuItem3.addActionListener(e -> singleInput(e));
                    menu3.add(menuItem3);

                    //---- menuItem7 ----
                    menuItem7.setText("\u6a21\u677f\u5f55\u5165");
                    menuItem7.addActionListener(e -> templateInput(e));
                    menu3.add(menuItem7);

                    //---- menuItem6 ----
                    menuItem6.setText("\u76f4\u63a5\u5f55\u5165");
                    menuItem6.addActionListener(e -> directInput(e));
                    menu3.add(menuItem6);
                }
                menu2.add(menu3);

                //---- menuItem4 ----
                menuItem4.setText("\u9898\u76ee\u7ba1\u7406");
                menuItem4.addActionListener(e -> questionManage(e));
                menu2.add(menuItem4);

                //---- menuItem5 ----
                menuItem5.setText("\u9898\u76ee\u641c\u7d22");
                menuItem5.addActionListener(e -> questionSearch(e));
                menu2.add(menuItem5);

                //---- menuItem8 ----
                menuItem8.setText("\u9898\u76ee\u5bfc\u51fa");
                menuItem8.addActionListener(e -> questionOutput(e));
                menu2.add(menuItem8);
            }
            menuBar1.add(menu2);

            //---- button1 ----
            button1.setText("\u9000\u51fa");
            button1.addActionListener(e -> depart(e));
            menuBar1.add(button1);
        }
        setJMenuBar(menuBar1);

        GroupLayout contentPaneLayout = new GroupLayout(contentPane);
        contentPane.setLayout(contentPaneLayout);
        contentPaneLayout.setHorizontalGroup(
                contentPaneLayout.createParallelGroup()
                        .addComponent(imagePanel, GroupLayout.DEFAULT_SIZE, 773, Short.MAX_VALUE)
        );
        contentPaneLayout.setVerticalGroup(
                contentPaneLayout.createParallelGroup()
                        .addComponent(imagePanel, GroupLayout.DEFAULT_SIZE, 383, Short.MAX_VALUE)
        );
        pack();
        setLocationRelativeTo(getOwner());
    }
}
