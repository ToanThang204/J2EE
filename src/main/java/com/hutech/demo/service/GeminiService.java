package com.hutech.demo.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

@Service
@Slf4j
public class GeminiService {

    @Value("${gemini.api.key}")
    private String apiKey;

    private static final String BASE_URL = "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.0-flash-exp:generateContent";
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    public GeminiService() {
        this.restTemplate = new RestTemplate();
        this.objectMapper = new ObjectMapper();
    }

    /**
     * Phân tích độ phù hợp giữa CV và Job Description
     */
    public Map<String, Object> analyzeMatch(String jobDescription, String resumeContent) {
        try {
            String prompt = buildPrompt(jobDescription, resumeContent);

            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("contents", new Object[]{
                Map.of("parts", new Object[]{Map.of("text", prompt)})
            });
            requestBody.put("generationConfig", Map.of(
                "temperature", 0.7,
                "maxOutputTokens", 4096,
                "topK", 40,
                "topP", 0.95
            ));

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);

            String url = BASE_URL + "?key=" + apiKey;
            ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.POST, entity, String.class);

            if (!response.getStatusCode().is2xxSuccessful()) {
                log.error("Gemini API error: {}", response.getBody());
                return null;
            }

            JsonNode root = objectMapper.readTree(response.getBody());
            String text = root.path("candidates").get(0)
                    .path("content").path("parts").get(0)
                    .path("text").asText();

            if (text == null || text.isEmpty()) {
                return null;
            }

            String cleanedText = extractJson(text);
            Map<String, Object> data = objectMapper.readValue(cleanedText, Map.class);

            if (!validateResponse(data)) {
                log.error("Invalid response structure from Gemini");
                return null;
            }

            return data;

        } catch (Exception e) {
            log.error("Error calling Gemini API", e);
            return null;
        }
    }

    private String buildPrompt(String jobDescription, String resumeContent) {
        return String.format("""
            Bạn là một chuyên gia phân tích tuyển dụng. Hãy phân tích mức độ phù hợp giữa CV và mô tả công việc sau:

            **MÔ TẢ CÔNG VIỆC:**
            %s

            **NỘI DUNG CV:**
            %s

            Hãy trả về kết quả dưới dạng JSON với cấu trúc sau:
            {
              "match_score": <số từ 0-100>,
              "summary": "<tóm tắt ngắn gọn về độ phù hợp>",
              "strengths": ["<điểm mạnh 1>", "<điểm mạnh 2>", ...],
              "weaknesses": ["<điểm yếu 1>", "<điểm yếu 2>", ...],
              "recommendations": ["<khuyến nghị 1>", "<khuyến nghị 2>", ...]
            }

            Chỉ trả về JSON, không thêm text nào khác.
            """, jobDescription, resumeContent);
    }

    private String extractJson(String text) {
        // Remove markdown code blocks
        text = text.replaceAll("```json\\s*", "").replaceAll("```\\s*", "");
        
        // Find JSON object
        int start = text.indexOf("{");
        int end = text.lastIndexOf("}");
        
        if (start >= 0 && end > start) {
            return text.substring(start, end + 1);
        }
        
        return text;
    }

    private boolean validateResponse(Map<String, Object> data) {
        return data != null &&
                data.containsKey("match_score") &&
                data.containsKey("summary") &&
                data.containsKey("strengths") &&
                data.containsKey("weaknesses") &&
                data.containsKey("recommendations");
    }
}
