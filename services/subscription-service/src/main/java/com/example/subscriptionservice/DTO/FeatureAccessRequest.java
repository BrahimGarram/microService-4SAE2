package com.example.subscriptionservice.DTO;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class FeatureAccessRequest {
    private Long userId;
    private String featureName; // BID, ANALYTICS, PROFILE_BOOST, etc.
}
