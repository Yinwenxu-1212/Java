package util;

import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Image;
import javax.swing.ImageIcon;
import javax.swing.JPanel;

public class ImagePanel extends JPanel {
    private Image backgroundImage;

    public ImagePanel(String imagePath) {
        backgroundImage = new ImageIcon(imagePath).getImage();

        // Adjust the size based on screen width
        int screenWidth = java.awt.Toolkit.getDefaultToolkit().getScreenSize().width;
        int newWidth = Math.min(backgroundImage.getWidth(null), screenWidth);
        int newHeight = (int) ((double) newWidth / backgroundImage.getWidth(null) * backgroundImage.getHeight(null));

        Dimension size = new Dimension(newWidth, newHeight);
        setPreferredSize(size);
        setMinimumSize(size);
        setMaximumSize(size);
        setSize(size);
        setLayout(null);
    }


    @Override
    protected void paintComponent(Graphics g) {
        // Draw the background image
        super.paintComponent(g);
        g.drawImage(backgroundImage, 0, 0, this);
    }
}
