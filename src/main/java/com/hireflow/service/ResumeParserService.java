package com.hireflow.service;

import com.hireflow.dto.response.resume.ResumeAnalysisResponse;
import lombok.extern.slf4j.Slf4j;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service
@Slf4j
public class ResumeParserService {

    private static final Set<String> ACTION_VERBS = Set.of(
        "developed", "designed", "implemented", "built", "created", "managed", "led",
        "architected", "optimized", "improved", "deployed", "maintained", "collaborated",
        "delivered", "engineered", "established", "launched", "streamlined", "automated",
        "reduced", "increased", "achieved", "solved", "analyzed", "migrated"
    );

    private static final Set<String> TECH_KEYWORDS = Set.of(
        "java", "python", "javascript", "typescript", "react", "angular", "vue", "spring",
        "springboot", "django", "nodejs", "express", "mysql", "postgresql", "mongodb",
        "redis", "kafka", "aws", "azure", "gcp", "docker", "kubernetes", "git",
        "microservices", "rest", "graphql", "hibernate", "jpa", "junit", "maven",
        "gradle", "jenkins", "ci/cd", "linux", "html", "css", "bootstrap", "tailwind",
        "flutter", "kotlin", "swift", "golang", "rust", "scala", "hadoop", "spark",
        "tensorflow", "pytorch", "machine learning", "ai", "sql", "nosql"
    );

    private static final Pattern EXPERIENCE_SECTION = Pattern.compile(
        "(?i)(experience|work experience|employment|career)", Pattern.CASE_INSENSITIVE);
    private static final Pattern EDUCATION_SECTION = Pattern.compile(
        "(?i)(education|academic|qualifications|degree)", Pattern.CASE_INSENSITIVE);
    private static final Pattern SKILLS_SECTION = Pattern.compile(
        "(?i)(skills|technical skills|core competencies|technologies)", Pattern.CASE_INSENSITIVE);
    private static final Pattern CONTACT_PATTERN = Pattern.compile(
        "(?i)(\\b[A-Z0-9._%+-]+@[A-Z0-9.-]+\\.[A-Z]{2,}\\b|\\+?[0-9]{10,13})", Pattern.CASE_INSENSITIVE);

    public ResumeAnalysisResponse parsePdf(byte[] fileBytes, Long resumeFileId) {
        try (PDDocument document = PDDocument.load(new ByteArrayInputStream(fileBytes))) {
            PDFTextStripper stripper = new PDFTextStripper();
            String text = stripper.getText(document).toLowerCase();

            List<String> extractedSkills = extractSkills(text);
            int keywordCount = extractedSkills.size();
            int actionVerbCount = countActionVerbs(text);
            boolean hasContact = CONTACT_PATTERN.matcher(text).find();
            boolean hasExperience = EXPERIENCE_SECTION.matcher(text).find();
            boolean hasEducation = EDUCATION_SECTION.matcher(text).find();
            boolean hasSkills = SKILLS_SECTION.matcher(text).find();

            int atsScore = calculateAtsScore(keywordCount, actionVerbCount, hasContact,
                hasExperience, hasEducation, hasSkills);

            Map<String, String> sectionSummary = new LinkedHashMap<>();
            sectionSummary.put("Experience Section", hasExperience ? "✅ Detected" : "❌ Missing");
            sectionSummary.put("Education Section", hasEducation ? "✅ Detected" : "❌ Missing");
            sectionSummary.put("Skills Section", hasSkills ? "✅ Detected" : "❌ Missing");
            sectionSummary.put("Contact Info", hasContact ? "✅ Detected" : "❌ Missing");

            List<String> recommendations = generateRecommendations(
                hasContact, hasExperience, hasEducation, hasSkills, actionVerbCount, keywordCount
            );

            return ResumeAnalysisResponse.builder()
                .resumeFileId(resumeFileId)
                .atsScore(atsScore)
                .extractedSkills(extractedSkills)
                .keywordCount(keywordCount)
                .actionVerbCount(actionVerbCount)
                .hasContactInfo(hasContact)
                .hasExperienceSection(hasExperience)
                .hasEducationSection(hasEducation)
                .hasSkillsSection(hasSkills)
                .sectionSummary(sectionSummary)
                .recommendations(recommendations)
                .build();

        } catch (IOException e) {
            log.error("Failed to parse PDF: {}", e.getMessage());
            throw new com.hireflow.exception.FileUploadException("Failed to parse PDF: " + e.getMessage());
        }
    }

    private List<String> extractSkills(String text) {
        return TECH_KEYWORDS.stream()
            .filter(text::contains)
            .sorted()
            .collect(Collectors.toList());
    }

    private int countActionVerbs(String text) {
        return (int) ACTION_VERBS.stream().filter(text::contains).count();
    }

    private int calculateAtsScore(int keywordCount, int actionVerbCount, boolean hasContact,
                                   boolean hasExperience, boolean hasEducation, boolean hasSkills) {
        int score = 0;

        // Keywords (30 points max, 2 per keyword up to 15)
        score += Math.min(keywordCount * 2, 30);

        // Action verbs (20 points max, 2 per verb up to 10)
        score += Math.min(actionVerbCount * 2, 20);

        // Sections (50 points)
        if (hasContact) score += 15;
        if (hasExperience) score += 15;
        if (hasEducation) score += 10;
        if (hasSkills) score += 10;

        return Math.min(score, 100);
    }

    private List<String> generateRecommendations(boolean hasContact, boolean hasExperience,
                                                   boolean hasEducation, boolean hasSkills,
                                                   int actionVerbCount, int keywordCount) {
        List<String> recommendations = new ArrayList<>();

        if (!hasContact) recommendations.add("Add contact information (email and phone number)");
        if (!hasExperience) recommendations.add("Add a clear 'Work Experience' section");
        if (!hasEducation) recommendations.add("Add an 'Education' section with your degrees");
        if (!hasSkills) recommendations.add("Add a 'Technical Skills' section");
        if (actionVerbCount < 5) recommendations.add("Use more action verbs (e.g., 'developed', 'implemented', 'optimized')");
        if (keywordCount < 10) recommendations.add("Include more technical keywords relevant to your target roles");

        if (recommendations.isEmpty()) {
            recommendations.add("Your resume looks well-structured! Focus on quantifying your achievements.");
        }

        return recommendations;
    }
}
