package com.next2me.next2view.controller;

import com.next2me.next2view.service.ProjectService;
import com.next2me.next2view.dto.ProjectDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestClient;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/ai")
@RequiredArgsConstructor
@Slf4j
public class AiController {

    private final ProjectService projectService;

    @Value("${anthropic.api.key:}")
    private String anthropicApiKey;

    @PostMapping("/ceo-report")
    @PreAuthorize("hasRole('CEO')")
    public ResponseEntity<Map<String, String>> generateCeoReport() {
        List<ProjectDto> projects = projectService.findAll(null, null);

        StringBuilder prompt = new StringBuilder();
        prompt.append("Είσαι ο AI σύμβουλος του CEO του Ομίλου Next2me. ");
        prompt.append("Ανάλυσε τα παρακάτω projects και δώσε αναφορά στα Ελληνικά.\n\n");
        prompt.append("PROJECTS:\n");

        for (ProjectDto p : projects) {
            prompt.append(String.format("- %s | %s | %s | %d%% | %s | Tasks: %d/%d\n",
                p.title(), p.companyName(), p.category(), p.completion(),
                p.status(), p.tasksDone(), p.tasksTotal()));
        }

        prompt.append("\nΔώσε:\n");
        prompt.append("1. ΣΥΝΟΛΙΚΗ ΕΙΚΟΝΑ (2-3 προτάσεις)\n");
        prompt.append("2. TOP 3 ΠΡΟΒΛΗΜΑΤΑ (αν υπάρχουν)\n");
        prompt.append("3. ΑΝΑΛΥΣΗ ΑΝΑ ΚΑΤΗΓΟΡΙΑ (Finance/Legal/Developing/Marketing)\n");
        prompt.append("4. ΠΡΟΤΕΙΝΟΜΕΝΕΣ ΕΝΕΡΓΕΙΕΣ (3-5 bullet points)\n");
        prompt.append("Χρησιμοποίησε emoji για καλύτερη αναγνωσιμότητα. Απάντησε σε Markdown format.");

        try {
            var client = RestClient.create();
            var requestBody = Map.of(
                "model", "claude-sonnet-4-5",
                "max_tokens", 1500,
                "messages", List.of(Map.of("role", "user", "content", prompt.toString()))
            );

            var response = client.post()
                .uri("https://api.anthropic.com/v1/messages")
                .header("x-api-key", anthropicApiKey)
                .header("anthropic-version", "2023-06-01")
                .header("content-type", "application/json")
                .body(requestBody)
                .retrieve()
                .toEntity(Map.class);

            var body = response.getBody();
            var content = (List<?>) body.get("content");
            var text = (Map<?,?>) content.get(0);
            String report = (String) text.get("text");

            return ResponseEntity.ok(Map.of("report", report));
        } catch (Exception e) {
            log.error("AI Report error: {}", e.getMessage());
            return ResponseEntity.ok(Map.of("report",
                "## ⚠️ AI Report\n\nΔεν ήταν δυνατή η δημιουργία αναφοράς: " + e.getMessage()));
        }
    }
}