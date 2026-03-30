package com.ourmemories.OurMemoriesEduSmart.controller;

import com.ourmemories.OurMemoriesEduSmart.model.Announcement;
import com.ourmemories.OurMemoriesEduSmart.repository.AnnouncementRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequiredArgsConstructor
public class PublicController {

    private final AnnouncementRepository announcementRepository;

    @GetMapping("/api/announcements")  // ← Correct path!
    public ResponseEntity<Map<String, Object>> getPublicAnnouncements() {
        Map<String, Object> response = new HashMap<>();
        try {
            List<Map<String, Object>> list = announcementRepository.findAll()
                    .stream()
                    .sorted((a, b) -> b.getDate().compareTo(a.getDate()))
                    .map(this::toMap)
                    .toList();

            response.put("success", true);
            response.put("announcements", list);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Failed to load announcements");
            return ResponseEntity.ok(response);
        }
    }

    private Map<String, Object> toMap(Announcement a) {
        return Map.of(
                "id", a.getId(),
                "title", a.getTitle() != null ? a.getTitle() : "",
                "message", a.getMessage() != null ? a.getMessage() : "",
                "date", a.getDate().toString(),
                "type", a.getType() != null ? a.getType() : "info",
                "important", a.isImportant()
        );
    }
}