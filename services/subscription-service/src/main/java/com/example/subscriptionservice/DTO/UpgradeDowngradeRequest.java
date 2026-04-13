package com.example.subscriptionservice.DTO;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class UpgradeDowngradeRequest {
    private Long userId;
    private Long newPlanId;
    private Boolean immediate = true; // Apply immediately or at end of current period
}
