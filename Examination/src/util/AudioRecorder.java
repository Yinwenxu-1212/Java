package util;
import javax.sound.sampled.*;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;

import java.io.*;

public class AudioRecorder {
    private static boolean isRecording = false;

    public static void main(String[] args) {
        recordAudio("output.wav");
    }

    public static void recordAudio(String filePath) {
        try {
            isRecording = true;

            AudioFormat format = new AudioFormat(AudioFormat.Encoding.PCM_SIGNED, 44100, 16, 2, 4, 44100, false);
            DataLine.Info info = new DataLine.Info(TargetDataLine.class, format);

            if (!AudioSystem.isLineSupported(info)) {
                System.out.println("不支持该行");
                return;
            }

            TargetDataLine line = (TargetDataLine) AudioSystem.getLine(info);
            line.open(format);
            line.start();

            System.out.println("录音中...");

            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();

            // 缓冲区大小决定一次读取多少字节
            byte[] buffer = new byte[4096];
            int bytesRead;

            while (isRecording) {
                bytesRead = line.read(buffer, 0, buffer.length);
                byteArrayOutputStream.write(buffer, 0, bytesRead);
            }

            // 录音完成后停止并关闭该线路
            line.stop();
            line.close();
            System.out.println("录音完成。文件保存到：" + filePath);

            // 将录制的音频数据转换为字节数组
            byte[] audioData = byteArrayOutputStream.toByteArray();

            // 将字节数组保存到WAV文件
            saveByteArrayToWavFile(audioData, format, filePath);

        } catch (LineUnavailableException e) {
            e.printStackTrace();
        }
    }

    private static void saveByteArrayToWavFile(byte[] audioData, AudioFormat format, String filePath) {
        try {
            AudioInputStream audioInputStream = new AudioInputStream(new ByteArrayInputStream(audioData), format,
                    audioData.length / format.getFrameSize());

            AudioSystem.write(audioInputStream, AudioFileFormat.Type.WAVE, new File(filePath));
            audioInputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void stopRecording() {
        isRecording = false;
    }
}



