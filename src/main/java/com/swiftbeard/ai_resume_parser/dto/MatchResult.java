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
public class MatchResult {
    private String resumeId;
    private String jobDescriptionId;
    private double matchScore; // 0.0 to 1.0
    private List<String> matchedSkills;
    private List<String> missingSkills;
    private String analysis;
    private Map<String, Double> categoryScores;
    private List<String> recommendations;
}
