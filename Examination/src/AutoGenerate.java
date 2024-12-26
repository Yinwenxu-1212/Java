import org.apache.batik.swing.JSVGCanvas;
import org.apache.batik.swing.svg.SVGUserAgentAdapter;
import org.apache.batik.transcoder.TranscoderInput;
import org.apache.batik.transcoder.TranscoderOutput;
import org.apache.batik.transcoder.image.PNGTranscoder;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.util.Units;
import org.apache.poi.xwpf.usermodel.ParagraphAlignment;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.apache.poi.xwpf.usermodel.XWPFRun;
import util.DatabaseConnector;
import util.GetId;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.text.BadLocationException;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;



/**
 * @author 11219
 */
public class AutoGenerate extends JFrame {
    private String content;
    private int score;
    private ArrayList<Integer> selectedQuestions;
    private String svgImagePath;
    private String answer;
    private int questionTypeId;
    private String optionA;
    private String optionB;
    private String optionC;
    private String optionD;
    private String imagePath;
    private String audioPath;
    private Map<String, Integer> questionTypeCountMap = new HashMap<>();
    private JSplitPane splitPane1;
    private JPanel panel1;
    private JLabel label1;
    private JLabel label2;
    private JTextField textField1;
    private JComboBox comboBox1;
    private JPanel panel2;
    private JLabel label5;
    private JTextField textField2;
    private JLabel label6;
    private JLabel label7;
    private JTextField textField3;
    private JLabel label8;
    private JTextField textField4;
    private JLabel label10;
    private JLabel label11;
    private JTextField textField5;
    private JLabel label12;
    private JLabel label9;
    private JPanel panel3;
    private JLabel label3;
    private JScrollPane scrollPane1;
    private JList list1;
    private JLabel label4;
    private JScrollPane scrollPane2;
    private JList list2;
    private JButton button1;
    private JButton button2;
    private JButton button3;
    private JButton button4;
    private JTabbedPane tabbedPane1;
    public AutoGenerate() {
        initComponents();
        presentSubject();
        presentTopic();
        // 添加学科选择的事件监听器
        comboBox1.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                presentTopic(); // 在用户选择学科时更新知识点列表
            }
        });
        button1.addActionListener(e -> button1ActionPerformed(e));
        button2.addActionListener(e -> button2ActionPerformed(e));
        button3.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                generateAutoExam();
                updatePreviewText();
            }
        });
        button4.addActionListener(e -> button4ActionPerformed(e));
        // 设置默认值为 0
        textField2.setText("0");
        textField3.setText("0");
        textField4.setText("0");
        textField5.setText("0");

    }
    private void button4ActionPerformed(ActionEvent e) {
        try {
            exportPaperToDoc();
        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "操作失败：" + ex.getMessage());
        }
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

                int num = 1;
                // 遍历 model2 中的数据，将其添加到文档
                for (Integer questionId : selectedQuestions) {
                    getDetail(questionId);
                    if(imagePath != null && !imagePath.isEmpty() && !imagePath.equals("null")) {
                        if (isSVG(imagePath)) {
                            svgImagePath = convertSvgToPngAndGetPath(imagePath, folderPath.toString());
                        }
                    }
                    // 添加标题和内容到文档
                    appendDetailToDocument(document, String.valueOf(num), content, questionId, score);

                    // 复制音频文件到文件夹
                    if (audioPath != null && !audioPath.isEmpty()) {
                        // 获取音频文件名称，并在文件名中包含题号
                        String audioFileName = String.format("Q%d_%s", num, Paths.get(audioPath).getFileName().toString());
                        String targetAudioPath = Paths.get(folderPath.toString(), audioFileName).toString();
                        Files.copy(Paths.get(audioPath), Paths.get(targetAudioPath), StandardCopyOption.REPLACE_EXISTING);
                    }
                    num++;
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
    private void updateQuestionTypeCountMap() {
        // 获取单选题数量
        int numSingleChoice = Integer.parseInt(textField2.getText());
        // 获取多选题数量
        int numMultipleChoice = Integer.parseInt(textField3.getText());
        int numQAChoice = Integer.parseInt(textField4.getText());
        int numListenChoice = Integer.parseInt(textField5.getText());

        // 更新映射
        questionTypeCountMap.put("1", numSingleChoice);
        questionTypeCountMap.put("2", numMultipleChoice);
        questionTypeCountMap.put("5", numQAChoice);
        questionTypeCountMap.put("6", numListenChoice);
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

        // 清空预览文本
        previewPane.setText("");
        previewTextArea2.setText("");

        selectedQuestions = convertIdsToContent(generateAutoExam());
        // 遍历随机产生的题目
        int i = 1;
        for (Integer questionId : selectedQuestions) {
            getDetail(questionId);
            System.out.println(imagePath);

            // 添加文本到JTextPane
            StyledDocument doc = previewPane.getStyledDocument();
            SimpleAttributeSet style = new SimpleAttributeSet();
            StyleConstants.setBold(style, false);
            StyleConstants.setFontSize(style, 14);
            try {
                doc.insertString(doc.getLength(), i + ". " + "(" + score + "分)" + content + "\n", style);
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
            previewTextArea2.append(i + ". ");
            for (int j = 0; j < answer.length(); j++) {
                previewTextArea2.append(answer.charAt(j) + " ");
            }
            previewTextArea2.append("\n");
            i++;
        }
        System.out.println("Preview Text: " + previewPane.getText());
    }
    private void getDetail(int questionId){
        try (Connection connection = DatabaseConnector.connect()) {
            String query1 = "SELECT image_path, audio_path, content, score, question_type_id FROM questions WHERE questions.id = ?";
            try (PreparedStatement statement = connection.prepareStatement(query1)){
                statement.setInt(1, questionId);
                ResultSet resultSet = statement.executeQuery();
                while (resultSet.next()) {
                    imagePath = resultSet.getString("image_path");
                    audioPath = resultSet.getString("audio_path");
                    content = resultSet.getString("content");
                    score = resultSet.getInt("score");
                    questionTypeId = Integer.parseInt(resultSet.getString("question_type_id"));
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
    private ArrayList<Integer> convertIdsToContent(ArrayList<String> questionContents) {
        ArrayList<Integer> questionIds = new ArrayList<>();

        for (String questionContent : questionContents) {
            int id = GetId.getQuestionID(questionContent);
            questionIds.add(id);
        }
        return questionIds;
    }

    private ArrayList<String> generateAutoExam() {
        ArrayList<String> selectedKnowledgePoints = getSelectedKnowledgePoints(); // 获取用户选择的知识点
        ArrayList<String> autoGeneratedExam = new ArrayList<>();
        // 更新题目类型和数量映射
        updateQuestionTypeCountMap();
        try (Connection connection = DatabaseConnector.connect()) {
            // 构建查询语句，检索符合知识点条件的题目
            String query = "SELECT questions.content, questions.question_type_id FROM questions " +
                    "JOIN topics ON questions.topic_id = topics.id " +
                    "WHERE topics.name IN (" + String.join(",", Collections.nCopies(selectedKnowledgePoints.size(), "?")) + ")";
            try (PreparedStatement statement = connection.prepareStatement(query)) {
                // 设置每个知识点的参数值
                for (int i = 0; i < selectedKnowledgePoints.size(); i++) {
                    statement.setString(i + 1, selectedKnowledgePoints.get(i));
                }

                ResultSet resultSet = statement.executeQuery();
                Map<String, ArrayList<String>> availableQuestionsMap = new HashMap<>();

                while (resultSet.next()) {
                    String content = resultSet.getString("content");
                    String type = resultSet.getString("question_type_id");
                    availableQuestionsMap.computeIfAbsent(type, k -> new ArrayList<>()).add(content);
                }

                System.out.println(availableQuestionsMap);

                // 根据用户填写的数量要求，随机选择题目
                for (Map.Entry<String, Integer> entry : questionTypeCountMap.entrySet()) {
                    String type = entry.getKey();
                    int numQuestions = entry.getValue();

                    if (availableQuestionsMap.containsKey(type)) {
                        autoGeneratedExam.addAll(selectRandomQuestions(availableQuestionsMap.get(type), numQuestions));
                    }
                }

                System.out.println(autoGeneratedExam);
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
            // 处理异常
        }

        return autoGeneratedExam;
    }


    private ArrayList<String> getSelectedKnowledgePoints() {
        DefaultListModel<Object> list2Model = (DefaultListModel<Object>) list2.getModel();
        ArrayList<String> selectedKnowledgePoints = new ArrayList<>();

        for (int i = 0; i < list2Model.getSize(); i++) {
            selectedKnowledgePoints.add(list2Model.getElementAt(i).toString());
        }

        return selectedKnowledgePoints;
    }

    private ArrayList<String> selectRandomQuestions(ArrayList<String> availableQuestions, int numQuestions) {
        ArrayList<String> selectedQuestions = new ArrayList<>(availableQuestions);
        Collections.shuffle(selectedQuestions);

        return new ArrayList<>(selectedQuestions.subList(0, Math.min(numQuestions, selectedQuestions.size())));
    }
    private void button1ActionPerformed(ActionEvent e) {
        // 获取选定的知识点
        Object[] selectedItems = list1.getSelectedValues();

        // 检查是否已经存在相同的知识点
        if (containsDuplicateItems(list2.getModel(), selectedItems)) {
            JOptionPane.showMessageDialog(this, "已存在相同的知识点，请重新选择。", "重复知识点", JOptionPane.WARNING_MESSAGE);
            return;
        }

        // 将选定的知识点添加到list2中
        ListModel list2Model = list2.getModel();
        DefaultListModel<Object> newListModel = new DefaultListModel<>();

        // 复制原有的数据到新的ListModel中
        for (int i = 0; i < list2Model.getSize(); i++) {
            newListModel.addElement(list2Model.getElementAt(i));
        }

        // 添加选定的知识点到新的ListModel中
        for (Object selectedItem : selectedItems) {
            newListModel.addElement(selectedItem);
        }

        // 设置新的ListModel到list2
        list2.setModel(newListModel);
    }

    private void button2ActionPerformed(ActionEvent e) {
        // 获取选中的知识点
        Object[] selectedItems = list2.getSelectedValues();

        // 将选中的知识点从list2中移除
        DefaultListModel<Object> list2Model = (DefaultListModel<Object>) list2.getModel();
        for (Object selectedItem : selectedItems) {
            list2Model.removeElement(selectedItem);
        }
    }

    // 检查ListModel中是否包含重复的项目
    private boolean containsDuplicateItems(ListModel model, Object[] items) {
        for (Object item : items) {
            if (containsItem(model, item)) {
                return true;
            }
        }
        return false;
    }

    // 判断ListModel中是否包含特定的项目
    private boolean containsItem(ListModel model, Object item) {
        for (int i = 0; i < model.getSize(); i++) {
            if (item.equals(model.getElementAt(i))) {
                return true;
            }
        }
        return false;
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
    private void presentTopic() {
        try (Connection connection = DatabaseConnector.connect()) {
            String query = "SELECT name FROM topics WHERE subject_id = ? ";
            try (PreparedStatement statement = connection.prepareStatement(query)) {
                // 获取用户选择的学科
                String selectedSubject = (String) comboBox1.getSelectedItem();
                statement.setInt(1, GetId.getSubjectId(selectedSubject));

                ResultSet resultSet = statement.executeQuery();

                DefaultListModel<String> listModel = new DefaultListModel<>();

                while (resultSet.next()) {
                    listModel.addElement(resultSet.getString("name"));
                }

                list1.setModel(listModel); // 将知识点添加到 list1 中
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "数据库连接错误：" + ex.getMessage());
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            AutoGenerate frame = new AutoGenerate();
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setVisible(true);
        });
    }

    private void initComponents() {
        splitPane1 = new JSplitPane();
        panel1 = new JPanel();
        label1 = new JLabel();
        label2 = new JLabel();
        textField1 = new JTextField();
        comboBox1 = new JComboBox();
        panel2 = new JPanel();
        label5 = new JLabel();
        textField2 = new JTextField();
        label6 = new JLabel();
        label7 = new JLabel();
        textField3 = new JTextField();
        label8 = new JLabel();
        textField4 = new JTextField();
        label10 = new JLabel();
        label11 = new JLabel();
        textField5 = new JTextField();
        label12 = new JLabel();
        label9 = new JLabel();
        panel3 = new JPanel();
        label3 = new JLabel();
        scrollPane1 = new JScrollPane();
        list1 = new JList();
        label4 = new JLabel();
        scrollPane2 = new JScrollPane();
        list2 = new JList();
        button1 = new JButton();
        button2 = new JButton();
        button3 = new JButton();
        button4 = new JButton();
        tabbedPane1 = new JTabbedPane();

        //======== this ========
        setTitle("\u81ea\u52a8\u7ec4\u5377");
        var contentPane = getContentPane();

        //======== splitPane1 ========
        {

            //======== panel1 ========
            {

                //---- label1 ----
                label1.setText("\u5b66\u79d1\uff1a");

                //---- label2 ----
                label2.setText("\u8bd5\u5377\u540d\u79f0\uff1a");

                //======== panel2 ========
                {

                    //---- label5 ----
                    label5.setText("\u5355\u9009\u9898");

                    //---- label6 ----
                    label6.setText("\u9898");

                    //---- label7 ----
                    label7.setText("\u591a\u9009\u9898");

                    //---- label8 ----
                    label8.setText("\u9898");

                    //---- label10 ----
                    label10.setText("\u9898");

                    //---- label11 ----
                    label11.setText("\u95ee\u7b54\u9898");

                    //---- label12 ----
                    label12.setText("\u9898");

                    //---- label9 ----
                    label9.setText("\u542c\u529b\u9898");

                    GroupLayout panel2Layout = new GroupLayout(panel2);
                    panel2.setLayout(panel2Layout);
                    panel2Layout.setHorizontalGroup(
                            panel2Layout.createParallelGroup()
                                    .addGroup(panel2Layout.createSequentialGroup()
                                            .addGap(8, 8, 8)
                                            .addGroup(panel2Layout.createParallelGroup(GroupLayout.Alignment.TRAILING)
                                                    .addGroup(panel2Layout.createSequentialGroup()
                                                            .addComponent(label11)
                                                            .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                                                            .addComponent(textField4, GroupLayout.PREFERRED_SIZE, 30, GroupLayout.PREFERRED_SIZE)
                                                            .addGap(6, 6, 6)
                                                            .addComponent(label10, GroupLayout.PREFERRED_SIZE, 24, GroupLayout.PREFERRED_SIZE))
                                                    .addGroup(panel2Layout.createSequentialGroup()
                                                            .addComponent(label5)
                                                            .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                                                            .addComponent(textField2, GroupLayout.PREFERRED_SIZE, 30, GroupLayout.PREFERRED_SIZE)
                                                            .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                                                            .addComponent(label6, GroupLayout.PREFERRED_SIZE, 24, GroupLayout.PREFERRED_SIZE)))
                                            .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                            .addGroup(panel2Layout.createParallelGroup(GroupLayout.Alignment.LEADING, false)
                                                    .addGroup(GroupLayout.Alignment.TRAILING, panel2Layout.createSequentialGroup()
                                                            .addComponent(label7)
                                                            .addGap(6, 6, 6)
                                                            .addComponent(textField3, GroupLayout.PREFERRED_SIZE, 30, GroupLayout.PREFERRED_SIZE)
                                                            .addGap(6, 6, 6)
                                                            .addComponent(label8, GroupLayout.PREFERRED_SIZE, 24, GroupLayout.PREFERRED_SIZE))
                                                    .addGroup(GroupLayout.Alignment.TRAILING, panel2Layout.createSequentialGroup()
                                                            .addComponent(label9)
                                                            .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                                            .addComponent(textField5, GroupLayout.PREFERRED_SIZE, 30, GroupLayout.PREFERRED_SIZE)
                                                            .addGap(6, 6, 6)
                                                            .addComponent(label12, GroupLayout.PREFERRED_SIZE, 24, GroupLayout.PREFERRED_SIZE)))
                                            .addContainerGap())
                    );
                    panel2Layout.setVerticalGroup(
                            panel2Layout.createParallelGroup()
                                    .addGroup(panel2Layout.createSequentialGroup()
                                            .addGap(12, 12, 12)
                                            .addGroup(panel2Layout.createParallelGroup()
                                                    .addComponent(textField3, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                                                    .addGroup(panel2Layout.createSequentialGroup()
                                                            .addGap(6, 6, 6)
                                                            .addGroup(panel2Layout.createParallelGroup()
                                                                    .addComponent(label7)
                                                                    .addComponent(label8)))
                                                    .addGroup(panel2Layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                                                            .addComponent(label5)
                                                            .addComponent(textField2, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                                                            .addComponent(label6)))
                                            .addGap(18, 18, 18)
                                            .addGroup(panel2Layout.createParallelGroup()
                                                    .addGroup(panel2Layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                                                            .addComponent(textField4, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                                                            .addComponent(label11))
                                                    .addComponent(textField5, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                                                    .addGroup(panel2Layout.createSequentialGroup()
                                                            .addGap(6, 6, 6)
                                                            .addGroup(panel2Layout.createParallelGroup()
                                                                    .addGroup(panel2Layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                                                                            .addComponent(label10)
                                                                            .addComponent(label9))
                                                                    .addComponent(label12))))
                                            .addContainerGap(10, Short.MAX_VALUE))
                    );
                }

                //======== panel3 ========
                {

                    //---- label3 ----
                    label3.setText("\u6240\u6709\u77e5\u8bc6\u70b9");

                    //======== scrollPane1 ========
                    {
                        scrollPane1.setViewportView(list1);
                    }

                    //---- label4 ----
                    label4.setText("\u5df2\u9009\u77e5\u8bc6\u70b9");

                    //======== scrollPane2 ========
                    {
                        scrollPane2.setViewportView(list2);
                    }

                    GroupLayout panel3Layout = new GroupLayout(panel3);
                    panel3.setLayout(panel3Layout);
                    panel3Layout.setHorizontalGroup(
                            panel3Layout.createParallelGroup()
                                    .addGroup(panel3Layout.createSequentialGroup()
                                            .addGap(28, 28, 28)
                                            .addGroup(panel3Layout.createParallelGroup()
                                                    .addComponent(label3)
                                                    .addComponent(scrollPane1, GroupLayout.PREFERRED_SIZE, 83, GroupLayout.PREFERRED_SIZE))
                                            .addGap(33, 33, 33)
                                            .addGroup(panel3Layout.createParallelGroup()
                                                    .addComponent(label4)
                                                    .addComponent(scrollPane2, GroupLayout.PREFERRED_SIZE, 83, GroupLayout.PREFERRED_SIZE))
                                            .addContainerGap(27, Short.MAX_VALUE))
                    );
                    panel3Layout.setVerticalGroup(
                            panel3Layout.createParallelGroup()
                                    .addGroup(panel3Layout.createSequentialGroup()
                                            .addGap(17, 17, 17)
                                            .addGroup(panel3Layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                                                    .addComponent(label3)
                                                    .addComponent(label4))
                                            .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                                            .addGroup(panel3Layout.createParallelGroup()
                                                    .addComponent(scrollPane1, GroupLayout.PREFERRED_SIZE, 92, GroupLayout.PREFERRED_SIZE)
                                                    .addComponent(scrollPane2, GroupLayout.PREFERRED_SIZE, 92, GroupLayout.PREFERRED_SIZE))
                                            .addContainerGap(GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                    );
                }

                //---- button1 ----
                button1.setText("\u52a0\u5165\u77e5\u8bc6\u70b9");

                //---- button2 ----
                button2.setText("\u5220\u9664\u77e5\u8bc6\u70b9");

                //---- button3 ----
                button3.setText("\u81ea\u52a8\u7ec4\u5377");

                //---- button4 ----
                button4.setText("\u5bfc\u51fa\u8bd5\u5377");

                GroupLayout panel1Layout = new GroupLayout(panel1);
                panel1.setLayout(panel1Layout);
                panel1Layout.setHorizontalGroup(
                        panel1Layout.createParallelGroup()
                                .addGroup(GroupLayout.Alignment.TRAILING, panel1Layout.createSequentialGroup()
                                        .addContainerGap(93, Short.MAX_VALUE)
                                        .addGroup(panel1Layout.createParallelGroup(GroupLayout.Alignment.TRAILING)
                                                .addComponent(label2)
                                                .addComponent(label1))
                                        .addGap(38, 38, 38)
                                        .addGroup(panel1Layout.createParallelGroup()
                                                .addComponent(comboBox1, GroupLayout.Alignment.TRAILING, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                                                .addComponent(textField1, GroupLayout.Alignment.TRAILING, GroupLayout.PREFERRED_SIZE, 80, GroupLayout.PREFERRED_SIZE))
                                        .addGap(94, 94, 94))
                                .addGroup(panel1Layout.createSequentialGroup()
                                        .addGap(46, 46, 46)
                                        .addGroup(panel1Layout.createParallelGroup()
                                                .addComponent(panel3, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                                                .addGroup(GroupLayout.Alignment.TRAILING, panel1Layout.createParallelGroup()
                                                        .addGroup(panel1Layout.createSequentialGroup()
                                                                .addGap(6, 6, 6)
                                                                .addComponent(panel2, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
                                                        .addGroup(panel1Layout.createSequentialGroup()
                                                                .addComponent(button1)
                                                                .addGap(47, 47, 47)
                                                                .addComponent(button2))
                                                        .addGroup(panel1Layout.createSequentialGroup()
                                                                .addComponent(button3)
                                                                .addGap(74, 74, 74)
                                                                .addComponent(button4))))
                                        .addContainerGap(68, Short.MAX_VALUE))
                );
                panel1Layout.setVerticalGroup(
                        panel1Layout.createParallelGroup()
                                .addGroup(GroupLayout.Alignment.TRAILING, panel1Layout.createSequentialGroup()
                                        .addGap(23, 23, 23)
                                        .addGroup(panel1Layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                                                .addComponent(textField1, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                                                .addComponent(label2))
                                        .addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED)
                                        .addGroup(panel1Layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                                                .addComponent(comboBox1, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                                                .addComponent(label1))
                                        .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                                        .addComponent(panel3, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                                        .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                                        .addGroup(panel1Layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                                                .addComponent(button2, GroupLayout.PREFERRED_SIZE, 27, GroupLayout.PREFERRED_SIZE)
                                                .addComponent(button1, GroupLayout.PREFERRED_SIZE, 27, GroupLayout.PREFERRED_SIZE))
                                        .addGap(8, 8, 8)
                                        .addComponent(panel2, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                                        .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                                        .addGroup(panel1Layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                                                .addComponent(button3)
                                                .addComponent(button4))
                                        .addContainerGap(14, Short.MAX_VALUE))
                );
            }
            splitPane1.setLeftComponent(panel1);
            splitPane1.setRightComponent(tabbedPane1);
        }

        GroupLayout contentPaneLayout = new GroupLayout(contentPane);
        contentPane.setLayout(contentPaneLayout);
        contentPaneLayout.setHorizontalGroup(
                contentPaneLayout.createParallelGroup()
                        .addGroup(contentPaneLayout.createSequentialGroup()
                                .addContainerGap(GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addComponent(splitPane1, GroupLayout.DEFAULT_SIZE, 841, Short.MAX_VALUE)
                                .addContainerGap(GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        contentPaneLayout.setVerticalGroup(
                contentPaneLayout.createParallelGroup()
                        .addGroup(contentPaneLayout.createSequentialGroup()
                                .addContainerGap(GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addComponent(splitPane1)
                                .addContainerGap(GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        pack();
        setLocationRelativeTo(getOwner());
    }
}
