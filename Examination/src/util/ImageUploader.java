package util;

import javax.swing.*;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

public class ImageUploader {

    public static String uploadImage(JFrame parentFrame) {
        int choice = JOptionPane.showConfirmDialog(parentFrame, "是否需要上传图片?", "上传图片", JOptionPane.YES_NO_OPTION);

        if (choice == JOptionPane.YES_OPTION) {
            JFileChooser fileChooser = new JFileChooser();
            int result = fileChooser.showOpenDialog(parentFrame);

            if (result == JFileChooser.APPROVE_OPTION) {
                File selectedFile = fileChooser.getSelectedFile();

                // 复制文件到指定目录，并获取路径
                return copyImage(selectedFile);
            }
        }
        return null;
    }

    private static String copyImage(File sourceFile) {
        // 指定图像存储目录
        String destinationDirectory = "D:/project/Examination/image/directory/";

        try {
            // 生成新的文件名，避免重名
            String fileName = System.currentTimeMillis() + "_" + sourceFile.getName();
            File destinationFile = new File(destinationDirectory + fileName);

            // 复制文件到指定目录
            Files.copy(sourceFile.toPath(), destinationFile.toPath());

            // 返回保存的路径
            return destinationFile.getAbsolutePath();
        } catch (IOException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(null, "图像上传失败：" + e.getMessage());
            return null;
        }
    }
}
