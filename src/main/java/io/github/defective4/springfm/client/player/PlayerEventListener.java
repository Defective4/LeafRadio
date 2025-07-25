package io.github.defective4.springfm.client.player;

import io.github.defective4.springfm.server.data.AudioAnnotation;

public interface PlayerEventListener {
    void analogTune(int freq);

    void audioAnnotationReceived(AudioAnnotation annotation);

    void digitalTune(int index);

    void playerErrored(Exception e);

    void serviceChanged(int index);
}
