package io.github.defective4.springfm.client.event;

import io.github.defective4.springfm.server.data.AudioAnnotation;

public interface PlayerEventListener {
    void audioAnnotationReceived(AudioAnnotation annotation);

    void playerErrored(Exception e);
}
