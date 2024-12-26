package util;

import javazoom.jl.decoder.JavaLayerException;
import javazoom.jl.player.advanced.AdvancedPlayer;

import java.io.FileInputStream;
import java.io.IOException;

public class MP3Player {
    private static AdvancedPlayer player;

    public static void playMP3(String mp3FilePath) {
        try {
            FileInputStream fileInputStream = new FileInputStream(mp3FilePath);
            player = new AdvancedPlayer(fileInputStream);


            // 播放
            new Thread(() -> {
                try {
                    player.play();
                } catch (JavaLayerException e) {
                    e.printStackTrace();
                }
            }).start();
        } catch (JavaLayerException | IOException e) {
            e.printStackTrace();
        }
    }

    public static void stopMP3() {
        if (player != null) {
            player.close();
        }
    }

    public static void main(String[] args) {
        // 播放 MP3 文件示例
        playMP3("path/to/your/mp3/file.mp3");

        // 停止播放示例
        stopMP3();
    }
}
