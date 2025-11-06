package com.swiftbeard.ai_resume_parser.config;

import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.vectorstore.SimpleVectorStore;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class VectorStoreConfig {

    @Bean
    public VectorStore vectorStore(EmbeddingModel embeddingModel) {
        // Using SimpleVectorStore (in-memory) for simplicity
        // For production, consider using PgVectorStore or other persistent options
        return new SimpleVectorStore(embeddingModel);
    }
}
