package io.github.defective4.springfm.client.audio;

import java.io.DataInputStream;
import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.Future;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.SourceDataLine;
import javax.sound.sampled.UnsupportedAudioFileException;

import io.github.defective4.springfm.client.SpringFMClient;
import io.github.defective4.springfm.client.utils.ThreadUtils;
import io.github.defective4.springfm.server.data.AudioAnnotation;
import io.github.defective4.springfm.server.data.PlayerCommand;
import io.github.defective4.springfm.server.data.SerializableAudioFormat;
import io.github.defective4.springfm.server.packet.Packet;
import io.github.defective4.springfm.server.packet.PacketPayload;

public class AudioPlayer {
    private DataInputStream audioInputStream, controlInputStream;
    private SourceDataLine audioSink;
    private Future<?> audioTask;
    private final SpringFMClient client;
    private Future<?> dataTask;
    private final List<AudioPlayerEventListener> listeners = new CopyOnWriteArrayList<>();

    public AudioPlayer(SpringFMClient client) {
        this.client = Objects.requireNonNull(client);
    }

    public boolean addListener(AudioPlayerEventListener listener) {
        return listeners.add(listener);
    }

    public List<AudioPlayerEventListener> getListeners() {
        return Collections.unmodifiableList(listeners);
    }

    public boolean isAlive() {
        return audioTask != null && dataTask != null && audioInputStream != null && controlInputStream != null
                && audioSink != null && !audioTask.isCancelled() && !dataTask.isCancelled() && audioSink.isActive();
    }

    public boolean removeListener(AudioPlayerEventListener listener) {
        return listeners.remove(listener);
    }

    public void start(String profile) throws IOException, UnsupportedAudioFileException, LineUnavailableException {
        AudioInputStream audioStream = client.openAudioStream(profile);
        reopenAudioSink(audioStream.getFormat());
        audioInputStream = new DataInputStream(audioStream);
        controlInputStream = client.openControlStream(profile);
        audioTask = ThreadUtils.submit(() -> {
            try {
                byte[] buffer = new byte[4096];
                while (isAlive()) {
                    audioInputStream.readFully(buffer);
                    if (SerializableAudioFormat.Codec.isSwitchFrame(buffer)) {
                        AudioFormat newFormat = SerializableAudioFormat.Codec.fromSwitchFrame(buffer);
                        reopenAudioSink(newFormat);
                        listeners.forEach(ls -> ls.audioFormatChanged(newFormat));
                    } else {
                        audioSink.write(buffer, 0, buffer.length);
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
                try {
                    stop();
                } catch (IOException e1) {
                    e1.printStackTrace();
                }
                listeners.forEach(ls -> ls.playerErrored(e));
            }
        });
        dataTask = ThreadUtils.submit(() -> {
            try {
                while (isAlive()) {
                    Packet packet = Packet.fromStream(controlInputStream);
                    PacketPayload payload = packet.getPayload();
                    switch (payload.getKey().toLowerCase()) {
                        case "annotation" -> {
                            AudioAnnotation annotation = payload.getPayloadAsObject(AudioAnnotation.class);
                            listeners.forEach(ls -> ls.annotationReceived(annotation));
                        }
                        case "command" -> {
                            PlayerCommand cmd = payload.getPayloadAsObject(PlayerCommand.class);
                            switch (cmd.getCommand()) {
                                case PlayerCommand.COMMAND_CHANGE_SERVICE -> {
                                    int index = Integer.parseInt(cmd.getData());
                                    listeners.forEach(ls -> ls.serviceChanged(index));
                                }
                                case PlayerCommand.COMMAND_ANALOG_TUNE -> {
                                    float freq = Float.parseFloat(cmd.getData());
                                    listeners.forEach(ls -> ls.analogTuned(freq));
                                }
                                case PlayerCommand.COMMAND_DIGITAL_TUNE -> {
                                    int index = Integer.parseInt(cmd.getData());
                                    listeners.forEach(ls -> ls.digitalTuned(index));
                                }
                                case PlayerCommand.COMMAND_ADJUST_GAIN -> {
                                    float newGain = Float.parseFloat(cmd.getData());
                                    listeners.forEach(ls -> ls.gainChanged(newGain));
                                }
                                default -> {}
                            }
                        }
                        default -> {}
                    }
                }
            } catch (Exception e2) {
                e2.printStackTrace();
                try {
                    stop();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                listeners.forEach(ls -> ls.playerErrored(e2));
            }
        });
    }

    public void stop() throws IOException {
        if (audioSink != null) audioSink.close();
        if (audioTask != null) audioTask.cancel(true);
        if (dataTask != null) dataTask.cancel(true);
        if (audioInputStream != null) audioInputStream.close();
        if (controlInputStream != null) controlInputStream.close();
        listeners.forEach(AudioPlayerEventListener::playerStopped);
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
