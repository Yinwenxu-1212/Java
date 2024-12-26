package util;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.*;
import java.awt.event.KeyEvent;
import java.awt.event.KeyAdapter;
import java.util.ArrayList;
import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

public class Painting extends JFrame {
    private enum ShapeType { POINT, LINE, ARC, RECTANGLE }

    private String savedFilePath;
    private ShapeType currentShape;
    private Color currentColor;
    private BasicStroke currentStroke;
    private ArrayList<Shape> shapes;
    private ArrayList<Color> shapeColors;
    private ArrayList<BasicStroke> shapeStrokes;
    private DrawingPanel drawingPanel;
    private JButton pointButton = new JButton("画点");
    private JButton lineButton = new JButton("画线");
    private JButton arcButton = new JButton("画弧");
    private JButton rectangleButton = new JButton("绘制矩形");
    private JButton rubberButton = new JButton("撤回");
    private JButton clearButton = new JButton("清空");

    public Painting() {
        setTitle("绘图");
        setSize(800, 600);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        shapes = new ArrayList<>();
        shapeColors = new ArrayList<>();
        shapeStrokes = new ArrayList<>();
        currentShape = ShapeType.POINT;
        currentColor = Color.RED;
        currentStroke = new BasicStroke(1.0f);

        // 菜单
        JMenuBar menuBar = new JMenuBar();
        setJMenuBar(menuBar);

        // 文件
        JMenu fileMenu = new JMenu("文件");
        menuBar.add(fileMenu);

        JMenuItem saveItem = new JMenuItem("保存文件");
        saveItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                saveToFile();
                drawingPanel.requestFocusInWindow(); // 请求DrawingPanel获得焦点
            }
        });
        fileMenu.add(saveItem);

        JMenuItem loadItem = new JMenuItem("打开文件");
        loadItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                loadFromFile();
                repaint();
                drawingPanel.requestFocusInWindow();
            }
        });

        fileMenu.add(loadItem);
        // 说明书
        JMenu manualMenu = new JMenu("说明书");
        menuBar.add(manualMenu);

        JMenuItem helpItem = new JMenuItem("帮助");
        helpItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                showManualDialog();
            }
        });
        manualMenu.add(helpItem);
        // 画板
        drawingPanel = new DrawingPanel();
        drawingPanel.setBackground(Color.GRAY);
        drawingPanel.setPreferredSize(new Dimension(800, 400));
        add(drawingPanel, BorderLayout.CENTER);

        // 选择栏
        JPanel collectivePanel = new JPanel();
        JPanel shapePanel = new JPanel();
        JPanel revisePanel = new JPanel();
        JPanel formatPanel = new JPanel();
        add(collectivePanel, BorderLayout.NORTH);
        collectivePanel.add(shapePanel,BorderLayout.WEST);
        collectivePanel.add(revisePanel,BorderLayout.CENTER);
        collectivePanel.add(formatPanel,BorderLayout.EAST);

        pointButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                currentShape = ShapeType.POINT;
                drawingPanel.requestFocusInWindow();
            }
        });
        shapePanel.add(pointButton);

        lineButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                currentShape = ShapeType.LINE;
                drawingPanel.requestFocusInWindow();
            }
        });
        shapePanel.add(lineButton);

        arcButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                currentShape = ShapeType.ARC;
                drawingPanel.requestFocusInWindow();
            }
        });
        shapePanel.add(arcButton);

        rectangleButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                currentShape = ShapeType.RECTANGLE;
                drawingPanel.requestFocusInWindow();
            }
        });
        shapePanel.add(rectangleButton);

        JPanel colorPanel = new JPanel();
        add(colorPanel, BorderLayout.EAST);

        rubberButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (!shapes.isEmpty()) {
                    shapes.remove(shapes.size() - 1);
                    shapeColors.remove(shapeColors.size() - 1);
                    shapeStrokes.remove(shapeStrokes.size() - 1);
                    drawingPanel.repaint();
                }
                drawingPanel.requestFocusInWindow();
            }
        });
        revisePanel.add(rubberButton);

        clearButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                int option = JOptionPane.showConfirmDialog(Painting.this, "清空操作将会删除当前所有内容。\n确定要清空画板吗？", "清空", JOptionPane.YES_NO_OPTION);
                if (option==JOptionPane.YES_OPTION) {
                    shapes.clear();
                    shapeColors.clear();
                    shapeStrokes.clear();
                    drawingPanel.repaint();
                }
                drawingPanel.requestFocusInWindow();
            }
        });
        revisePanel.add(clearButton);

        JButton colorButton = new JButton("选择颜色");
        colorButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                currentColor = JColorChooser.showDialog(Painting.this, "选择颜色", currentColor);
                drawingPanel.requestFocusInWindow();
            }
        });
        formatPanel.add(colorButton);

        JButton strokeButton = new JButton("选择线条宽度");
        strokeButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String input = JOptionPane.showInputDialog("输入线条宽度：");
                if(input == null || input.trim().isEmpty()){
                    JOptionPane.showMessageDialog(null,"用户取消或输入为空", "错误", JOptionPane.ERROR_MESSAGE);
                }else {
                    try {
                        double width = Double.parseDouble(input);
                        currentStroke = new BasicStroke((float) width);
                    } catch (NumberFormatException ex) {
                        // 处理无效的数字输入
                        JOptionPane.showMessageDialog(null, "无效的输入，请输入有效的数字。", "错误", JOptionPane.ERROR_MESSAGE);
                    }
                }
                drawingPanel.requestFocusInWindow();
            }
        });
        formatPanel.add(strokeButton);

        JButton toggleColorButton = new JButton("切换画板颜色");
        toggleColorButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                toggleDrawingPanelColor();
            }
        });
        formatPanel.add(toggleColorButton);
    }

    private void showManualDialog() {
        String manualText = "这是一个画图应用。\n你可以使用以下键盘快捷键：\n"
                + "按 'a'：画点\n按 's'：画线\n按 'd'：画弧\n按 'f'：绘制矩形\n"
                + "按 'g'：撤回\n按 'h'：清空 \n"
                + "在绘图区域内，按空格键可进行相应的绘图操作。\n"
                + "在菜单栏的文件菜单中，你可以保存和加载绘图内容。\n"
                + "在说明书菜单中，你可以查看帮助信息。";

        JOptionPane.showMessageDialog(this, manualText, "帮助", JOptionPane.INFORMATION_MESSAGE);
    }

    private void toggleDrawingPanelColor() {
        Color currentColor = drawingPanel.getBackground();
        Color newColor = (currentColor.equals(Color.WHITE)) ? Color.GRAY : Color.WHITE;

        drawingPanel.setBackground(newColor);
        drawingPanel.repaint();  // 重新绘制画板
    }
    private void saveToFile() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Save as PNG");

        int userSelection = fileChooser.showSaveDialog(this);

        if (userSelection == JFileChooser.APPROVE_OPTION) {
            File fileToSave = fileChooser.getSelectedFile();
            if (!fileToSave.getName().toLowerCase().endsWith(".png")) {
                fileToSave = new File(fileToSave.getAbsolutePath() + ".png");
            }

            try {
                // 创建绘图区域的图像
                BufferedImage image = new BufferedImage(getContentPane().getWidth(), getContentPane().getHeight(), BufferedImage.TYPE_INT_RGB);
                Graphics2D g2d = image.createGraphics();

                // 绘制白色背景
                g2d.setColor(Color.WHITE);
                g2d.fillRect(0, 0, getWidth(), getHeight());

                // 绘制形状
                for (int i = 0; i < shapes.size(); i++) {
                    g2d.setColor(shapeColors.get(i));
                    g2d.setStroke(shapeStrokes.get(i));
                    g2d.draw(shapes.get(i));
                }

                // 保存图像
                ImageIO.write(image, "png", fileToSave);

                savedFilePath = fileToSave.getAbsolutePath();  // 保存文件地址
                JOptionPane.showMessageDialog(this, "文件已成功保存至:\n" + savedFilePath, "保存成功", JOptionPane.INFORMATION_MESSAGE);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void loadFromFile() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("加载图像文件");
        fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);

        int userSelection = fileChooser.showOpenDialog(this);

        if (userSelection == JFileChooser.APPROVE_OPTION) {
            File fileToLoad = fileChooser.getSelectedFile();

            try {
                BufferedImage loadedImage = ImageIO.read(fileToLoad);

                // 清除现有图纸数据
                shapes.clear();
                shapeColors.clear();
                shapeStrokes.clear();

                // 创建代表已加载图像的形状
                shapes.add(new Rectangle2D.Double(0, 0, getWidth(), getHeight()));
                shapeColors.add(Color.WHITE);
                shapeStrokes.add(new BasicStroke(1.0f));

                // 用加载的图像重新绘制面板
                drawingPanel.setImage(loadedImage);
                drawingPanel.repaint();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }


    //画板函数
    private class DrawingPanel extends JPanel {
        private Point startPoint;
        private Point endPoint;
        private Shape currentTempShape; // 鼠标拖动时的临时形状
        private BufferedImage image; // 添加一个字段来保存图像
        private Robot robot;

        public DrawingPanel() {

            try {
                robot = new Robot();
            } catch (AWTException e) {
                e.printStackTrace();
            }

            addMouseListener(new MouseAdapter() {
                @Override
                public void mousePressed(MouseEvent e) {
                    startPoint = e.getPoint();
                }

                @Override
                public void mouseReleased(MouseEvent e) {
                    endPoint = e.getPoint();
                    drawShape();
                    repaint();
                    // 重置下一次绘图的起点和终点
                    startPoint = null;
                    endPoint = null;
                    currentTempShape = null; // 重置临时形状
                }
            });
            addMouseMotionListener(new MouseAdapter() {
                @Override
                public void mouseDragged(MouseEvent e) {
                    clearTempShape(); // 清除之前的临时形状
                    endPoint = e.getPoint();
                    updateTempShape(); // 在拖动过程中更新临时形状
                    repaint();
                }
            });

            setFocusable(true);
            requestFocusInWindow();
            addKeyListener(new KeyAdapter() {
                @Override
                public void keyPressed(KeyEvent e) {
                    double x = e.getComponent().getMousePosition().getX();
                    double y = e.getComponent().getMousePosition().getY();
                    double px = drawingPanel.getX();
                    double py = drawingPanel.getY();
                    double width = drawingPanel.getWidth();
                    double height = drawingPanel.getHeight();

                    if ((x > px && x < px + width) && (y > py && y < py + height)) {
                        handleKeyPress(e);
                    } else {
                        JOptionPane.showMessageDialog(null, "光标已移出画图区", "错误", JOptionPane.ERROR_MESSAGE);
                    }

                    // 按下键盘按键时的逻辑
                    if (e.getKeyChar() == 'a') {
                        pointButton.doClick();  // 模拟点击“画点”按钮
                    } else if (e.getKeyChar() == 's') {
                        lineButton.doClick();  // 模拟点击“画线”按钮
                    } else if (e.getKeyChar() == 'd') {
                        arcButton.doClick();   // 模拟点击“画弧”按钮
                    } else if (e.getKeyChar() == 'f') {
                        rectangleButton.doClick();  // 模拟点击“绘制矩形”按钮
                    }
                    else if (e.getKeyChar() == 'g') {
                        rubberButton.doClick();  // 模拟点击“撤回”按钮
                    }
                    else if (e.getKeyChar() == 'h') {
                        clearButton.doClick();  // 模拟点击“清空”按钮
                    }
                }
            });
        }

        private void drawPoint(){
            Graphics2D g2d = (Graphics2D) getGraphics();
            g2d.setColor(currentColor);
            g2d.setStroke(currentStroke);

            float lineWidth = currentStroke.getLineWidth();
            float pointSize = Math.max(2, lineWidth);  // 确保最小尺寸
            g2d.fillOval(startPoint.x - (int)(pointSize / 2), startPoint.y - (int)(pointSize / 2), (int)pointSize, (int)pointSize);
            shapes.add(new Ellipse2D.Double(startPoint.x - pointSize / 2, startPoint.y - pointSize / 2, pointSize, pointSize));
            shapeColors.add(currentColor);
            shapeStrokes.add(currentStroke);
        }
        private void drawLine(boolean flag){
            Graphics2D g2d = (Graphics2D) getGraphics();
            g2d.setColor(currentColor);
            g2d.setStroke(currentStroke);

            if (flag) {
                g2d.drawLine(startPoint.x, startPoint.y, endPoint.x, endPoint.y);
                shapes.add(new Line2D.Double(startPoint, endPoint));
                shapeColors.add(currentColor);
                shapeStrokes.add(currentStroke);
            }else{currentTempShape = new Line2D.Double(startPoint, endPoint);}

        }
        private void drawArc(boolean flag) {
            Graphics2D g2d = (Graphics2D) getGraphics();
            g2d.setColor(currentColor);
            g2d.setStroke(currentStroke);

            int width = Math.abs(endPoint.x - startPoint.x);
            int height = Math.abs(endPoint.y - startPoint.y);
            int x = Math.min(startPoint.x, endPoint.x);
            int y = Math.min(startPoint.y, endPoint.y);

            double angle;
            int startAngle;
            int arcAngle;

            // 计算鼠标拖动的方向
            int dx = endPoint.x - startPoint.x;
            int dy = endPoint.y - startPoint.y;

            if (Math.abs(dx) > Math.abs(dy)) {
                // 水平方向
                if (dx > 0) {
                    // 右
                    startAngle = 0;
                    arcAngle = 180;
                } else {
                    // 左
                    startAngle = 180;
                    arcAngle = 180;
                }
            } else {
                // 垂直方向
                if (dy > 0) {
                    // 下
                    startAngle = 90;
                    arcAngle = 180;
                } else {
                    // 上
                    startAngle = -90;
                    arcAngle = 180;
                }
            }

            if (flag) {
                g2d.drawArc(x, y, width, height, startAngle, arcAngle);
                shapes.add(new Arc2D.Double(x, y, width, height, startAngle, arcAngle, Arc2D.OPEN));
                shapeColors.add(currentColor);
                shapeStrokes.add(currentStroke);
            } else {
                currentTempShape = new Arc2D.Double(x, y, width, height, startAngle, arcAngle, Arc2D.OPEN);
            }
        }



        private void drawRectangle(boolean flag){
            Graphics2D g2d = (Graphics2D) getGraphics();
            g2d.setColor(currentColor);
            g2d.setStroke(currentStroke);

            int width = Math.abs(endPoint.x - startPoint.x);
            int height = Math.abs(endPoint.y - startPoint.y);
            int x = Math.min(startPoint.x, endPoint.x);
            int y = Math.min(startPoint.y, endPoint.y);
            if (flag) {
                g2d.drawRect(x, y, width, height);
                shapes.add(new Rectangle2D.Double(x, y, width, height));
                shapeColors.add(currentColor);
                shapeStrokes.add(currentStroke);
            }else{currentTempShape = new Rectangle2D.Double(x, y, width, height);}
        }
        private void handleKeyPress(KeyEvent e) {
            if (e.getKeyCode()==KeyEvent.VK_SPACE){
                switch (currentShape){
                    case POINT:
                        startPoint = e.getComponent().getMousePosition();
                        drawPoint();
                        repaint();
                        startPoint = null;
                        endPoint = null;
                        currentTempShape = null;
                        break;
                    case LINE:
                        if (startPoint==null){
                            startPoint=e.getComponent().getMousePosition();
                        }else if (endPoint!=null){
                            endPoint=e.getComponent().getMousePosition();
                            drawLine(true);
                            repaint();
                            startPoint = null;
                            endPoint = null;
                            currentTempShape = null;
                        }
                        break;
                    case ARC:
                        if (startPoint==null){
                            startPoint=e.getComponent().getMousePosition();
                        }else if (endPoint!=null){
                            endPoint=e.getComponent().getMousePosition();
                            drawArc(true);
                            repaint();
                            startPoint = null;
                            endPoint = null;
                            currentTempShape = null;
                        }
                        break;
                    case RECTANGLE:
                        if (startPoint==null){
                            startPoint=e.getComponent().getMousePosition();
                        }else if (endPoint!=null){
                            endPoint=e.getComponent().getMousePosition();
                            drawRectangle(true);
                            repaint();
                            startPoint = null;
                            endPoint = null;
                            currentTempShape = null;
                        }
                }
            }else{moveKey(e);}
        }

        private void moveKey(KeyEvent e){
            switch (e.getKeyCode()) {
                case KeyEvent.VK_UP:
                    moveMouse(0, -10, e);
                    if (startPoint!=null){
                        endPoint = e.getComponent().getMousePosition();
                        updateTempShape();
                        repaint();
                    }
                    break;
                case KeyEvent.VK_DOWN:
                    moveMouse(0, 10, e);
                    if (startPoint!=null) {
                        endPoint = e.getComponent().getMousePosition();
                        updateTempShape();
                        repaint();
                    }
                    break;
                case KeyEvent.VK_LEFT:
                    moveMouse(-10, 0, e);
                    if (startPoint!=null) {
                        endPoint = e.getComponent().getMousePosition();
                        updateTempShape();
                    }
                    repaint();
                    break;
                case KeyEvent.VK_RIGHT:
                    moveMouse(10, 0, e);
                    if (startPoint!=null) {
                        endPoint = e.getComponent().getMousePosition();
                        updateTempShape();
                    }
                    repaint();
                    break;
            }
        }
        private void moveMouse(int deltaX, int deltaY, KeyEvent e) {
            Point currentMouseLocation = MouseInfo.getPointerInfo().getLocation();
            int newX = currentMouseLocation.x + deltaX;
            int newY = currentMouseLocation.y + deltaY;
            robot.mouseMove(newX,newY);
        }

        private void clearTempShape() {
            currentTempShape = null;
            repaint();
        }
        private void updateTempShape() {
            // 根据当前鼠标位置更新临时形状
            switch (currentShape) {
                case LINE:
                    drawLine(false);
                    break;
                case ARC:
                    drawArc(false);
                    break;
                case RECTANGLE:
                    drawRectangle(false);
                    break;
            }
        }

        public void setImage(BufferedImage image) {
            this.image = image;
        }
        private void drawShape() {
            switch (currentShape) {
                case POINT:
                    drawPoint();
                    break;
                case LINE:
                    drawLine(true);
                    break;
                case ARC:
                    drawArc(true);
                    break;
                case RECTANGLE:
                    drawRectangle(true);
                    break;
            }
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2d = (Graphics2D) g;

            // 绘制加载的图像
            if (image != null) {
                g2d.drawImage(image, 0, 0, getWidth(), getHeight(), this);
            }

            for (int i = 0; i < shapes.size(); i++) {
                g2d.setColor(shapeColors.get(i));
                g2d.setStroke(shapeStrokes.get(i));
                g2d.draw(shapes.get(i));
            }
            // 在拖动过程中绘制临时形状
            if (currentTempShape != null) {
                g2d.setColor(currentColor);
                g2d.setStroke(currentStroke);
                g2d.draw(currentTempShape);
            }
            requestFocusInWindow();
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                new Painting().setVisible(true);
            }
        });
    }
}

