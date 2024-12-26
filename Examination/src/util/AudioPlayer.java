package util;

import javax.sound.sampled.*;
import java.io.File;
import java.io.IOException;

public class AudioPlayer {

    private static Clip clip;

    public static void playAudio(String audioPath) {
        try {
            File audioFile = new File(audioPath);
            if (audioFile.exists()) {
                AudioInputStream audioInputStream = AudioSystem.getAudioInputStream(audioFile);
                clip = AudioSystem.getClip();
                clip.open(audioInputStream);
                clip.start();
            } else {
                System.out.println("音频文件不存在：" + audioPath);
            }
        } catch (UnsupportedAudioFileException | LineUnavailableException | IOException e) {
            e.printStackTrace();
            System.out.println("播放音频时发生错误：" + e.getMessage());
        }
    }

    public static void stopAudio() {
        if (clip != null && clip.isRunning()) {
            clip.stop();
        }
    }

    public static void main(String[] args) {
        // 播放音频示例
        playAudio("path/to/your/audio/file.wav");

        // 等待一段时间，模拟播放过程
        try {
            Thread.sleep(5000); // 5秒
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        // 停止音频播放
        stopAudio();
    }
}
