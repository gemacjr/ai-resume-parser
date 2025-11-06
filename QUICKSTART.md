# Quick Start Guide

Get up and running with AI Resume Parser in 5 minutes!

## Prerequisites

- Java 17 or higher installed
- Maven installed
- OpenAI API key (get one at https://platform.openai.com/api-keys)

## Step 1: Get Your OpenAI API Key

1. Go to https://platform.openai.com/api-keys
2. Create a new API key
3. Copy the key (you won't see it again!)

## Step 2: Set Environment Variable

### On Linux/Mac:
```bash
export OPENAI_API_KEY=sk-your-api-key-here
```

### On Windows:
```cmd
set OPENAI_API_KEY=sk-your-api-key-here
```

## Step 3: Build and Run

```bash
# Build the project
./mvnw clean install

# Run the application
./mvnw spring-boot:run
```

Wait for the message: `Started AiResumeParserApplication`

## Step 4: Test the API

### Test Health Check
```bash
curl http://localhost:8080/api/health
```

Expected response:
```json
{"status":"UP","service":"AI Resume Parser","version":"1.0.0"}
```

## Step 5: Upload Your First Resume

### Option 1: Using cURL

```bash
curl -X POST http://localhost:8080/api/resumes/upload \
  -F "file=@/path/to/your/resume.pdf"
```

### Option 2: Using Postman

1. Import `postman-collection.json`
2. Use the "Upload Resume" request
3. Select your resume file
4. Click Send

### Sample Resume

A sample resume is provided at:
`src/test/resources/sample-resumes/john-doe-resume.txt`

You can convert it to PDF or use your own resume!

## Step 6: Try the Features

### Get the Resume ID
From the upload response, copy the `id` field.

### Match with a Job

```bash
curl -X POST http://localhost:8080/api/resumes/YOUR_RESUME_ID/match \
  -H "Content-Type: application/json" \
  -d @src/test/resources/sample-jobs/senior-java-developer.json
```

### Get ATS Optimization Suggestions

```bash
curl -X POST http://localhost:8080/api/resumes/YOUR_RESUME_ID/optimize-ats
```

## Common Issues

### "OpenAI API key not configured"
Make sure you set the `OPENAI_API_KEY` environment variable before running the app.

### "Unsupported file format"
Only PDF and DOCX files are supported. Make sure your file has the correct extension.

### Port 8080 already in use
Change the port in `application.yaml`:
```yaml
server:
  port: 8081
```

## Next Steps

- Read the full [README.md](README.md) for detailed API documentation
- Explore the [sample data](src/test/resources/)
- Check out the code in `src/main/java/com/swiftbeard/ai_resume_parser/`

## API Quick Reference

| Endpoint | Method | Description |
|----------|--------|-------------|
| `/api/health` | GET | Health check |
| `/api/resumes/upload` | POST | Upload and parse resume |
| `/api/resumes` | GET | Get all resumes |
| `/api/resumes/{id}` | GET | Get specific resume |
| `/api/resumes/{id}/match` | POST | Match with job |
| `/api/resumes/{id}/optimize-ats` | POST | Get ATS tips |
| `/api/resumes/search` | GET | Search resumes |
| `/api/resumes/find-candidates` | POST | Find candidates |

## Support

Need help? Check out:
- [Full Documentation](README.md)
- [Sample Data](src/test/resources/)
- Open an issue on GitHub

Happy parsing!
