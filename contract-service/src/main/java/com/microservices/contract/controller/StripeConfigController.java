package com.microservices.contract.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/stripe")
public class StripeConfigController {

    @Value("${stripe.public.key}")
    private String publicKey;

    @GetMapping("/config")
    public Map<String, String> getConfig() {
        return Map.of("publicKey", publicKey);
    }
}
