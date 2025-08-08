package io.github.defective4.springfm.client;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.Writer;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.UnsupportedAudioFileException;

import com.google.gson.Gson;

import io.github.defective4.springfm.server.data.AuthResponse;

public class SpringFMClient {
    private final String baseURL;
    private final Gson gson = new Gson();

    public SpringFMClient(URL baseURL) {
        String base = baseURL.toExternalForm();
        if (baseURL.getPath().isEmpty()) base += "/";
        this.baseURL = base;
    }

    public void analogTune(String profile, int freq) throws IOException {
        prepareConnection("profile/" + profile + "/tune/analog", Map.of("frequency", freq));
    }

    public AuthResponse auth() throws IOException {
        HttpURLConnection connection = prepareConnection("auth");
        try (Reader reader = new InputStreamReader(connection.getInputStream(), StandardCharsets.UTF_8)) {
            return gson.fromJson(reader, AuthResponse.class);
        }
    }

    public String getBaseURL() {
        return baseURL;
    }

    public Gson getGson() {
        return gson;
    }

    public AudioInputStream openAudioStream(String profile) throws IOException, UnsupportedAudioFileException {
        DataInputStream in = new DataInputStream(prepareConnection("profile/" + profile + "/audio").getInputStream());
        byte[] header = new byte[44];
        in.readFully(header);
        AudioFormat format = AudioSystem.getAudioFileFormat(new ByteArrayInputStream(header)).getFormat();
        return new AudioInputStream(in, format, AudioSystem.NOT_SPECIFIED);
    }

    public DataInputStream openControlStream(String profile) throws IOException {
        return new DataInputStream(prepareConnection("profile/" + profile + "/data").getInputStream());
    }

    public void setService(String profile, int index) throws IOException {
        prepareConnection("profile/" + profile + "/service", Map.of("index", index));
    }

    private HttpURLConnection prepareConnection(String path) throws IOException, MalformedURLException {
        return prepareConnection(path, null);
    }

    private HttpURLConnection prepareConnection(String path, Map<String, Object> post)
            throws IOException, MalformedURLException {
        HttpURLConnection con = (HttpURLConnection) URI.create(baseURL + path).toURL().openConnection();
        if (post != null) {
            con.setRequestMethod("POST");
            con.setDoOutput(true);
            List<String> params = new ArrayList<>();
            for (Entry<String, Object> entry : post.entrySet()) {
                params.add(entry.getKey() + "="
                        + URLEncoder.encode(String.valueOf(entry.getValue()), StandardCharsets.UTF_8));
            }
            try (Writer writer = new OutputStreamWriter(con.getOutputStream())) {
                writer.write(String.join("&", params.toArray(new String[0])));
            }
        }
        if (con.getResponseCode() >= 300) {
            StringBuilder builder = new StringBuilder();
            try (Reader reader = new InputStreamReader(con.getErrorStream(), StandardCharsets.UTF_8)) {
                while (true) {
                    int read = reader.read();
                    if (read < 0) break;
                    builder.append((char) read);
                }
            }
            throw new IOException(builder.toString());
        }
        return con;
    }
}
