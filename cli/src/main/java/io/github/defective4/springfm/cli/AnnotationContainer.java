package io.github.defective4.springfm.cli;

import com.google.gson.Gson;

import io.github.defective4.springfm.server.data.AudioAnnotation;

public class AnnotationContainer {
    private static final Gson GSON = new Gson();
    private final String description;
    private final boolean isMusic;
    private final long timestamp = System.currentTimeMillis();

    private final String title;

    public AnnotationContainer(AudioAnnotation annotation) {
        this(annotation.getTitle(), annotation.getDescription(), !annotation.isNonMusic());
    }

    public AnnotationContainer(String title, String description, boolean isMusic) {
        this.title = title;
        this.description = description;
        this.isMusic = isMusic;
    }

    public String getDescription() {
        return description;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public String getTitle() {
        return title;
    }

    public boolean isMusic() {
        return isMusic;
    }

    public String toJSON() {
        return GSON.toJson(this);
    }

    @Override
    public String toString() {
        return "AnnotationContainer [timestamp=" + timestamp + ", title=" + title + ", description=" + description
                + ", isMusic=" + isMusic + "]";
    }
}
