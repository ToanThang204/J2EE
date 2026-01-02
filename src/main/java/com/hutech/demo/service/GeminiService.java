package com.hutech.demo.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hutech.demo.model.Category;
import com.hutech.demo.model.IndustryContext;
import com.hutech.demo.repository.CategoryRepository;
import com.hutech.demo.repository.IndustryContextRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@Slf4j
@RequiredArgsConstructor
public class GeminiService {

    @Value("${gemini.api.key}")
    private String apiKey;

    private static final String BASE_URL = "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.5-flash:generateContent";
    
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;
    private final IndustryContextRepository industryContextRepository;
    private final CategoryRepository categoryRepository;

    private String lastDetectedIndustry = null;

    /**
     * Phân tích độ phù hợp CV với Job - FULL VERSION
     * 
     * @param jobDescription Job description text
     * @param resumeContent Resume content text
     * @param companyContext JSON object về công ty (culture, values...)
     * @param jobAiContext JSON object về job (must_have_skills, weights...)
     * @param categoryNames Danh sách tên categories của job
     * @return Map với match_score, summary, strengths, weaknesses, improvement_tip
     */
    public Map<String, Object> analyzeMatch(
            String jobDescription,
            String resumeContent,
            Map<String, Object> companyContext,
            Map<String, Object> jobAiContext,
            List<String> categoryNames) {
        
        try {
            // 1. Xác định ngành nghề
            Map<String, Object> industryConfig = getIndustryContext(categoryNames, jobDescription);
            
            // 2. Build prompt đầy đủ
            String prompt = buildCompletePrompt(
                jobDescription,
                resumeContent,
                companyContext,
                jobAiContext,
                industryConfig
            );

            // 3. Gọi Gemini API
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
                log.error("Gemini API error: status={}", response.getStatusCode());
                return null;
            }

            // 4. Parse response
            JsonNode root = objectMapper.readTree(response.getBody());
            String text = root.path("candidates").get(0)
                    .path("content").path("parts").get(0)
                    .path("text").asText();

            if (text == null || text.isEmpty()) {
                log.error("Empty response from Gemini");
                return null;
            }

            // 5. Extract và parse JSON
            String cleanedText = extractJson(text);
            Map<String, Object> data = objectMapper.readValue(cleanedText, new TypeReference<Map<String, Object>>() {});

            // 6. Validate structure
            if (!validateResponse(data)) {
                log.error("Invalid response structure from Gemini: {}", data);
                return null;
            }

            return data;

        } catch (Exception e) {
            log.error("Error calling Gemini API", e);
            return null;
        }
    }

    /**
     * Lấy industry đã detect
     */
    public String getLastDetectedIndustry() {
        return lastDetectedIndustry;
    }

    /**
     * Xác định industry context
     */
    private Map<String, Object> getIndustryContext(List<String> categoryNames, String jobDescription) {
        String industryKey = "general";

        // Ưu tiên từ database
        if (categoryNames != null && !categoryNames.isEmpty()) {
            Category category = categoryRepository.findAll().stream()
                .filter(c -> categoryNames.contains(c.getName()) && c.getIndustryKey() != null)
                .findFirst()
                .orElse(null);

            if (category != null && category.getIndustryKey() != null) {
                industryKey = category.getIndustryKey();
            } else {
                industryKey = mapCategoriesToIndustry(categoryNames);
            }
        } else {
            industryKey = detectIndustryFromText(jobDescription);
        }

        lastDetectedIndustry = industryKey;

        // Load từ DB
        IndustryContext industry = industryContextRepository.findByKey(industryKey)
            .filter(IndustryContext::getIsActive)
            .orElse(null);

        if (industry != null) {
            try {
                return objectMapper.readValue(industry.getConfig(), new TypeReference<Map<String, Object>>() {});
            } catch (Exception e) {
                log.error("Error parsing industry config", e);
            }
        }

        // Fallback hardcode
        return getDefaultIndustryConfig(industryKey);
    }

    /**
     * Build prompt hoàn chỉnh
     */
    private String buildCompletePrompt(
            String jobDescription,
            String resumeContent,
            Map<String, Object> companyContext,
            Map<String, Object> jobAiContext,
            Map<String, Object> industryConfig) {
        
        StringBuilder prompt = new StringBuilder();
        
        // Role
        prompt.append("BẠN LÀ: ").append(industryConfig.get("role")).append("\n\n");
        prompt.append("NHIỆM VỤ: Phân tích độ phù hợp giữa CV và Job Description\n\n");

        // Company Context
        if (companyContext != null && !companyContext.isEmpty()) {
            prompt.append("=== NGỮ CẢNH CÔNG TY ===\n");
            if (companyContext.containsKey("culture")) {
                prompt.append("Văn hóa: ").append(companyContext.get("culture")).append("\n");
            }
            if (companyContext.containsKey("values")) {
                prompt.append("Giá trị cốt lõi: ").append(companyContext.get("values")).append("\n");
            }
            if (companyContext.containsKey("work_style")) {
                prompt.append("Phong cách làm việc: ").append(companyContext.get("work_style")).append("\n");
            }
            prompt.append("\n");
        }

        // Job AI Context
        if (jobAiContext != null && !jobAiContext.isEmpty()) {
            prompt.append("=== TIÊU CHÍ ĐÁNH GIÁ CỤ THỂ ===\n");
            
            if (jobAiContext.containsKey("must_have_skills")) {
                prompt.append("KỸ NĂNG BẮT BUỘC: ").append(jobAiContext.get("must_have_skills")).append("\n");
            }
            if (jobAiContext.containsKey("nice_to_have_skills")) {
                prompt.append("KỸ NĂNG ƯU TIÊN: ").append(jobAiContext.get("nice_to_have_skills")).append("\n");
            }
            if (jobAiContext.containsKey("min_experience_years")) {
                prompt.append("Kinh nghiệm tối thiểu: ").append(jobAiContext.get("min_experience_years")).append(" năm\n");
            }
            if (jobAiContext.containsKey("weights")) {
                Map<String, Object> weights = (Map<String, Object>) jobAiContext.get("weights");
                prompt.append("TRỌNG SỐ:\n");
                weights.forEach((k, v) -> prompt.append("  - ").append(k).append(": ").append(v).append("%\n"));
            }
            if (jobAiContext.containsKey("red_flags")) {
                prompt.append("RED FLAGS: ").append(jobAiContext.get("red_flags")).append("\n");
            }
            prompt.append("\n");
        }

        // Industry Focus
        prompt.append("=== TIÊU CHÍ ĐÁNH GIÁ THEO NGÀNH ===\n");
        prompt.append("Tập trung vào:\n");
        if (industryConfig.containsKey("focus_areas")) {
            List<String> focusAreas = (List<String>) industryConfig.get("focus_areas");
            focusAreas.forEach(area -> prompt.append("- ").append(area).append("\n"));
        }
        prompt.append("\nKey Metrics: ").append(industryConfig.get("key_metrics")).append("\n");
        prompt.append("Red Flags: ").append(industryConfig.get("red_flags")).append("\n\n");

        // Job Description
        prompt.append("=== JOB DESCRIPTION ===\n").append(jobDescription).append("\n\n");

        // Resume Content
        prompt.append("=== CV ỨNG VIÊN ===\n").append(resumeContent).append("\n\n");

        // Yêu cầu output
        prompt.append("""
=== YÊU CẦU OUTPUT ===
Trả về JSON (KHÔNG markdown):
{
  "match_score": 0-100,
  "summary": "Tóm tắt 2-3 câu",
  "strengths": ["Điểm mạnh 1", "Điểm mạnh 2", ...],
  "weaknesses": ["Điểm yếu 1", "Điểm yếu 2", ...],
  "improvement_tip": "Gợi ý cải thiện chi tiết"
}

NGUYÊN TẮC CHẤM ĐIỂM:
- 90-100: Hoàn hảo, vượt mong đợi
- 75-89: Rất phù hợp, thiếu vài kỹ năng nhỏ
- 60-74: Phù hợp trung bình, thiếu kinh nghiệm hoặc skills quan trọng
- 40-59: Partial match, cần training nhiều
- 20-39: Mismatch nghiêm trọng (sai ngành/level)
- 0-19: Hoàn toàn không phù hợp

CHÚ Ý:
- Không được quá khoan dung (tránh điểm ảo)
- Must-have skills phải có, không có thì trừ điểm nặng
- Red flags phải xử lý nghiêm khắc
- Số liệu cụ thể quan trọng hơn mô tả chung chung
""");

        return prompt.toString();
    }

    /**
     * Map categories sang industry
     */
    private String mapCategoriesToIndustry(List<String> categoryNames) {
        Map<String, List<String>> mapping = Map.of(
            "it", List.of("công nghệ thông tin", "lập trình", "software", "developer", "backend", "frontend", "it"),
            "marketing", List.of("marketing", "digital marketing", "seo", "content"),
            "sales", List.of("kinh doanh", "bán hàng", "sales"),
            "finance", List.of("tài chính", "kế toán", "finance", "accounting")
        );

        for (Map.Entry<String, List<String>> entry : mapping.entrySet()) {
            for (String cat : categoryNames) {
                String catLower = cat.toLowerCase();
                for (String keyword : entry.getValue()) {
                    if (catLower.contains(keyword)) {
                        return entry.getKey();
                    }
                }
            }
        }

        return "general";
    }

    /**
     * Detect industry từ text
     */
    private String detectIndustryFromText(String text) {
        String textLower = text.toLowerCase();
        
        Map<String, List<String>> patterns = Map.of(
            "it", List.of("laravel", "react", "vue", "nodejs", "python", "java", "php", "api", "docker", "spring", "backend", "frontend"),
            "marketing", List.of("seo", "sem", "google ads", "facebook ads", "content", "campaign", "marketing")
        );

        for (Map.Entry<String, List<String>> entry : patterns.entrySet()) {
            for (String keyword : entry.getValue()) {
                if (textLower.contains(keyword)) {
                    return entry.getKey();
                }
            }
        }

        return "general";
    }

    /**
     * Default industry config (hardcode fallback)
     */
    private Map<String, Object> getDefaultIndustryConfig(String industry) {
        Map<String, Map<String, Object>> configs = Map.of(
            "it", Map.of(
                "role", "Chuyên gia Tuyển dụng IT/Tech",
                "focus_areas", List.of(
                    "Tech Stack cụ thể (frameworks, languages)",
                    "Kinh nghiệm thực tế với dự án",
                    "Portfolio/GitHub"
                ),
                "key_metrics", "Số năm với tech stack, project scale",
                "red_flags", "Thiếu hands-on experience, không có portfolio"
            ),
            "marketing", Map.of(
                "role", "Chuyên gia Tuyển dụng Marketing",
                "focus_areas", List.of(
                    "Kênh marketing (digital, social media, SEO)",
                    "Tools (Google Analytics, Ads Manager)",
                    "Metrics (CTR, ROI, conversion rate)"
                ),
                "key_metrics", "Budget quản lý, traffic growth, conversion",
                "red_flags", "Không có số liệu, thiếu case study"
            ),
            "general", Map.of(
                "role", "Chuyên gia Tuyển dụng",
                "focus_areas", List.of("Kinh nghiệm", "Kỹ năng", "Học vấn"),
                "key_metrics", "Số năm kinh nghiệm",
                "red_flags", "Thiếu kinh nghiệm cần thiết"
            )
        );

        return configs.getOrDefault(industry, configs.get("general"));
    }

    /**
     * Extract JSON từ markdown
     */
    private String extractJson(String text) {
        // Remove markdown code blocks
        text = text.replaceAll("```json\\s*", "").replaceAll("```\\s*", "");
        text = text.trim();
        
        // Find JSON object
        int start = text.indexOf("{");
        int end = text.lastIndexOf("}");
        
        if (start >= 0 && end > start) {
            return text.substring(start, end + 1);
        }
        
        return text;
    }

    /**
     * Validate response structure
     */
    private boolean validateResponse(Map<String, Object> data) {
        return data != null &&
                data.containsKey("match_score") &&
                data.containsKey("summary") &&
                data.containsKey("strengths") &&
                data.containsKey("weaknesses") &&
                data.get("strengths") instanceof List &&
                data.get("weaknesses") instanceof List;
    }
}
