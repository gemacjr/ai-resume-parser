package com.swiftbeard.ai_resume_parser.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class JobDescription {
    private String id;
    private String title;
    private String company;
    private String description;
    private List<String> requiredSkills;
    private List<String> preferredSkills;
    private String experienceLevel;
    private String location;
    private List<String> responsibilities;
    private List<String> qualifications;
}
