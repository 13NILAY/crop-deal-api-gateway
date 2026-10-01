package com.cropdeal.gateway.service;

import java.net.URI;
import java.util.Map;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.discovery.DiscoveryClient;

@Service
public class AdminDashboardService {
    private static final ParameterizedTypeReference<Map<String, Long>> STATS_TYPE =
            new ParameterizedTypeReference<>() { };

    private final DiscoveryClient discoveryClient;
    private final RestClient restClient = RestClient.create();

    public AdminDashboardService(DiscoveryClient discoveryClient) {
        this.discoveryClient = discoveryClient;
    }

    public Dashboard getDashboard(String authorization) {
        return new Dashboard(
                stats("AUTHENTICATION-SERVICE", authorization),
                stats("CROP-SERVICE", authorization),
                stats("ORDER-SERVICE", authorization),
                stats("PAYMENT-SERVICE", authorization),
                stats("SUBSCRIPTION-SERVICE", authorization));
    }

    private Map<String, Long> stats(String serviceId, String authorization) {
        ServiceInstance instance = discoveryClient.getInstances(serviceId).stream().findFirst()
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE,
                        "Statistics service unavailable: " + serviceId));
        URI uri = URI.create(instance.getUri() + "/internal/admin/stats");
        Map<String, Long> result = restClient.get().uri(uri)
                .header(HttpHeaders.AUTHORIZATION, authorization)
                .retrieve()
                .body(STATS_TYPE);
        if (result == null) {
            throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE,
                    "Statistics response unavailable: " + serviceId);
        }
        return result;
    }

    public record Dashboard(Map<String, Long> users, Map<String, Long> crops,
                            Map<String, Long> orders, Map<String, Long> payments,
                            Map<String, Long> subscriptions) { }
}