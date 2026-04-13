package com.example.servicetest.client;

import com.example.servicetest.api.UserSummaryDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

/**
 * OpenFeign client to call user-service.
 * Uses a direct URL (no Eureka required).
 */
@FeignClient(name = "user-service", url = "${user.service.url:http://localhost:8081}")
public interface UserClient {

    @GetMapping("/api/users/internal/{id}")
    UserSummaryDto getUserById(@PathVariable("id") Long id);
}

