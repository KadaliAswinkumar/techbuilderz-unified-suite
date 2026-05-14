package com.vidyalaya.school;

import com.vidyalaya.domain.Faq;
import com.vidyalaya.domain.repository.FaqRepository;
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
@RequestMapping("/api/faqs")
public class FaqController {

    private final FaqRepository faqRepository;

    public FaqController(FaqRepository faqRepository) {
        this.faqRepository = faqRepository;
    }

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public List<Faq> list() {
        return faqRepository.findAll();
    }

    @GetMapping("/active")
    public List<Faq> active() {
        return faqRepository.findByActiveIsTrue();
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN','SUPER_ADMIN')")
    public Faq create(@RequestBody FaqReq req) {
        Faq f = new Faq();
        f.setQuestion(req.question());
        f.setAnswer(req.answer());
        f.setActive(req.active());
        return faqRepository.save(f);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','SUPER_ADMIN')")
    public Faq update(@PathVariable UUID id, @RequestBody FaqReq req) {
        Faq f = faqRepository.findById(id).orElseThrow();
        f.setQuestion(req.question());
        f.setAnswer(req.answer());
        f.setActive(req.active());
        return faqRepository.save(f);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','SUPER_ADMIN')")
    public void delete(@PathVariable UUID id) {
        faqRepository.deleteById(id);
    }

    public record FaqReq(@NotBlank String question, @NotBlank String answer, boolean active) {}
}
