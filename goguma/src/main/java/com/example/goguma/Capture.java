package com.example.goguma;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
public class Capture {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String imageUrl;      // 이미지 저장 경로/URL
    private String category;      // 맛집, 카페, 쇼핑, 기타

    @Column(columnDefinition = "TEXT")
    private String extractedText; // OCR 추출 텍스트

    private LocalDateTime createdAt = LocalDateTime.now();

    public Capture() {}

    public Long getId() { return id; }
    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }
    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }
    public String getExtractedText() { return extractedText; }
    public void setExtractedText(String extractedText) { this.extractedText = extractedText; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
