package com.freelance.userservice.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.client.RestClientException;

import java.util.Map;

/**
 * IP Geolocation Service
 * 
 * Gets country information from IP address using ip-api.com (free service)
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class IpGeolocationService {
    
    private final RestTemplate restTemplate;
    
    // Free IP geolocation API (no API key required)
    private static final String IP_API_URL = "http://ip-api.com/json/";
    
    /**
     * Get country from IP address
     * @param ipAddress IP address (can be IPv4 or IPv6)
     * @return Country name, or "Unknown" if unable to determine
     */
    public String getCountryFromIp(String ipAddress) {
        if (ipAddress == null || ipAddress.isBlank()) {
            log.warn("IP address is null or blank");
            return "Unknown";
        }
        
        // Handle localhost and private IPs
        if (ipAddress.equals("127.0.0.1") || ipAddress.equals("localhost") || 
            ipAddress.startsWith("192.168.") || ipAddress.startsWith("10.") || 
            ipAddress.startsWith("172.16.") || ipAddress.startsWith("172.17.") ||
            ipAddress.startsWith("172.18.") || ipAddress.startsWith("172.19.") ||
            ipAddress.startsWith("172.20.") || ipAddress.startsWith("172.21.") ||
            ipAddress.startsWith("172.22.") || ipAddress.startsWith("172.23.") ||
            ipAddress.startsWith("172.24.") || ipAddress.startsWith("172.25.") ||
            ipAddress.startsWith("172.26.") || ipAddress.startsWith("172.27.") ||
            ipAddress.startsWith("172.28.") || ipAddress.startsWith("172.29.") ||
            ipAddress.startsWith("172.30.") || ipAddress.startsWith("172.31.")) {
            log.info("IP address {} is localhost or private, returning 'Unknown'", ipAddress);
            return "Unknown";
        }
        
        try {
            String url = IP_API_URL + ipAddress;
            log.debug("Fetching country for IP: {}", ipAddress);
            
            ResponseEntity<Map> response = restTemplate.getForEntity(url, Map.class);
            
            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                Map<String, Object> data = response.getBody();
                
                // Check if request was successful
                String status = (String) data.get("status");
                if ("success".equals(status)) {
                    String country = (String) data.get("country");
                    if (country != null && !country.isBlank()) {
                        log.info("Country detected for IP {}: {}", ipAddress, country);
                        return country;
                    }
                } else {
                    String message = (String) data.get("message");
                    log.warn("IP geolocation API returned error for IP {}: {}", ipAddress, message);
                }
            }
        } catch (RestClientException e) {
            log.error("Error fetching country for IP {}: {}", ipAddress, e.getMessage());
        } catch (Exception e) {
            log.error("Unexpected error fetching country for IP {}: {}", ipAddress, e.getMessage());
        }
        
        return "Unknown";
    }
    
    /**
     * Extract IP address from request
     * Handles X-Forwarded-For header for proxied requests
     * @param clientIp Client IP from request
     * @param forwardedFor X-Forwarded-For header value
     * @return IP address
     */
    public String extractIpAddress(String clientIp, String forwardedFor) {
        // If X-Forwarded-For is present, use the first IP (original client)
        if (forwardedFor != null && !forwardedFor.isBlank()) {
            String[] ips = forwardedFor.split(",");
            if (ips.length > 0) {
                return ips[0].trim();
            }
        }
        
        // Otherwise use the direct client IP
        return clientIp != null ? clientIp : "Unknown";
    }
}
