package com.playback.services.tests;

import com.playback.services.UrlSigningService;
import org.junit.Before;
import org.junit.Test;

import javax.crypto.NoSuchPaddingException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.security.NoSuchAlgorithmException;
import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThat;

public class UrlSigningServiceTest {
    private URL url;
    private UrlSigningService urlSigningService;

    @Before
    public void setup() throws MalformedURLException, NoSuchAlgorithmException, NoSuchPaddingException {
        url = URI.create("https://somewhere.com").toURL();
        urlSigningService = UrlSigningService.create();
    }

    @Test
    public void invalidUrlExpiry() throws MalformedURLException {
        assertThat(urlSigningService.isNotExpired(URI.create(
                "https://somewhere.com?expiry=oidhfuauhfd&key=udhsafuihusifas").toURL()))
                .isFalse();
    }

    @Test
    public void invalidUrlSignature() throws MalformedURLException {
        assertThat(urlSigningService.isNotExpired(URI.create(String.format(
                "https://somewhere.com?expiry=oidhfuauhfd&key=%s",
                urlSigningService.signingPublicKey())).toURL()))
                .isFalse();
    }

    @Test
    public void expiresEightHoursFromNow() {
        final URL signedUrl = urlSigningService.expireEightHoursFromNow(url);
        assertThat(urlSigningService.isNotExpired(signedUrl)).isTrue();
    }

    @Test
    public void expiredUrl() {
        final URL signedUrl = urlSigningService.expire(url, Duration.ofHours(-1));
        assertThat(urlSigningService.isNotExpired(signedUrl)).isFalse();
    }
}
