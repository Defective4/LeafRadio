package io.github.defective4.springfm.client;

import java.net.URL;

import io.github.defective4.springfm.client.audio.AudioPlayer;
import io.github.defective4.springfm.server.data.AuthResponse;
import io.github.defective4.springfm.server.data.ProfileInformation;

public class Main {
    public static void main(String[] args) {
        try {
            SpringFMClient client = new SpringFMClient(new URL("http://localhost:8080/"));
            AudioPlayer player = new AudioPlayer(client);
            AuthResponse auth = client.auth();
            ProfileInformation profile = auth.getProfiles().get(0);
            client.setService(profile.getName(), 0);
            player.start(profile.getName());

            synchronized (Main.class) {
                Main.class.wait();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
