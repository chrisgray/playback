package com.playback.services;

import com.playback.clients.SteeringClient;
import com.playback.models.VideoMetadata;

import java.net.URL;
import java.util.Collection;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

public class SteeringService {
    private final SteeringClient client;
    private final UrlSigningService urlSigningService;

    public SteeringService(SteeringClient client, UrlSigningService urlSigningService) {
        this.client = client;
        this.urlSigningService = urlSigningService;
    }

    public Optional<Collection<URL>> fromVideoMetadata(VideoMetadata videoMetadata,
                                                       Function<Collection<URL>, Collection<URL>> reduce) {
        return client.fromVideoMetadata(videoMetadata)
                .map(reduce)
                .map((value) -> value
                        .stream()
                        .map(urlSigningService::expireEightHoursFromNow)
                        .collect(Collectors.toList()));
    }

    //Seems a bit arbitrary, but leaving it open to a better way to select the top 3. Perhaps based on geo-proximity to
    //the nearest region
    public Optional<Collection<URL>> abitraryLimitFirstThree(VideoMetadata videoMetadata) {
        return fromVideoMetadata(videoMetadata, SteeringService::limitFirstThree);
    }

    public static Collection<URL> limitFirstThree(Collection<URL> collection) {
        return collection.stream().limit(3).collect(Collectors.toList());
    }
}
