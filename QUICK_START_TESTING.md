# 🧪 QUICK START - TEST AI MATCHING API

## 1️⃣ SETUP CƠ BẢN (10 phút)

### Bước 1: Chạy SQL Scripts
```sql
-- Mở SQL Server Management Studio, chạy lần lượt:
-- File trong thư mục sql/
01_create_ai_match_results_table.sql
02_create_ai_usage_logs_table.sql
03_create_industry_contexts_table.sql
04_alter_companies_add_company_context.sql
05_alter_jobs_add_ai_context.sql
06_alter_categories_add_industry_key.sql
```

### Bước 2: Cấu hình Gemini API Key
```bash
# Windows PowerShell
$env:GEMINI_API_KEY="AIzaSy..."

# Hoặc thêm vào application.properties:
gemini.api.key=AIzaSy...
```

### Bước 3: Build & Run
```bash
mvn clean install
mvn spring-boot:run
```

---

## 2️⃣ TEST VỚI POSTMAN

### Test 1: Guest User (Không đăng nhập)

**Request:**
```
POST http://localhost:8081/api/ai/check-match
Content-Type: application/json

{
  "jobId": 1,
  "resumeId": 1
}
```

**Expected Response:**
```json
{
  "success": true,
  "message": "Phân tích thành công",
  "data": {
    "source": "ai",
    "match_score": 75,
    "analysis": {
      "match_score": 75,
      "summary": "...",
      "strengths": [...],
      "weaknesses": [...],
      "improvement_tip": "..."
    },
    "industry_detected": "it",
    "remaining_usage": 1
  }
}
```

**Rate Limit:** 2 lượt/IP/ngày

---

### Test 2: Authenticated User

**Step 1: Login để lấy JWT token**
```
POST http://localhost:8081/api/auth/login
Content-Type: application/json

{
  "email": "user@example.com",
  "password": "password123"
}
```

**Step 2: Call AI Match API với Bearer token**
```
POST http://localhost:8081/api/ai/check-match
Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
Content-Type: application/json

{
  "jobId": 1,
  "resumeId": 2
}
```

**Rate Limit:** 100 lượt/user/ngày

---

## 3️⃣ TEST ADMIN ENDPOINTS

### Login as Admin
```
POST http://localhost:8081/api/auth/login
Content-Type: application/json

{
  "email": "admin@example.com",
  "password": "Admin@123"
}
```

### Get All Industry Contexts
```
GET http://localhost:8081/api/admin/industry-contexts
Authorization: Bearer <admin-jwt-token>
```

### Create New Industry Context
```
POST http://localhost:8081/api/admin/industry-contexts
Authorization: Bearer <admin-jwt-token>
Content-Type: application/json

{
  "key": "healthcare",
  "name": "Y tế",
  "description": "Ngành y tế và chăm sóc sức khỏe",
  "config": "{\"role\":\"Chuyên gia Tuyển dụng Y tế\",\"focus_areas\":[\"Chứng chỉ hành nghề\",\"Kinh nghiệm lâm sàng\"],\"key_metrics\":\"Số năm kinh nghiệm\",\"red_flags\":\"Thiếu chứng chỉ\"}",
  "isActive": true
}
```

### Toggle Active/Inactive
```
PATCH http://localhost:8081/api/admin/industry-contexts/1/toggle
Authorization: Bearer <admin-jwt-token>
```

---

## 4️⃣ TEST CASES - EXPECTED RESULTS

### Case 1: High Match (IT Job + IT CV)
```json
{
  "jobId": 1,     // "Backend Developer - Spring Boot, MySQL, REST API"
  "resumeId": 2   // CV: "4 năm Spring Boot, Java, MySQL, có 5 dự án backend"
}
```
**Expected Score:** 80-95

---

### Case 2: Low Match (Mismatch)
```json
{
  "jobId": 5,     // "Frontend Developer - React, TypeScript"
  "resumeId": 8   // CV: "Graphic Designer - Photoshop, Illustrator, UI/UX"
}
```
**Expected Score:** 10-30

---

### Case 3: Medium Match (Partial Skills)
```json
{
  "jobId": 3,     // "Full-stack Developer - React + Node.js"
  "resumeId": 4   // CV: "Full-stack - React ✅, PHP ❌ (không có Node.js)"
}
```
**Expected Score:** 45-65

---

## 5️⃣ VERIFY CACHE WORKING

**Test 1: First Call (AI)**
```
POST /api/ai/check-match
Body: {"jobId": 1, "resumeId": 2}

Response: "source": "ai"  ← Gọi Gemini API
```

**Test 2: Second Call (Cache)**
```
POST /api/ai/check-match
Body: {"jobId": 1, "resumeId": 2}  ← Same jobId + resumeId

Response: "source": "cache"  ← Lấy từ cache, KHÔNG gọi API
```

**Cache valid trong 24h và resume không bị update.**

---

## 6️⃣ VERIFY RATE LIMITING

### Test User Rate Limit (100 lượt/ngày)

```bash
# Loop 101 lần (Windows PowerShell)
for ($i=1; $i -le 101; $i++) {
  Invoke-RestMethod -Uri "http://localhost:8081/api/ai/check-match" `
    -Method Post `
    -Headers @{"Authorization"="Bearer $token"} `
    -Body '{"jobId":1,"resumeId":2}' `
    -ContentType "application/json"
}

# Lần thứ 101 sẽ trả về:
# "message": "Bạn đã hết lượt sử dụng miễn phí hôm nay"
```

### Test IP Rate Limit (2 lượt/ngày)

```bash
# Call 3 lần không đăng nhập từ cùng IP
# Lần thứ 3 sẽ bị block:
# "message": "Bạn đã hết lượt sử dụng miễn phí. Vui lòng đăng nhập"
```

---

## 7️⃣ CHECK DATABASE

### View AI Match Results
```sql
SELECT TOP 10
    id,
    user_id,
    job_id,
    resume_id,
    match_score,
    industry_detected,
    created_at,
    updated_at
FROM ai_match_results
ORDER BY created_at DESC;
```

### View Usage Logs
```sql
SELECT TOP 20
    user_id,
    ip_address,
    feature_name,
    created_at
FROM ai_usage_logs
ORDER BY created_at DESC;
```

### Count User Usage
```sql
SELECT 
    user_id,
    COUNT(*) as usage_count
FROM ai_usage_logs
WHERE feature_name = 'match_cv'
  AND created_at >= DATEADD(HOUR, -24, GETDATE())
GROUP BY user_id
ORDER BY usage_count DESC;
```

---

## 8️⃣ TROUBLESHOOTING

### ❌ Error: "Gemini API error: 403"
**Cause:** API key không đúng hoặc hết quota  
**Fix:** 
1. Check API key: https://aistudio.google.com/app/apikey
2. Tạo key mới nếu cần
3. Update `GEMINI_API_KEY` environment variable

### ❌ Error: "Resume không tồn tại"
**Cause:** ResumeId không tồn tại trong DB  
**Fix:** Check `SELECT * FROM resumes WHERE id = X`

### ❌ Error: "Invalid JSON in analysis_data"
**Cause:** Gemini trả về JSON bị lỗi format  
**Fix:** 
1. Check logs để xem raw response
2. Có thể Gemini API đang có vấn đề tạm thời
3. Retry sau vài phút

### ❌ Cache không hoạt động
**Cause:** Resume bị update sau khi cache  
**Fix:** 
```sql
-- Check timestamps
SELECT 
    r.updated_at as resume_updated,
    a.updated_at as cache_updated,
    a.created_at as cache_created
FROM ai_match_results a
JOIN resumes r ON a.resume_id = r.id
WHERE a.id = X;
```

---

## 9️⃣ PERFORMANCE MONITORING

### Check Response Time
```bash
# Measure API response time
Measure-Command {
  Invoke-RestMethod -Uri "http://localhost:8081/api/ai/check-match" `
    -Method Post `
    -Headers @{"Authorization"="Bearer $token"} `
    -Body '{"jobId":1,"resumeId":2}' `
    -ContentType "application/json"
}

# Expected:
# - AI call: 3-8 seconds (gọi Gemini API)
# - Cache hit: < 500ms
```

### Monitor Gemini API Usage
```bash
# Check trong Google AI Studio:
# https://aistudio.google.com/app/apikey
# → Xem usage statistics
```

---

## 🎉 SUCCESS CRITERIA

✅ **Setup Complete:**
- [ ] All SQL scripts executed successfully
- [ ] Gemini API key configured
- [ ] Application runs without errors

✅ **API Working:**
- [ ] `/api/ai/check-match` returns valid response
- [ ] Match score is between 0-100
- [ ] Analysis contains strengths, weaknesses, improvement_tip
- [ ] Industry detected correctly (it, marketing, sales, etc.)

✅ **Cache Working:**
- [ ] First call: `source: "ai"`
- [ ] Second call: `source: "cache"`
- [ ] Response time < 500ms for cached results

✅ **Rate Limiting Working:**
- [ ] User can call 100 times/day
- [ ] Guest IP can call 2 times/day
- [ ] After limit: 429 Too Many Requests

✅ **Admin Panel Working:**
- [ ] Can view industry contexts
- [ ] Can create new industry context
- [ ] Can update and toggle active/inactive

---

**Happy Testing! 🚀**
