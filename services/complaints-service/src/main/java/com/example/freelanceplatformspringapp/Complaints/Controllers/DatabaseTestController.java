package com.example.freelanceplatformspringapp.Complaints.Controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/db-test")
public class DatabaseTestController {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @GetMapping("/tables")
    public List<Map<String, Object>> getTables() {
        return jdbcTemplate.queryForList(
            "SELECT TABLE_NAME FROM INFORMATION_SCHEMA.TABLES WHERE TABLE_SCHEMA='PUBLIC'"
        );
    }

    @GetMapping("/query")
    public List<Map<String, Object>> executeQuery(@RequestParam String sql) {
        return jdbcTemplate.queryForList(sql);
    }

    @GetMapping("/status")
    public Map<String, String> getStatus() {
        return Map.of(
            "database", "H2",
            "status", "connected",
            "url", "jdbc:h2:file:./data/freelancify-local"
        );
    }
}
