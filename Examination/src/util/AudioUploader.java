package util;

import javax.swing.*;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

public class AudioUploader {

    public static String uploadAudio(JFrame parentFrame) {
        int choice = JOptionPane.showConfirmDialog(parentFrame, "是否需要上传音频?", "上传音频", JOptionPane.YES_NO_OPTION);

        if (choice == JOptionPane.YES_OPTION) {
            JFileChooser fileChooser = new JFileChooser();
            int result = fileChooser.showOpenDialog(parentFrame);

            if (result == JFileChooser.APPROVE_OPTION) {
                File selectedFile = fileChooser.getSelectedFile();

                // 复制文件到指定目录，并获取路径
                return copyAudio(selectedFile);
            }
        }
        return null;
    }

    private static String copyAudio(File sourceFile) {
        // 指定音频存储目录
        String destinationDirectory = "D:/project/Examination/audio/directory/";

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
            JOptionPane.showMessageDialog(null, "音频上传失败：" + e.getMessage());
            return null;
        }
    }
}
