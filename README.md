# AI Resume Parser & Optimizer

A comprehensive Spring AI application that intelligently parses resumes, matches them with job descriptions, and provides ATS (Applicant Tracking System) optimization suggestions.

## Features

### 1. Intelligent Resume Parsing
- Extracts structured information from PDF and DOCX resume files
- Uses AI to identify candidate details, skills, experience, education, and certifications
- Supports multiple document formats

### 2. Vector-Based Resume Storage
- Stores resume embeddings in a vector database for semantic search
- Enables similarity-based resume searches
- Fast and efficient candidate matching

### 3. Job Matching
- Analyzes how well a resume matches a job description
- Provides detailed match scores and skill gap analysis
- Offers specific recommendations for improvement

### 4. ATS Optimization
- Evaluates resume ATS-friendliness (0-100 score)
- Identifies formatting and keyword optimization issues
- Provides actionable suggestions with priority levels

## Technologies Used

- **Spring Boot 3.5.7** - Application framework
- **Spring AI 1.0.0-M4** - AI integration framework
- **OpenAI API** - LLM for parsing and analysis
- **Apache PDFBox** - PDF parsing
- **Apache POI** - DOCX parsing
- **Apache Tika** - Document reading
- **Vector Store** - Semantic search and embeddings
- **Lombok** - Code generation
- **Maven** - Build tool

## Prerequisites

- Java 17 or higher
- Maven 3.6+
- OpenAI API key

## Setup

### 1. Clone the repository

```bash
git clone <repository-url>
cd ai-resume-parser
```

### 2. Configure OpenAI API Key

Set your OpenAI API key as an environment variable:

```bash
export OPENAI_API_KEY=your-api-key-here
```

Or edit `src/main/resources/application.yaml`:

```yaml
spring:
  ai:
    openai:
      api-key: your-api-key-here
```

### 3. Build the application

```bash
./mvnw clean install
```

### 4. Run the application

```bash
./mvnw spring-boot:run
```

The application will start on `http://localhost:8080`

## API Endpoints

### Health Check

```http
GET /api/health
```

Response:
```json
{
  "status": "UP",
  "service": "AI Resume Parser",
  "version": "1.0.0"
}
```

### Upload and Parse Resume

```http
POST /api/resumes/upload
Content-Type: multipart/form-data
```

Parameters:
- `file` - Resume file (PDF or DOCX)

Example with cURL:
```bash
curl -X POST http://localhost:8080/api/resumes/upload \
  -F "file=@/path/to/resume.pdf"
```

Response:
```json
{
  "id": "uuid",
  "fileName": "resume.pdf",
  "candidateName": "John Doe",
  "email": "john.doe@email.com",
  "phone": "(555) 123-4567",
  "summary": "Experienced software engineer...",
  "skills": ["Java", "Spring Boot", "AWS"],
  "experiences": [...],
  "educations": [...],
  "certifications": [...],
  "parsedAt": "2025-11-06T10:00:00"
}
```

### Get Resume by ID

```http
GET /api/resumes/{resumeId}
```

### Get All Resumes

```http
GET /api/resumes
```

### Match Resume with Job

```http
POST /api/resumes/{resumeId}/match
Content-Type: application/json
```

Request Body:
```json
{
  "id": "job-001",
  "title": "Senior Java Developer",
  "company": "Tech Company",
  "description": "Job description...",
  "requiredSkills": ["Java", "Spring Boot", "AWS"],
  "preferredSkills": ["Kubernetes", "React"],
  "experienceLevel": "Senior (5+ years)",
  "responsibilities": [...],
  "qualifications": [...]
}
```

Response:
```json
{
  "resumeId": "uuid",
  "jobDescriptionId": "job-001",
  "matchScore": 0.85,
  "matchedSkills": ["Java", "Spring Boot", "AWS"],
  "missingSkills": ["Kubernetes"],
  "analysis": "Strong match with excellent technical background...",
  "categoryScores": {
    "skills": 0.9,
    "experience": 0.85,
    "education": 0.8
  },
  "recommendations": [
    "Add Kubernetes experience to strengthen profile",
    "Highlight cloud architecture projects"
  ]
}
```

### Optimize Resume for ATS

```http
POST /api/resumes/{resumeId}/optimize-ats
```

Response:
```json
{
  "resumeId": "uuid",
  "atsScore": 78.5,
  "suggestions": [
    {
      "category": "Keywords",
      "issue": "Low keyword density for required skills",
      "recommendation": "Increase mentions of Java and Spring Boot",
      "priority": "HIGH"
    },
    {
      "category": "Formatting",
      "issue": "Missing standard section headers",
      "recommendation": "Use clear headers like 'EXPERIENCE' and 'EDUCATION'",
      "priority": "MEDIUM"
    }
  ],
  "metrics": {
    "keywordDensity": 0.025,
    "totalWords": 450,
    "uniqueWords": 180,
    "hasContactInfo": true,
    "hasStandardSections": true
  },
  "overallAssessment": "Good foundation with room for keyword optimization"
}
```

### Search Similar Resumes

```http
GET /api/resumes/search?query=Java Spring Boot developer&topK=5
```

Parameters:
- `query` - Search query string
- `topK` - Number of results to return (default: 5)

### Find Candidates for Job

```http
POST /api/resumes/find-candidates
Content-Type: application/json
```

Request Body: Job description object (same as match endpoint)

## Project Structure

```
ai-resume-parser/
├── src/
│   ├── main/
│   │   ├── java/com/swiftbeard/ai_resume_parser/
│   │   │   ├── config/           # Configuration classes
│   │   │   │   ├── AppProperties.java
│   │   │   │   └── VectorStoreConfig.java
│   │   │   ├── controller/       # REST controllers
│   │   │   │   ├── HealthController.java
│   │   │   │   └── ResumeController.java
│   │   │   ├── dto/             # Data transfer objects
│   │   │   │   ├── ATSOptimizationResult.java
│   │   │   │   └── MatchResult.java
│   │   │   ├── model/           # Domain models
│   │   │   │   ├── JobDescription.java
│   │   │   │   └── ParsedResume.java
│   │   │   ├── service/         # Business logic
│   │   │   │   ├── ATSOptimizationService.java
│   │   │   │   ├── DocumentParsingService.java
│   │   │   │   ├── ResumeAnalysisService.java
│   │   │   │   ├── ResumeParsingService.java
│   │   │   │   └── VectorStoreService.java
│   │   │   └── AiResumeParserApplication.java
│   │   └── resources/
│   │       └── application.yaml
│   └── test/
│       ├── java/                # Test classes
│       └── resources/           # Sample data
│           ├── sample-resumes/
│           └── sample-jobs/
├── pom.xml
└── README.md
```

## Key Components

### DocumentParsingService
Handles extraction of text from PDF and DOCX files using Apache PDFBox and POI.

### ResumeParsingService
Uses OpenAI's GPT model to parse unstructured resume text into structured data with fields like name, skills, experience, etc.

### VectorStoreService
Manages resume embeddings in a vector database for semantic search and similarity matching.

### ResumeAnalysisService
Analyzes how well a resume matches a job description, providing match scores and recommendations.

### ATSOptimizationService
Evaluates resumes for ATS compatibility and provides optimization suggestions.

## Configuration Options

### application.yaml

```yaml
spring:
  ai:
    openai:
      api-key: ${OPENAI_API_KEY}
      chat:
        options:
          model: gpt-4o-mini          # Chat model
          temperature: 0.7              # Response creativity
      embedding:
        options:
          model: text-embedding-3-small # Embedding model

  servlet:
    multipart:
      max-file-size: 10MB              # Max upload size

vector:
  store:
    type: simple                       # simple or pgvector

app:
  resume:
    storage-path: ./data/resumes
    allowed-extensions: pdf,docx,doc
  ats:
    keywords:
      required-density: 0.02
      max-suggestions: 10
```

## Advanced Features

### Switching to PostgreSQL Vector Store

For production use, configure PostgreSQL with pgvector extension:

1. Install PostgreSQL with pgvector
2. Update `application.yaml`:

```yaml
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/resume_db
    username: your_username
    password: your_password

vector:
  store:
    type: pgvector
```

3. Update `VectorStoreConfig.java` to use `PgVectorStore`

## Testing

Run tests:

```bash
./mvnw test
```

### Sample Test Data

Sample resumes and job descriptions are available in:
- `src/test/resources/sample-resumes/`
- `src/test/resources/sample-jobs/`

## Use Cases

1. **Recruitment Platforms**: Automatically parse and match candidates with job openings
2. **Job Seekers**: Get ATS optimization tips to improve resume visibility
3. **HR Departments**: Quickly screen large volumes of resumes
4. **Career Coaches**: Provide data-driven resume improvement suggestions
5. **Resume Banks**: Build searchable databases with semantic search

## Future Enhancements

- [ ] Support for more document formats (RTF, HTML)
- [ ] Resume scoring and ranking algorithms
- [ ] Interview question generation based on resume
- [ ] Cover letter generation
- [ ] Batch processing for multiple resumes
- [ ] Integration with LinkedIn API
- [ ] Export optimized resumes in ATS-friendly formats
- [ ] Multi-language support
- [ ] Custom AI model fine-tuning

## Performance Considerations

- **Vector Store**: For high-volume applications, use PgVectorStore instead of SimpleVectorStore
- **Caching**: Implement Redis caching for frequently accessed resumes
- **Async Processing**: Use Spring's async capabilities for large batch operations
- **Rate Limiting**: Consider API rate limits when using OpenAI

## Troubleshooting

### Issue: "OpenAI API key not configured"
**Solution**: Set the `OPENAI_API_KEY` environment variable

### Issue: "Unsupported file format"
**Solution**: Ensure file is PDF or DOCX format

### Issue: "Vector store initialization failed"
**Solution**: Check that embedding model is properly configured

## Contributing

Contributions are welcome! Please:
1. Fork the repository
2. Create a feature branch
3. Make your changes
4. Submit a pull request

## License

This project is licensed under the MIT License.

## Contact

For questions or support, please open an issue on GitHub.

## Acknowledgments

- Spring AI Team for the excellent framework
- OpenAI for powerful language models
- Apache Foundation for document processing libraries

---

Built with Spring AI and OpenAI
