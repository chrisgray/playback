package com.playback.tests;

import com.playback.PlaybackStore;
import com.playback.clients.ContentSecurityPolicyClient;
import com.playback.models.*;
import com.playback.services.*;
import com.playback.standards.AudioEncoding;
import com.playback.standards.Bitrate;
import com.playback.standards.VideoEncoding;
import com.playback.standards.VideoResolution;
import org.assertj.core.api.Assertions;
import org.assertj.core.util.Sets;
import org.junit.Before;
import org.junit.Test;

import javax.crypto.NoSuchPaddingException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

public class PlaybackStoreTest {
    private final VideoMetadataService videoMetadataService = VideoMetadataService.create();
    private final CustomerInfoService customerInfoService = mock(CustomerInfoService.class);
    private final ContentSecurityPolicyClient contentSecurityPolicyClient = mock(ContentSecurityPolicyClient.class);
    private final SteeringService steeringService = mock(SteeringService.class);
    private final String oauthToken = UUID.randomUUID().toString();
    private final String sampleTitle = "Title 2";
    private final Device device_H264_DTS = new Device(VideoEncoding.H264, AudioEncoding.DTS);
    private Collection<URL> sampleVideoMetadataURLs;
    private PlaybackStore playbackStore;
    private UrlSigningService urlSigningService;

    @Before
    public void setup() throws MalformedURLException, NoSuchAlgorithmException, NoSuchPaddingException {
        reset(customerInfoService, contentSecurityPolicyClient, steeringService);
        urlSigningService = UrlSigningService.create();
        sampleVideoMetadataURLs = java.util.stream.Stream.of(
                    URI.create("http://location-one.com").toURL(),
                    URI.create("http://location-two.com").toURL(),
                    URI.create("http://location-three.com").toURL())
                .map(urlSigningService::expireEightHoursFromNow)
                .collect(Collectors.toList());
        playbackStore = new PlaybackStore(videoMetadataService,
                customerInfoService,
                new ContentSecurityPolicyService(contentSecurityPolicyClient),
                steeringService);
    }

    @Test
    public void invalidCustomer() {
        when(customerInfoService.fromOAuth(anyString())).thenReturn(Optional.empty());

        Assertions.assertThat(playbackStore.request(oauthToken, sampleTitle, device_H264_DTS)).isEmpty();
    }

    @Test
    public void nonActiveCustomer() {
        when(customerInfoService.fromOAuth(anyString()))
                .thenReturn(Optional.of(Customer.inactive(Customer.Plan.NORMAL)));

        Assertions.assertThat(playbackStore.request(oauthToken, sampleTitle, device_H264_DTS)).isEmpty();
    }

    @Test
    public void activeNormalCustomer() {
        when(customerInfoService.fromOAuth(anyString()))
                .thenReturn(Optional.of(Customer.active(Customer.Plan.NORMAL)));
        when(contentSecurityPolicyClient.fromDevice(device_H264_DTS)).thenReturn(false);
        when(steeringService.abitraryLimitFirstThree(any(VideoMetadata.class)))
                .thenReturn(Optional.of(sampleVideoMetadataURLs));

        final PlaybackManifest expected = new PlaybackManifest(buildStreams(device_H264_DTS, sampleTitle,
                VideoResolution.P1080));

        final Optional<PlaybackManifest> actual = playbackStore.request(oauthToken, sampleTitle, device_H264_DTS);
        assertThat(Sets.newHashSet(actual.get().getStreams())).isEqualTo(Sets.newHashSet(expected.getStreams()));
        validateUrls(actual.get().getStreams());
    }

    @Test
    public void activePremiumCustomerWithContentSecurityPolicyFailure() {
        when(customerInfoService.fromOAuth(anyString()))
                .thenReturn(Optional.of(Customer.active(Customer.Plan.NORMAL)));
        when(contentSecurityPolicyClient.fromDevice(device_H264_DTS)).thenThrow(RuntimeException.class);
        when(steeringService.abitraryLimitFirstThree(any(VideoMetadata.class)))
                .thenReturn(Optional.of(sampleVideoMetadataURLs));

        final PlaybackManifest expected = new PlaybackManifest(buildStreams(device_H264_DTS, sampleTitle,
                VideoResolution.P1080));

        final Optional<PlaybackManifest> actual = playbackStore.request(oauthToken, sampleTitle, device_H264_DTS);
        assertThat(Sets.newHashSet(actual.get().getStreams())).isEqualTo(Sets.newHashSet(expected.getStreams()));
        validateUrls(actual.get().getStreams());
    }

    @Test
    public void activePremiumCustomer() {
        when(customerInfoService.fromOAuth(anyString()))
                .thenReturn(Optional.of(Customer.active(Customer.Plan.PREMIUM)));
        when(contentSecurityPolicyClient.fromDevice(device_H264_DTS)).thenReturn(true);
        when(steeringService.abitraryLimitFirstThree(any(VideoMetadata.class)))
                .thenReturn(Optional.of(sampleVideoMetadataURLs));

        final PlaybackManifest expected = new PlaybackManifest(buildStreams(device_H264_DTS, sampleTitle,
                VideoResolution.P2160));

        final Optional<PlaybackManifest> actual = playbackStore.request(oauthToken, sampleTitle, device_H264_DTS);
        assertThat(Sets.newHashSet(actual.get().getStreams())).isEqualTo(Sets.newHashSet(expected.getStreams()));
        validateUrls(actual.get().getStreams());
    }

    private void validateUrls(Collection<Stream> streams) {
        for (Stream stream : streams) {
            for (URL url : stream.getUrls()) {
                assertThat(urlSigningService.isNotExpired(url)).isTrue();
            }
        }
    }

    private Collection<Stream> buildStreams(Device device, String title, VideoResolution maxResolution) {
        final Collection<VideoMetadata> videoMetadatas = new ArrayList<>();
        for (VideoResolution videoResolution : VideoResolution.values()) {
            if (videoResolution.ordinal() <= maxResolution.ordinal()) {
                for (Bitrate bitrate : Bitrate.values()) {
                    videoMetadatas.add(new VideoMetadata(title, device.getSupportedVideoEncoding(), videoResolution,
                            device.getSupportedAudioEncoding(), bitrate));
                }
            }
        }
        return VideoMetadataService
                .limitBitRates(videoMetadatas)
                .stream()
                .map((value) -> new Stream(value, sampleVideoMetadataURLs))
                .collect(Collectors.toList());
    }
}
