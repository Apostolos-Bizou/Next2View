package com.next2me.next2view.controller;

import com.next2me.next2view.dto.ReportDataDTO;
import com.next2me.next2view.dto.ReportTemplateDTO;
import com.next2me.next2view.model.User;
import com.next2me.next2view.repository.UserRepository;
import com.next2me.next2view.service.ReportService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/reports")
@RequiredArgsConstructor
@Slf4j
public class ReportController {

    private final ReportService reportService;
    private final UserRepository userRepository;

    // ── Helper: get current user + CEO check ──
    private User getCurrentUser() {
        String subjectId = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findById(UUID.fromString(subjectId))
            .orElseThrow(() -> new RuntimeException("User not found: " + subjectId));
    }

    private boolean isCEO(User user) {
        return "CEO".equals(user.getRole().name());
    }

    // ── GET /api/reports/templates ──
    @GetMapping("/templates")
    public ResponseEntity<List<ReportTemplateDTO>> getTemplates() {
        User user = getCurrentUser();
        if (!isCEO(user)) {
            return ResponseEntity.status(403).build();
        }
        return ResponseEntity.ok(reportService.getTemplates());
    }

    // ── GET /api/reports/preview/{templateId} ──
    @GetMapping("/preview/{templateId}")
    public ResponseEntity<ReportDataDTO> getPreview(@PathVariable String templateId) {
        User user = getCurrentUser();
        if (!isCEO(user)) {
            return ResponseEntity.status(403).build();
        }

        var template = reportService.getTemplate(templateId);
        if (template.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        try {
            ReportDataDTO data = reportService.generatePreviewData(templateId);
            return ResponseEntity.ok(data);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    // ── GET /api/reports/generate/{templateId} — PDF download ──
    @GetMapping("/generate/{templateId}")
    public ResponseEntity<byte[]> generateReport(@PathVariable String templateId) {
        User user = getCurrentUser();
        if (!isCEO(user)) {
            return ResponseEntity.status(403).build();
        }

        var template = reportService.getTemplate(templateId);
        if (template.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        try {
            // For now, return preview data as JSON
            // PDF generation will be added in Step 3 (PdfReportGenerator)
            ReportDataDTO data = reportService.generatePreviewData(templateId);

            // Log the generation
            reportService.logGeneration(templateId, template.get().getName(), user.getId(), 0L);

            // TODO: Replace with actual PDF bytes from PdfReportGenerator
            // For now, return a placeholder response
            String jsonContent = "PDF generation will be implemented in Step 3";
            byte[] content = jsonContent.getBytes();

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_PDF);
            headers.setContentDispositionFormData("attachment",
                templateId + "_" + java.time.LocalDate.now() + ".pdf");

            return ResponseEntity.ok()
                .headers(headers)
                .body(content);

        } catch (Exception e) {
            log.error("Error generating report: {}", templateId, e);
            return ResponseEntity.internalServerError().build();
        }
    }
}
