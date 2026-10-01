package com.cropdeal.gateway.controller;

import com.cropdeal.gateway.service.AdminDashboardService;
import java.security.Principal;
import org.springframework.http.HttpHeaders;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class AdminDashboardController {
    private final AdminDashboardService dashboardService;

    public AdminDashboardController(AdminDashboardService dashboardService) {
        this.dashboardService = dashboardService;
    }

    @GetMapping("/admin/dashboard")
    public AdminDashboardService.Dashboard get(@RequestHeader(HttpHeaders.AUTHORIZATION) String authorization,
                                              Principal principal) {
        return dashboardService.getDashboard(authorization);
    }
}