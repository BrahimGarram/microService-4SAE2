package com.example.servicetest.interfaces;

import com.example.servicetest.Dto.ProjectDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

@FeignClient(name = "project-service")
public interface ProjectClient {


    @RequestMapping("/api/projects")
    public ResponseEntity<List<ProjectDTO>> getAllProjects();

}

