package com.playback.services.tests;

import com.playback.models.Customer;
import com.playback.services.CustomerInfoService;
import org.junit.Before;
import org.junit.Test;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

public class CustomerInfoServiceTest {
    private final CustomerInfoService customerInfoService = mock(CustomerInfoService.class);
    private final Customer activeNormalCustomer = Customer.active(Customer.Plan.NORMAL);

    @Before
    public void setup() {
        reset(customerInfoService);
    }

    @Test
    public void invalidOAuth() {
        when(customerInfoService.fromOAuth(anyString())).thenReturn(Optional.empty());

        assertThat(customerInfoService.fromOAuth(UUID.randomUUID().toString())).isEmpty();
    }

    @Test(expected = RuntimeException.class)
    public void unableToAuthenticate() {
        when(customerInfoService.fromOAuth(anyString())).thenThrow(RuntimeException.class);

        customerInfoService.fromOAuth(UUID.randomUUID().toString());
    }

    @Test
    public void fetchACustomer() {
        when(customerInfoService.fromOAuth(anyString())).thenReturn(Optional.of(activeNormalCustomer));

        assertThat(customerInfoService.fromOAuth(UUID.randomUUID().toString())).contains(activeNormalCustomer);
    }

}
