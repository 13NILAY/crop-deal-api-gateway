package com.cropdeal.gateway.controller;

import com.netflix.appinfo.InstanceInfo;
import com.netflix.discovery.EurekaClient;
import java.net.URI;
import java.util.LinkedHashMap;
import java.util.Map;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.http.HttpHeaders;
import org.springframework.web.client.RestClient;

@RestController
public class AdminHealthController {
    private static final String[] SERVICES = {
            "AUTHENTICATION-SERVICE", "PROFILE-SERVICE", "CROP-SERVICE", "ORDER-SERVICE",
            "PAYMENT-SERVICE", "NOTIFICATION-SERVICE", "SUBSCRIPTION-SERVICE"
    };

    private final DiscoveryClient discoveryClient;
    private final RestClient restClient = RestClient.create();
    private final URI eurekaHealthUri;

    public AdminHealthController(DiscoveryClient discoveryClient,
            @Value("${eureka.client.service-url.defaultZone}") String eurekaUrl) {
        this.discoveryClient = discoveryClient;
        String baseUrl = eurekaUrl.replaceFirst("/eureka/?$", "");
        this.eurekaHealthUri = URI.create(baseUrl + "/actuator/health");
    }

    @GetMapping("/admin/health")
    public Map<String, String> status(@RequestHeader(HttpHeaders.AUTHORIZATION) String authorization) {
        Map<String, String> statuses = new LinkedHashMap<>();
        for (String service : SERVICES) statuses.put(service, serviceHealth(service, authorization));
        statuses.put("EUREKA-SERVER", health(eurekaHealthUri, authorization));
        statuses.put("API-GATEWAY", "UP");
        return statuses;
    }

    private String serviceHealth(String serviceId, String authorization) {
        ServiceInstance instance = discoveryClient.getInstances(serviceId).stream().findFirst().orElse(null);
        if (instance == null) return "UNKNOWN";
        String baseUrl = instance.getUri().toString().replaceFirst("/$", "");
        return health(URI.create(baseUrl + "/actuator/health"), authorization);
    }

    private String health(URI uri, String authorization) {
        try {
            Map<?, ?> response = restClient.get().uri(uri)
                    .header(HttpHeaders.AUTHORIZATION, authorization)
                    .retrieve().body(Map.class);
            Object status = response == null ? null : response.get("status");
            return status == null ? "UNKNOWN" : status.toString();
        } catch (RuntimeException exception) {
            return "DOWN";
        }
    }
}