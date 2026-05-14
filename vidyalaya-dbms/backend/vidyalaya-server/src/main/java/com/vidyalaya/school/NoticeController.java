package com.vidyalaya.school;

import com.vidyalaya.domain.Notice;
import com.vidyalaya.domain.repository.NoticeRepository;
import jakarta.validation.constraints.NotBlank;
import java.util.List;
import java.util.UUID;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/notices")
public class NoticeController {

    private final NoticeRepository noticeRepository;

    public NoticeController(NoticeRepository noticeRepository) {
        this.noticeRepository = noticeRepository;
    }

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public List<Notice> list() {
        return noticeRepository.findAll();
    }

    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public Notice get(@PathVariable UUID id) {
        return noticeRepository.findById(id).orElseThrow();
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN','SUPER_ADMIN')")
    public Notice create(@RequestBody NoticeReq req) {
        Notice n = new Notice();
        n.setTitle(req.title());
        n.setBody(req.body());
        n.setImageUrl(req.imageUrl());
        return noticeRepository.save(n);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','SUPER_ADMIN')")
    public Notice update(@PathVariable UUID id, @RequestBody NoticeReq req) {
        Notice n = noticeRepository.findById(id).orElseThrow();
        n.setTitle(req.title());
        n.setBody(req.body());
        n.setImageUrl(req.imageUrl());
        return noticeRepository.save(n);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','SUPER_ADMIN')")
    public void delete(@PathVariable UUID id) {
        noticeRepository.deleteById(id);
    }

    public record NoticeReq(@NotBlank String title, String body, String imageUrl) {}
}
