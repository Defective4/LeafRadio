package io.github.defective4.springfm.client;

import java.io.DataInputStream;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.SourceDataLine;

import io.github.defective4.springfm.client.web.SpringFMClient;
import io.github.defective4.springfm.server.data.ProfileInformation;
import io.github.defective4.springfm.server.packet.Packet;

public class RadioPlayer {
    private SpringFMClient client;
    private final ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor();
    private SourceDataLine sdl;
    private Future<?> task;

    public SpringFMClient getClient() {
        return client;
    }

    public void setClient(SpringFMClient client) {
        this.client = client;
    }

    public void start(ProfileInformation profile) throws LineUnavailableException {
        stop();
        sdl = AudioSystem.getSourceDataLine(new AudioFormat(171e3f, 16, 1, true, false));
        sdl.open();
        sdl.start();
        task = executor.submit(() -> {
            try (DataInputStream in = new DataInputStream(client.connect(profile.getName()))) {
                while (!task.isCancelled()) {
                    Packet packet = Packet.fromStream(in);
                    if (packet.getType() == Packet.TYPE_PAYLOAD) {} else {
                        sdl.write(packet.getPayload(), 0, packet.getPayload().length);
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
                stop();
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
        }
    }

}
