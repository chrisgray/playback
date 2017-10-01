package com.playback.services;

import com.playback.models.Customer;

import java.util.Optional;

public interface CustomerInfoService {
    Optional<Customer> fromOAuth(String token);
}
