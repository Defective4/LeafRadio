package io.github.defective4.springfm.client.event;

import java.io.DataInputStream;
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
import io.github.defective4.springfm.server.data.ProfileInformation;
import io.github.defective4.springfm.server.packet.Packet;

public class RadioPlayer {
    private SpringFMClient client;
    private final ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor();
    private final Gson gson = new Gson();
    private final PlayerEventListener listener;
    private ProfileInformation profile;
    private SourceDataLine sdl;
    private Future<?> task;

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
        listener.audioAnnotationReceived(new AudioAnnotation(null, null));
        task = executor.submit(() -> {
            try (DataInputStream in = new DataInputStream(client.connect(profile.getName()))) {
                while (!task.isCancelled()) {
                    Packet packet = Packet.fromStream(in);
                    if (packet.getType() == Packet.TYPE_PAYLOAD) {
                        JsonObject root = packet.getPayloadAsJSON();
                        String key = root.get("key").getAsString();
                        JsonElement payloadElement = root.get("payload");
                        switch (key.toLowerCase()) {
                            case "annotation" -> {
                                AudioAnnotation annotation = gson.fromJson(payloadElement, AudioAnnotation.class);
                                if (annotation != null)
                                    Display.getDefault().asyncExec(() -> listener.audioAnnotationReceived(annotation));
                            }
                            default -> {}
                        }
                    } else {
                        sdl.write(packet.getPayload(), 0, packet.getPayload().length);
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
                stop();
                Display.getDefault().asyncExec(() -> listener.playerErrored(e));
            }
        });
    }

    public void stop() {
        try {
            if (task != null) task.cancel(true);
            if (sdl != null) sdl.close();
        } finally {
            task = null;
            sdl = null;
            profile = null;
        }
    }

}
