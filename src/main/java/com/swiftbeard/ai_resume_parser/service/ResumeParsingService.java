package com.swiftbeard.ai_resume_parser.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.swiftbeard.ai_resume_parser.model.ParsedResume;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class ResumeParsingService {

    private final ChatModel chatModel;
    private final ObjectMapper objectMapper = new ObjectMapper();

    private static final String RESUME_PARSING_PROMPT = """
            You are an expert resume parser. Extract structured information from the following resume text.

            Resume Text:
            {resumeText}

            Extract and return the following information in JSON format:
            - candidateName: Full name of the candidate
            - email: Email address
            - phone: Phone number
            - summary: Professional summary or objective (if present)
            - skills: Array of technical and soft skills
            - experiences: Array of work experiences with company, position, duration, description, and achievements
            - educations: Array of education entries with institution, degree, field, and year
            - certifications: Array of certifications

            Return ONLY valid JSON, no additional text.
            """;

    public ParsedResume parseResume(String resumeText, String fileName) {
        try {
            log.info("Parsing resume: {}", fileName);

            PromptTemplate promptTemplate = new PromptTemplate(RESUME_PARSING_PROMPT);
            Map<String, Object> params = new HashMap<>();
            params.put("resumeText", resumeText);

            Prompt prompt = promptTemplate.create(params);
            String response = chatModel.call(prompt).getResult().getOutput().getContent();

            log.debug("AI Response: {}", response);

            // Parse the JSON response
            ParsedResume parsedResume = parseAIResponse(response);
            parsedResume.setId(UUID.randomUUID().toString());
            parsedResume.setFileName(fileName);
            parsedResume.setRawText(resumeText);
            parsedResume.setParsedAt(LocalDateTime.now());

            return parsedResume;

        } catch (Exception e) {
            log.error("Error parsing resume: {}", e.getMessage(), e);
            // Return a basic parsed resume with raw text
            return createFallbackResume(resumeText, fileName);
        }
    }

    private ParsedResume parseAIResponse(String response) throws JsonProcessingException {
        // Clean up the response to extract JSON
        String jsonResponse = extractJson(response);
        return objectMapper.readValue(jsonResponse, ParsedResume.class);
    }

    private String extractJson(String response) {
        // Remove markdown code blocks if present
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

    private ParsedResume createFallbackResume(String resumeText, String fileName) {
        return ParsedResume.builder()
                .id(UUID.randomUUID().toString())
                .fileName(fileName)
                .rawText(resumeText)
                .parsedAt(LocalDateTime.now())
                .skills(new ArrayList<>())
                .experiences(new ArrayList<>())
                .educations(new ArrayList<>())
                .certifications(new ArrayList<>())
                .metadata(new HashMap<>())
                .build();
    }

    public List<String> extractKeywords(String text) {
        // Extract important keywords using AI
        String prompt = String.format("""
                Extract the most important keywords and technical terms from this text.
                Return only a comma-separated list of keywords, no additional text.

                Text: %s
                """, text);

        try {
            String response = chatModel.call(prompt);
            return Arrays.asList(response.split(",\\s*"));
        } catch (Exception e) {
            log.error("Error extracting keywords: {}", e.getMessage());
            return new ArrayList<>();
        }
    }
}
