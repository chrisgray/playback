package com.playback.clients;

import com.playback.models.VideoMetadata;

import java.net.URL;
import java.util.Collection;
import java.util.Optional;

public interface SteeringClient {
    //Can consider adding location to the request to the SteeringClient so that it can
    //provide URLs optimized for that location based on geo-proximity
    Optional<Collection<URL>> fromVideoMetadata(VideoMetadata videoMetadata);
}
