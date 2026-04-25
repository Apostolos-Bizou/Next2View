package com.next2me.next2view.dto;

import lombok.*;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class ReportDataDTO {
    private String templateId;
    private String templateName;
    private LocalDateTime generatedAt;
    private Map<String, Object> summary;
    private List<Map<String, Object>> sections;
}
