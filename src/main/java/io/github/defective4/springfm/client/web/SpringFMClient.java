package io.github.defective4.springfm.client.web;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;

import com.google.gson.Gson;
import com.google.gson.JsonParseException;

import io.github.defective4.springfm.server.data.AuthResponse;

public class SpringFMClient {
    private final String baseURL;
    private final Gson gson = new Gson();

    public SpringFMClient(URL baseURL) {
        this.baseURL = baseURL.toString() + "/";
    }

    public AuthResponse authenticate() throws IOException, JsonParseException {
        HttpURLConnection con = (HttpURLConnection) URI.create(baseURL + "auth").toURL().openConnection();
        try (Reader reader = new InputStreamReader(con.getInputStream())) {
            return gson.fromJson(reader, AuthResponse.class);
        }
    }
}
