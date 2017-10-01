package com.playback.services;

import javax.crypto.Cipher;
import javax.crypto.NoSuchPaddingException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.time.Duration;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.temporal.TemporalAmount;
import java.util.*;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.mapping;
import static java.util.stream.Collectors.toList;

public class UrlSigningService {
    private final KeyPair keyPair;
    private final Cipher rsaCipher; //TODO: This doesn't seem like a very threadsafe class
    private static final String EXPIRY_KEY = "expiry";
    private static final String PUBLICKEY_KEY = "key";

    private UrlSigningService(KeyPair keyPair, Cipher rsaCipher) {
        this.keyPair = keyPair;
        this.rsaCipher = rsaCipher;
    }

    public static UrlSigningService create() throws NoSuchAlgorithmException, NoSuchPaddingException {
        final KeyPairGenerator keyGen = KeyPairGenerator.getInstance("RSA");
        keyGen.initialize(512);

        return new UrlSigningService(keyGen.generateKeyPair(), Cipher.getInstance("RSA"));

    }

    public URL expire(URL url, TemporalAmount timeToAdd) {
        try {
            return URI.create(String.format("%s?%s=%s&%s=%s",
                    url,
                    EXPIRY_KEY,
                    signedExpiry(ZonedDateTime.now(ZoneOffset.UTC).plus(timeToAdd)),
                    PUBLICKEY_KEY,
                    signingPublicKey()))
                    .toURL();
        } catch (GeneralSecurityException | MalformedURLException err) {
            //LOG
            throw new RuntimeException(err);
        }
    }

    public URL expireEightHoursFromNow(URL url) {
        return expire(url, Duration.ofHours(8));
    }

    public String signingPublicKey() {
        return base64Encode(keyPair.getPublic().getEncoded());
    }

    public boolean isNotExpired(URL url) {
        final ZonedDateTime now = ZonedDateTime.now(ZoneOffset.UTC);
        final Map<String, List<String>> queryParams = splitQuery(url);
        try {
            if (queryParams.containsKey(EXPIRY_KEY)) {
                final List<String> values = queryParams.get(EXPIRY_KEY);
                if (values.size() == 1) {
                    final ZonedDateTime parsedTime = ZonedDateTime.parse(decrypt(base64Decode(values.get(0))));
                    return parsedTime.isAfter(now);
                }
            }
        } catch (GeneralSecurityException err) {
            return false;
        }

        return false;
    }

    private String signedExpiry(ZonedDateTime time) throws GeneralSecurityException {
        rsaCipher.init(Cipher.ENCRYPT_MODE, keyPair.getPrivate());
        return base64Encode(rsaCipher.doFinal(time.toString().getBytes(StandardCharsets.UTF_8)));
    }

    private String decrypt(byte[] bytes) throws GeneralSecurityException {
        rsaCipher.init(Cipher.DECRYPT_MODE, keyPair.getPublic());
        return new String(rsaCipher.doFinal(bytes), StandardCharsets.UTF_8);
    }

    private static Map<String, List<String>> splitQuery(URL url) {
        final String query = url.getQuery();
        if (query == null || query.isEmpty()) {
            return Collections.emptyMap();
        }
        return Arrays.stream(url.getQuery().split("&"))
                .map(UrlSigningService::splitQueryParameter)
                .collect(Collectors.groupingBy(AbstractMap.SimpleImmutableEntry::getKey, HashMap::new,
                        mapping(Map.Entry::getValue, toList())));
    }

    private static AbstractMap.SimpleImmutableEntry<String, String> splitQueryParameter(String it) {
        final int idx = it.indexOf("=");
        final String key = idx > 0 ? it.substring(0, idx) : it;
        final String value = idx > 0 && it.length() > idx + 1 ? it.substring(idx + 1) : null;
        return new AbstractMap.SimpleImmutableEntry<>(key, value);
    }

    private static String base64Encode(byte[] bytes) {
        return Base64.getUrlEncoder().encodeToString(bytes);
    }

    private static byte[] base64Decode(String value) {
        return Base64.getUrlDecoder().decode(value.getBytes(StandardCharsets.UTF_8));
    }
}
