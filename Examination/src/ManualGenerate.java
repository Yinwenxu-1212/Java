import org.apache.batik.swing.JSVGCanvas;
import org.apache.batik.swing.svg.SVGUserAgentAdapter;
import org.apache.batik.transcoder.TranscoderInput;
import org.apache.batik.transcoder.TranscoderOutput;
import org.apache.batik.transcoder.image.PNGTranscoder;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.util.Units;
import org.apache.poi.xwpf.usermodel.*;
import util.DatabaseConnector;
import util.GetId;

import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.sql.*;
import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.GroupLayout;
import javax.swing.table.DefaultTableModel;
import javax.swing.text.BadLocationException;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;

/**
 * @author 11219
 */
public class ManualGenerate extends JFrame {
    private String svgImagePath;
    private String answer;
    private int questionTypeId;
    private int questionId;
    private String optionA;
    private String optionB;
    private String optionC;
    private String optionD;
    private String imagePath;
    private String audioPath;
    private int questionCount = 1;
    private JSplitPane splitPane2;
    private JPanel panel1;
    private JLabel label1;
    private JComboBox comboBox1;
    private JLabel label2;
    private JComboBox comboBox2;
    private JLabel label3;
    private JComboBox comboBox3;
    private JScrollPane scrollPane1;
    private JTable table1;
    private JPanel panel3;
    private JButton button6;
    private JButton button7;
    private JButton button2;
    private JSplitPane splitPane3;
    private JPanel panel2;
    private JPanel panel4;
    private JLabel label9;
    private JScrollPane scrollPane2;
    private JTable table2;
    private JButton button1;
    private JTabbedPane tabbedPane1;
    private JPanel panel5;
    private JButton button8;
    private JLabel label10;
    private JTextField textField1;
    private static final int SELECTION_COLUMN_INDEX = 0;

    public ManualGenerate() {
        initComponents();
        presentSubject();
        presentType();
        presentTopic();
        comboBox1.addActionListener(e -> {
            performQuestion(e);
            presentTopic();
            updatePreviewText();
        });
        comboBox2.addActionListener(e -> {
            performQuestion(e);
            updatePreviewText();
        });
        comboBox3.addActionListener(e -> {
            performQuestion(e);
            updatePreviewText();
        });
        button6.addActionListener(e -> selectAllQuestions());
        table1.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                selectQuestion(e);
            }
        });
        button1.addActionListener(e -> deleteSelectedQuestions());
        button8.addActionListener(e -> button8ActionPerformed(e));

    }

    private void exportPaperToDoc() {
        try {
            // 提示用户选择保存路径
            JFileChooser fileChooser = new JFileChooser();
            fileChooser.setDialogTitle("选择保存路径");
            fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);

            int userSelection = fileChooser.showSaveDialog(this);

            if (userSelection == JFileChooser.APPROVE_OPTION) {
                // 用户选择的保存路径
                String savePath = fileChooser.getSelectedFile().getAbsolutePath();

                // 问卷名来自 textField1
                String paperName = textField1.getText();
                if (paperName == null || paperName.trim().isEmpty()) {
                    JOptionPane.showMessageDialog(this, "试卷名称为空！");
                    return; // 如果试卷名称为空，则不执行导出过程
                }

                // 创建保存试卷和音频文件的文件夹
                Path folderPath = Paths.get(savePath, paperName);
                Files.createDirectories(folderPath);

                // 创建 Word 文档
                XWPFDocument document = new XWPFDocument();
                XWPFParagraph titleParagraph = document.createParagraph();
                titleParagraph.setAlignment(ParagraphAlignment.CENTER);
                XWPFRun titleRun = titleParagraph.createRun();
                titleRun.setBold(true);
                titleRun.setFontSize(16);
                titleRun.setText(paperName);

                DefaultTableModel model2 = (DefaultTableModel) table2.getModel();

                // 遍历 model2 中的数据，将其添加到文档
                for (int i = 0; i < model2.getRowCount(); i++) {
                    int questionId = GetId.getQuestionID(model2.getValueAt(i, 1).toString());
                    getDetail(questionId);

                    if (imagePath != null && !imagePath.isEmpty() && !imagePath.equals("null")) {
                        if (isSVG(imagePath)) {
                            svgImagePath = convertSvgToPngAndGetPath(imagePath, folderPath.toString());
                        }
                    }

                    // 添加标题和内容到文档
                    appendDetailToDocument(document, model2.getValueAt(i, 0).toString(), model2.getValueAt(i, 1).toString(), questionId, Integer.parseInt(model2.getValueAt(i, 3).toString()));

                    // 复制音频文件到文件夹
                    if (audioPath != null && !audioPath.isEmpty()) {
                        // 获取音频文件名称，并在文件名中包含题号
                        String audioFileName = String.format("Q%d_%s", model2.getValueAt(i, 0), Paths.get(audioPath).getFileName().toString());
                        String targetAudioPath = Paths.get(folderPath.toString(), audioFileName).toString();
                        Files.copy(Paths.get(audioPath), Paths.get(targetAudioPath), StandardCopyOption.REPLACE_EXISTING);
                    }
                }

                // 将答案预览文本添加到文档
                JTextArea previewTextArea2 = getPreviewTextArea2();
                appendTextToDocument(document, "答案", previewTextArea2.getText());

                // 保存文档到文件
                Path filePath = Paths.get(folderPath.toString(), paperName + ".docx");
                try (FileOutputStream out = new FileOutputStream(filePath.toFile())) {
                    document.write(out);
                }

                JOptionPane.showMessageDialog(this, "文档导出成功，保存路径：" + filePath);
            }
        } catch (IOException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "导出失败：" + e.getMessage());
        }
    }

    private void appendTextToDocument(XWPFDocument document, String title, String text) {
        if (text != null && !text.isEmpty()) {
            // 添加标题
            XWPFParagraph titleParagraph = document.createParagraph();
            titleParagraph.setAlignment(ParagraphAlignment.CENTER);
            XWPFRun titleRun = titleParagraph.createRun();
            titleRun.setBold(true);
            titleRun.setFontSize(16);
            titleRun.setText(title);

            // 添加正文
            XWPFParagraph paragraph = document.createParagraph();
            paragraph.setAlignment(ParagraphAlignment.LEFT);
            XWPFRun run = paragraph.createRun();

            // 将文本按行分割，并逐行处理
            String[] lines = text.split("\n");
            for (String line : lines) {
                // 添加新行
                paragraph = document.createParagraph();
                paragraph.setAlignment(ParagraphAlignment.LEFT);
                run = paragraph.createRun();

                // 设置文本
                run.setText(line);
            }
        }
    }


    private JTextArea getPreviewTextArea2() {
        Component component = tabbedPane1.getComponentAt(1);
        if (component instanceof JPanel) {
            JPanel panel = (JPanel) component;
            for (Component subComponent : panel.getComponents()) {
                if (subComponent instanceof JScrollPane) {
                    JScrollPane scrollPane = (JScrollPane) subComponent;
                    Component viewComponent = scrollPane.getViewport().getView();
                    if (viewComponent instanceof JTextArea) {
                        return (JTextArea) viewComponent;
                    }
                }
            }
        }
        return null;
    }

    private void appendDetailToDocument(XWPFDocument document, String questionNumber, String questionText, int questionId, int score) {
        // 添加标题和文本
        XWPFParagraph questionParagraph = document.createParagraph();
        questionParagraph.setAlignment(ParagraphAlignment.LEFT);
        XWPFRun questionRun = questionParagraph.createRun();
        questionRun.setBold(false);
        questionRun.setFontSize(12);
        questionRun.setText(questionNumber + ". " + "(" + score + "分)" + questionText);

        // 获取题目的详细信息
        getDetail(questionId);

        // 添加图片
        if (imagePath != null && !imagePath.isEmpty()) {
            try {
                if(!isSVG(imagePath)) {
                    addImageToDocument(document, imagePath);
                }else{
                    addImageToDocument(document, svgImagePath);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        // 添加题目选项或答案等详细信息
        if (questionTypeId == 1 || questionTypeId == 6 || questionTypeId == 2) {
            addOptionDetailsToDocument(document);
        }
    }

    private void addImageToDocument(XWPFDocument document, String imagePath) throws IOException {
        try (FileInputStream imageStream = new FileInputStream(imagePath)) {
            // 创建段落
            XWPFParagraph paragraph = document.createParagraph();

            // 获取原始图片的宽度和高度
            BufferedImage bufferedImage = ImageIO.read(new File(imagePath));
            // 设置目标宽度和高度
            int targetWidthEMU = Units.toEMU(200); // 设置目标宽度
            int targetHeightEMU = Units.toEMU(150); // 设置目标高度

            // 插入图片，并设置新的宽度和高度
            paragraph.createRun().addPicture(imageStream, XWPFDocument.PICTURE_TYPE_JPEG, "My Picture", targetWidthEMU, targetHeightEMU);
        } catch (InvalidFormatException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "InvalidFormatException: " + e.getMessage());
        } catch (IOException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "IOException: " + e.getMessage());
        }
    }



    private String convertSvgToPngAndGetPath(String svgPath, String savePath) throws IOException {
        if (!isSVG(svgPath)) {
            throw new IOException("文件不是SVG格式");
        }

        try {
            // 创建PNG转码器
            PNGTranscoder transcoder = new PNGTranscoder();

            // 读取SVG文件
            File svgFile = new File(svgPath);
            InputStream inputStream = new FileInputStream(svgFile);

            // 使用原始SVG文件的名称作为基础，更改文件扩展名为.png
            String svgFileName = svgFile.getName();
            String pngImagePath = savePath + File.separator + svgFileName.substring(0, svgFileName.lastIndexOf(".")) + ".png";
            OutputStream outputStream = new FileOutputStream(pngImagePath);
            TranscoderInput input = new TranscoderInput(inputStream);
            TranscoderOutput output = new TranscoderOutput(outputStream);

            // 执行转码
            transcoder.transcode(input, output);

            // 关闭输入和输出流
            inputStream.close();
            outputStream.close();

            System.out.println("SVG转换为PNG成功：" + pngImagePath);
            return pngImagePath;
        } catch (Exception e) {
            e.printStackTrace();
            throw new IOException("SVG转换为PNG失败：" + e.getMessage());
        }
    }

    private boolean isSVG(String imagePath) {
        return imagePath.toLowerCase().endsWith(".svg");
    }

    private void addOptionDetailsToDocument(XWPFDocument document) {
        // 根据需要添加选项的详细信息，比如 optionA、optionB、optionC、optionD 等
        // 你可以根据需要调整具体的样式和格式
        XWPFParagraph optionParagraph = document.createParagraph();
        optionParagraph.setAlignment(ParagraphAlignment.LEFT);
        XWPFRun optionRun = optionParagraph.createRun();
        optionRun.setFontSize(12);

        optionRun.setText("A. " + optionA);
        optionRun.addCarriageReturn();
        optionRun.setText("B. " + optionB);
        optionRun.addCarriageReturn();
        optionRun.setText("C. " + optionC);
        optionRun.addCarriageReturn();
        optionRun.setText("D. " + optionD);
        optionRun.addCarriageReturn();
    }



    private void button8ActionPerformed(ActionEvent e) {
        try {
            exportPaperToDoc();
        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "操作失败：" + ex.getMessage());
        }
    }

    private void deleteSelectedQuestions() {
        DefaultTableModel model2 = (DefaultTableModel) table2.getModel();
        int[] selectedRows = table2.getSelectedRows();

        for (int i = selectedRows.length - 1; i >= 0; i--) {
            model2.removeRow(selectedRows[i]);
        }

        // 重新排序题号
        updateQuestionNumbers(model2);

        // 更新试题预览文本
        updatePreviewText();
    }

    private void updateQuestionNumbers(DefaultTableModel model) {
        for (int i = 0; i < model.getRowCount(); i++) {
            model.setValueAt(i + 1, i, 0);  // 重新设置题号
        }
        model.fireTableDataChanged();
    }
    private void updatePreviewText() {
        tabbedPane1.removeAll();

        // 创建带有滚动条的JTextPane
        JTextPane previewPane = new JTextPane();
        previewPane.setEditable(false);  // 设置为不可编辑
        JScrollPane previewScrollPane = new JScrollPane(previewPane);

        // 设置垂直滚动条自动出现
        previewScrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);

        // 将带有滚动条的JScrollPane添加到JPanel中
        JPanel previewPanel1 = new JPanel(new BorderLayout());
        previewPanel1.add(previewScrollPane, BorderLayout.CENTER);

        // 将带有滚动条的JPanel添加到选项卡面板中
        tabbedPane1.addTab("试题预览", previewPanel1);


        JTextArea previewTextArea2 = new JTextArea();
        JScrollPane previewScrollPane2 = new JScrollPane(previewTextArea2);
        JPanel previewPanel2 = new JPanel(new BorderLayout());
        previewPanel2.add(previewScrollPane2, BorderLayout.CENTER);
        tabbedPane1.addTab("答案预览", previewPanel2);

        DefaultTableModel model2 = (DefaultTableModel) table2.getModel();

        // 清空预览文本
        previewPane.setText("");
        previewTextArea2.setText("");

        // 遍历model2中的数据，将其添加到预览文本中
        for (int i = 0; i < model2.getRowCount(); i++) {
            questionId = GetId.getQuestionID(model2.getValueAt(i, 1).toString());
            questionTypeId = GetId.getTypeID1(model2.getValueAt(i, 1).toString());
            getDetail(questionId);
            System.out.println(imagePath);

            // 添加文本到JTextPane
            StyledDocument doc = previewPane.getStyledDocument();
            SimpleAttributeSet style = new SimpleAttributeSet();
            StyleConstants.setBold(style, false);
            StyleConstants.setFontSize(style, 14);
            try {
                doc.insertString(doc.getLength(), model2.getValueAt(i, 0) + ". " + "(" + model2.getValueAt(i, 3) + "分)" + model2.getValueAt(i, 1) + "\n", style);
                if (questionTypeId == 1 || questionTypeId == 6|| questionTypeId == 2) {
                    doc.insertString(doc.getLength(), "A. " + optionA + "\n", style);
                    doc.insertString(doc.getLength(), "B. " + optionB + "\n", style);
                    doc.insertString(doc.getLength(), "C. " + optionC + "\n", style);
                    doc.insertString(doc.getLength(), "D. " + optionD + "\n" + "\n", style);
                }
            } catch (BadLocationException e) {
                e.printStackTrace();
            }
            // 添加图像
            if (imagePath != null && !imagePath.isEmpty()) {
                try {
                    // 判断是否是 SVG 文件
                    if (imagePath.toLowerCase().endsWith(".svg")) {
                        // 创建 SVGCanvas 用于显示 SVG 图像
                        JSVGCanvas svgCanvas = new JSVGCanvas(new SVGUserAgentAdapter(), true, true);
                        svgCanvas.setURI("file:///" + imagePath);
                        previewPane.insertComponent(svgCanvas);
                    } else {
                        // 加载原始图像
                        ImageIcon originalIcon = new ImageIcon(imagePath);
                        Image originalImage = originalIcon.getImage();

                        // 计算新的宽度和高度
                        int tabbedPaneWidth = tabbedPane1.getWidth();
                        int newWidth = 2 * tabbedPaneWidth / 3;
                        int newHeight = (int) (newWidth * ((double) originalImage.getHeight(null) / originalImage.getWidth(null)));

                        // 调整图像大小
                        Image resizedImage = originalImage.getScaledInstance(newWidth, newHeight, Image.SCALE_SMOOTH);

                        // 创建图像标签，并插入到文档
                        JLabel imageLabel = new JLabel(new ImageIcon(resizedImage));
                        imageLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
                        previewPane.insertComponent(imageLabel);
                    }

                    // 在文档中添加换行符
                    previewPane.getDocument().insertString(previewPane.getDocument().getLength(), "\n\n", null);
                } catch (BadLocationException e) {
                    e.printStackTrace();
                }
            }
            previewTextArea2.append(model2.getValueAt(i, 0) + ". ");
            for (int j = 0; j < answer.length(); j++) {
                previewTextArea2.append(answer.charAt(j) + " ");
            }
            previewTextArea2.append("\n");
        }
        System.out.println("Preview Text: " + previewPane.getText());
    }

    private void getDetail(int questionId){
        try (Connection connection = DatabaseConnector.connect()) {
            String query1 = "SELECT image_path, audio_path FROM questions WHERE questions.id = ?";
            try (PreparedStatement statement = connection.prepareStatement(query1)){
                statement.setInt(1, questionId);
                ResultSet resultSet = statement.executeQuery();
                while (resultSet.next()) {
                    imagePath = resultSet.getString("image_path");
                    audioPath = resultSet.getString("audio_path");
                }
            }
            if(questionTypeId == 1||questionTypeId == 6||questionTypeId == 2) {
                String query = "SELECT option_name, option_text, is_correct FROM options WHERE question_id = ?";
                try (PreparedStatement statement = connection.prepareStatement(query)) {
                    statement.setInt(1, questionId);
                    ResultSet resultSet = statement.executeQuery();
                    answer = "";
                    while (resultSet.next()) {
                        String optionName = resultSet.getString("option_name");
                        String optionText = resultSet.getString("option_text");
                        int isCorrect = resultSet.getInt("is_correct");
                        // 根据选项名设置对应的属性值
                        switch (optionName) {
                            case "A":
                                optionA = optionText;
                                if (isCorrect == 1) {
                                    answer += optionName;
                                }
                                break;
                            case "B":
                                optionB = optionText;
                                if (isCorrect == 1) {
                                    answer += optionName;
                                }
                                break;
                            case "C":
                                optionC = optionText;
                                if (isCorrect == 1) {
                                    answer += optionName;
                                }
                                break;
                            case "D":
                                optionD = optionText;
                                if (isCorrect == 1) {
                                    answer += optionName;
                                }
                                break;
                        }
                    }
                }
            } else if (questionTypeId == 5) {
                String query = "SELECT correct_answer_text FROM answers WHERE question_id = ?";
                try (PreparedStatement statement = connection.prepareStatement(query)) {
                    statement.setInt(1, questionId);
                    ResultSet resultSet = statement.executeQuery();
                    if (resultSet.next()) {
                        answer = resultSet.getString("correct_answer_text");
                    }
                }
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "数据库连接错误：" + ex.getMessage());
        }
    }
    private void selectAllQuestions() {
        DefaultTableModel model = (DefaultTableModel) table1.getModel();
        int rowCount = model.getRowCount();

        for (int i = 0; i < rowCount; i++) {
            model.setValueAt(true, i, 0);  // 将每一行的第一列（选择列）设置为选中
        }
    }

    private void presentTopic(){
        String subject = comboBox1.getSelectedItem().toString();
        try (Connection connection = DatabaseConnector.connect()) {
            String query = "SELECT name FROM topics WHERE subject_id = ? ";
            try (PreparedStatement statement = connection.prepareStatement(query)) {
                statement.setInt(1, GetId.getSubjectId(subject));
                ResultSet resultSet = statement.executeQuery();
                comboBox3.removeAllItems();
                while (resultSet.next()) {
                    comboBox3.addItem(resultSet.getString("name"));
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
    private void performQuestion(ActionEvent e) {
        table1.setModel(presentQuestion());
    }
    private DefaultTableModel presentQuestion() {
        try (Connection connection = DatabaseConnector.connect()) {
            String query = "";
            String type = (String)comboBox2.getSelectedItem();
            int typeId = GetId.getTypeID(type);
            int subjectId = GetId.getSubjectId((String)comboBox1.getSelectedItem());
            int topicId = GetId.getTopicId((String)comboBox3.getSelectedItem());
            query = "SELECT " +
                    "    questions.content AS '题干', " +
                    "    questions.difficulty AS '难度', " +
                    "    questions.score AS '分数', " +
                    "    topics.name AS '知识点' " +
                    "FROM " +
                    "    questions " +
                    "JOIN topics ON questions.topic_id = topics.id " +
                    "WHERE " +
                    "    topics.subject_id = ? AND questions.question_type_id = ? AND topics.id = ?";

            try (PreparedStatement statement = connection.prepareStatement(query)) {
                statement.setInt(1, subjectId);
                statement.setInt(2, typeId);
                statement.setInt(3, topicId);
                System.out.println("SQL Query: " + query);  // 输出SQL查询语句，用于调试
                try (ResultSet resultSet = statement.executeQuery()) {
                    DefaultTableModel model = new DefaultTableModel();
                    ResultSetMetaData metaData = resultSet.getMetaData();
                    int columnCount = metaData.getColumnCount();
                    model.addColumn("是否选择");
                    // 添加列名
                    for (int columnIndex = 1; columnIndex <= columnCount; columnIndex++) {
                        model.addColumn(metaData.getColumnLabel(columnIndex));
                    }
                    // 添加数据
                    while (resultSet.next()) {
                        Object[] rowData = new Object[columnCount+1];
                        rowData[0] = false;
                        for (int columnIndex = 2; columnIndex <= columnCount+1; columnIndex++) {
                            rowData[columnIndex - 1] = resultSet.getObject(columnIndex-1);
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

    private void selectQuestion(MouseEvent e) {
        if (table1.columnAtPoint(e.getPoint()) == SELECTION_COLUMN_INDEX) {
            int row = table1.rowAtPoint(e.getPoint());
            DefaultTableModel model = (DefaultTableModel) table1.getModel();
            boolean currentValue = (boolean) model.getValueAt(row, SELECTION_COLUMN_INDEX);
            model.setValueAt(!currentValue, row, SELECTION_COLUMN_INDEX);
        }
    }

    private void addToTable2(ActionEvent e) {
        DefaultTableModel model1 = (DefaultTableModel) table1.getModel();
        DefaultTableModel model2 = (DefaultTableModel) table2.getModel();
        model2.setColumnIdentifiers(new Object[]{"题号", "题干", "难度", "分数", "知识点"});

        for (int i = 0; i < model1.getRowCount(); i++) {
            Boolean isSelected = (Boolean) model1.getValueAt(i, 0);

            if (isSelected) {
                // 获取题目的其他信息（题干等）
                Object[] rowData = new Object[model1.getColumnCount()];
                rowData[0] = questionCount++;  // 将 questionCount 放在数组的第一个位置

                for (int j = 1; j < model1.getColumnCount(); j++) {
                    rowData[j] = model1.getValueAt(i, j);
                }

                // 添加到新行
                model2.addRow(rowData);
            }
        }

        int rowCount = model2.getRowCount();
        int columnCount = model2.getColumnCount();

        for (int i = 0; i < rowCount; i++) {
            System.out.print("Row " + i + ": ");
            for (int j = 0; j < columnCount; j++) {
                System.out.print(model2.getValueAt(i, j) + "\t");
            }
            System.out.println();
        }


        updateQuestionNumbers(model2);
        // 更新试题预览文本
        updatePreviewText();

        model2.fireTableDataChanged();
        table2.setModel(model2);
    }

    private void questionSearch(ActionEvent e) {
        String fuzzyContent = JOptionPane.showInputDialog(this, "请输入想要搜索的内容：");
        DefaultTableModel model = searchQuestion(fuzzyContent);
        table1.setModel(model);
    }

    private DefaultTableModel searchQuestion(String fuzzyContent) {
        try (Connection connection = DatabaseConnector.connect()) {
            String query = "SELECT questions.content AS '题干', " +
                    "questions.difficulty AS '难度', " +
                    "questions.score AS '分数', " +
                    "topics.name AS '知识点' " +
                    "FROM " +
                    "    questions " +
                    "JOIN topics ON questions.topic_id = topics.id " +
                    "WHERE questions.content LIKE ? OR topics.name LIKE ?";
            try (PreparedStatement statement = connection.prepareStatement(query)) {
                statement.setString(1, "%" + fuzzyContent + "%");
                statement.setString(2, "%" + fuzzyContent + "%");
                try (ResultSet resultSet = statement.executeQuery()) {
                    DefaultTableModel model = new DefaultTableModel();
                    ResultSetMetaData metaData = resultSet.getMetaData();
                    int columnCount = metaData.getColumnCount();
                    model.addColumn("是否选择");
                    // 添加列名
                    for (int columnIndex = 1; columnIndex <= columnCount; columnIndex++) {
                        model.addColumn(metaData.getColumnLabel(columnIndex));
                    }
                    // 添加数据
                    while (resultSet.next()) {
                        Object[] rowData = new Object[columnCount+1];
                        rowData[0] = false;
                        for (int columnIndex = 2; columnIndex <= columnCount+1; columnIndex++) {
                            rowData[columnIndex - 1] = resultSet.getObject(columnIndex-1);
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




    private void initComponents() {
        splitPane2 = new JSplitPane();
        panel1 = new JPanel();
        label1 = new JLabel();
        comboBox1 = new JComboBox();
        label2 = new JLabel();
        comboBox2 = new JComboBox();
        label3 = new JLabel();
        comboBox3 = new JComboBox();
        scrollPane1 = new JScrollPane();
        table1 = new JTable();
        panel3 = new JPanel();
        button6 = new JButton();
        button7 = new JButton();
        button2 = new JButton();
        splitPane3 = new JSplitPane();
        panel2 = new JPanel();
        panel4 = new JPanel();
        label9 = new JLabel();
        scrollPane2 = new JScrollPane();
        table2 = new JTable();
        button1 = new JButton();
        tabbedPane1 = new JTabbedPane();
        panel5 = new JPanel();
        button8 = new JButton();
        label10 = new JLabel();
        textField1 = new JTextField();

        //======== this ========
        setTitle("\u624b\u52a8\u7ec4\u5377");
        var contentPane = getContentPane();

        //======== splitPane2 ========
        {

            //======== panel1 ========
            {

                //---- label1 ----
                label1.setText("\u5b66\u79d1\uff1a");

                //---- label2 ----
                label2.setText("\u9898\u578b\uff1a");

                //---- comboBox2 ----
                comboBox2.addActionListener(e -> performQuestion(e));

                //---- label3 ----
                label3.setText("\u77e5\u8bc6\u70b9\uff1a");

                //---- comboBox3 ----
                comboBox3.addActionListener(e -> performQuestion(e));

                GroupLayout panel1Layout = new GroupLayout(panel1);
                panel1.setLayout(panel1Layout);
                panel1Layout.setHorizontalGroup(
                        panel1Layout.createParallelGroup()
                                .addGroup(panel1Layout.createSequentialGroup()
                                        .addGroup(panel1Layout.createParallelGroup()
                                                .addGroup(panel1Layout.createSequentialGroup()
                                                        .addGap(18, 18, 18)
                                                        .addGroup(panel1Layout.createParallelGroup(GroupLayout.Alignment.TRAILING)
                                                                .addComponent(label1, GroupLayout.Alignment.LEADING)
                                                                .addComponent(label2, GroupLayout.Alignment.LEADING))
                                                        .addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED)
                                                        .addGroup(panel1Layout.createParallelGroup()
                                                                .addComponent(comboBox1, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                                                                .addComponent(comboBox2, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)))
                                                .addGroup(panel1Layout.createSequentialGroup()
                                                        .addContainerGap()
                                                        .addComponent(label3)
                                                        .addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED)
                                                        .addComponent(comboBox3, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)))
                                        .addContainerGap(10, Short.MAX_VALUE))
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
                                        .addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED)
                                        .addGroup(panel1Layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                                                .addComponent(label3)
                                                .addComponent(comboBox3, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
                                        .addContainerGap(13, Short.MAX_VALUE))
                );
            }
            splitPane2.setLeftComponent(panel1);

            //======== scrollPane1 ========
            {
                scrollPane1.setViewportView(table1);
            }
            splitPane2.setRightComponent(scrollPane1);
        }

        //======== panel3 ========
        {

            //---- button6 ----
            button6.setText("\u5168\u9009");

            //---- button7 ----
            button7.setText("\u52a0\u5165\u5217\u8868");
            button7.addActionListener(e -> addToTable2(e));

            //---- button2 ----
            button2.setText("\u9898\u76ee\u641c\u7d22");
            button2.addActionListener(e -> questionSearch(e));

            GroupLayout panel3Layout = new GroupLayout(panel3);
            panel3.setLayout(panel3Layout);
            panel3Layout.setHorizontalGroup(
                    panel3Layout.createParallelGroup()
                            .addGroup(panel3Layout.createSequentialGroup()
                                    .addContainerGap()
                                    .addGroup(panel3Layout.createParallelGroup()
                                            .addComponent(button6)
                                            .addComponent(button7)
                                            .addComponent(button2))
                                    .addContainerGap(GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
            );
            panel3Layout.setVerticalGroup(
                    panel3Layout.createParallelGroup()
                            .addGroup(panel3Layout.createSequentialGroup()
                                    .addGap(19, 19, 19)
                                    .addComponent(button6)
                                    .addGap(12, 12, 12)
                                    .addComponent(button2)
                                    .addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED)
                                    .addComponent(button7)
                                    .addContainerGap(GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
            );
        }

        //======== splitPane3 ========
        {

            //======== panel2 ========
            {

                //======== panel4 ========
                {

                    //---- label9 ----
                    label9.setText("\u5df2\u9009\u9898\u76ee\u5217\u8868");

                    GroupLayout panel4Layout = new GroupLayout(panel4);
                    panel4.setLayout(panel4Layout);
                    panel4Layout.setHorizontalGroup(
                            panel4Layout.createParallelGroup()
                                    .addGroup(panel4Layout.createSequentialGroup()
                                            .addContainerGap(42, Short.MAX_VALUE)
                                            .addComponent(label9)
                                            .addGap(45, 45, 45))
                    );
                    panel4Layout.setVerticalGroup(
                            panel4Layout.createParallelGroup()
                                    .addGroup(GroupLayout.Alignment.TRAILING, panel4Layout.createSequentialGroup()
                                            .addGap(12, 12, 12)
                                            .addComponent(label9)
                                            .addContainerGap(GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                    );
                }

                //======== scrollPane2 ========
                {
                    scrollPane2.setViewportView(table2);
                }

                //---- button1 ----
                button1.setText("\u5220\u9664");

                GroupLayout panel2Layout = new GroupLayout(panel2);
                panel2.setLayout(panel2Layout);
                panel2Layout.setHorizontalGroup(
                        panel2Layout.createParallelGroup()
                                .addComponent(panel4, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addGroup(panel2Layout.createSequentialGroup()
                                        .addGap(38, 38, 38)
                                        .addComponent(button1)
                                        .addContainerGap(43, Short.MAX_VALUE))
                                .addGroup(GroupLayout.Alignment.TRAILING, panel2Layout.createSequentialGroup()
                                        .addContainerGap()
                                        .addComponent(scrollPane2, GroupLayout.DEFAULT_SIZE, 0, Short.MAX_VALUE)
                                        .addContainerGap())
                );
                panel2Layout.setVerticalGroup(
                        panel2Layout.createParallelGroup()
                                .addGroup(panel2Layout.createSequentialGroup()
                                        .addContainerGap(GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                        .addComponent(panel4, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                                        .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                        .addComponent(scrollPane2, GroupLayout.PREFERRED_SIZE, 209, GroupLayout.PREFERRED_SIZE)
                                        .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                                        .addComponent(button1)
                                        .addContainerGap())
                );
            }
            splitPane3.setLeftComponent(panel2);

            //======== tabbedPane1 ========
            {
                tabbedPane1.setMaximumSize(new Dimension(300, 200));
            }
            splitPane3.setRightComponent(tabbedPane1);
        }

        //======== panel5 ========
        {

            //---- button8 ----
            button8.setText("\u7ec4\u5377\u5b8c\u6210");

            //---- label10 ----
            label10.setText("\u8bd5\u5377\u540d\u79f0");

            GroupLayout panel5Layout = new GroupLayout(panel5);
            panel5.setLayout(panel5Layout);
            panel5Layout.setHorizontalGroup(
                    panel5Layout.createParallelGroup()
                            .addGroup(panel5Layout.createSequentialGroup()
                                    .addGroup(panel5Layout.createParallelGroup()
                                            .addGroup(panel5Layout.createSequentialGroup()
                                                    .addGap(21, 21, 21)
                                                    .addComponent(label10))
                                            .addGroup(panel5Layout.createSequentialGroup()
                                                    .addContainerGap()
                                                    .addComponent(textField1, GroupLayout.DEFAULT_SIZE, 82, Short.MAX_VALUE)))
                                    .addContainerGap(16, Short.MAX_VALUE))
                            .addGroup(panel5Layout.createSequentialGroup()
                                    .addContainerGap()
                                    .addComponent(button8)
                                    .addContainerGap(16, Short.MAX_VALUE))
            );
            panel5Layout.setVerticalGroup(
                    panel5Layout.createParallelGroup()
                            .addGroup(panel5Layout.createSequentialGroup()
                                    .addContainerGap(63, Short.MAX_VALUE)
                                    .addComponent(label10)
                                    .addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED)
                                    .addComponent(textField1, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                                    .addGap(50, 50, 50)
                                    .addComponent(button8)
                                    .addGap(89, 89, 89))
            );
        }

        GroupLayout contentPaneLayout = new GroupLayout(contentPane);
        contentPane.setLayout(contentPaneLayout);
        contentPaneLayout.setHorizontalGroup(
                contentPaneLayout.createParallelGroup()
                        .addGroup(contentPaneLayout.createSequentialGroup()
                                .addContainerGap()
                                .addGroup(contentPaneLayout.createParallelGroup()
                                        .addComponent(splitPane2, GroupLayout.DEFAULT_SIZE, 742, Short.MAX_VALUE)
                                        .addComponent(splitPane3))
                                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addGroup(contentPaneLayout.createParallelGroup()
                                        .addComponent(panel3, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                                        .addComponent(panel5, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
                                .addGap(0, 0, Short.MAX_VALUE))
        );
        contentPaneLayout.setVerticalGroup(
                contentPaneLayout.createParallelGroup()
                        .addGroup(GroupLayout.Alignment.TRAILING, contentPaneLayout.createSequentialGroup()
                                .addContainerGap(GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addGroup(contentPaneLayout.createParallelGroup()
                                        .addComponent(panel5, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                                        .addComponent(splitPane3, GroupLayout.DEFAULT_SIZE, 294, Short.MAX_VALUE))
                                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addGroup(contentPaneLayout.createParallelGroup()
                                        .addComponent(splitPane2, GroupLayout.DEFAULT_SIZE, 152, Short.MAX_VALUE)
                                        .addComponent(panel3, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                                .addContainerGap(GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        pack();
        setLocationRelativeTo(getOwner());
    }

}

