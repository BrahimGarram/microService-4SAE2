package com.freelance.userservice.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.freelance.userservice.dto.UserDTO;
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

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Suggests freelancers based on natural language using Ollama.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class FreelancerSuggestService {

    private final UserService userService;

    @Qualifier("ollamaRestTemplate")
    private final RestTemplate ollamaRestTemplate;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Value("${ollama.base-url:http://localhost:11434}")
    private String ollamaBaseUrl;

    @Value("${ollama.model:qwen2.5:3b}")
    private String ollamaModel;

    private static final Pattern JSON_ARRAY = Pattern.compile("\\[\\s*[\\d,\\s]*\\]");

    /**
     * Suggest freelancers matching the user's message (e.g. "I need a React developer").
     */
    public List<UserDTO> suggestFreelancers(String message) {
        List<UserDTO> allUsers = userService.getPublicUsers();
        if (allUsers.isEmpty()) {
            return List.of();
        }
        String freelancersSummary = buildFreelancersSummary(allUsers);
        String prompt = buildPrompt(freelancersSummary, message);
        String content = callOllama(prompt);
        List<Long> suggestedIds = parseIdsFromResponse(content, allUsers);
        return orderUsersByIds(allUsers, suggestedIds);
    }

    private String buildFreelancersSummary(List<UserDTO> users) {
        StringBuilder sb = new StringBuilder();
        for (UserDTO u : users) {
            String skills = u.getSkills() != null
                ? u.getSkills().stream().map(s -> s.getName()).filter(Objects::nonNull).collect(Collectors.joining(", "))
                : "";
            String bio = u.getBio() != null ? u.getBio().replace("\n", " ") : "";
            if (bio.length() > 150) bio = bio.substring(0, 147) + "...";
            sb.append("- ID: ").append(u.getId())
                .append(", username: ").append(u.getUsername())
                .append(", role: ").append(u.getRole())
                .append(", skills: [").append(skills).append("]")
                .append(", bio: ").append(bio)
                .append("\n");
        }
        return sb.toString();
    }

    private String buildPrompt(String freelancersSummary, String userMessage) {
        return """
            You are a freelance platform assistant. Below is a list of freelancers (each line: ID, username, role, skills, bio).
            The client request is: "%s"
            Reply with ONLY a JSON array of the best matching user IDs (max 10), in order of relevance. Example: [2, 5, 1]
            If no good match, return [].
            Freelancers list:
            %s
            """.formatted(userMessage.replace("\"", "\\\""), freelancersSummary);
    }

    private String callOllama(String prompt) {
        String url = ollamaBaseUrl + "/api/chat";
        Map<String, Object> body = new HashMap<>();
        body.put("model", ollamaModel);
        body.put("stream", false);
        body.put("messages", List.of(Map.of("role", "user", "content", prompt)));

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<Map<String, Object>> request = new HttpEntity<>(body, headers);

        try {
            ResponseEntity<Map<String, Object>> response = ollamaRestTemplate.exchange(
                url, HttpMethod.POST, request,
                new ParameterizedTypeReference<Map<String, Object>>() {});
            if (response.getBody() == null) return "";
            Object messageObj = response.getBody().get("message");
            if (!(messageObj instanceof Map)) return "";
            Object contentObj = ((Map<?, ?>) messageObj).get("content");
            return contentObj != null ? contentObj.toString() : "";
        } catch (Exception e) {
            log.warn("Ollama call failed: {}", e.getMessage());
            throw new IllegalStateException("AI unavailable. Ensure Ollama is running (ollama run " + ollamaModel + ").", e);
        }
    }

    private List<Long> parseIdsFromResponse(String content, List<UserDTO> allUsers) {
        if (content == null || content.isBlank()) return List.of();
        Set<Long> validIds = allUsers.stream().map(UserDTO::getId).filter(Objects::nonNull).collect(Collectors.toSet());
        List<Long> result = new ArrayList<>();

        // Try to find JSON array in response
        Matcher m = JSON_ARRAY.matcher(content);
        if (m.find()) {
            try {
                JsonNode arr = objectMapper.readTree(m.group());
                if (arr.isArray()) {
                    for (JsonNode n : arr) {
                        if (n.isNumber()) {
                            long id = n.asLong();
                            if (validIds.contains(id) && !result.contains(id)) result.add(id);
                        }
                    }
                }
            } catch (Exception e) {
                log.debug("Parse LLM array failed: {}", e.getMessage());
            }
        }
        // Fallback: look for numbers in content
        if (result.isEmpty()) {
            for (String part : content.split("[^0-9]+")) {
                if (!part.isEmpty()) {
                    try {
                        long id = Long.parseLong(part);
                        if (validIds.contains(id) && !result.contains(id)) {
                            result.add(id);
                            if (result.size() >= 10) break;
                        }
                    } catch (NumberFormatException ignored) {}
                }
            }
        }
        return result;
    }

    private List<UserDTO> orderUsersByIds(List<UserDTO> allUsers, List<Long> ids) {
        Map<Long, UserDTO> byId = allUsers.stream().filter(u -> u.getId() != null).collect(Collectors.toMap(UserDTO::getId, u -> u));
        return ids.stream().map(byId::get).filter(Objects::nonNull).distinct().collect(Collectors.toList());
    }
}
