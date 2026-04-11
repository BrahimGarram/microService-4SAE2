package com.freelance.userservice.service;

import com.freelance.userservice.dto.ChatRequest;
import com.freelance.userservice.dto.ChatResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Chat with Freelancify assistant using Ollama (e.g. llama3.2).
 * Context-aware for the Freelancify platform (projects, freelancers, proposals, messages).
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class FreelancifyChatService {

    @Qualifier("ollamaRestTemplate")
    private final RestTemplate ollamaRestTemplate;

    @Value("${ollama.base-url:http://localhost:11434}")
    private String ollamaBaseUrl;

    @Value("${ollama.chat-model:llama3.2}")
    private String ollamaModel;

    private static final String SYSTEM_PROMPT = """
        You are the Freelancify assistant. Freelancify is a freelance platform where:
        - Clients post projects (OPEN, IN_PROGRESS, COMPLETED) and receive proposals from freelancers.
        - Freelancers browse projects, send proposals (amount, deadline, message), and get accepted or rejected.
        - After a proposal is accepted, the client and freelancer use a "project room" to communicate and track progress (messages, updates).
        - Users can message each other, get notifications (new proposal, proposal accepted/rejected), and manage their profile (skills, bio, portfolio).

        SUBSCRIPTION PLANS (you can answer questions about these):
        - FREE: 10 bids/month, 5 active projects, 15% commission, basic features. Good for getting started.
        - BASIC: more bids and projects, 12% commission.
        - PRO: unlimited bids, 50 active projects, 8% commission, analytics dashboard, profile boost, priority support. Most popular for active freelancers.
        - ENTERPRISE: everything in PRO plus unlimited projects, 5% commission, featured listings, dedicated account executive. For agencies and high-volume users.
        Billing: monthly or yearly (save with annual). Paid plans have a 14-day free trial. Users can upgrade or downgrade anytime; upgrades are immediate, downgrades apply at next billing cycle. Pro-rated billing when switching.

        Your role: answer questions about how to use Freelancify (projects, proposals, project room, messages, profile) AND about subscription plans (features, pricing, billing, recommendations). Be helpful and concise.
        Reply in the same language as the user. If the user writes in French, answer in French.
        """;

    public ChatResponse chat(ChatRequest request) {
        String userMessage = request.getMessage();
        if (userMessage == null || userMessage.isBlank()) {
            return new ChatResponse("Please type a message.");
        }

        List<Map<String, String>> ollamaMessages = buildOllamaMessages(request);
        String reply = callOllama(ollamaMessages);
        return new ChatResponse(reply != null ? reply : "I couldn't generate a reply. Please check that Ollama is running (e.g. ollama run " + ollamaModel + ").");
    }

    private List<Map<String, String>> buildOllamaMessages(ChatRequest request) {
        List<Map<String, String>> messages = new ArrayList<>();
        messages.add(Map.of("role", "system", "content", SYSTEM_PROMPT));

        if (request.getHistory() != null && !request.getHistory().isEmpty()) {
            int maxHistory = 20;
            int start = Math.max(0, request.getHistory().size() - maxHistory);
            for (int i = start; i < request.getHistory().size(); i++) {
                ChatRequest.ChatMessageDTO m = request.getHistory().get(i);
                String role = "user".equalsIgnoreCase(m.getRole()) ? "user" : "assistant";
                messages.add(Map.of("role", role, "content", m.getContent() != null ? m.getContent() : ""));
            }
        }

        messages.add(Map.of("role", "user", "content", request.getMessage()));
        return messages;
    }

    private String callOllama(List<Map<String, String>> messages) {
        String url = ollamaBaseUrl + "/api/chat";
        Map<String, Object> body = new HashMap<>();
        body.put("model", ollamaModel);
        body.put("stream", false);
        body.put("messages", messages);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(body, headers);

        try {
            ResponseEntity<Map<String, Object>> response = ollamaRestTemplate.exchange(
                url, HttpMethod.POST, entity,
                new ParameterizedTypeReference<Map<String, Object>>() {});
            if (response.getBody() == null) return "";
            Object messageObj = response.getBody().get("message");
            if (!(messageObj instanceof Map)) return "";
            Object contentObj = ((Map<?, ?>) messageObj).get("content");
            return contentObj != null ? contentObj.toString().trim() : "";
        } catch (Exception e) {
            log.warn("Ollama chat failed: {}", e.getMessage());
            throw new IllegalStateException(
                "AI unavailable. Ensure Ollama is running: ollama run " + ollamaModel, e);
        }
    }
}
