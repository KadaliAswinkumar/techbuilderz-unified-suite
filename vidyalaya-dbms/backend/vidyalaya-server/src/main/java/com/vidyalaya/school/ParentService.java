package com.vidyalaya.school;

import com.vidyalaya.domain.AppUser;
import com.vidyalaya.domain.Parent;
import com.vidyalaya.domain.ParentCommunity;
import com.vidyalaya.domain.Student;
import com.vidyalaya.domain.repository.AppUserRepository;
import com.vidyalaya.domain.repository.ExamResultRepository;
import com.vidyalaya.domain.repository.FeePaymentRepository;
import com.vidyalaya.domain.repository.ParentCommunityRepository;
import com.vidyalaya.domain.repository.ParentRepository;
import com.vidyalaya.domain.repository.StudentRepository;
import com.vidyalaya.security.JwtAuthToken;
import com.vidyalaya.security.SecurityUtils;
import com.vidyalaya.school.dto.ParentDtos.CommunityRequest;
import com.vidyalaya.school.dto.ParentDtos.ParentResponse;
import com.vidyalaya.school.dto.ParentDtos.ParentUpsertRequest;
import java.util.List;
import java.util.UUID;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class ParentService {

    private final ParentRepository parentRepository;
    private final AppUserRepository appUserRepository;
    private final ParentCommunityRepository parentCommunityRepository;
    private final ExamResultRepository examResultRepository;
    private final FeePaymentRepository feePaymentRepository;
    private final StudentRepository studentRepository;

    public ParentService(
            ParentRepository parentRepository,
            AppUserRepository appUserRepository,
            ParentCommunityRepository parentCommunityRepository,
            ExamResultRepository examResultRepository,
            FeePaymentRepository feePaymentRepository,
            StudentRepository studentRepository) {
        this.parentRepository = parentRepository;
        this.appUserRepository = appUserRepository;
        this.parentCommunityRepository = parentCommunityRepository;
        this.examResultRepository = examResultRepository;
        this.feePaymentRepository = feePaymentRepository;
        this.studentRepository = studentRepository;
    }

    @Transactional(readOnly = true)
    public List<ParentResponse> list() {
        requireAdmin();
        return parentRepository.findAll().stream().map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public ParentResponse get(UUID id) {
        JwtAuthToken jwt = SecurityUtils.requireAuth();
        if (SecurityUtils.isTenantAdminRole(jwt.getRole())) {
            return toResponse(parentRepository.findById(id).orElseThrow());
        }
        if ("PARENT".equals(jwt.getRole())) {
            AppUser u = appUserRepository.findById(jwt.getUserId()).orElseThrow();
            if (u.getParent() == null || !u.getParent().getId().equals(id)) {
                throw new AccessDeniedException("Cannot view");
            }
            return toResponse(parentRepository.findById(id).orElseThrow());
        }
        throw new AccessDeniedException("Cannot view parent");
    }

    @Transactional
    public ParentResponse create(ParentUpsertRequest req) {
        requireAdmin();
        Parent p = map(new Parent(), req);
        return toResponse(parentRepository.save(p));
    }

    @Transactional
    public ParentResponse update(UUID id, ParentUpsertRequest req) {
        requireAdmin();
        Parent p = parentRepository.findById(id).orElseThrow();
        map(p, req);
        return toResponse(parentRepository.save(p));
    }

    @Transactional
    public void delete(UUID id) {
        requireAdmin();
        parentRepository.deleteById(id);
    }

    @Transactional(readOnly = true)
    public Object children(UUID parentId) {
        assertCanViewParent(parentId);
        Parent p = parentRepository.findById(parentId).orElseThrow();
        return p.getChildren().stream()
                .map(
                        s ->
                                java.util.Map.of(
                                        "studentId",
                                        s.getId(),
                                        "fullName",
                                        s.getFullName(),
                                        "className",
                                        s.getClassName(),
                                        "section",
                                        s.getSection()))
                .toList();
    }

    @Transactional(readOnly = true)
    public Object examResults(UUID parentId) {
        assertCanViewParent(parentId);
        Parent p = parentRepository.findById(parentId).orElseThrow();
        List<UUID> ids = p.getChildren().stream().map(Student::getId).toList();
        return ids.stream()
                .flatMap(sid -> examResultRepository.findByStudentIdOrderByCreatedAtDesc(sid).stream())
                .toList();
    }

    @Transactional(readOnly = true)
    public Object feeDues(UUID parentId) {
        assertCanViewParent(parentId);
        Parent p = parentRepository.findById(parentId).orElseThrow();
        return p.getChildren().stream()
                .flatMap(
                        st ->
                                feePaymentRepository.findByStudentIdOrderByCreatedAtDesc(st.getId()).stream()
                                        .filter(fp -> "DUE".equalsIgnoreCase(fp.getStatus())))
                .toList();
    }

    @Transactional(readOnly = true)
    public List<ParentCommunity> communities(UUID parentId) {
        assertCanViewParent(parentId);
        return parentCommunityRepository.findByParentId(parentId);
    }

    @Transactional
    public ParentCommunity addCommunity(UUID parentId, CommunityRequest req) {
        assertCanViewParent(parentId);
        ParentCommunity c = new ParentCommunity();
        c.setParent(parentRepository.getReferenceById(parentId));
        c.setName(req.name());
        return parentCommunityRepository.save(c);
    }

    @Transactional
    public void linkStudent(UUID parentId, UUID studentId) {
        requireAdmin();
        Parent p = parentRepository.findById(parentId).orElseThrow();
        Student s = studentRepository.findById(studentId).orElseThrow();
        p.getChildren().add(s);
        parentRepository.save(p);
    }

    @Transactional
    public void removeCommunity(UUID communityId) {
        JwtAuthToken jwt = SecurityUtils.requireAuth();
        ParentCommunity c =
                parentCommunityRepository.findById(communityId).orElseThrow();
        if (SecurityUtils.isTenantAdminRole(jwt.getRole())) {
            parentCommunityRepository.delete(c);
            return;
        }
        if ("PARENT".equals(jwt.getRole())) {
            AppUser u = appUserRepository.findById(jwt.getUserId()).orElseThrow();
            if (u.getParent() != null && c.getParent().getId().equals(u.getParent().getId())) {
                parentCommunityRepository.delete(c);
                return;
            }
        }
        throw new AccessDeniedException("Cannot remove");
    }

    private void assertCanViewParent(UUID parentId) {
        JwtAuthToken jwt = SecurityUtils.requireAuth();
        if (SecurityUtils.isTenantAdminRole(jwt.getRole())) {
            return;
        }
        if ("PARENT".equals(jwt.getRole())) {
            AppUser u = appUserRepository.findById(jwt.getUserId()).orElseThrow();
            if (u.getParent() != null && u.getParent().getId().equals(parentId)) {
                return;
            }
        }
        throw new AccessDeniedException("Cannot view parent");
    }

    private void requireAdmin() {
        SecurityUtils.requireTenantAdmin();
    }

    private ParentResponse toResponse(Parent p) {
        int cc = p.getChildren() == null ? 0 : p.getChildren().size();
        return new ParentResponse(
                p.getId(),
                p.getFullName(),
                p.getEmail(),
                p.getPhone(),
                p.getAddress(),
                p.getOccupation(),
                p.getEmployer(),
                p.getEducationSummary(),
                p.getPhotoUrl(),
                p.getSocialLinks(),
                cc);
    }

    private static Parent map(Parent p, ParentUpsertRequest r) {
        p.setFullName(r.fullName());
        p.setEmail(r.email());
        p.setPhone(r.phone());
        p.setAddress(r.address());
        p.setOccupation(r.occupation());
        p.setEmployer(r.employer());
        p.setEducationSummary(r.educationSummary());
        p.setPhotoUrl(r.photoUrl());
        p.setSocialLinks(r.socialLinks());
        return p;
    }
}
