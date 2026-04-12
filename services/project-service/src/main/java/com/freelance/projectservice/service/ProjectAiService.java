package com.freelance.projectservice.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.freelance.projectservice.dto.ProjectDraftDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Calls Ollama (local LLM) to generate a project draft from natural language.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ProjectAiService {

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Value("${ollama.base-url:http://localhost:11434}")
    private String ollamaBaseUrl;

    @Value("${ollama.model:qwen2.5:3b}")
    private String ollamaModel;

    private static final Pattern JSON_BLOCK = Pattern.compile("```(?:json)?\\s*([\\s\\S]*?)```", Pattern.CASE_INSENSITIVE);

    /**
     * Call Ollama to extract title, description, category, tags, suggestedBudget from user message.
     */
    public ProjectDraftDTO draftFromText(String userMessage) {
        String prompt = buildPrompt(userMessage);
        String content = callOllama(prompt);
        return parseResponse(content, userMessage);
    }

    private String buildPrompt(String userMessage) {
        return """
            From the following user request for a freelance project, extract and return ONLY a valid JSON object.
            Use exactly these keys: "title", "description", "category", "tags", "suggestedBudget".
            - title: short project title (string)
            - description: clear project description (string)
            - category: one of Web Development, Design, Mobile, Writing, Marketing, Other (string)
            - tags: comma-separated keywords (string)
            - suggestedBudget: number or null if unknown
            Return only the JSON, no markdown and no explanation.
            User request: "%s"
            """.formatted(userMessage.replace("\"", "\\\""));
    }

    private String callOllama(String prompt) {
        String url = ollamaBaseUrl + "/api/chat";
        Map<String, Object> body = new HashMap<>();
        body.put("model", ollamaModel);
        body.put("stream", false);
        body.put("messages", java.util.List.of(
            Map.of("role", "user", "content", prompt)
        ));

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<Map<String, Object>> request = new HttpEntity<>(body, headers);

        try {
            ResponseEntity<Map<String, Object>> response = restTemplate.exchange(
                url, HttpMethod.POST, request,
                new ParameterizedTypeReference<Map<String, Object>>() {});
            if (response.getBody() == null) {
                throw new IllegalStateException("Ollama returned empty body");
            }
            Object messageObj = response.getBody().get("message");
            if (!(messageObj instanceof Map)) {
                throw new IllegalStateException("Ollama response missing message");
            }
            Object contentObj = ((Map<?, ?>) messageObj).get("content");
            return contentObj != null ? contentObj.toString() : "";
        } catch (Exception e) {
            log.warn("Ollama call failed: {}", e.getMessage());
            throw new IllegalStateException("AI service unavailable. Ensure Ollama is running (ollama run " + ollamaModel + ").", e);
        }
    }

    private ProjectDraftDTO parseResponse(String content, String fallbackUserMessage) {
        String json = extractJson(content);
        if (json == null || json.isBlank()) {
            return fallbackDraft(fallbackUserMessage);
        }
        try {
            JsonNode root = objectMapper.readTree(json);
            BigDecimal budget = null;
            if (root.has("suggestedBudget") && !root.get("suggestedBudget").isNull()) {
                JsonNode b = root.get("suggestedBudget");
                if (b.isNumber()) {
                    budget = BigDecimal.valueOf(b.asDouble());
                } else if (b.isTextual()) {
                    try {
                        budget = new BigDecimal(b.asText().replaceAll("[^0-9.]", ""));
                    } catch (NumberFormatException ignored) {
                        // leave null
                    }
                }
            }
            return ProjectDraftDTO.builder()
                .title(trim(root, "title", 200, fallbackUserMessage))
                .description(trim(root, "description", 5000, null))
                .category(trim(root, "category", 100, "Other"))
                .tags(trim(root, "tags", 500, ""))
                .suggestedBudget(budget)
                .build();
        } catch (Exception e) {
            log.warn("Failed to parse LLM JSON: {}", e.getMessage());
            return fallbackDraft(fallbackUserMessage);
        }
    }

    private String extractJson(String content) {
        if (content == null) return null;
        content = content.trim();
        Matcher m = JSON_BLOCK.matcher(content);
        if (m.find()) {
            return m.group(1).trim();
        }
        if (content.startsWith("{")) {
            return content;
        }
        return content;
    }

    private String trim(JsonNode root, String key, int maxLen, String defaultValue) {
        if (!root.has(key) || root.get(key).isNull()) {
            return defaultValue != null ? defaultValue : "";
        }
        String s = root.get(key).asText();
        if (s == null || s.isBlank()) return defaultValue != null ? defaultValue : "";
        if (s.length() > maxLen) s = s.substring(0, maxLen);
        return s.trim();
    }

    private ProjectDraftDTO fallbackDraft(String userMessage) {
        return ProjectDraftDTO.builder()
            .title(userMessage != null && userMessage.length() > 200 ? userMessage.substring(0, 200) : (userMessage != null ? userMessage : "New Project"))
            .description(userMessage != null ? userMessage : "")
            .category("Other")
            .tags("")
            .suggestedBudget(null)
            .build();
    }
}
