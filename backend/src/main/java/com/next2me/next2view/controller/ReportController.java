package com.next2me.next2view.controller;

import com.next2me.next2view.dto.ReportDataDTO;
import com.next2me.next2view.dto.ReportTemplateDTO;
import com.next2me.next2view.model.User;
import com.next2me.next2view.repository.UserRepository;
import com.next2me.next2view.service.ReportService;
import com.next2me.next2view.service.PdfReportGenerator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestClient;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/reports")
@RequiredArgsConstructor
@Slf4j
public class ReportController {

    private final ReportService reportService;
    private final PdfReportGenerator pdfReportGenerator;
    private final UserRepository userRepository;

    @Value("${anthropic.api.key:}")
    private String anthropicApiKey;

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
            ReportDataDTO data = reportService.generatePreviewData(templateId);
            byte[] content = pdfReportGenerator.generatePdf(data);

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

    // ── POST /api/reports/ai-query ── Free-form AI question ──
    @PostMapping("/ai-query")
    public ResponseEntity<java.util.Map<String, String>> aiQuery(@RequestBody java.util.Map<String, String> request) {
        User user = getCurrentUser();
        if (!isCEO(user)) {
            return ResponseEntity.status(403).build();
        }

        String question = request.get("question");
        if (question == null || question.isBlank()) {
            return ResponseEntity.badRequest().body(java.util.Map.of("answer", "No question provided"));
        }

        try {
            // Build context from all data
            var data = reportService.generateExecSummaryData();
            StringBuilder context = new StringBuilder();
            context.append("PLATFORM DATA (Next2View - Next2me Group):\n");
            if (data.getSummary() != null) {
                data.getSummary().forEach((k, v) -> context.append(k).append(": ").append(v).append("\n"));
            }
            if (data.getSections() != null) {
                for (var section : data.getSections()) {
                    context.append("\nSection: ").append(section.get("title")).append("\n");
                    var users = (java.util.List<?>) section.get("users");
                    if (users != null) {
                        for (var u : users) {
                            var m = (java.util.Map<?,?>) u;
                            context.append("  - ").append(m.get("fullName"))
                                .append(" | ").append(m.get("company"))
                                .append(" | ").append(m.get("username"))
                                .append(" | Status: ").append(m.get("role")).append("\n");
                        }
                    }
                }
            }

            String prompt = "\u0395\u03af\u03c3\u03b1\u03b9 \u03bf AI \u03c3\u03cd\u03bc\u03b2\u03bf\u03c5\u03bb\u03bf\u03c2 \u03c4\u03bf\u03c5 CEO \u03c4\u03bf\u03c5 Next2me Group. " +
                "\u0391\u03c0\u03ac\u03bd\u03c4\u03b7\u03c3\u03b5 \u03c3\u03c4\u03b1 \u0395\u03bb\u03bb\u03b7\u03bd\u03b9\u03ba\u03ac \u03bc\u03b5 \u03b2\u03ac\u03c3\u03b7 \u03c4\u03b1 \u03c0\u03b1\u03c1\u03b1\u03ba\u03ac\u03c4\u03c9 data:\n\n" +
                context.toString() + "\n\nUSER QUESTION: " + question +
                "\n\n\u0391\u03c0\u03ac\u03bd\u03c4\u03b7\u03c3\u03b5 \u03c3\u03b5 Markdown format, \u03c3\u03cd\u03bd\u03c4\u03bf\u03bc\u03b1 \u03ba\u03b1\u03b9 \u03bf\u03c5\u03c3\u03b9\u03b1\u03c3\u03c4\u03b9\u03ba\u03ac.";

            var client = RestClient.create();
            var requestBody = java.util.Map.of(
                "model", "claude-sonnet-4-5",
                "max_tokens", 1500,
                "messages", java.util.List.of(java.util.Map.of("role", "user", "content", prompt))
            );

            var response = client.post()
                .uri("https://api.anthropic.com/v1/messages")
                .header("x-api-key", anthropicApiKey)
                .header("anthropic-version", "2023-06-01")
                .header("content-type", "application/json")
                .body(requestBody)
                .retrieve()
                .toEntity(java.util.Map.class);

            var body = response.getBody();
            var content = (java.util.List<?>) body.get("content");
            var text = (java.util.Map<?,?>) content.get(0);
            String answer = (String) text.get("text");

            return ResponseEntity.ok(java.util.Map.of("answer", answer));
        } catch (Exception e) {
            return ResponseEntity.ok(java.util.Map.of("answer",
                "\u26a0\ufe0f Error: " + e.getMessage()));
        }
    }

}