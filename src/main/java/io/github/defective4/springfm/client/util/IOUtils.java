package io.github.defective4.springfm.client.util;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;

public class IOUtils {
    public static String readTextContent(InputStream in) throws IOException {
        StringBuilder builder = new StringBuilder();
        try (Reader reader = new InputStreamReader(in, StandardCharsets.UTF_8)) {
            while (true) {
                int read = reader.read();
                if (read < 0) break;
                builder.append((char) read);
            }
        }
        return builder.toString();
    }
}
