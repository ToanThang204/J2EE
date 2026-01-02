package com.hutech.demo.controller;

import com.hutech.demo.dto.response.ApiResponse;
import com.hutech.demo.model.IndustryContext;
import com.hutech.demo.repository.IndustryContextRepository;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Controller quản lý Industry Contexts (Admin only)
 */
@RestController
@RequestMapping("/api/admin/industry-contexts")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
@Slf4j
public class IndustryContextController {

    private final IndustryContextRepository industryContextRepository;

    /**
     * GET /api/admin/industry-contexts
     * Lấy danh sách tất cả industry contexts
     */
    @GetMapping
    public ResponseEntity<ApiResponse<List<IndustryContext>>> getAllIndustryContexts() {
        List<IndustryContext> contexts = industryContextRepository.findAll();
        return ResponseEntity.ok(ApiResponse.success("Lấy danh sách thành công", contexts));
    }

    /**
     * GET /api/admin/industry-contexts/{id}
     * Lấy chi tiết một industry context
     */
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<IndustryContext>> getIndustryContext(@PathVariable Long id) {
        IndustryContext context = industryContextRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Industry context không tồn tại"));
        return ResponseEntity.ok(ApiResponse.success("Lấy thông tin thành công", context));
    }

    /**
     * POST /api/admin/industry-contexts
     * Tạo mới industry context
     */
    @PostMapping
    public ResponseEntity<ApiResponse<IndustryContext>> createIndustryContext(
            @Valid @RequestBody IndustryContext request) {
        
        // Check key đã tồn tại chưa
        if (industryContextRepository.existsByKey(request.getKey())) {
            return ResponseEntity.badRequest()
                .body(ApiResponse.error("Key đã tồn tại: " + request.getKey(), null));
        }

        IndustryContext context = new IndustryContext();
        context.setKey(request.getKey());
        context.setName(request.getName());
        context.setDescription(request.getDescription());
        context.setConfig(request.getConfig());
        context.setIsActive(request.getIsActive() != null ? request.getIsActive() : true);
        
        IndustryContext saved = industryContextRepository.save(context);
        log.info("Created industry context: key={}, name={}", saved.getKey(), saved.getName());
        
        return ResponseEntity.ok(ApiResponse.success("Tạo mới thành công", saved));
    }

    /**
     * PUT /api/admin/industry-contexts/{id}
     * Cập nhật industry context
     */
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<IndustryContext>> updateIndustryContext(
            @PathVariable Long id,
            @Valid @RequestBody IndustryContext request) {
        
        IndustryContext context = industryContextRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Industry context không tồn tại"));

        // Cập nhật các field
        if (request.getName() != null) {
            context.setName(request.getName());
        }
        if (request.getDescription() != null) {
            context.setDescription(request.getDescription());
        }
        if (request.getConfig() != null) {
            context.setConfig(request.getConfig());
        }
        if (request.getIsActive() != null) {
            context.setIsActive(request.getIsActive());
        }
        context.setUpdatedAt(LocalDateTime.now());
        
        IndustryContext saved = industryContextRepository.save(context);
        log.info("Updated industry context: id={}, key={}", id, saved.getKey());
        
        return ResponseEntity.ok(ApiResponse.success("Cập nhật thành công", saved));
    }

    /**
     * DELETE /api/admin/industry-contexts/{id}
     * Xóa industry context
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteIndustryContext(@PathVariable Long id) {
        if (!industryContextRepository.existsById(id)) {
            return ResponseEntity.badRequest()
                .body(ApiResponse.error("Industry context không tồn tại", null));
        }
        
        industryContextRepository.deleteById(id);
        log.info("Deleted industry context: id={}", id);
        
        return ResponseEntity.ok(ApiResponse.success("Xóa thành công", null));
    }

    /**
     * PATCH /api/admin/industry-contexts/{id}/toggle
     * Bật/tắt industry context
     */
    @PatchMapping("/{id}/toggle")
    public ResponseEntity<ApiResponse<IndustryContext>> toggleActive(@PathVariable Long id) {
        IndustryContext context = industryContextRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Industry context không tồn tại"));
        
        context.setIsActive(!context.getIsActive());
        context.setUpdatedAt(LocalDateTime.now());
        
        IndustryContext saved = industryContextRepository.save(context);
        log.info("Toggled industry context: id={}, isActive={}", id, saved.getIsActive());
        
        return ResponseEntity.ok(ApiResponse.success("Cập nhật trạng thái thành công", saved));
    }
}
