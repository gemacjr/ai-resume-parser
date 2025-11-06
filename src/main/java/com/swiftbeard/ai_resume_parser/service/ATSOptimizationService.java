package com.swiftbeard.ai_resume_parser.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.swiftbeard.ai_resume_parser.config.AppProperties;
import com.swiftbeard.ai_resume_parser.dto.ATSOptimizationResult;
import com.swiftbeard.ai_resume_parser.model.ParsedResume;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.stereotype.Service;

import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class ATSOptimizationService {

    private final ChatModel chatModel;
    private final AppProperties appProperties;
    private final ObjectMapper objectMapper = new ObjectMapper();

    private static final String ATS_OPTIMIZATION_PROMPT = """
            You are an ATS (Applicant Tracking System) optimization expert. Analyze this resume for ATS-friendliness.

            Resume:
            {resumeText}

            Evaluate the resume based on:
            1. Keyword optimization and density
            2. Formatting and structure (sections, headers)
            3. Contact information completeness
            4. Use of standard section names
            5. Avoidance of complex formatting (tables, graphics)
            6. Action verbs and quantifiable achievements

            Provide:
            - ATS Score (0-100)
            - Specific suggestions for improvement with priority (HIGH/MEDIUM/LOW)
            - Overall assessment

            Return the response in this JSON format:
            {
              "atsScore": 0.0,
              "suggestions": [
                {
                  "category": "Keywords",
                  "issue": "Low keyword density",
                  "recommendation": "Add more relevant technical skills",
                  "priority": "HIGH"
                }
              ],
              "overallAssessment": ""
            }

            Provide at least 5 actionable suggestions. Return ONLY valid JSON, no additional text.
            """;

    public ATSOptimizationResult optimizeForATS(ParsedResume resume) {
        try {
            log.info("Optimizing resume for ATS: {}", resume.getId());

            String resumeText = buildResumeText(resume);

            PromptTemplate promptTemplate = new PromptTemplate(ATS_OPTIMIZATION_PROMPT);
            Map<String, Object> params = new HashMap<>();
            params.put("resumeText", resumeText);

            Prompt prompt = promptTemplate.create(params);
            String response = chatModel.call(prompt).getResult().getOutput().getContent();

            log.debug("ATS Optimization Response: {}", response);

            ATSOptimizationResult result = parseOptimizationResponse(response);
            result.setResumeId(resume.getId());

            // Calculate metrics
            Map<String, Object> metrics = calculateMetrics(resume);
            result.setMetrics(metrics);

            return result;

        } catch (Exception e) {
            log.error("Error optimizing resume for ATS: {}", e.getMessage(), e);
            return createFallbackOptimizationResult(resume);
        }
    }

    private String buildResumeText(ParsedResume resume) {
        StringBuilder text = new StringBuilder();

        if (resume.getCandidateName() != null) {
            text.append("Name: ").append(resume.getCandidateName()).append("\n");
        }
        if (resume.getEmail() != null) {
            text.append("Email: ").append(resume.getEmail()).append("\n");
        }
        if (resume.getPhone() != null) {
            text.append("Phone: ").append(resume.getPhone()).append("\n");
        }

        text.append("\n");

        if (resume.getSummary() != null) {
            text.append("SUMMARY\n").append(resume.getSummary()).append("\n\n");
        }

        if (resume.getSkills() != null && !resume.getSkills().isEmpty()) {
            text.append("SKILLS\n").append(String.join(", ", resume.getSkills())).append("\n\n");
        }

        if (resume.getExperiences() != null && !resume.getExperiences().isEmpty()) {
            text.append("EXPERIENCE\n");
            for (ParsedResume.Experience exp : resume.getExperiences()) {
                text.append(exp.getPosition()).append(" | ").append(exp.getCompany())
                        .append(" | ").append(exp.getDuration()).append("\n");
                if (exp.getDescription() != null) {
                    text.append(exp.getDescription()).append("\n");
                }
                text.append("\n");
            }
        }

        if (resume.getEducations() != null && !resume.getEducations().isEmpty()) {
            text.append("EDUCATION\n");
            for (ParsedResume.Education edu : resume.getEducations()) {
                text.append(edu.getDegree()).append(" - ").append(edu.getField())
                        .append(" | ").append(edu.getInstitution())
                        .append(" | ").append(edu.getYear()).append("\n");
            }
        }

        return text.toString();
    }

    private ATSOptimizationResult parseOptimizationResponse(String response) throws JsonProcessingException {
        String jsonResponse = extractJson(response);
        Map<String, Object> data = objectMapper.readValue(jsonResponse, new TypeReference<>() {});

        List<ATSOptimizationResult.Suggestion> suggestions = new ArrayList<>();
        List<Map<String, String>> suggestionsData = (List<Map<String, String>>) data.getOrDefault("suggestions", new ArrayList<>());

        for (Map<String, String> suggestionData : suggestionsData) {
            suggestions.add(ATSOptimizationResult.Suggestion.builder()
                    .category(suggestionData.getOrDefault("category", "General"))
                    .issue(suggestionData.getOrDefault("issue", ""))
                    .recommendation(suggestionData.getOrDefault("recommendation", ""))
                    .priority(suggestionData.getOrDefault("priority", "MEDIUM"))
                    .build());
        }

        return ATSOptimizationResult.builder()
                .atsScore(((Number) data.getOrDefault("atsScore", 50.0)).doubleValue())
                .suggestions(suggestions)
                .overallAssessment((String) data.getOrDefault("overallAssessment", ""))
                .build();
    }

    private String extractJson(String response) {
        response = response.trim();
        if (response.startsWith("```json")) {
            response = response.substring(7);
        } else if (response.startsWith("```")) {
            response = response.substring(3);
        }
        if (response.endsWith("```")) {
            response = response.substring(0, response.length() - 3);
        }
        return response.trim();
    }

    private Map<String, Object> calculateMetrics(ParsedResume resume) {
        Map<String, Object> metrics = new HashMap<>();

        // Calculate keyword density
        if (resume.getRawText() != null) {
            String[] words = resume.getRawText().split("\\s+");
            metrics.put("totalWords", words.length);

            Set<String> uniqueWords = new HashSet<>(Arrays.asList(words));
            metrics.put("uniqueWords", uniqueWords.size());

            double keywordDensity = resume.getSkills() != null
                    ? (double) resume.getSkills().size() / words.length
                    : 0.0;
            metrics.put("keywordDensity", keywordDensity);
        }

        // Check contact information
        boolean hasContactInfo = resume.getEmail() != null || resume.getPhone() != null;
        metrics.put("hasContactInfo", hasContactInfo);

        // Check standard sections
        boolean hasStandardSections = resume.getSkills() != null && resume.getExperiences() != null;
        metrics.put("hasStandardSections", hasStandardSections);

        return metrics;
    }

    private ATSOptimizationResult createFallbackOptimizationResult(ParsedResume resume) {
        List<ATSOptimizationResult.Suggestion> suggestions = Arrays.asList(
                ATSOptimizationResult.Suggestion.builder()
                        .category("General")
                        .issue("Unable to perform automated analysis")
                        .recommendation("Please review resume manually for ATS optimization")
                        .priority("MEDIUM")
                        .build()
        );

        return ATSOptimizationResult.builder()
                .resumeId(resume.getId())
                .atsScore(50.0)
                .suggestions(suggestions)
                .metrics(calculateMetrics(resume))
                .overallAssessment("Manual review recommended")
                .build();
    }
}
