package com.swiftbeard.ai_resume_parser.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.swiftbeard.ai_resume_parser.dto.MatchResult;
import com.swiftbeard.ai_resume_parser.model.JobDescription;
import com.swiftbeard.ai_resume_parser.model.ParsedResume;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ResumeAnalysisService {

    private final ChatModel chatModel;
    private final ObjectMapper objectMapper = new ObjectMapper();

    private static final String MATCH_ANALYSIS_PROMPT = """
            You are an expert recruiter analyzing how well a resume matches a job description.

            Resume Summary:
            - Candidate: {candidateName}
            - Skills: {skills}
            - Experience: {experience}
            - Education: {education}

            Job Description:
            - Title: {jobTitle}
            - Required Skills: {requiredSkills}
            - Responsibilities: {responsibilities}
            - Qualifications: {qualifications}

            Analyze the match and provide:
            1. Match score (0.0 to 1.0)
            2. List of matched skills
            3. List of missing critical skills
            4. Detailed analysis (2-3 sentences)
            5. Specific recommendations for improvement

            Return the response in this JSON format:
            {
              "matchScore": 0.0,
              "matchedSkills": [],
              "missingSkills": [],
              "analysis": "",
              "recommendations": []
            }

            Return ONLY valid JSON, no additional text.
            """;

    public MatchResult analyzeMatch(ParsedResume resume, JobDescription jobDescription) {
        try {
            log.info("Analyzing match between resume {} and job {}", resume.getId(), jobDescription.getId());

            PromptTemplate promptTemplate = new PromptTemplate(MATCH_ANALYSIS_PROMPT);
            Map<String, Object> params = buildPromptParams(resume, jobDescription);

            Prompt prompt = promptTemplate.create(params);
            String response = chatModel.call(prompt).getResult().getOutput().getContent();

            log.debug("AI Match Analysis Response: {}", response);

            MatchResult matchResult = parseMatchResponse(response);
            matchResult.setResumeId(resume.getId());
            matchResult.setJobDescriptionId(jobDescription.getId());

            // Calculate category scores
            Map<String, Double> categoryScores = calculateCategoryScores(resume, jobDescription);
            matchResult.setCategoryScores(categoryScores);

            return matchResult;

        } catch (Exception e) {
            log.error("Error analyzing match: {}", e.getMessage(), e);
            return createFallbackMatchResult(resume, jobDescription);
        }
    }

    private Map<String, Object> buildPromptParams(ParsedResume resume, JobDescription jobDescription) {
        Map<String, Object> params = new HashMap<>();

        params.put("candidateName", resume.getCandidateName() != null ? resume.getCandidateName() : "Unknown");
        params.put("skills", resume.getSkills() != null ? String.join(", ", resume.getSkills()) : "None listed");

        String experience = resume.getExperiences() != null
                ? resume.getExperiences().stream()
                .map(exp -> exp.getPosition() + " at " + exp.getCompany())
                .collect(Collectors.joining("; "))
                : "No experience listed";
        params.put("experience", experience);

        String education = resume.getEducations() != null
                ? resume.getEducations().stream()
                .map(edu -> edu.getDegree() + " in " + edu.getField())
                .collect(Collectors.joining("; "))
                : "No education listed";
        params.put("education", education);

        params.put("jobTitle", jobDescription.getTitle());
        params.put("requiredSkills", jobDescription.getRequiredSkills() != null
                ? String.join(", ", jobDescription.getRequiredSkills())
                : "Not specified");
        params.put("responsibilities", jobDescription.getResponsibilities() != null
                ? String.join("; ", jobDescription.getResponsibilities())
                : "Not specified");
        params.put("qualifications", jobDescription.getQualifications() != null
                ? String.join("; ", jobDescription.getQualifications())
                : "Not specified");

        return params;
    }

    private MatchResult parseMatchResponse(String response) throws JsonProcessingException {
        String jsonResponse = extractJson(response);
        Map<String, Object> data = objectMapper.readValue(jsonResponse, new TypeReference<>() {});

        return MatchResult.builder()
                .matchScore(((Number) data.getOrDefault("matchScore", 0.0)).doubleValue())
                .matchedSkills((List<String>) data.getOrDefault("matchedSkills", new ArrayList<>()))
                .missingSkills((List<String>) data.getOrDefault("missingSkills", new ArrayList<>()))
                .analysis((String) data.getOrDefault("analysis", ""))
                .recommendations((List<String>) data.getOrDefault("recommendations", new ArrayList<>()))
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

    private Map<String, Double> calculateCategoryScores(ParsedResume resume, JobDescription jobDescription) {
        Map<String, Double> scores = new HashMap<>();

        // Skills match score
        double skillsScore = calculateSkillsMatch(resume.getSkills(), jobDescription.getRequiredSkills());
        scores.put("skills", skillsScore);

        // Experience score (simplified)
        double experienceScore = resume.getExperiences() != null && !resume.getExperiences().isEmpty() ? 0.8 : 0.4;
        scores.put("experience", experienceScore);

        // Education score (simplified)
        double educationScore = resume.getEducations() != null && !resume.getEducations().isEmpty() ? 0.8 : 0.4;
        scores.put("education", educationScore);

        return scores;
    }

    private double calculateSkillsMatch(List<String> resumeSkills, List<String> requiredSkills) {
        if (resumeSkills == null || resumeSkills.isEmpty() || requiredSkills == null || requiredSkills.isEmpty()) {
            return 0.0;
        }

        Set<String> resumeSkillsLower = resumeSkills.stream()
                .map(String::toLowerCase)
                .collect(Collectors.toSet());

        long matchedCount = requiredSkills.stream()
                .map(String::toLowerCase)
                .filter(resumeSkillsLower::contains)
                .count();

        return (double) matchedCount / requiredSkills.size();
    }

    private MatchResult createFallbackMatchResult(ParsedResume resume, JobDescription jobDescription) {
        return MatchResult.builder()
                .resumeId(resume.getId())
                .jobDescriptionId(jobDescription.getId())
                .matchScore(0.5)
                .matchedSkills(new ArrayList<>())
                .missingSkills(new ArrayList<>())
                .analysis("Unable to perform detailed analysis")
                .categoryScores(new HashMap<>())
                .recommendations(List.of("Please review the resume manually"))
                .build();
    }
}
