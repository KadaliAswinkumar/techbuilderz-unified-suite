package com.vidyalaya.school;

import com.vidyalaya.domain.AppUser;
import com.vidyalaya.domain.Parent;
import com.vidyalaya.domain.Student;
import com.vidyalaya.domain.repository.AppUserRepository;
import com.vidyalaya.domain.repository.AttendanceRecordRepository;
import com.vidyalaya.domain.repository.ExamResultRepository;
import com.vidyalaya.domain.repository.ExtracurricularRepository;
import com.vidyalaya.domain.repository.ParentRepository;
import com.vidyalaya.domain.repository.StudentRepository;
import com.vidyalaya.security.JwtAuthToken;
import com.vidyalaya.security.SecurityUtils;
import com.vidyalaya.school.dto.StudentDtos.StudentResponse;
import com.vidyalaya.school.dto.StudentDtos.StudentUpsertRequest;
import java.io.BufferedReader;
import java.io.StringReader;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class StudentService {

    private final StudentRepository studentRepository;
    private final AppUserRepository appUserRepository;
    private final ParentRepository parentRepository;
    private final ExamResultRepository examResultRepository;
    private final AttendanceRecordRepository attendanceRecordRepository;
    private final ExtracurricularRepository extracurricularRepository;

    public StudentService(
            StudentRepository studentRepository,
            AppUserRepository appUserRepository,
            ParentRepository parentRepository,
            ExamResultRepository examResultRepository,
            AttendanceRecordRepository attendanceRecordRepository,
            ExtracurricularRepository extracurricularRepository) {
        this.studentRepository = studentRepository;
        this.appUserRepository = appUserRepository;
        this.parentRepository = parentRepository;
        this.examResultRepository = examResultRepository;
        this.attendanceRecordRepository = attendanceRecordRepository;
        this.extracurricularRepository = extracurricularRepository;
    }

    @Transactional(readOnly = true)
    public List<StudentResponse> list() {
        JwtAuthToken jwt = SecurityUtils.requireAuth();
        if (SecurityUtils.isTenantAdminRole(jwt.getRole()) || "TEACHER".equals(jwt.getRole())) {
            return studentRepository.findAll().stream().map(StudentService::toResponse).toList();
        }
        if ("STUDENT".equals(jwt.getRole())) {
            AppUser u =
                    appUserRepository
                            .findById(jwt.getUserId())
                            .orElseThrow(() -> new AccessDeniedException("No user"));
            if (u.getStudent() == null) {
                throw new AccessDeniedException("No student profile");
            }
            return List.of(toResponse(u.getStudent()));
        }
        if ("PARENT".equals(jwt.getRole())) {
            AppUser u = appUserRepository.findById(jwt.getUserId()).orElseThrow();
            if (u.getParent() == null) {
                throw new AccessDeniedException("No parent profile");
            }
            Parent p = parentRepository.findById(u.getParent().getId()).orElseThrow();
            return p.getChildren().stream().map(StudentService::toResponse).toList();
        }
        throw new AccessDeniedException("Role cannot list students");
    }

    @Transactional(readOnly = true)
    public StudentResponse get(UUID id) {
        JwtAuthToken jwt = SecurityUtils.requireAuth();
        Student s = studentRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("Not found"));
        assertCanViewStudent(jwt, s.getId());
        return toResponse(s);
    }

    @Transactional
    public StudentResponse create(StudentUpsertRequest req) {
        requireAdmin();
        Student s = map(new Student(), req);
        return toResponse(studentRepository.save(s));
    }

    @Transactional
    public StudentResponse update(UUID id, StudentUpsertRequest req) {
        requireAdmin();
        Student s = studentRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("Not found"));
        map(s, req);
        return toResponse(studentRepository.save(s));
    }

    @Transactional
    public void delete(UUID id) {
        requireAdmin();
        studentRepository.deleteById(id);
    }

    @Transactional(readOnly = true)
    public Object academicRecord(UUID id) {
        assertCanViewStudent(SecurityUtils.requireAuth(), id);
        return new java.util.LinkedHashMap<>(
                java.util.Map.of(
                        "examResults",
                        examResultRepository.findByStudentIdOrderByCreatedAtDesc(id),
                        "extracurriculars",
                        extracurricularRepository.findByStudentId(id)));
    }

    @Transactional(readOnly = true)
    public Object attendance(UUID id) {
        assertCanViewStudent(SecurityUtils.requireAuth(), id);
        return attendanceRecordRepository.findByStudentIdOrderByRecordDateDesc(id);
    }

    @Transactional
    public int importCsv(String csv) {
        requireAdmin();
        int count = 0;
        try (BufferedReader br = new BufferedReader(new StringReader(csv))) {
            String line;
            boolean header = true;
            while ((line = br.readLine()) != null) {
                if (line.isBlank()) {
                    continue;
                }
                if (header) {
                    header = false;
                    continue;
                }
                String[] p = line.split(",");
                if (p.length < 1) {
                    continue;
                }
                Student s = new Student();
                s.setFullName(trim(p, 0));
                s.setEmail(trim(p, 1));
                s.setPhone(trim(p, 2));
                s.setGender(trim(p, 3));
                s.setFatherName(trim(p, 4));
                s.setMotherName(trim(p, 5));
                s.setDateOfBirth(parseDate(trim(p, 6)));
                s.setReligion(trim(p, 7));
                s.setCaste(trim(p, 8));
                s.setAddress(trim(p, 9));
                s.setClassName(trim(p, 10));
                s.setSection(trim(p, 11));
                s.setAdmissionDate(parseDate(trim(p, 12)));
                studentRepository.save(s);
                count++;
            }
        } catch (Exception e) {
            throw new IllegalArgumentException("CSV parse error: " + e.getMessage());
        }
        return count;
    }

    private static String trim(String[] p, int i) {
        if (i >= p.length) {
            return null;
        }
        String v = p[i].trim();
        return v.isEmpty() ? null : v;
    }

    private static LocalDate parseDate(String v) {
        if (v == null) {
            return null;
        }
        try {
            return LocalDate.parse(v, DateTimeFormatter.ISO_LOCAL_DATE);
        } catch (Exception e) {
            return null;
        }
    }

    private void assertCanViewStudent(JwtAuthToken jwt, UUID studentId) {
        if (SecurityUtils.isTenantAdminRole(jwt.getRole()) || "TEACHER".equals(jwt.getRole())) {
            return;
        }
        if ("STUDENT".equals(jwt.getRole())) {
            AppUser u = appUserRepository.findById(jwt.getUserId()).orElseThrow();
            if (u.getStudent() != null && u.getStudent().getId().equals(studentId)) {
                return;
            }
        }
        if ("PARENT".equals(jwt.getRole())) {
            AppUser u = appUserRepository.findById(jwt.getUserId()).orElseThrow();
            Parent par = u.getParent();
            if (par != null) {
                Parent loaded = parentRepository.findById(par.getId()).orElseThrow();
                boolean ok =
                        loaded.getChildren().stream().anyMatch(ch -> ch.getId().equals(studentId));
                if (ok) {
                    return;
                }
            }
        }
        throw new AccessDeniedException("Cannot view student");
    }

    private void requireAdmin() {
        SecurityUtils.requireTenantAdmin();
    }

    private static StudentResponse toResponse(Student s) {
        return new StudentResponse(
                s.getId(),
                s.getFullName(),
                s.getFirstName(),
                s.getMiddleName(),
                s.getLastName(),
                s.getEmail(),
                s.getPhone(),
                s.getGender(),
                s.getFatherName(),
                s.getMotherName(),
                s.getFatherOccupation(),
                s.getMotherOccupation(),
                s.getDateOfBirth(),
                s.getReligion(),
                s.getCaste(),
                s.getAddress(),
                s.getClassName(),
                s.getSection(),
                s.getAdmissionDate(),
                s.getPhotoUrl(),
                s.getSocialLinks(),
                s.getAboutStudent());
    }

    private static Student map(Student s, StudentUpsertRequest r) {
        s.setFullName(r.fullName());
        s.setEmail(r.email());
        s.setPhone(r.phone());
        s.setGender(r.gender());
        s.setFatherName(r.fatherName());
        s.setMotherName(r.motherName());
        s.setFatherOccupation(r.fatherOccupation());
        s.setMotherOccupation(r.motherOccupation());
        s.setDateOfBirth(r.dateOfBirth());
        s.setReligion(r.religion());
        s.setCaste(r.caste());
        s.setAddress(r.address());
        s.setClassName(r.className());
        s.setSection(r.section());
        s.setAdmissionDate(r.admissionDate());
        s.setPhotoUrl(r.photoUrl());
        s.setSocialLinks(r.socialLinks());
        s.setFirstName(r.firstName());
        s.setMiddleName(r.middleName());
        s.setLastName(r.lastName());
        s.setAboutStudent(r.aboutStudent());
        return s;
    }
}
