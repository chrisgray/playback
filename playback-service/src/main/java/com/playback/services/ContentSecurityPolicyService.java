package com.playback.services;

import com.playback.clients.ContentSecurityPolicyClient;
import com.playback.models.Device;

public class ContentSecurityPolicyService {
    private final ContentSecurityPolicyClient client;

    public ContentSecurityPolicyService(ContentSecurityPolicyClient client) {
        this.client = client;
    }

    //Assume that we just fallback to not allowing 4K if ContentSecurityPolicy is unreachable
    public boolean fromDevice(Device device) {
        try {
            return client.fromDevice(device);
        } catch (RuntimeException err) {
            //LOG?
            return false;
        }
    }
}
