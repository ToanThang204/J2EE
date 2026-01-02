# 🤖 HỆ THỐNG AI MATCHING CV VỚI CÔNG VIỆC

## 📋 MÔ TẢ

Hệ thống AI phân tích độ phù hợp giữa CV ứng viên và Job Description, tích hợp Google Gemini AI với các tính năng:

- ✅ Cho điểm match từ 0-100
- ✅ Phân tích chi tiết: strengths, weaknesses, improvement tips
- ✅ Context ngành nghề (IT, Marketing, Sales, Finance...)
- ✅ Context công ty (văn hóa, giá trị, yêu cầu)
- ✅ Caching 24h để tiết kiệm API calls
- ✅ Rate limiting: 100 lượt/user/ngày, 2 lượt/IP/ngày
- ✅ Admin có thể tùy chỉnh prompt theo ngành

---

## 🗄️ CẤU TRÚC DATABASE

### Bảng mới
1. **ai_match_results** - Cache kết quả phân tích AI
2. **ai_usage_logs** - Log usage cho rate limiting
3. **industry_contexts** - Cấu hình prompt AI theo ngành nghề

### Bảng đã có (đã thêm cột)
4. **companies** - Thêm `company_context` (JSON)
5. **jobs** - Thêm `ai_context` (JSON)
6. **categories** - Thêm `industry_key` (VARCHAR)

---

## 🚀 HƯỚNG DẪN CÀI ĐẶT

### 1. Chạy SQL Migration Scripts

Chạy các file SQL trong thư mục `sql/` theo thứ tự:

```bash
# SQL Server Management Studio hoặc Azure Data Studio
01_create_ai_match_results_table.sql
02_create_ai_usage_logs_table.sql
03_create_industry_contexts_table.sql
04_alter_companies_add_company_context.sql
05_alter_jobs_add_ai_context.sql
06_alter_categories_add_industry_key.sql
```

### 2. Cấu hình Gemini API Key

File `application.properties` hoặc biến môi trường:

```properties
# Gemini API Configuration
gemini.api.key=${GEMINI_API_KEY:your-gemini-api-key}
```

**Lấy Gemini API Key:**
1. Truy cập: https://aistudio.google.com/app/apikey
2. Tạo API key mới
3. Copy và paste vào `GEMINI_API_KEY` environment variable

### 3. Build & Run Application

```bash
# Build với Maven
mvn clean install

# Chạy ứng dụng
mvn spring-boot:run

# Hoặc chạy file JAR
java -jar target/demo-0.0.1-SNAPSHOT.jar
```

---

## 📡 API ENDPOINTS

### 1. AI CV Matching (Cho User/Candidate)

**POST** `/api/ai/check-match`

**Request Body:**
```json
{
  "jobId": 1,
  "resumeId": 2
}
```

**Response Success (200 OK):**
```json
{
  "success": true,
  "message": "Phân tích thành công",
  "data": {
    "source": "ai",
    "match_score": 85,
    "analysis": {
      "match_score": 85,
      "summary": "Ứng viên rất phù hợp với vị trí Backend Laravel...",
      "strengths": [
        "4 năm kinh nghiệm với Laravel framework",
        "Thành thạo MySQL, REST API design",
        "Có kinh nghiệm Docker và AWS"
      ],
      "weaknesses": [
        "CV không đề cập đến testing (PHPUnit)",
        "Thiếu thông tin về CI/CD pipeline"
      ],
      "improvement_tip": "Bổ sung thêm thông tin về testing practices..."
    },
    "industry_detected": "it",
    "remaining_usage": 99
  }
}
```

**Response Error (429 Too Many Requests):**
```json
{
  "success": false,
  "message": "Bạn đã hết lượt sử dụng miễn phí hôm nay",
  "data": null
}
```

**Rate Limiting:**
- User đã đăng nhập: **100 lượt/ngày**
- Guest (IP): **2 lượt/ngày**

---

### 2. Industry Contexts Management (Admin Only)

#### GET `/api/admin/industry-contexts`
Lấy danh sách tất cả industry contexts

**Response:**
```json
{
  "success": true,
  "data": [
    {
      "id": 1,
      "key": "it",
      "name": "Công nghệ thông tin",
      "description": "Ngành IT/Tech",
      "config": "{...}",
      "isActive": true,
      "createdAt": "2025-01-01T10:00:00",
      "updatedAt": "2025-01-01T10:00:00"
    }
  ]
}
```

#### POST `/api/admin/industry-contexts`
Tạo mới industry context

**Request Body:**
```json
{
  "key": "healthcare",
  "name": "Y tế",
  "description": "Ngành y tế và chăm sóc sức khỏe",
  "config": "{\"role\":\"Chuyên gia Tuyển dụng Y tế\",\"focus_areas\":[\"Chứng chỉ hành nghề\",\"Kinh nghiệm lâm sàng\"],\"key_metrics\":\"Số năm kinh nghiệm, chứng chỉ\",\"red_flags\":\"Thiếu chứng chỉ bắt buộc\"}",
  "isActive": true
}
```

#### PUT `/api/admin/industry-contexts/{id}`
Cập nhật industry context

#### DELETE `/api/admin/industry-contexts/{id}`
Xóa industry context

#### PATCH `/api/admin/industry-contexts/{id}/toggle`
Bật/tắt industry context

---

## 🎯 CẤU TRÚC DỮ LIỆU JSON

### 1. Company Context (`companies.company_context`)

```json
{
  "culture": "Startup năng động, học hỏi nhanh",
  "values": ["Innovation", "Teamwork", "Customer-first"],
  "work_style": "Agile, Remote-friendly",
  "general_requirements": "Tự chủ cao, chủ động học hỏi"
}
```

**Cách cập nhật:**
```sql
UPDATE companies 
SET company_context = N'{
  "culture": "Startup năng động",
  "values": ["Innovation", "Teamwork"],
  "work_style": "Agile"
}'
WHERE id = 1;
```

### 2. Job AI Context (`jobs.ai_context`)

```json
{
  "must_have_skills": ["Laravel", "MySQL", "REST API"],
  "nice_to_have_skills": ["Vue.js", "Docker", "AWS"],
  "min_experience_years": 3,
  "weights": {
    "skills": 40,
    "experience": 30,
    "education": 10,
    "soft_skills": 20
  },
  "red_flags": ["Không có kinh nghiệm backend", "CV quá chung chung"]
}
```

**Cách cập nhật:**
```sql
UPDATE jobs 
SET ai_context = N'{
  "must_have_skills": ["Spring Boot", "Java", "MySQL"],
  "nice_to_have_skills": ["React", "Docker"],
  "min_experience_years": 2,
  "weights": {"skills": 50, "experience": 30, "education": 20}
}'
WHERE id = 1;
```

### 3. Industry Context (`industry_contexts.config`)

```json
{
  "role": "Chuyên gia Tuyển dụng IT/Tech",
  "focus_areas": [
    "Tech Stack và công nghệ cụ thể (frameworks, languages, tools)",
    "Kinh nghiệm thực tế với dự án, codebase scale",
    "Kỹ năng giải quyết vấn đề kỹ thuật",
    "Portfolio/GitHub"
  ],
  "key_metrics": "Số năm kinh nghiệm với từng tech stack, project complexity",
  "red_flags": "Thiếu hands-on experience, không có portfolio/GitHub"
}
```

---

## 🔧 HƯỚNG DẪN SỬ DỤNG

### Cho Developer

1. **Setup môi trường:**
   - Chạy SQL migration scripts
   - Cấu hình Gemini API key
   - Build & run application

2. **Test API:**
   ```bash
   # Test với Postman
   POST http://localhost:8081/api/ai/check-match
   Headers: Authorization: Bearer <your-jwt-token>
   Body: {"jobId": 1, "resumeId": 2}
   ```

### Cho Admin

1. **Quản lý Industry Contexts:**
   - Truy cập: `GET /api/admin/industry-contexts`
   - Tạo mới prompt cho ngành mới
   - Tùy chỉnh focus_areas, key_metrics, red_flags

2. **Cấu hình Company Context:**
   - Vào database, update `companies.company_context`
   - Hoặc tạo API endpoint riêng (tuỳ chọn)

3. **Cấu hình Job AI Context:**
   - Khi tạo/edit job, thêm field `aiContext` (JSON)
   - Hoặc update trực tiếp trong database

4. **Map Categories với Industry:**
   ```sql
   UPDATE categories 
   SET industry_key = 'it' 
   WHERE name LIKE N'%Công nghệ%';
   
   UPDATE categories 
   SET industry_key = 'marketing' 
   WHERE name LIKE N'%Marketing%';
   ```

### Cho Users/Candidates

1. **Phân tích CV với Job:**
   - Click "Phân tích độ phù hợp" trên job detail page
   - Chọn CV đã tạo
   - Xem kết quả: điểm số + chi tiết strengths/weaknesses
   - Mỗi user có **100 lượt/ngày**

2. **Guest Users (chưa đăng nhập):**
   - Có thể dùng thử **2 lượt/IP/ngày**
   - Khuyến khích đăng nhập để có thêm quota

---

## 🧪 TEST CASES

### Test 1: Perfect Match IT
```json
{
  "jobId": 1,  // Backend Developer Laravel
  "resumeId": 2 // CV: 4 năm Laravel, MySQL, REST API, Docker
}
```
**Expected Score:** 85-95

### Test 2: Mismatch
```json
{
  "jobId": 3,  // Frontend Developer React
  "resumeId": 5 // CV: Graphic Designer - Photoshop, Illustrator
}
```
**Expected Score:** 10-25

### Test 3: Partial Match
```json
{
  "jobId": 2,  // Full-stack: React + Node.js
  "resumeId": 6 // CV: Full-stack - React ✅, PHP ❌ (không có Node.js)
}
```
**Expected Score:** 45-60

---

## 📊 MONITORING & LOGGING

### Logs quan trọng

```java
log.info("AI Match request: userId={}, jobId={}, resumeId={}, ip={}", ...);
log.info("Using cached result: id={}", cachedResult.getId());
log.info("Calling Gemini API for analysis...");
log.info("Saved AI match result: id={}, score={}", result.getId(), matchScore);
```

### Rate Limiting Check

```sql
-- Check usage của user
SELECT COUNT(*) FROM ai_usage_logs 
WHERE user_id = 1 
  AND feature_name = 'match_cv'
  AND created_at >= DATEADD(HOUR, -24, GETDATE());

-- Check usage của IP
SELECT COUNT(*) FROM ai_usage_logs 
WHERE ip_address = '192.168.1.100'
  AND user_id IS NULL
  AND feature_name = 'match_cv'
  AND created_at >= DATEADD(HOUR, -24, GETDATE());
```

---

## 🔒 BẢO MẬT & OPTIMIZATION

### Rate Limiting
- User: 100 lượt/ngày
- Guest IP: 2 lượt/ngày
- Auto reset sau 24h

### Caching
- Cache 24h
- Invalidate khi resume updated
- Check `resume.updatedAt` vs `aiMatchResult.updatedAt`

### Security
- JWT authentication required (trừ guest)
- User chỉ được analyze CV của mình
- Admin role required cho industry context management

### Performance
- Index trên `(user_id, job_id)` cho cache lookup
- Index trên `(user_id, feature_name, created_at)` cho rate limit
- Lazy loading cho Job.categories, Resume.skills...

---

## 🐛 TROUBLESHOOTING

### Lỗi: "Gemini API error: 403"
- Check API key có đúng không
- Check quota của Gemini API (free tier có limit)

### Lỗi: "Hết lượt sử dụng miễn phí"
- User: Check trong `ai_usage_logs` với `user_id`
- Guest: Check với `ip_address`
- Reset: Xóa record cũ hơn 24h

### Lỗi: "Resume không tồn tại"
- Check `resumeId` có tồn tại trong database
- Check user có quyền access resume đó không

### Cache không hoạt động
- Check `resume.updatedAt` <= `aiMatchResult.updatedAt`
- Check `aiMatchResult.createdAt` trong vòng 24h
- Check `userId` có khớp không

---

## 📚 THAM KHẢO

- **Gemini API Docs:** https://ai.google.dev/docs
- **Spring Boot Docs:** https://spring.io/projects/spring-boot
- **JPA Repository:** https://docs.spring.io/spring-data/jpa/docs/current/reference/html/

---

## 📝 NOTES

1. **Gemini API Key** cần valid và có quota đủ
2. **Database relationships** phải đã được định nghĩa đầy đủ
3. **Resume structure** phải có relations: educations, experiences, skills, projects...
4. **Error handling** đã được implement cho các trường hợp: timeout, invalid JSON, rate limit
5. **Testing** nên test với nhiều edge cases: empty CV, generic JD, overqualified...

---

## ✅ CHECKLIST TRIỂN KHAI

- [ ] Chạy 6 SQL migration scripts
- [ ] Cấu hình `GEMINI_API_KEY` trong environment
- [ ] Build & run application
- [ ] Test API `/api/ai/check-match` với Postman
- [ ] Seed data cho `industry_contexts` (đã có sẵn trong script 03)
- [ ] Map `categories.industry_key` cho các category hiện có
- [ ] (Optional) Update `company_context` cho các company
- [ ] (Optional) Update `ai_context` cho các job
- [ ] Test trên UI (nếu có frontend)
- [ ] Monitor logs và performance

---

**Version:** 1.0.0  
**Last Updated:** 2026-01-02  
**Author:** AI System Development Team
