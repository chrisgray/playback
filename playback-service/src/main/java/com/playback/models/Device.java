package com.playback.models;

import com.playback.standards.AudioEncoding;
import com.playback.standards.VideoEncoding;

import java.util.Objects;

public class Device {
    private final VideoEncoding supportedVideoEncoding;
    private final AudioEncoding supportedAudioEncoding;

    public Device(VideoEncoding supportedVideoEncoding, AudioEncoding supportedAudioEncoding) {
        this.supportedVideoEncoding = supportedVideoEncoding;
        this.supportedAudioEncoding = supportedAudioEncoding;
    }

    public VideoEncoding getSupportedVideoEncoding() {
        return supportedVideoEncoding;
    }

    public AudioEncoding getSupportedAudioEncoding() {
        return supportedAudioEncoding;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Device device = (Device) o;
        return supportedVideoEncoding == device.supportedVideoEncoding &&
                supportedAudioEncoding == device.supportedAudioEncoding;
    }

    @Override
    public int hashCode() {
        return Objects.hash(supportedVideoEncoding, supportedAudioEncoding);
    }
}
