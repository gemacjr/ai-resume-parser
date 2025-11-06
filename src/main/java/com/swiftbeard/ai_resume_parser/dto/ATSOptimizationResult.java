package com.swiftbeard.ai_resume_parser.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ATSOptimizationResult {
    private String resumeId;
    private double atsScore; // 0.0 to 100.0
    private List<Suggestion> suggestions;
    private Map<String, Object> metrics;
    private String overallAssessment;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Suggestion {
        private String category;
        private String issue;
        private String recommendation;
        private String priority; // HIGH, MEDIUM, LOW
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Metrics {
        private double keywordDensity;
        private int totalWords;
        private int uniqueKeywords;
        private boolean hasContactInfo;
        private boolean hasStandardSections;
        private int formattingIssues;
    }
}
