package com.swiftbeard.ai_resume_parser.service;

import com.swiftbeard.ai_resume_parser.model.ParsedResume;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class VectorStoreService {

    private final VectorStore vectorStore;

    public void storeResume(ParsedResume resume) {
        log.info("Storing resume in vector store: {}", resume.getId());

        // Create a comprehensive text representation of the resume
        String resumeText = buildResumeText(resume);

        // Create metadata
        Map<String, Object> metadata = new HashMap<>();
        metadata.put("resumeId", resume.getId());
        metadata.put("fileName", resume.getFileName());
        metadata.put("candidateName", resume.getCandidateName());
        metadata.put("email", resume.getEmail());
        metadata.put("type", "resume");

        // Create document and add to vector store
        Document document = new Document(resumeText, metadata);
        vectorStore.add(List.of(document));

        log.info("Resume successfully stored in vector store");
    }

    public List<Document> searchSimilarResumes(String query, int topK) {
        log.info("Searching for similar resumes with query: {}", query);

        SearchRequest searchRequest = SearchRequest.query(query)
                .withTopK(topK)
                .withSimilarityThreshold(0.7);

        return vectorStore.similaritySearch(searchRequest);
    }

    public List<Document> findMatchingResumes(String jobDescription, int topK) {
        log.info("Finding resumes matching job description");

        SearchRequest searchRequest = SearchRequest.query(jobDescription)
                .withTopK(topK)
                .withSimilarityThreshold(0.6);

        return vectorStore.similaritySearch(searchRequest);
    }

    private String buildResumeText(ParsedResume resume) {
        StringBuilder text = new StringBuilder();

        text.append("Candidate: ").append(resume.getCandidateName()).append("\n\n");

        if (resume.getSummary() != null) {
            text.append("Summary: ").append(resume.getSummary()).append("\n\n");
        }

        if (resume.getSkills() != null && !resume.getSkills().isEmpty()) {
            text.append("Skills: ").append(String.join(", ", resume.getSkills())).append("\n\n");
        }

        if (resume.getExperiences() != null && !resume.getExperiences().isEmpty()) {
            text.append("Experience:\n");
            for (ParsedResume.Experience exp : resume.getExperiences()) {
                text.append("- ").append(exp.getPosition())
                        .append(" at ").append(exp.getCompany())
                        .append(" (").append(exp.getDuration()).append(")\n");
                if (exp.getDescription() != null) {
                    text.append("  ").append(exp.getDescription()).append("\n");
                }
            }
            text.append("\n");
        }

        if (resume.getEducations() != null && !resume.getEducations().isEmpty()) {
            text.append("Education:\n");
            for (ParsedResume.Education edu : resume.getEducations()) {
                text.append("- ").append(edu.getDegree())
                        .append(" in ").append(edu.getField())
                        .append(" from ").append(edu.getInstitution())
                        .append(" (").append(edu.getYear()).append(")\n");
            }
            text.append("\n");
        }

        if (resume.getCertifications() != null && !resume.getCertifications().isEmpty()) {
            text.append("Certifications: ").append(String.join(", ", resume.getCertifications()));
        }

        return text.toString();
    }
}
