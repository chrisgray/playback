package com.playback.models;

import java.util.Collection;
import java.util.Objects;

public class PlaybackManifest {
    private final Collection<Stream> streams;

    public PlaybackManifest(Collection<Stream> streams) {
        this.streams = streams;
    }

    public Collection<Stream> getStreams() {
        return streams;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PlaybackManifest that = (PlaybackManifest) o;
        return Objects.equals(streams, that.streams);
    }

    @Override
    public int hashCode() {
        return Objects.hash(streams);
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("PlaybackManifest{");
        sb.append("streams=").append(streams);
        sb.append('}');
        return sb.toString();
    }
}
