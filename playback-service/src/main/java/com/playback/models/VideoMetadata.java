package com.playback.models;

import com.playback.standards.AudioEncoding;
import com.playback.standards.Bitrate;
import com.playback.standards.VideoEncoding;
import com.playback.standards.VideoResolution;

import java.util.Objects;

public class VideoMetadata {
    private final String title;
    private final VideoEncoding videoEncoding;
    private final VideoResolution videoResolution;
    private final AudioEncoding audioEncoding;
    private final Bitrate bitrate;

    public VideoMetadata(String title, VideoEncoding videoEncoding, VideoResolution videoResolution, AudioEncoding audioEncoding, Bitrate bitrate) {
        this.title = title;
        this.videoEncoding = videoEncoding;
        this.videoResolution = videoResolution;
        this.audioEncoding = audioEncoding;
        this.bitrate = bitrate;
    }

    public String getTitle() {
        return title;
    }

    public VideoEncoding getVideoEncoding() {
        return videoEncoding;
    }

    public VideoResolution getVideoResolution() {
        return videoResolution;
    }

    public AudioEncoding getAudioEncoding() {
        return audioEncoding;
    }

    public Bitrate getBitrate() {
        return bitrate;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        VideoMetadata that = (VideoMetadata) o;
        return Objects.equals(title, that.title) &&
                videoEncoding == that.videoEncoding &&
                videoResolution == that.videoResolution &&
                audioEncoding == that.audioEncoding &&
                bitrate == that.bitrate;
    }

    @Override
    public int hashCode() {
        return Objects.hash(title, videoEncoding, videoResolution, audioEncoding, bitrate);
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("VideoMetadata{");
        sb.append("title='").append(title).append('\'');
        sb.append(", videoEncoding=").append(videoEncoding);
        sb.append(", videoResolution=").append(videoResolution);
        sb.append(", audioEncoding=").append(audioEncoding);
        sb.append(", bitrate=").append(bitrate);
        sb.append('}');
        return sb.toString();
    }
}
