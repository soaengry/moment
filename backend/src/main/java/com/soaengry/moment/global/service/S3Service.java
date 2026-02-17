package com.soaengry.moment.global.service;

import com.soaengry.moment.global.exception.CustomException;
import com.soaengry.moment.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class S3Service {

    private static final List<String> ALLOWED_IMAGE_TYPES = Arrays.asList(
            "image/jpeg", "image/jpg", "image/png", "image/webp"
    );
    private static final long MAX_FILE_SIZE = 10 * 1024 * 1024; // 10MB
    private final S3Client s3Client;
    @Value("${spring.cloud.aws.s3.bucket}")
    private String bucketName;
    @Value("${spring.cloud.aws.region.static}")
    private String region;

    /**
     * 프로필 이미지 업로드
     */
    public String uploadProfileImage(MultipartFile file) {
        validateImageFile(file);

        try {
            String fileName = generateFileName(file.getOriginalFilename());
            String key = "profiles/" + fileName;

            PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                    .bucket(bucketName)
                    .key(key)
                    .contentType(file.getContentType())
                    .contentLength(file.getSize())
                    .build();

            s3Client.putObject(putObjectRequest, RequestBody.fromBytes(file.getBytes()));

            String fileUrl = String.format("https://%s.s3.%s.amazonaws.com/%s",
                    bucketName, region, key);

            log.info("S3 업로드 완료 - 파일명: {}, URL: {}", fileName, fileUrl);

            return fileUrl;

        } catch (IOException e) {
            log.error("S3 업로드 실패", e);
            throw new CustomException(ErrorCode.FILE_UPLOAD_FAILED, "파일 업로드에 실패했습니다");
        }
    }

    /**
     * S3에서 파일 삭제
     */
    public void deleteFile(String fileUrl) {
        try {
            String key = extractKeyFromUrl(fileUrl);

            DeleteObjectRequest deleteObjectRequest = DeleteObjectRequest.builder()
                    .bucket(bucketName)
                    .key(key)
                    .build();

            s3Client.deleteObject(deleteObjectRequest);

            log.info("S3 파일 삭제 완료 - Key: {}", key);

        } catch (Exception e) {
            log.error("S3 파일 삭제 실패 - URL: {}", fileUrl, e);
            // 삭제 실패해도 예외를 던지지 않음
        }
    }

    /**
     * 이미지 파일 검증
     */
    private void validateImageFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new CustomException(ErrorCode.FILE_EMPTY, "파일이 비어있습니다");
        }

        // 파일 크기 검증
        if (file.getSize() > MAX_FILE_SIZE) {
            throw new CustomException(ErrorCode.FILE_SIZE_EXCEEDED, "파일 크기는 10MB를 초과할 수 없습니다");
        }

        // 파일 타입 검증
        String contentType = file.getContentType();
        if (contentType == null || !ALLOWED_IMAGE_TYPES.contains(contentType.toLowerCase())) {
            throw new CustomException(ErrorCode.FILE_UNSUPPORTED_FORMAT,
                    "지원하지 않는 파일 형식입니다. (jpg, png, webp만 가능)");
        }
    }

    /**
     * 파일명 생성 (UUID + 확장자)
     */
    private String generateFileName(String originalFilename) {
        String extension = "";
        if (originalFilename != null && originalFilename.contains(".")) {
            extension = originalFilename.substring(originalFilename.lastIndexOf("."));
        }
        return UUID.randomUUID() + extension;
    }

    /**
     * S3 URL에서 Key 추출
     */
    private String extractKeyFromUrl(String fileUrl) {
        // https://merry-moment.s3.ap-northeast-2.amazonaws.com/profiles/xxx.jpg
        // -> profiles/xxx.jpg
        String prefix = String.format("https://%s.s3.%s.amazonaws.com/", bucketName, region);
        return fileUrl.replace(prefix, "");
    }
}