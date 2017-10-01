package com.playback.services;

import com.playback.models.VideoMetadata;
import com.playback.standards.AudioEncoding;
import com.playback.standards.Bitrate;
import com.playback.standards.VideoEncoding;
import com.playback.standards.VideoResolution;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class VideoMetadataService {
    private final Map<String, Collection<VideoMetadata>> metadata;

    private VideoMetadataService(Map<String, Collection<VideoMetadata>> metadata) {
        this.metadata = metadata;
    }

    public Map<String, Collection<VideoMetadata>> getMetadata() {
        return metadata;
    }

    public static VideoMetadataService create() {
        return new VideoMetadataService(populate());
    }

    //This seems pretty arbitrary at the moment. Perhaps a better selection would be based on available bandidth, etc.
    //That or simply let the client decide.
    public static Collection<VideoMetadata> limitBitRates(Collection<VideoMetadata> values) {
         return values
                 .stream()
                 .collect(Collectors.groupingBy((value) -> String.format("%s%s%s",
                         value.getAudioEncoding(),
                         value.getVideoEncoding(),
                         value.getVideoResolution())))
                 .entrySet()
                 .stream()
                 .flatMap((value) -> value.getValue().stream().limit(2))
                 .collect(Collectors.toList());
    }

    private static Map<String, Collection<VideoMetadata>> populate() {
        return Stream.of("Title 1", "Title 2", "Title 3")
                .collect(Collectors.toMap(Function.identity(), VideoMetadataService::generateVideoMetadata));
    }

    private static Collection<VideoMetadata> generateVideoMetadata(String title) {
        Collection<VideoMetadata> value = new ArrayList<>();
        for (VideoEncoding videoEncoding : VideoEncoding.values()) {
            for (VideoResolution videoResolution : VideoResolution.values()) {
                for (AudioEncoding audioEncoding : AudioEncoding.values()) {
                    for (Bitrate bitrate : Bitrate.values()) {
                        value.add(new VideoMetadata(title, videoEncoding, videoResolution, audioEncoding, bitrate));
                    }
                }
            }
        }
        return value;
    }
}
