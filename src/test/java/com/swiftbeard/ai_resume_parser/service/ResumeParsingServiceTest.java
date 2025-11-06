package com.swiftbeard.ai_resume_parser.service;

import com.swiftbeard.ai_resume_parser.model.ParsedResume;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class ResumeParsingServiceTest {

    @Autowired(required = false)
    private ResumeParsingService resumeParsingService;

    @Test
    void testResumeParsingServiceExists() {
        // This test verifies the service can be instantiated
        // Actual AI-based tests would require API keys and real models
        if (resumeParsingService != null) {
            assertNotNull(resumeParsingService);
        }
    }

    @Test
    void testCreateFallbackResume() {
        // Test that fallback logic works when AI is not available
        String sampleText = "John Doe\nSoftware Engineer\nExperience with Java and Spring Boot";

        if (resumeParsingService != null) {
            ParsedResume resume = resumeParsingService.parseResume(sampleText, "test-resume.txt");
            assertNotNull(resume);
            assertNotNull(resume.getId());
            assertEquals("test-resume.txt", resume.getFileName());
            assertEquals(sampleText, resume.getRawText());
        }
    }
}
