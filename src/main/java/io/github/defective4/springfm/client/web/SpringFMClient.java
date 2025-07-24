package io.github.defective4.springfm.client.web;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.Writer;
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

    public InputStream connectAudioChannel(String profile) throws IOException {
        HttpURLConnection con = (HttpURLConnection) URI.create(baseURL + "profile/" + profile + "/audio").toURL()
                .openConnection();
        return con.getInputStream();
    }

    public InputStream connectDataChannel(String profile) throws IOException {
        HttpURLConnection con = (HttpURLConnection) URI.create(baseURL + "profile/" + profile + "/data").toURL()
                .openConnection();
        return con.getInputStream();
    }

    public void setService(String profile, int index) throws IOException {
        HttpURLConnection con = (HttpURLConnection) URI.create(baseURL + "profile/" + profile + "/service").toURL()
                .openConnection();
        con.setDoOutput(true);
        con.setRequestMethod("POST");
        con.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
        try (Writer writer = new OutputStreamWriter(con.getOutputStream())) {
            writer.write("index=" + index);
        }

        if (con.getResponseCode() >= 400) throw new IOException(con.getResponseMessage());
    }
}
