import type.MultiChoice;
import util.DatabaseConnector;
import type.ListenQuestion;
import type.QuestionAndAnswer;
import type.SingleChoice;

import java.awt.event.*;
import javax.swing.*;
import javax.swing.GroupLayout;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * @author 11219
 */
public class SingleInput extends JFrame {
    private JPanel panel1;
    private JPanel panel2;
    private JLabel label1;
    private JLabel label2;
    private JComboBox comboBox1;
    private JComboBox<String> comboBox2;
    private JButton button1;

    public SingleInput() {
        initComponents();
        presentSubject();
        presentType();;
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

    private void questionInput(ActionEvent e) {
        String selectedSubject = (String) comboBox1.getSelectedItem();
        String selectedType = (String) comboBox2.getSelectedItem();
        if( "单项选择题".equals(selectedType)){
            SingleChoice singleChoice = new SingleChoice(selectedSubject, selectedType, -1);
            singleChoice.setVisible(true);
            this.dispose();
        }
        else if("问答题".equals(selectedType)){
            QuestionAndAnswer questionAndAnswer = new QuestionAndAnswer(selectedSubject, selectedType, -1);
            questionAndAnswer.setVisible(true);
            this.dispose();
        }
        else if("听力题".equals(selectedType)){
            ListenQuestion listenQuestion = new ListenQuestion(selectedSubject, selectedType, -1);
            listenQuestion.setVisible(true);
            this.dispose();
        }
        else if("多项选择题".equals(selectedType)){
            MultiChoice multiChoice = new MultiChoice(selectedSubject, selectedType , -1);
            multiChoice.setVisible(true);
            this.dispose();
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
    private void initComponents() {

        panel1 = new JPanel();
        panel2 = new JPanel();
        label1 = new JLabel();
        label2 = new JLabel();
        comboBox1 = new JComboBox();
        comboBox2 = new JComboBox<>();
        button1 = new JButton();

        //======== this ========
        setTitle("\u9009\u62e9\u9898\u76ee\u7c7b\u578b");
        var contentPane = getContentPane();

        //======== panel1 ========
        {

            //======== panel2 ========
            {

                //---- label1 ----
                label1.setText("\u9009\u62e9\u5b66\u79d1");

                //---- label2 ----
                label2.setText("\u9009\u62e9\u9898\u578b\uff1a");

                GroupLayout panel2Layout = new GroupLayout(panel2);
                panel2.setLayout(panel2Layout);
                panel2Layout.setHorizontalGroup(
                        panel2Layout.createParallelGroup()
                                .addGroup(panel2Layout.createSequentialGroup()
                                        .addGap(0, 73, Short.MAX_VALUE)
                                        .addGroup(panel2Layout.createParallelGroup()
                                                .addComponent(label1, GroupLayout.Alignment.TRAILING, GroupLayout.PREFERRED_SIZE, 70, GroupLayout.PREFERRED_SIZE)
                                                .addComponent(label2, GroupLayout.Alignment.TRAILING, GroupLayout.PREFERRED_SIZE, 70, GroupLayout.PREFERRED_SIZE)))
                );
                panel2Layout.setVerticalGroup(
                        panel2Layout.createParallelGroup()
                                .addGroup(panel2Layout.createSequentialGroup()
                                        .addContainerGap(11, Short.MAX_VALUE)
                                        .addComponent(label1, GroupLayout.PREFERRED_SIZE, 27, GroupLayout.PREFERRED_SIZE)
                                        .addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED)
                                        .addComponent(label2, GroupLayout.PREFERRED_SIZE, 27, GroupLayout.PREFERRED_SIZE)
                                        .addContainerGap())
                );
            }

            //---- comboBox2 ----
            comboBox2.setModel(new DefaultComboBoxModel<>(new String[] {
                    "\u5355\u9879\u9009\u62e9\u9898",
                    "\u591a\u9879\u9009\u62e9\u9898",
                    "\u586b\u7a7a\u9898",
                    "\u5224\u65ad\u9898",
                    "\u95ee\u7b54\u9898",
                    "\u542c\u529b\u9898",
                    "\u7efc\u5408\u9898"
            }));

            GroupLayout panel1Layout = new GroupLayout(panel1);
            panel1.setLayout(panel1Layout);
            panel1Layout.setHorizontalGroup(
                    panel1Layout.createParallelGroup()
                            .addGroup(panel1Layout.createSequentialGroup()
                                    .addComponent(panel2, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                                    .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                                    .addGroup(panel1Layout.createParallelGroup()
                                            .addComponent(comboBox1, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                                            .addComponent(comboBox2, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
                                    .addContainerGap(32, Short.MAX_VALUE))
            );
            panel1Layout.setVerticalGroup(
                    panel1Layout.createParallelGroup()
                            .addGroup(panel1Layout.createSequentialGroup()
                                    .addComponent(panel2, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                                    .addGap(0, 0, Short.MAX_VALUE))
                            .addGroup(panel1Layout.createSequentialGroup()
                                    .addGap(14, 14, 14)
                                    .addComponent(comboBox1, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                                    .addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED)
                                    .addComponent(comboBox2, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                                    .addContainerGap(GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
            );
        }

        //---- button1 ----
        button1.setText("\u5f55\u5165\u8bd5\u9898");
        button1.addActionListener(e -> questionInput(e));

        GroupLayout contentPaneLayout = new GroupLayout(contentPane);
        contentPane.setLayout(contentPaneLayout);
        contentPaneLayout.setHorizontalGroup(
                contentPaneLayout.createParallelGroup()
                        .addComponent(panel1, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addGroup(contentPaneLayout.createSequentialGroup()
                                .addGap(103, 103, 103)
                                .addComponent(button1)
                                .addContainerGap(103, Short.MAX_VALUE))
        );
        contentPaneLayout.setVerticalGroup(
                contentPaneLayout.createParallelGroup()
                        .addGroup(contentPaneLayout.createSequentialGroup()
                                .addComponent(panel1, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED)
                                .addComponent(button1)
                                .addGap(0, 10, Short.MAX_VALUE))
        );
        pack();
        setLocationRelativeTo(getOwner());

    }

}
