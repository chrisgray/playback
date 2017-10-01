package com.playback.services.tests;

import com.playback.models.VideoMetadata;
import com.playback.services.VideoMetadataService;
import com.playback.standards.AudioEncoding;
import com.playback.standards.Bitrate;
import com.playback.standards.VideoEncoding;
import org.junit.Test;

import java.util.Map;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;


public class VideoMetadataServiceTest {
    private final VideoMetadataService videoMetadataService = VideoMetadataService.create();

    @Test
    public void notEmpty() {
        assertThat(videoMetadataService.getMetadata()).isNotEmpty();
    }

    @Test
    public void hasAllVideoEncodings() {
        videoMetadataService
                .getMetadata()
                .values()
                .forEach((value) -> assertThat(value
                        .stream()
                        .map(VideoMetadata::getVideoEncoding)
                        .collect(Collectors.toSet()))
                        .containsExactlyInAnyOrder(VideoEncoding.values()));
    }

    @Test
    public void hasAllAudioEncodings() {
        videoMetadataService
                .getMetadata()
                .values()
                .forEach((value) -> assertThat(value
                        .stream()
                        .map(VideoMetadata::getAudioEncoding)
                        .collect(Collectors.toSet()))
                        .containsExactlyInAnyOrder(AudioEncoding.values()));
    }

    @Test
    public void hasMultipleBitRatesPerUniqueStream() {
        videoMetadataService
                .getMetadata()
                .values()
                .forEach((value) -> assertThat((value
                        .stream()
                        .collect(Collectors.groupingBy((metadata) ->
                                String.format("%s%s", metadata.getVideoEncoding(), metadata.getAudioEncoding()),
                                Collectors.collectingAndThen(Collectors.groupingBy(VideoMetadata::getBitrate, Collectors.counting()),
                                        Map::size)))))
                        .containsValue(Bitrate.values().length));
    }
}
