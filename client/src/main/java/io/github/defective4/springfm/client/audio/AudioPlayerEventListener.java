package io.github.defective4.springfm.client.audio;

import javax.sound.sampled.AudioFormat;

import io.github.defective4.springfm.server.data.AudioAnnotation;

public interface AudioPlayerEventListener {
    void analogTuned(float frequency);

    void annotationReceived(AudioAnnotation annotation);

    void audioFormatChanged(AudioFormat newFormat);

    void digitalTuned(int index);

    void gainChanged(float newGain);

    void playerErrored(Exception ex);

    void playerStopped();

    void serviceChanged(int serviceIndex);
}
