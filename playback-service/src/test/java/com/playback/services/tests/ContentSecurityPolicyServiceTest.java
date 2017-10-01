package com.playback.services.tests;

import com.playback.clients.ContentSecurityPolicyClient;
import com.playback.models.Device;
import com.playback.services.ContentSecurityPolicyService;
import com.playback.standards.AudioEncoding;
import com.playback.standards.VideoEncoding;
import org.junit.Before;
import org.junit.Test;

import java.net.MalformedURLException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

public class ContentSecurityPolicyServiceTest {
    private final ContentSecurityPolicyClient client = mock(ContentSecurityPolicyClient.class);
    private ContentSecurityPolicyService service;
    private final Device sampleDevice = new Device(VideoEncoding.H264, AudioEncoding.AC3);

    @Before
    public void setup() throws MalformedURLException {
        reset(client);
        service = new ContentSecurityPolicyService(client);
    }

    @Test
    public void notAllowedIfUnableToVerifySecurityInAnyway() {
        when(client.fromDevice(any(Device.class))).thenReturn(false);
        assertThat(service.fromDevice(sampleDevice)).isFalse();

        when(client.fromDevice(any(Device.class))).thenThrow(RuntimeException.class);
        assertThat(service.fromDevice(sampleDevice)).isFalse();
    }

    @Test
    public void meetsPolicyFor4K() {
        when(client.fromDevice(sampleDevice)).thenReturn(true);
        assertThat(service.fromDevice(sampleDevice)).isTrue();
    }
}
