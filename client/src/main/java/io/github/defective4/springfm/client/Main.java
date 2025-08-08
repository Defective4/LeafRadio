package io.github.defective4.springfm.client;

import java.net.URL;

import javax.sound.sampled.AudioFormat;

import io.github.defective4.springfm.client.audio.AudioPlayer;
import io.github.defective4.springfm.client.audio.AudioPlayerEventListener;
import io.github.defective4.springfm.server.data.AudioAnnotation;
import io.github.defective4.springfm.server.data.AuthResponse;
import io.github.defective4.springfm.server.data.ProfileInformation;

public class Main {
    public static void main(String[] args) {
        try {
            SpringFMClient client = new SpringFMClient(new URL("http://localhost:8080/"));
            AudioPlayer player = new AudioPlayer(client);

            player.addListener(new AudioPlayerEventListener() {

                @Override
                public void analogTuned(float freq) {
                    System.out.println("Service frequency changed to " + freq + " Hz");
                }

                @Override
                public void annotationReceived(AudioAnnotation annotation) {
                    System.out.println("Audio annotation: " + annotation);
                }

                @Override
                public void audioFormatChanged(AudioFormat newFormat) {
                    System.out.println("Audio format changed mid-stream: " + newFormat);
                }

                @Override
                public void digitalTuned(int index) {
                    System.out.println("Digital service tuned to index " + index);
                }

                @Override
                public void gainChanged(float newGain) {
                    System.out.println("Gain changed: " + newGain + " db");
                }

                @Override
                public void serviceChanged(int index) {
                    System.out.println("Service changed to #" + index);
                }
            });

            AuthResponse auth = client.auth();
            ProfileInformation profile = auth.getProfiles().get(0);
            player.start(profile.getName());

            synchronized (Main.class) {
                Main.class.wait();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
