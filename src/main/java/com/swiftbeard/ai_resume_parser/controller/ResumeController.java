package com.swiftbeard.ai_resume_parser.controller;

import com.swiftbeard.ai_resume_parser.dto.ATSOptimizationResult;
import com.swiftbeard.ai_resume_parser.dto.MatchResult;
import com.swiftbeard.ai_resume_parser.model.JobDescription;
import com.swiftbeard.ai_resume_parser.model.ParsedResume;
import com.swiftbeard.ai_resume_parser.service.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.document.Document;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/resumes")
@RequiredArgsConstructor
public class ResumeController {

    private final DocumentParsingService documentParsingService;
    private final ResumeParsingService resumeParsingService;
    private final VectorStoreService vectorStoreService;
    private final ResumeAnalysisService resumeAnalysisService;
    private final ATSOptimizationService atsOptimizationService;

    private final Map<String, ParsedResume> resumeStore = new HashMap<>();

    @PostMapping("/upload")
    public ResponseEntity<?> uploadAndParseResume(@RequestParam("file") MultipartFile file) {
        try {
            log.info("Received resume upload: {}", file.getOriginalFilename());

            // Validate file
            if (!documentParsingService.isValidFileType(file.getOriginalFilename())) {
                return ResponseEntity.badRequest()
                        .body(Map.of("error", "Invalid file type. Supported: PDF, DOCX, DOC"));
            }

            // Extract text from document
            String extractedText = documentParsingService.extractTextFromFile(file);
            log.debug("Extracted {} characters from document", extractedText.length());

            // Parse resume using AI
            ParsedResume parsedResume = resumeParsingService.parseResume(extractedText, file.getOriginalFilename());

            // Store in memory (in production, use a database)
            resumeStore.put(parsedResume.getId(), parsedResume);

            // Store in vector database
            vectorStoreService.storeResume(parsedResume);

            log.info("Successfully parsed resume: {}", parsedResume.getId());

            return ResponseEntity.ok(parsedResume);

        } catch (Exception e) {
            log.error("Error processing resume upload: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to process resume: " + e.getMessage()));
        }
    }

    @GetMapping("/{resumeId}")
    public ResponseEntity<?> getResume(@PathVariable String resumeId) {
        ParsedResume resume = resumeStore.get(resumeId);
        if (resume == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(resume);
    }

    @GetMapping
    public ResponseEntity<List<ParsedResume>> getAllResumes() {
        return ResponseEntity.ok(resumeStore.values().stream().toList());
    }

    @PostMapping("/{resumeId}/match")
    public ResponseEntity<?> matchWithJob(
            @PathVariable String resumeId,
            @RequestBody JobDescription jobDescription) {
        try {
            ParsedResume resume = resumeStore.get(resumeId);
            if (resume == null) {
                return ResponseEntity.notFound().build();
            }

            log.info("Matching resume {} with job {}", resumeId, jobDescription.getTitle());

            MatchResult matchResult = resumeAnalysisService.analyzeMatch(resume, jobDescription);

            return ResponseEntity.ok(matchResult);

        } catch (Exception e) {
            log.error("Error matching resume with job: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to analyze match: " + e.getMessage()));
        }
    }

    @PostMapping("/{resumeId}/optimize-ats")
    public ResponseEntity<?> optimizeForATS(@PathVariable String resumeId) {
        try {
            ParsedResume resume = resumeStore.get(resumeId);
            if (resume == null) {
                return ResponseEntity.notFound().build();
            }

            log.info("Optimizing resume {} for ATS", resumeId);

            ATSOptimizationResult optimizationResult = atsOptimizationService.optimizeForATS(resume);

            return ResponseEntity.ok(optimizationResult);

        } catch (Exception e) {
            log.error("Error optimizing resume for ATS: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to optimize resume: " + e.getMessage()));
        }
    }

    @GetMapping("/search")
    public ResponseEntity<?> searchSimilarResumes(
            @RequestParam String query,
            @RequestParam(defaultValue = "5") int topK) {
        try {
            log.info("Searching for resumes similar to: {}", query);

            List<Document> similarDocuments = vectorStoreService.searchSimilarResumes(query, topK);

            List<Map<String, Object>> results = similarDocuments.stream()
                    .map(doc -> {
                        Map<String, Object> result = new HashMap<>();
                        result.put("content", doc.getContent());
                        result.put("metadata", doc.getMetadata());
                        return result;
                    })
                    .toList();

            return ResponseEntity.ok(results);

        } catch (Exception e) {
            log.error("Error searching resumes: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to search resumes: " + e.getMessage()));
        }
    }

    @PostMapping("/find-candidates")
    public ResponseEntity<?> findCandidatesForJob(@RequestBody JobDescription jobDescription) {
        try {
            log.info("Finding candidates for job: {}", jobDescription.getTitle());

            String jobQuery = buildJobQuery(jobDescription);
            List<Document> matchingResumes = vectorStoreService.findMatchingResumes(jobQuery, 10);

            List<Map<String, Object>> results = matchingResumes.stream()
                    .map(doc -> {
                        Map<String, Object> result = new HashMap<>();
                        result.put("resumeId", doc.getMetadata().get("resumeId"));
                        result.put("candidateName", doc.getMetadata().get("candidateName"));
                        result.put("relevanceScore", doc.getMetadata().get("distance"));
                        return result;
                    })
                    .toList();

            return ResponseEntity.ok(results);

        } catch (Exception e) {
            log.error("Error finding candidates: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to find candidates: " + e.getMessage()));
        }
    }

    private String buildJobQuery(JobDescription jobDescription) {
        StringBuilder query = new StringBuilder();
        query.append(jobDescription.getTitle()).append(" ");

        if (jobDescription.getRequiredSkills() != null) {
            query.append(String.join(" ", jobDescription.getRequiredSkills())).append(" ");
        }

        if (jobDescription.getDescription() != null) {
            query.append(jobDescription.getDescription());
        }

        return query.toString();
    }
}
