package com.vidyalaya.school;

import com.vidyalaya.domain.ParentCommunity;
import com.vidyalaya.school.dto.ParentDtos.CommunityRequest;
import com.vidyalaya.school.dto.ParentDtos.ParentResponse;
import com.vidyalaya.school.dto.ParentDtos.ParentUpsertRequest;
import jakarta.validation.Valid;
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
@RequestMapping("/api/parents")
public class ParentController {

    private final ParentService parentService;

    public ParentController(ParentService parentService) {
        this.parentService = parentService;
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN','SUPER_ADMIN')")
    public List<ParentResponse> list() {
        return parentService.list();
    }

    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ParentResponse get(@PathVariable UUID id) {
        return parentService.get(id);
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN','SUPER_ADMIN')")
    public ParentResponse create(@Valid @RequestBody ParentUpsertRequest req) {
        return parentService.create(req);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','SUPER_ADMIN')")
    public ParentResponse update(@PathVariable UUID id, @Valid @RequestBody ParentUpsertRequest req) {
        return parentService.update(id, req);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','SUPER_ADMIN')")
    public void delete(@PathVariable UUID id) {
        parentService.delete(id);
    }

    @GetMapping("/{id}/children")
    @PreAuthorize("isAuthenticated()")
    public Object children(@PathVariable UUID id) {
        return parentService.children(id);
    }

    @GetMapping("/{id}/exam-results")
    @PreAuthorize("isAuthenticated()")
    public Object examResults(@PathVariable UUID id) {
        return parentService.examResults(id);
    }

    @GetMapping("/{id}/fee-dues")
    @PreAuthorize("isAuthenticated()")
    public Object feeDues(@PathVariable UUID id) {
        return parentService.feeDues(id);
    }

    @GetMapping("/{id}/communities")
    @PreAuthorize("isAuthenticated()")
    public List<ParentCommunity> communities(@PathVariable UUID id) {
        return parentService.communities(id);
    }

    @PostMapping("/{id}/communities")
    @PreAuthorize("isAuthenticated()")
    public ParentCommunity addCommunity(@PathVariable UUID id, @Valid @RequestBody CommunityRequest req) {
        return parentService.addCommunity(id, req);
    }

    @DeleteMapping("/communities/{communityId}")
    @PreAuthorize("isAuthenticated()")
    public void removeCommunity(@PathVariable UUID communityId) {
        parentService.removeCommunity(communityId);
    }

    @PostMapping("/{parentId}/students/{studentId}")
    @PreAuthorize("hasAnyRole('ADMIN','SUPER_ADMIN')")
    public void linkStudent(@PathVariable UUID parentId, @PathVariable UUID studentId) {
        parentService.linkStudent(parentId, studentId);
    }
}
