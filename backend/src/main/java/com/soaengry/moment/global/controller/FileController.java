package com.soaengry.moment.global.controller;

import com.soaengry.moment.global.common.ApiResponse;
import com.soaengry.moment.global.service.S3Service;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/files")
@RequiredArgsConstructor
public class FileController {

    private final S3Service s3Service;

    @PostMapping("/upload")
    public ResponseEntity<ApiResponse<Map<String, String>>> uploadImage(@RequestParam("file") MultipartFile file) {
        String url = s3Service.uploadWeddingImage(file);
        return ResponseEntity.ok(ApiResponse.success(Map.of("url", url)));
    }

    @PostMapping("/upload/multiple")
    public ResponseEntity<ApiResponse<Map<String, List<String>>>> uploadMultipleImages(
            @RequestParam("files") List<MultipartFile> files) {
        List<String> urls = new ArrayList<>();
        for (MultipartFile file : files) {
            urls.add(s3Service.uploadWeddingImage(file));
        }
        return ResponseEntity.ok(ApiResponse.success(Map.of("urls", urls)));
    }
}
