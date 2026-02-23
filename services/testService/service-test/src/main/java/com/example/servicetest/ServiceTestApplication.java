package com.example.servicetest;

import com.example.servicetest.entity.Domain;
import com.example.servicetest.service.QuestionTestService;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

import java.util.ArrayList;
import java.util.List;
@EnableFeignClients
@SpringBootApplication
public class ServiceTestApplication {

    public static void main(String[] args) {
        SpringApplication.run(ServiceTestApplication.class, args);
    }





}
