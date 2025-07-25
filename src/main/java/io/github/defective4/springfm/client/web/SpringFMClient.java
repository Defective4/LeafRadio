package io.github.defective4.springfm.client.web;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.Writer;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URI;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Map;
import java.util.Map.Entry;

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
        HttpURLConnection con = createGetHttpConnection("auth");
        try (Reader reader = new InputStreamReader(con.getInputStream())) {
            return gson.fromJson(reader, AuthResponse.class);
        }
    }

    public InputStream connectAudioChannel(String profile) throws IOException {
        HttpURLConnection con = createGetHttpConnection("profile/" + profile + "/audio");
        InputStream in = con.getInputStream();
        in.skip(44);
        return in;
    }

    public InputStream connectDataChannel(String profile) throws IOException {
        HttpURLConnection con = createGetHttpConnection("profile/" + profile + "/data");
        return con.getInputStream();
    }

    public void digitalTune(String profile, int index) throws IOException {
        HttpURLConnection con = createPostHttpConnection("profile/" + profile + "/tune/digital",
                Map.of("index", Integer.toString(index)));

        if (con.getResponseCode() >= 400) throw new IOException(con.getResponseMessage());
    }

    public void setService(String profile, int index) throws IOException {
        HttpURLConnection con = createPostHttpConnection("profile/" + profile + "/service",
                Map.of("index", Integer.toString(index)));

        if (con.getResponseCode() >= 400) throw new IOException(con.getResponseMessage());
    }

    private HttpURLConnection createGetHttpConnection(String suburl)
            throws IOException, MalformedURLException, ProtocolException {
        return createHttpConnection(suburl, "get", null);
    }

    private HttpURLConnection createHttpConnection(String suburl, String method, Map<String, String> args)
            throws IOException, MalformedURLException, ProtocolException {
        HttpURLConnection con = (HttpURLConnection) URI.create(baseURL + suburl).toURL().openConnection();
        con.setRequestMethod(method.toUpperCase());
        if (method.equalsIgnoreCase("post")) {
            con.setDoOutput(true);
            con.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
            String[] pairs = new String[args.size()];
            int index = 0;
            for (Entry<String, String> entry : args.entrySet()) {
                pairs[index++] = entry.getKey() + "=" + URLEncoder.encode(entry.getValue());
            }
            try (Writer writer = new OutputStreamWriter(con.getOutputStream())) {
                writer.write(String.join("&", pairs));
            }
        }
        return con;
    }

    private HttpURLConnection createPostHttpConnection(String suburl, Map<String, String> args)
            throws IOException, MalformedURLException, ProtocolException {
        return createHttpConnection(suburl, "post", args);
    }
}
