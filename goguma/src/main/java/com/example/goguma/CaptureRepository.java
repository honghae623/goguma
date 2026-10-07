package com.example.goguma;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CaptureRepository extends JpaRepository<Capture, Long> {

    List<Capture> findByCategoryOrderByCreatedAtDesc(String category);

    List<Capture> findAllByOrderByCreatedAtDesc();
}
