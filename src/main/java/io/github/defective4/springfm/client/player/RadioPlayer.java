package io.github.defective4.springfm.client.player;

import java.io.DataInputStream;
import java.io.InputStream;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.SourceDataLine;

import org.eclipse.swt.widgets.Display;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import io.github.defective4.springfm.client.web.SpringFMClient;
import io.github.defective4.springfm.server.data.AudioAnnotation;
import io.github.defective4.springfm.server.data.PlayerCommand;
import io.github.defective4.springfm.server.data.ProfileInformation;
import io.github.defective4.springfm.server.packet.Packet;

public class RadioPlayer {
    private Future<?> audioTask, dataTask;
    private SpringFMClient client;
    private final ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor();
    private final Gson gson = new Gson();
    private final PlayerEventListener listener;
    private ProfileInformation profile;
    private SourceDataLine sdl;

    public RadioPlayer(PlayerEventListener listener) {
        this.listener = listener;
    }

    public SpringFMClient getClient() {
        return client;
    }

    public ProfileInformation getProfile() {
        return profile;
    }

    public void setClient(SpringFMClient client) {
        this.client = client;
    }

    public void start(ProfileInformation profile) throws LineUnavailableException {
        stop();
        sdl = AudioSystem.getSourceDataLine(new AudioFormat(171e3f, 16, 1, true, false));
        sdl.open();
        sdl.start();
        this.profile = profile;
        Display.getDefault().asyncExec(() -> listener.audioAnnotationReceived(new AudioAnnotation(null, null)));

        audioTask = executor.submit(() -> {
            try (InputStream audioIn = client.connectAudioChannel(profile.getName())) {
                byte[] buffer = new byte[4096];
                while (audioTask != null && !audioTask.isCancelled()) {
                    int read = audioIn.read(buffer);
                    sdl.write(buffer, 0, read);
                }
            } catch (Exception e) {
                e.printStackTrace();
                stop();
                Display.getDefault().asyncExec(() -> listener.playerErrored(e));
            }
        });
        dataTask = executor.submit(() -> {
            try (DataInputStream in = new DataInputStream(client.connectDataChannel(profile.getName()))) {
                while (dataTask != null && !dataTask.isCancelled()) {
                    Packet packet = Packet.fromStream(in);
                    JsonObject root = packet.getPayloadAsJSON();
                    String key = root.get("key").getAsString();
                    JsonElement payloadElement = root.get("payload");
                    switch (key.toLowerCase()) {
                        case "annotation" -> {
                            AudioAnnotation annotation = gson.fromJson(payloadElement, AudioAnnotation.class);
                            if (annotation != null)
                                Display.getDefault().asyncExec(() -> listener.audioAnnotationReceived(annotation));
                        }
                        case "command" -> {
                            PlayerCommand command = gson.fromJson(payloadElement, PlayerCommand.class);
                            try {
                                switch (command.getCommand()) {
                                    case PlayerCommand.COMMAND_CHANGE_SERVICE -> {
                                        int index = Integer.parseInt(command.getData());
                                        Display.getDefault().asyncExec(() -> listener.serviceChanged(index));
                                    }
                                    case PlayerCommand.COMMAND_DIGITAL_TUNE -> {
                                        int index = Integer.parseInt(command.getData());
                                        Display.getDefault().asyncExec(() -> listener.digitalTune(index));
                                    }
                                    case PlayerCommand.COMMAND_ANALOG_TUNE -> {
                                        int freq = Integer.parseInt(command.getData());
                                        Display.getDefault().asyncExec(() -> listener.analogTune(freq));
                                    }

                                    default -> {}
                                }
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                        default -> {}
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
                stop();
                Display.getDefault().asyncExec(() -> listener.playerErrored(e));
            }
        });
    }

    public synchronized void stop() {
        try {
            if (audioTask != null) audioTask.cancel(true);
            if (dataTask != null) dataTask.cancel(true);
            if (sdl != null) sdl.close();
        } finally {
            audioTask = null;
            dataTask = null;
            sdl = null;
            profile = null;
        }
    }

}
