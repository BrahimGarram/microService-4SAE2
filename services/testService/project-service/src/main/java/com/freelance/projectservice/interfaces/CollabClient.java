package com.freelance.projectservice.interfaces;

import com.freelance.projectservice.dto.adem.CompanyDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

@FeignClient(name = "collaboration-service")
public interface CollabClient {
    @RequestMapping("/api/companies/{id}")
    public ResponseEntity<CompanyDTO> getById(@PathVariable Long id);

}
