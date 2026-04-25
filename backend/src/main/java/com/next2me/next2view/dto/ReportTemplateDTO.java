package com.next2me.next2view.dto;

import lombok.*;
import java.util.List;

@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class ReportTemplateDTO {
    private String id;
    private String name;
    private String description;
    private String icon;
    private String category;
    private List<String> dataSources;
    private boolean aiEnhanced;
}
