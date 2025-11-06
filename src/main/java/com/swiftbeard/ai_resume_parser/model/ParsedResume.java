package com.swiftbeard.ai_resume_parser.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ParsedResume {
    private String id;
    private String fileName;
    private String rawText;
    private String candidateName;
    private String email;
    private String phone;
    private String summary;
    private List<String> skills;
    private List<Experience> experiences;
    private List<Education> educations;
    private List<String> certifications;
    private Map<String, Object> metadata;
    private LocalDateTime parsedAt;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Experience {
        private String company;
        private String position;
        private String duration;
        private String description;
        private List<String> achievements;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Education {
        private String institution;
        private String degree;
        private String field;
        private String year;
    }
}
