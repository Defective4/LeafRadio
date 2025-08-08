package io.github.defective4.springfm.client.audio;

import javax.sound.sampled.AudioFormat;

import io.github.defective4.springfm.server.data.AudioAnnotation;

public abstract class AudioPlayerEventAdapter implements AudioPlayerEventListener {

    @Override
    public void analogTuned(float frequency) {
    }

    @Override
    public void annotationReceived(AudioAnnotation annotation) {
    }

    @Override
    public void audioFormatChanged(AudioFormat newFormat) {
    }

    @Override
    public void digitalTuned(int index) {
    }

    @Override
    public void gainChanged(float newGain) {
    }

    @Override
    public void serviceChanged(int serviceIndex) {
    }

}
