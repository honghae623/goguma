package com.example.goguma;

import jakarta.persistence.*;

/** 업로드한 이미지 원본. Capture 목록 조회 시 함께 읽히지 않도록 별도 테이블로 분리한다. */
@Entity
public class CaptureImage {

    @Id
    private Long captureId;

    private String contentType;

    @Lob
    @Column(nullable = false)
    private byte[] data;

    public CaptureImage() {}

    public CaptureImage(Long captureId, String contentType, byte[] data) {
        this.captureId = captureId;
        this.contentType = contentType;
        this.data = data;
    }

    public Long getCaptureId() { return captureId; }
    public String getContentType() { return contentType; }
    public byte[] getData() { return data; }
}
