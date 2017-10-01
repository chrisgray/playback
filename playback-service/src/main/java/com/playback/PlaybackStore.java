package com.playback;

import com.playback.models.*;
import com.playback.services.ContentSecurityPolicyService;
import com.playback.services.CustomerInfoService;
import com.playback.services.SteeringService;
import com.playback.services.VideoMetadataService;
import com.playback.standards.VideoResolution;

import java.util.Collection;
import java.util.Optional;
import java.util.stream.Collectors;

public class PlaybackStore {
    private final VideoMetadataService videoMetadataService;
    private final CustomerInfoService customerInfoService;
    private final ContentSecurityPolicyService contentSecurityPolicyService;
    private final SteeringService steeringService;

    public PlaybackStore(VideoMetadataService videoMetadataService, CustomerInfoService customerInfoService, ContentSecurityPolicyService contentSecurityPolicyService, SteeringService steeringService) {
        this.videoMetadataService = videoMetadataService;
        this.customerInfoService = customerInfoService;
        this.contentSecurityPolicyService = contentSecurityPolicyService;
        this.steeringService = steeringService;
    }

    public Optional<PlaybackManifest> request(String oauth, String title, Device device) {
        if (!videoMetadataService.getMetadata().containsKey(title)) {
            return Optional.empty();
        }

        final Optional<Collection<VideoMetadata>> videoMetadatas = customerInfoService
                .fromOAuth(oauth)
                .filter(Customer::isActive)
                .map((activeCustomer) -> videoMetadataService
                    .getMetadata()
                    .get(title)
                    .stream()
                    .filter((value) -> value.getVideoEncoding() == device.getSupportedVideoEncoding())
                    .filter((value) -> value.getAudioEncoding() == device.getSupportedAudioEncoding())
                    .filter((value) -> is4KAllowed(activeCustomer, value, device))
                    .collect(Collectors.toList()));

        return videoMetadatas
                .map((value) -> VideoMetadataService.limitBitRates(value)
                    .stream()
                    .map((videoMetadata) -> new Stream(videoMetadata, steeringService
                                .abitraryLimitFirstThree(videoMetadata).get()))
                    .collect(Collectors.collectingAndThen(Collectors.toList(), PlaybackManifest::new)));
    }

    private boolean is4KAllowed(Customer customer, VideoMetadata videoMetadata, Device device) {
        return (videoMetadata.getVideoResolution() != VideoResolution.P2160) ||
                (customer.getPlan() == Customer.Plan.PREMIUM &&
                contentSecurityPolicyService.fromDevice(device));
    }
}
