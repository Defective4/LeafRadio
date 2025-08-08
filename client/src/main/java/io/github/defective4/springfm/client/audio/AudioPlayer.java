package io.github.defective4.springfm.client.audio;

import java.io.DataInputStream;
import java.io.IOException;
import java.util.Objects;
import java.util.concurrent.Future;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.SourceDataLine;
import javax.sound.sampled.UnsupportedAudioFileException;

import io.github.defective4.springfm.client.SpringFMClient;
import io.github.defective4.springfm.client.utils.ThreadUtils;
import io.github.defective4.springfm.server.data.SerializableAudioFormat;
import io.github.defective4.springfm.server.packet.Packet;

public class AudioPlayer {
    private DataInputStream audioInputStream, controlInputStream;
    private SourceDataLine audioSink;
    private Future<?> audioTask;
    private final SpringFMClient client;

    public AudioPlayer(SpringFMClient client) {
        this.client = Objects.requireNonNull(client);
    }

    public void start(String profile) throws IOException, UnsupportedAudioFileException, LineUnavailableException {
        AudioInputStream audioStream = client.openAudioStream(profile);
        reopenAudioSink(audioStream.getFormat());
        audioInputStream = new DataInputStream(audioStream);
        controlInputStream = client.openControlStream(profile);
        audioTask = ThreadUtils.submit(() -> {
            try {
                byte[] buffer = new byte[4096];
                while (true) { // TODO isAlive
                    audioInputStream.readFully(buffer);
                    if (SerializableAudioFormat.Codec.isSwitchFrame(buffer)) {
                        reopenAudioSink(SerializableAudioFormat.Codec.fromSwitchFrame(buffer));
                    } else {
                        audioSink.write(buffer, 0, buffer.length);
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
                // TODO stop();
            }
        });
        ThreadUtils.submit(() -> {
            try {
                while (true) { // TODO isAlive
                    Packet packet = Packet.fromStream(controlInputStream);
                    // TODO handle packets
                }
            } catch (Exception e2) {
                e2.printStackTrace();
                // TODO stop();
            }
        });
    }

    private void reopenAudioSink(AudioFormat fmt) throws LineUnavailableException {
        if (audioSink != null) {
            audioSink.flush();
            audioSink.stop();
            audioSink.close();
        }
        audioSink = AudioSystem.getSourceDataLine(fmt);
        audioSink.open();
        audioSink.start();
    }
}
