package com.swiftbeard.ai_resume_parser.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Data
@Configuration
@ConfigurationProperties(prefix = "app")
public class AppProperties {

    private Resume resume = new Resume();
    private Ats ats = new Ats();

    @Data
    public static class Resume {
        private String storagePath = "./data/resumes";
        private String allowedExtensions = "pdf,docx,doc";
    }

    @Data
    public static class Ats {
        private Keywords keywords = new Keywords();
    }

    @Data
    public static class Keywords {
        private double requiredDensity = 0.02;
        private int maxSuggestions = 10;
    }
}
