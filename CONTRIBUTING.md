# Contributing to AI Resume Parser

Thank you for considering contributing to the AI Resume Parser project! This document provides guidelines and instructions for contributing.

## Code of Conduct

- Be respectful and inclusive
- Focus on constructive feedback
- Help make this project better for everyone

## How to Contribute

### Reporting Bugs

If you find a bug, please open an issue with:
- Clear description of the problem
- Steps to reproduce
- Expected vs actual behavior
- Environment details (Java version, OS, etc.)
- Error logs if applicable

### Suggesting Enhancements

Enhancement suggestions are welcome! Please include:
- Clear description of the feature
- Use cases and benefits
- Possible implementation approach (optional)

### Pull Requests

1. Fork the repository
2. Create a feature branch: `git checkout -b feature/your-feature-name`
3. Make your changes
4. Add tests if applicable
5. Ensure all tests pass: `./mvnw test`
6. Commit with clear messages: `git commit -m "Add feature: your feature"`
7. Push to your fork: `git push origin feature/your-feature-name`
8. Open a Pull Request

## Development Setup

1. Clone the repository
2. Install Java 17+ and Maven
3. Set up your OpenAI API key
4. Run `./mvnw clean install`
5. Run tests: `./mvnw test`
6. Start the app: `./mvnw spring-boot:run`

## Code Style

- Follow Java naming conventions
- Use meaningful variable and method names
- Add JavaDoc comments for public methods
- Keep methods focused and concise
- Use Lombok annotations to reduce boilerplate

## Testing

- Write unit tests for new features
- Ensure existing tests still pass
- Aim for good test coverage
- Mock external dependencies (AI APIs)

## Commit Messages

Use clear, descriptive commit messages:

```
Add feature: resume batch processing
Fix: PDF parsing error for multi-column layouts
Update: improve ATS scoring algorithm
Docs: add API examples for job matching
```

## Areas for Contribution

We welcome contributions in these areas:

### High Priority
- Additional document format support (RTF, HTML)
- Improved error handling and validation
- Performance optimizations
- Better test coverage

### Medium Priority
- UI/Frontend (React, Vue, etc.)
- Batch processing capabilities
- Resume templates and generation
- Multi-language support

### Nice to Have
- Integration with job boards
- LinkedIn API integration
- Custom AI model fine-tuning
- Advanced analytics dashboard

## Questions?

Feel free to:
- Open an issue for questions
- Start a discussion
- Reach out to maintainers

## Recognition

Contributors will be recognized in:
- README.md contributors section
- Release notes
- Project documentation

Thank you for your contributions!
