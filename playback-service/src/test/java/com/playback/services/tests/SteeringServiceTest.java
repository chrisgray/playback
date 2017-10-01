package com.playback.services.tests;

import com.playback.clients.SteeringClient;
import com.playback.models.VideoMetadata;
import com.playback.services.SteeringService;
import com.playback.services.UrlSigningService;
import com.playback.standards.AudioEncoding;
import com.playback.standards.Bitrate;
import com.playback.standards.VideoEncoding;
import com.playback.standards.VideoResolution;
import org.junit.Before;
import org.junit.Test;

import javax.crypto.NoSuchPaddingException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.Collection;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

public class SteeringServiceTest {
    private final SteeringClient steeringClient = mock(SteeringClient.class);
    private final VideoMetadata sampleVideoMetadata = new VideoMetadata("sample",
            VideoEncoding.H264, VideoResolution.P1080, AudioEncoding.AC3, Bitrate.MBPS_2);
    private Collection<URL> sampleVideoMetadataURLs;
    private SteeringService steeringService;
    private UrlSigningService urlSigningService;

    @Before
    public void setup() throws MalformedURLException, NoSuchAlgorithmException, NoSuchPaddingException {
        reset(steeringClient);
        sampleVideoMetadataURLs = Arrays.asList(
                URI.create("http://location-one.com").toURL(),
                URI.create("http://location-two.com").toURL(),
                URI.create("http://location-three.com").toURL());
        urlSigningService = UrlSigningService.create();
        steeringService = new SteeringService(steeringClient, urlSigningService);
    }

    @Test(expected = RuntimeException.class)
    public void unableToProvideURLs() {
        when(steeringService).thenThrow(RuntimeException.class);
    }

    @Test
    public void invalidVideoMetadata() {
        when(steeringClient.fromVideoMetadata(any(VideoMetadata.class))).thenReturn(Optional.empty());

        assertThat(steeringService.fromVideoMetadata(sampleVideoMetadata, SteeringService::limitFirstThree))
                .isEmpty();
    }

    @Test
    public void suppliesURLs() {
        when(steeringClient.fromVideoMetadata(sampleVideoMetadata))
                .thenReturn(Optional.of(sampleVideoMetadataURLs));

        final Collection<URL> urls = steeringService.abitraryLimitFirstThree(sampleVideoMetadata).get();
        assertThat(urls).isNotEmpty();
        for (URL url : urls) {
            assertThat(urlSigningService.isNotExpired(url)).isTrue();
        }
    }
}
