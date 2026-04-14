package com.soaengry.moment.global.service;

import com.soaengry.moment.global.exception.ErrorCode;
import com.soaengry.moment.global.exception.FileException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ActiveProfiles;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.*;

@SpringBootTest
@ActiveProfiles("test")
class S3ServiceTest {

    @MockBean
    private S3Client s3Client;

    @Autowired
    private S3Service s3Service;

    private MockMultipartFile validImageFile;

    @BeforeEach
    void setUp() {
        validImageFile = new MockMultipartFile(
                "file",
                "test.jpg",
                "image/jpeg",
                "test image content".getBytes()
        );

        // S3Client mocking - putObject 항상 성공
        when(s3Client.putObject(any(PutObjectRequest.class), any(RequestBody.class)))
                .thenReturn(null);

        // S3Client mocking - deleteObject 항상 성공
        when(s3Client.deleteObject(any(DeleteObjectRequest.class)))
                .thenReturn(null);
    }

    @Test
    @DisplayName("프로필 이미지 업로드 성공 (profiles/)")
    void uploadProfileImage_Success() {
        // when
        String result = s3Service.uploadProfileImage(validImageFile);

        // then
        assertThat(result).isNotNull();
        assertThat(result).contains("profiles/");
        assertThat(result).contains(".jpg");
        assertThat(result).startsWith("https://");

        verify(s3Client, times(1)).putObject(any(PutObjectRequest.class), any(RequestBody.class));

        System.out.println("✅ 프로필 이미지 업로드 성공");
        System.out.println("   - URL: " + result);
    }

    @Test
    @DisplayName("웨딩 이미지 업로드 성공 (weddings/)")
    void uploadWeddingImage_Success() {
        // when
        String result = s3Service.uploadWeddingImage(validImageFile);

        // then
        assertThat(result).isNotNull();
        assertThat(result).contains("weddings/");
        assertThat(result).contains(".jpg");

        verify(s3Client, times(1)).putObject(any(PutObjectRequest.class), any(RequestBody.class));

        System.out.println("✅ 웨딩 이미지 업로드 성공");
        System.out.println("   - URL: " + result);
    }

    @Test
    @DisplayName("채팅 이미지 업로드 성공 (chat/)")
    void uploadChatImage_Success() {
        // when
        String result = s3Service.uploadChatImage(validImageFile);

        // then
        assertThat(result).isNotNull();
        assertThat(result).contains("chat/");
        assertThat(result).contains(".jpg");

        verify(s3Client, times(1)).putObject(any(PutObjectRequest.class), any(RequestBody.class));

        System.out.println("✅ 채팅 이미지 업로드 성공");
        System.out.println("   - URL: " + result);
    }

    @Test
    @DisplayName("null 파일 - FILE_EMPTY")
    void uploadProfileImage_NullFile_Fail() {
        // when & then
        assertThatThrownBy(() -> s3Service.uploadProfileImage(null))
                .isInstanceOf(FileException.class)
                .hasMessage(ErrorCode.FILE_EMPTY.getMessage());

        verify(s3Client, never()).putObject(any(PutObjectRequest.class), any(RequestBody.class));

        System.out.println("✅ null 파일 테스트 통과");
    }

    @Test
    @DisplayName("빈 파일 - FILE_EMPTY")
    void uploadProfileImage_EmptyFile_Fail() {
        // given
        MockMultipartFile emptyFile = new MockMultipartFile(
                "file",
                "test.jpg",
                "image/jpeg",
                new byte[0]
        );

        // when & then
        assertThatThrownBy(() -> s3Service.uploadProfileImage(emptyFile))
                .isInstanceOf(FileException.class)
                .hasMessage(ErrorCode.FILE_EMPTY.getMessage());

        verify(s3Client, never()).putObject(any(PutObjectRequest.class), any(RequestBody.class));

        System.out.println("✅ 빈 파일 테스트 통과");
    }

    @Test
    @DisplayName("파일 크기 초과 (> 10MB) - FILE_SIZE_EXCEEDED")
    void uploadProfileImage_FileSizeExceeded_Fail() {
        // given
        byte[] largeContent = new byte[11 * 1024 * 1024]; // 11MB
        MockMultipartFile largeFile = new MockMultipartFile(
                "file",
                "large.jpg",
                "image/jpeg",
                largeContent
        );

        // when & then
        assertThatThrownBy(() -> s3Service.uploadProfileImage(largeFile))
                .isInstanceOf(FileException.class)
                .hasMessage(ErrorCode.FILE_SIZE_EXCEEDED.getMessage());

        verify(s3Client, never()).putObject(any(PutObjectRequest.class), any(RequestBody.class));

        System.out.println("✅ 파일 크기 초과 테스트 통과");
        System.out.println("   - 파일 크기: 11MB > 10MB (한계)");
    }

    @Test
    @DisplayName("지원하지 않는 형식 (PDF) - FILE_UNSUPPORTED_FORMAT")
    void uploadProfileImage_UnsupportedFormat_Fail() {
        // given
        MockMultipartFile pdfFile = new MockMultipartFile(
                "file",
                "document.pdf",
                "application/pdf",
                "pdf content".getBytes()
        );

        // when & then
        assertThatThrownBy(() -> s3Service.uploadProfileImage(pdfFile))
                .isInstanceOf(FileException.class)
                .hasMessage("지원하지 않는 파일 형식입니다. (jpg, png, webp만 가능)");

        verify(s3Client, never()).putObject(any(PutObjectRequest.class), any(RequestBody.class));

        System.out.println("✅ 지원하지 않는 형식 테스트 통과 (PDF)");
    }

    @Test
    @DisplayName("JPEG 형식 검증 통과")
    void uploadProfileImage_JpegFormat_Success() {
        // given
        MockMultipartFile jpegFile = new MockMultipartFile(
                "file",
                "test.jpeg",
                "image/jpeg",
                "jpeg content".getBytes()
        );

        // when
        String result = s3Service.uploadProfileImage(jpegFile);

        // then
        assertThat(result).isNotNull();
        assertThat(result).contains(".jpeg");

        System.out.println("✅ JPEG 형식 검증 통과");
    }

    @Test
    @DisplayName("PNG 형식 검증 통과")
    void uploadProfileImage_PngFormat_Success() {
        // given
        MockMultipartFile pngFile = new MockMultipartFile(
                "file",
                "test.png",
                "image/png",
                "png content".getBytes()
        );

        // when
        String result = s3Service.uploadProfileImage(pngFile);

        // then
        assertThat(result).isNotNull();
        assertThat(result).contains(".png");

        System.out.println("✅ PNG 형식 검증 통과");
    }

    @Test
    @DisplayName("WEBP 형식 검증 통과")
    void uploadProfileImage_WebpFormat_Success() {
        // given
        MockMultipartFile webpFile = new MockMultipartFile(
                "file",
                "test.webp",
                "image/webp",
                "webp content".getBytes()
        );

        // when
        String result = s3Service.uploadProfileImage(webpFile);

        // then
        assertThat(result).isNotNull();
        assertThat(result).contains(".webp");

        System.out.println("✅ WEBP 형식 검증 통과");
    }

    @Test
    @DisplayName("S3 업로드 실패 (IOException) - FILE_UPLOAD_FAILED")
    void uploadProfileImage_S3UploadFail_Fail() {
        // given
        MockMultipartFile corruptedFile = new MockMultipartFile(
                "file",
                "test.jpg",
                "image/jpeg",
                "test".getBytes()
        ) {
            @Override
            public byte[] getBytes() throws IOException {
                throw new IOException("파일 읽기 실패");
            }
        };

        // when & then
        assertThatThrownBy(() -> s3Service.uploadProfileImage(corruptedFile))
                .isInstanceOf(FileException.class)
                .hasMessage(ErrorCode.FILE_UPLOAD_FAILED.getMessage());

        System.out.println("✅ S3 업로드 실패 테스트 통과");
    }

    @Test
    @DisplayName("파일 삭제 성공")
    void deleteFile_Success() {
        // given
        String fileUrl = "https://test-bucket.s3.ap-northeast-2.amazonaws.com/profiles/test.jpg";

        // when
        s3Service.deleteFile(fileUrl);

        // then
        verify(s3Client, times(1)).deleteObject(any(DeleteObjectRequest.class));

        System.out.println("✅ 파일 삭제 성공");
        System.out.println("   - URL: " + fileUrl);
    }

    @Test
    @DisplayName("파일 삭제 실패해도 예외 없음 (silent failure)")
    void deleteFile_Fail_NoException() {
        // given
        String fileUrl = "https://test-bucket.s3.ap-northeast-2.amazonaws.com/profiles/test.jpg";
        doThrow(new RuntimeException("S3 삭제 실패")).when(s3Client).deleteObject(any(DeleteObjectRequest.class));

        // when & then (예외 없이 성공해야 함)
        s3Service.deleteFile(fileUrl);

        verify(s3Client, times(1)).deleteObject(any(DeleteObjectRequest.class));

        System.out.println("✅ 파일 삭제 실패해도 예외 없음 (silent failure)");
    }

    @Test
    @DisplayName("파일명 생성 - UUID + extension")
    void generateFileName_Success() {
        // when
        String result1 = s3Service.uploadProfileImage(validImageFile);
        String result2 = s3Service.uploadProfileImage(validImageFile);

        // then
        assertThat(result1).isNotEqualTo(result2); // UUID이므로 매번 다름
        assertThat(result1).contains(".jpg");
        assertThat(result2).contains(".jpg");

        System.out.println("✅ 파일명 생성 (UUID + extension)");
        System.out.println("   - 파일명 1: " + result1.substring(result1.lastIndexOf("/") + 1));
        System.out.println("   - 파일명 2: " + result2.substring(result2.lastIndexOf("/") + 1));
    }

    @Test
    @DisplayName("URL에서 S3 key 추출")
    void extractKeyFromUrl_Success() {
        // given
        String fileUrl = s3Service.uploadProfileImage(validImageFile);

        // when
        s3Service.deleteFile(fileUrl);

        // then
        verify(s3Client, times(1)).deleteObject(argThat(
                (DeleteObjectRequest request) -> request.key().startsWith("profiles/") && request.key().endsWith(".jpg")
        ));

        System.out.println("✅ URL에서 S3 key 추출 성공");
        System.out.println("   - URL: " + fileUrl);
    }
}
