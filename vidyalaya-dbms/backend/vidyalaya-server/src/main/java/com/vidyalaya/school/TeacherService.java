package com.vidyalaya.school;

import com.vidyalaya.domain.AppUser;
import com.vidyalaya.domain.Teacher;
import com.vidyalaya.domain.repository.AppUserRepository;
import com.vidyalaya.domain.repository.ExamInvigilationRepository;
import com.vidyalaya.domain.repository.SalaryPaymentRepository;
import com.vidyalaya.domain.repository.TeacherAssignmentRepository;
import com.vidyalaya.domain.repository.TeacherRepository;
import com.vidyalaya.domain.repository.TimetableSlotRepository;
import com.vidyalaya.security.JwtAuthToken;
import com.vidyalaya.security.SecurityUtils;
import com.vidyalaya.school.dto.TeacherDtos.TeacherResponse;
import com.vidyalaya.school.dto.TeacherDtos.TeacherUpsertRequest;
import java.util.List;
import java.util.UUID;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class TeacherService {

    private final TeacherRepository teacherRepository;
    private final AppUserRepository appUserRepository;
    private final TeacherAssignmentRepository teacherAssignmentRepository;
    private final SalaryPaymentRepository salaryPaymentRepository;
    private final ExamInvigilationRepository examInvigilationRepository;
    private final TimetableSlotRepository timetableSlotRepository;

    public TeacherService(
            TeacherRepository teacherRepository,
            AppUserRepository appUserRepository,
            TeacherAssignmentRepository teacherAssignmentRepository,
            SalaryPaymentRepository salaryPaymentRepository,
            ExamInvigilationRepository examInvigilationRepository,
            TimetableSlotRepository timetableSlotRepository) {
        this.teacherRepository = teacherRepository;
        this.appUserRepository = appUserRepository;
        this.teacherAssignmentRepository = teacherAssignmentRepository;
        this.salaryPaymentRepository = salaryPaymentRepository;
        this.examInvigilationRepository = examInvigilationRepository;
        this.timetableSlotRepository = timetableSlotRepository;
    }

    @Transactional(readOnly = true)
    public List<TeacherResponse> list() {
        JwtAuthToken jwt = SecurityUtils.requireAuth();
        if (SecurityUtils.isTenantAdminRole(jwt.getRole())) {
            return teacherRepository.findAll().stream().map(TeacherService::toResponse).toList();
        }
        if ("TEACHER".equals(jwt.getRole())) {
            AppUser u = appUserRepository.findById(jwt.getUserId()).orElseThrow();
            if (u.getTeacher() == null) {
                throw new AccessDeniedException("No teacher profile");
            }
            return List.of(toResponse(u.getTeacher()));
        }
        throw new AccessDeniedException("Cannot list teachers");
    }

    @Transactional(readOnly = true)
    public TeacherResponse get(UUID id) {
        JwtAuthToken jwt = SecurityUtils.requireAuth();
        Teacher t = teacherRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("Not found"));
        assertCanViewTeacher(jwt, t.getId());
        return toResponse(t);
    }

    @Transactional
    public TeacherResponse create(TeacherUpsertRequest req) {
        requireAdmin();
        Teacher t = map(new Teacher(), req);
        return toResponse(teacherRepository.save(t));
    }

    @Transactional
    public TeacherResponse update(UUID id, TeacherUpsertRequest req) {
        requireAdmin();
        Teacher t = teacherRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("Not found"));
        map(t, req);
        return toResponse(teacherRepository.save(t));
    }

    @Transactional
    public void delete(UUID id) {
        requireAdmin();
        teacherRepository.deleteById(id);
    }

    @Transactional(readOnly = true)
    public Object assignments(UUID teacherId) {
        assertCanViewTeacher(SecurityUtils.requireAuth(), teacherId);
        return teacherAssignmentRepository.findByTeacherId(teacherId);
    }

    @Transactional(readOnly = true)
    public Object salary(UUID teacherId) {
        JwtAuthToken jwt = SecurityUtils.requireAuth();
        if (SecurityUtils.isTenantAdminRole(jwt.getRole())) {
            return salaryPaymentRepository.findByTeacherIdOrderByCreatedAtDesc(teacherId);
        }
        assertTeacherSelf(jwt, teacherId);
        return salaryPaymentRepository.findByTeacherIdOrderByCreatedAtDesc(teacherId);
    }

    @Transactional(readOnly = true)
    public Object invigilations(UUID teacherId) {
        assertCanViewTeacher(SecurityUtils.requireAuth(), teacherId);
        return examInvigilationRepository.findByTeacherId(teacherId);
    }

    @Transactional(readOnly = true)
    public Object timetable(UUID teacherId) {
        assertCanViewTeacher(SecurityUtils.requireAuth(), teacherId);
        return timetableSlotRepository.findByTeacherIdOrderByDayOfWeekAscStartTimeAsc(teacherId);
    }

    private void assertTeacherSelf(JwtAuthToken jwt, UUID teacherId) {
        if (!"TEACHER".equals(jwt.getRole())) {
            throw new AccessDeniedException("Teacher only");
        }
        AppUser u = appUserRepository.findById(jwt.getUserId()).orElseThrow();
        if (u.getTeacher() == null || !u.getTeacher().getId().equals(teacherId)) {
            throw new AccessDeniedException("Cannot view");
        }
    }

    private void assertCanViewTeacher(JwtAuthToken jwt, UUID teacherId) {
        if (SecurityUtils.isTenantAdminRole(jwt.getRole())) {
            return;
        }
        if ("TEACHER".equals(jwt.getRole())) {
            assertTeacherSelf(jwt, teacherId);
            return;
        }
        throw new AccessDeniedException("Cannot view teacher");
    }

    private void requireAdmin() {
        SecurityUtils.requireTenantAdmin();
    }

    private static TeacherResponse toResponse(Teacher t) {
        return new TeacherResponse(
                t.getId(),
                t.getFullName(),
                t.getEmail(),
                t.getPhone(),
                t.getGender(),
                t.getDateOfBirth(),
                t.getAddress(),
                t.getQualification(),
                t.getExperienceSummary(),
                t.getJoiningDate(),
                t.getSalaryAmount(),
                t.getPhotoUrl(),
                t.getSocialLinks());
    }

    private static Teacher map(Teacher t, TeacherUpsertRequest r) {
        t.setFullName(r.fullName());
        t.setEmail(r.email());
        t.setPhone(r.phone());
        t.setGender(r.gender());
        t.setDateOfBirth(r.dateOfBirth());
        t.setAddress(r.address());
        t.setQualification(r.qualification());
        t.setExperienceSummary(r.experienceSummary());
        t.setJoiningDate(r.joiningDate());
        t.setSalaryAmount(r.salaryAmount());
        t.setPhotoUrl(r.photoUrl());
        t.setSocialLinks(r.socialLinks());
        return t;
    }
}
