package com.vidyalaya.publicweb;

import com.vidyalaya.domain.PublicPage;
import com.vidyalaya.domain.repository.PublicPageRepository;
import jakarta.validation.constraints.NotBlank;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/public-pages")
public class ApiPublicPagesController {

    private final PublicPageRepository publicPageRepository;

    public ApiPublicPagesController(PublicPageRepository publicPageRepository) {
        this.publicPageRepository = publicPageRepository;
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN','SUPER_ADMIN')")
    public List<PublicPage> list() {
        return publicPageRepository.findAll();
    }

    @PutMapping("/{pageKey}")
    @PreAuthorize("hasAnyRole('ADMIN','SUPER_ADMIN')")
    public PublicPage upsert(@PathVariable String pageKey, @RequestBody PageReq req) {
        PublicPage p = publicPageRepository.findByPageKey(pageKey).orElseGet(PublicPage::new);
        p.setPageKey(pageKey);
        p.setTitle(req.title());
        p.setContentHtml(req.contentHtml());
        p.setMetaDescription(req.metaDescription());
        p.setUpdatedAt(Instant.now());
        return publicPageRepository.save(p);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','SUPER_ADMIN')")
    public void delete(@PathVariable UUID id) {
        publicPageRepository.deleteById(id);
    }

    public record PageReq(String title, String contentHtml, String metaDescription) {}
}
