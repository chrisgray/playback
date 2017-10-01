package com.playback.models;

import java.net.URL;
import java.util.Collection;
import java.util.Objects;

public class Stream {
    private final VideoMetadata videoMetadata;
    private final Collection<URL> urls;

    public Stream(VideoMetadata videoMetadata, Collection<URL> urls) {
        this.videoMetadata = videoMetadata;
        this.urls = urls;
    }

    public VideoMetadata getVideoMetadata() {
        return videoMetadata;
    }

    public Collection<URL> getUrls() {
        return urls;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Stream stream = (Stream) o;
        return Objects.equals(videoMetadata, stream.videoMetadata) &&
                Objects.equals(urls, stream.urls);
    }

    @Override
    public int hashCode() {
        return Objects.hash(videoMetadata, urls);
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("Stream{");
        sb.append("videoMetadata=").append(videoMetadata);
        sb.append(", urls=").append(urls);
        sb.append('}');
        return sb.toString();
    }
}
