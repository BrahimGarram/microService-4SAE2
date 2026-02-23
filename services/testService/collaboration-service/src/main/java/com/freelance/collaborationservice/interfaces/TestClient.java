package com.freelance.collaborationservice.interfaces;

import com.freelance.collaborationservice.dto.brahim.QuestionTest;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

@FeignClient(name = "service-test")
public interface TestClient {
    @RequestMapping("/service-test/questions")
    public List<QuestionTest> getAllQuestions();

}
