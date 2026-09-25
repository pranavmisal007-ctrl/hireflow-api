package com.hireflow.service;

import com.hireflow.exception.FileUploadException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.*;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;

import java.io.IOException;
import java.time.Duration;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class S3FileService {

    private final S3Client s3Client;
    private final S3Presigner s3Presigner;

    @Value("${aws.s3.bucket-name:hireflow-bucket}")
    private String bucketName;

    @Value("${aws.s3.endpoint:#{null}}")
    private String endpoint;

    private static final long MAX_FILE_SIZE_BYTES = 5 * 1024 * 1024; // 5MB
    private static final String ALLOWED_CONTENT_TYPE = "application/pdf";

    public String uploadFile(MultipartFile file, String folder, Long userId) {
        validateFile(file);

        String originalFilename = file.getOriginalFilename();
        String extension = originalFilename != null && originalFilename.contains(".")
            ? originalFilename.substring(originalFilename.lastIndexOf('.'))
            : ".pdf";

        String s3Key = String.format("%s/%d/%s%s", folder, userId, UUID.randomUUID(), extension);

        try {
            PutObjectRequest putRequest = PutObjectRequest.builder()
                .bucket(bucketName)
                .key(s3Key)
                .contentType(file.getContentType())
                .contentLength(file.getSize())
                .build();

            s3Client.putObject(putRequest, RequestBody.fromBytes(file.getBytes()));
            log.info("File uploaded to S3: {}", s3Key);

            return buildFileUrl(s3Key);
        } catch (IOException e) {
            throw new FileUploadException("Failed to read file: " + e.getMessage());
        } catch (S3Exception e) {
            throw new FileUploadException("Failed to upload file to S3: " + e.getMessage());
        }
    }

    public void deleteFile(String s3Key) {
        try {
            DeleteObjectRequest deleteRequest = DeleteObjectRequest.builder()
                .bucket(bucketName)
                .key(s3Key)
                .build();
            s3Client.deleteObject(deleteRequest);
            log.info("File deleted from S3: {}", s3Key);
        } catch (S3Exception e) {
            log.error("Failed to delete file from S3: {}", e.getMessage());
        }
    }

    public String generatePresignedUrl(String s3Key) {
        GetObjectPresignRequest presignRequest = GetObjectPresignRequest.builder()
            .signatureDuration(Duration.ofHours(1))
            .getObjectRequest(r -> r.bucket(bucketName).key(s3Key))
            .build();

        return s3Presigner.presignGetObject(presignRequest).url().toString();
    }

    private void validateFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new FileUploadException("File is empty or null");
        }
        if (file.getSize() > MAX_FILE_SIZE_BYTES) {
            throw new FileUploadException("File size exceeds 5MB limit");
        }
        String contentType = file.getContentType();
        if (contentType == null || !contentType.equalsIgnoreCase(ALLOWED_CONTENT_TYPE)) {
            throw new FileUploadException("Only PDF files are allowed");
        }
    }

    private String buildFileUrl(String s3Key) {
        if (endpoint != null && !endpoint.isBlank()) {
            return endpoint + "/" + bucketName + "/" + s3Key;
        }
        return "https://" + bucketName + ".s3.amazonaws.com/" + s3Key;
    }
}
