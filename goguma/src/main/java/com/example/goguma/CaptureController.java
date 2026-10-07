package com.example.goguma;

import java.io.IOException;
import java.util.List;
import java.util.Set;
import org.springframework.http.CacheControl;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/captures")
@CrossOrigin(origins = "*")
public class CaptureController {

    private static final Set<String> ALLOWED_EXTENSIONS = Set.of("jpg", "jpeg", "png", "gif", "webp");

    private final CaptureRepository captureRepository;
    private final CategoryClassifier classifier;
    private final CaptureImageRepository imageRepository;

    public CaptureController(CaptureRepository captureRepository,
                             CategoryClassifier classifier,
                             CaptureImageRepository imageRepository) {
        this.captureRepository = captureRepository;
        this.classifier = classifier;
        this.imageRepository = imageRepository;
    }

    @PostMapping("/classify")
    public java.util.Map<String, String> classify(@RequestBody Capture request) {
        return java.util.Map.of("category", classifier.classify(request.getExtractedText()));
    }

    private String resolveCategory(String category, String extractedText) {
        return (category == null || category.isBlank()) ? classifier.classify(extractedText) : category;
    }

    @GetMapping("/test")
    public String test() {
        return "파타주 Partage 백엔드 연동 성공!";
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Capture create(@RequestBody Capture request) {
        Capture capture = new Capture();
        capture.setImageUrl(request.getImageUrl());
        capture.setCategory(resolveCategory(request.getCategory(), request.getExtractedText()));
        capture.setExtractedText(request.getExtractedText());
        return captureRepository.save(capture);
    }

    @Transactional
    @PostMapping(value = "/upload", consumes = "multipart/form-data")
    @ResponseStatus(HttpStatus.CREATED)
    public Capture upload(@RequestParam("file") MultipartFile file,
                          @RequestParam(required = false) String category,
                          @RequestParam(required = false) String extractedText) throws IOException {
        String contentType = file.getContentType();
        if (file.isEmpty() || contentType == null || !contentType.startsWith("image/")) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "이미지 파일만 업로드할 수 있습니다.");
        }
        String original = file.getOriginalFilename() == null ? "" : file.getOriginalFilename();
        int dot = original.lastIndexOf('.');
        String ext = dot >= 0 ? original.substring(dot + 1).toLowerCase() : "";
        if (!ALLOWED_EXTENSIONS.contains(ext)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "지원하지 않는 이미지 형식입니다.");
        }

        Capture capture = new Capture();
        capture.setCategory(resolveCategory(category, extractedText));
        capture.setExtractedText(extractedText);
        capture = captureRepository.save(capture);

        imageRepository.save(new CaptureImage(capture.getId(), contentType, file.getBytes()));
        capture.setImageUrl("/api/captures/" + capture.getId() + "/image");
        return captureRepository.save(capture);
    }

    @GetMapping("/{id}/image")
    public ResponseEntity<byte[]> image(@PathVariable Long id) {
        return imageRepository.findById(id)
                .map(img -> ResponseEntity.ok()
                        .contentType(MediaType.parseMediaType(img.getContentType()))
                        .cacheControl(CacheControl.maxAge(java.time.Duration.ofDays(30)).cachePublic())
                        .body(img.getData()))
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping
    public List<Capture> list(@RequestParam(required = false) String category) {
        if (category == null || category.isBlank()) {
            return captureRepository.findAllByOrderByCreatedAtDesc();
        }
        return captureRepository.findByCategoryOrderByCreatedAtDesc(category);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Capture> get(@PathVariable Long id) {
        return captureRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @Transactional
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        if (!captureRepository.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        imageRepository.deleteById(id);
        captureRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}
