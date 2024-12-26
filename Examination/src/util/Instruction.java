package util;

import javax.swing.*;
import javax.swing.GroupLayout;

/**
 * @author 11219
 */
public class Instruction extends JFrame {
    private JTabbedPane tabbedPane1;
    private JScrollPane scrollPane1;
    private JTextArea textArea1;
    private JScrollPane scrollPane2;
    private JTextArea textArea2;
    public Instruction() {
        initComponents();
    }

    private void initComponents() {
        tabbedPane1 = new JTabbedPane();
        scrollPane1 = new JScrollPane();
        textArea1 = new JTextArea();
        scrollPane2 = new JScrollPane();
        textArea2 = new JTextArea();

        //======== this ========
        setTitle("\u6a21\u677f\u5bfc\u5165\u4f7f\u7528\u8bf4\u660e");
        var contentPane = getContentPane();

        //======== tabbedPane1 ========
        {

            //======== scrollPane1 ========
            {

                //---- textArea1 ----
                textArea1.setText("【单项选择题】\n" +
                        "1.（5分）选出与“向使三国各爱其地”的“爱”意义相同的一项是\n" +
                        "A. 秦爱纷奢\n" +
                        "B. 齐国虽褊小，我何爱一牛\n" +
                        "C. 予独爱莲之出淤泥而不染\n" +
                        "D. 爱而不见，搔首踯躅\n" +
                        "答案：B\n" +
                        "学科：语文\n" +
                        "知识点：文言文\n" +
                        "难度：难\n" +
                        "图片路径：D:\\project\\Examination\\image\\directory\\1701590185482_屏幕截图 2023-07-11 212552.png\n" +
                        "音频路径：null\n\n" +
                        "【多项选择题】\n" +
                        "2.（5分）对于过氧化氢（H2O2），以下哪些描述是正确的？\n" +
                        "A. 氧的电子构型为1s²2s²2p⁴。\n" +
                        "B. 氧通常以O₂的形式存在，是一种双原子分子。\n" +
                        "C. 氧的电负性较低，不容易与其他元素形成化合物。\n" +
                        "D. 过氧化氢是一种强氧化剂。\n" +
                        "答案：B,C\n" +
                        "学科：化学\n" +
                        "知识点：无机\n" +
                        "难度：易\n" +
                        "图片路径：null\n" +
                        "音频路径：null\n\n" +
                        "【听力题】\n" +
                        "3.（3分）Where must the puma have come from?\n" +
                        "A. the zoo\n" +
                        "B. the village\n" +
                        "C. the park\n" +
                        "D. America\n" +
                        "答案：A\n" +
                        "学科：英语\n" +
                        "知识点：听力\n" +
                        "难度：难\n" +
                        "图片路径：null\n" +
                        "音频路径：D:\\project\\Examination\\audio\\directory\\1702382108517_1.mp3\n\n" +
                        "【问答题】\n" +
                        "4.（5分）“夫人之相与也，俯而合同焉；死而不相离，同穴之木，心而已矣。夫唯桐之木、薄言巧辩而相成也。”请简要解释文中\"同穴之木，心而已矣\"这句话的意思。\n" +
                        "答案：\"同穴之木，心而已矣\"这句话的意思是，像同处一个洞穴的两棵树一样，它们的根相交在一起，心意已经非常合一了，表达了深厚的感情或默契。\n" +
                        "学科：语文\n" +
                        "知识点：文言文\n" +
                        "难度：难\n" +
                        "图片路径：null\n" +
                        "音频路径：null");
                scrollPane1.setViewportView(textArea1);
            }
            tabbedPane1.addTab("\u8f93\u5165\u8303\u4f8b", scrollPane1);

            //======== scrollPane2 ========
            {

                //---- textArea2 ----
                textArea2.setText("1.题号后用括号加上分值，然后加入题干部分\n" +
                        "2.选择题或者听力题输入选项，一行一个选项\n" +
                        "3.”答案：“后录入答案\n" +
                        "4.”学科：“后录入学科\n" +
                        "5.”难度：“后录入难度，难度分为（易、中、难）\n" +
                        "6.如果有图片，在”图片路径：“后录入，否则录入null；音频同理\n" +
                        "7.问答题和填空题的录入方式相同\n" +
                        "8.每道题之间空一行");
                scrollPane2.setViewportView(textArea2);
            }
            tabbedPane1.addTab("\u8f93\u5165\u89c4\u8303", scrollPane2);
        }

        GroupLayout contentPaneLayout = new GroupLayout(contentPane);
        contentPane.setLayout(contentPaneLayout);
        contentPaneLayout.setHorizontalGroup(
                contentPaneLayout.createParallelGroup()
                        .addGroup(contentPaneLayout.createSequentialGroup()
                                .addContainerGap()
                                .addComponent(tabbedPane1, GroupLayout.DEFAULT_SIZE, 491, Short.MAX_VALUE)
                                .addContainerGap())
        );
        contentPaneLayout.setVerticalGroup(
                contentPaneLayout.createParallelGroup()
                        .addComponent(tabbedPane1, GroupLayout.Alignment.TRAILING, GroupLayout.DEFAULT_SIZE, 399, Short.MAX_VALUE)
        );
        pack();
        setLocationRelativeTo(getOwner());
    }
}
