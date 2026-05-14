package com.vidyalaya.school;

import com.vidyalaya.domain.Exam;
import com.vidyalaya.domain.ExamInvigilation;
import com.vidyalaya.domain.ExamResult;
import com.vidyalaya.domain.SchoolClass;
import com.vidyalaya.domain.Section;
import com.vidyalaya.domain.Subject;
import com.vidyalaya.domain.TeacherAssignment;
import com.vidyalaya.domain.TimetableSlot;
import com.vidyalaya.domain.repository.ExamInvigilationRepository;
import com.vidyalaya.domain.repository.ExamRepository;
import com.vidyalaya.domain.repository.ExamResultRepository;
import com.vidyalaya.domain.repository.SchoolClassRepository;
import com.vidyalaya.domain.repository.SectionRepository;
import com.vidyalaya.domain.repository.StudentRepository;
import com.vidyalaya.domain.repository.SubjectRepository;
import com.vidyalaya.domain.repository.TeacherAssignmentRepository;
import com.vidyalaya.domain.repository.TeacherRepository;
import com.vidyalaya.domain.repository.TimetableSlotRepository;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.UUID;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
@Transactional(readOnly = true)
public class AcademicsController {

    private final SchoolClassRepository schoolClassRepository;
    private final SectionRepository sectionRepository;
    private final SubjectRepository subjectRepository;
    private final TeacherAssignmentRepository teacherAssignmentRepository;
    private final TimetableSlotRepository timetableSlotRepository;
    private final ExamRepository examRepository;
    private final ExamResultRepository examResultRepository;
    private final ExamInvigilationRepository examInvigilationRepository;
    private final TeacherRepository teacherRepository;
    private final StudentRepository studentRepository;

    public AcademicsController(
            SchoolClassRepository schoolClassRepository,
            SectionRepository sectionRepository,
            SubjectRepository subjectRepository,
            TeacherAssignmentRepository teacherAssignmentRepository,
            TimetableSlotRepository timetableSlotRepository,
            ExamRepository examRepository,
            ExamResultRepository examResultRepository,
            ExamInvigilationRepository examInvigilationRepository,
            TeacherRepository teacherRepository,
            StudentRepository studentRepository) {
        this.schoolClassRepository = schoolClassRepository;
        this.sectionRepository = sectionRepository;
        this.subjectRepository = subjectRepository;
        this.teacherAssignmentRepository = teacherAssignmentRepository;
        this.timetableSlotRepository = timetableSlotRepository;
        this.examRepository = examRepository;
        this.examResultRepository = examResultRepository;
        this.examInvigilationRepository = examInvigilationRepository;
        this.teacherRepository = teacherRepository;
        this.studentRepository = studentRepository;
    }

    @GetMapping("/school-classes")
    @PreAuthorize("hasAnyRole('ADMIN','TEACHER')")
    public List<SchoolClass> classes() {
        return schoolClassRepository.findAll();
    }

    @PostMapping("/school-classes")
    @PreAuthorize("hasAnyRole('ADMIN','SUPER_ADMIN')")
    @Transactional(readOnly = false)
    public SchoolClass createClass(@RequestBody NameReq req) {
        SchoolClass c = new SchoolClass();
        c.setName(req.name());
        return schoolClassRepository.save(c);
    }

    @GetMapping("/sections")
    @PreAuthorize("hasAnyRole('ADMIN','TEACHER')")
    public List<Section> sections(@RequestParam UUID classId) {
        return sectionRepository.findAll().stream()
                .filter(s -> s.getSchoolClass().getId().equals(classId))
                .toList();
    }

    @PostMapping("/sections")
    @PreAuthorize("hasAnyRole('ADMIN','SUPER_ADMIN')")
    @Transactional(readOnly = false)
    public Section createSection(@RequestBody SectionReq req) {
        Section s = new Section();
        s.setName(req.name());
        s.setSchoolClass(schoolClassRepository.getReferenceById(req.schoolClassId()));
        return sectionRepository.save(s);
    }

    @GetMapping("/subjects")
    @PreAuthorize("hasAnyRole('ADMIN','TEACHER')")
    public List<Subject> subjects() {
        return subjectRepository.findAll();
    }

    @PostMapping("/subjects")
    @PreAuthorize("hasAnyRole('ADMIN','SUPER_ADMIN')")
    @Transactional(readOnly = false)
    public Subject createSubject(@RequestBody NameReq req) {
        Subject s = new Subject();
        s.setName(req.name());
        return subjectRepository.save(s);
    }

    @GetMapping("/teacher-assignments")
    @PreAuthorize("hasAnyRole('ADMIN','TEACHER')")
    public List<TeacherAssignment> assignments(@RequestParam(required = false) UUID teacherId) {
        if (teacherId != null) {
            return teacherAssignmentRepository.findByTeacherId(teacherId);
        }
        return teacherAssignmentRepository.findAll();
    }

    @PostMapping("/teacher-assignments")
    @PreAuthorize("hasAnyRole('ADMIN','SUPER_ADMIN')")
    @Transactional(readOnly = false)
    public TeacherAssignment assign(@RequestBody AssignReq req) {
        TeacherAssignment a = new TeacherAssignment();
        a.setTeacher(teacherRepository.getReferenceById(req.teacherId()));
        a.setSchoolClass(schoolClassRepository.getReferenceById(req.schoolClassId()));
        if (req.subjectId() != null) {
            a.setSubject(subjectRepository.getReferenceById(req.subjectId()));
        }
        return teacherAssignmentRepository.save(a);
    }

    @GetMapping("/timetable")
    @PreAuthorize("hasAnyRole('ADMIN','TEACHER')")
    public List<TimetableSlot> timetable(@RequestParam UUID teacherId) {
        return timetableSlotRepository.findByTeacherIdOrderByDayOfWeekAscStartTimeAsc(teacherId);
    }

    @PostMapping("/timetable")
    @PreAuthorize("hasAnyRole('ADMIN','SUPER_ADMIN')")
    @Transactional(readOnly = false)
    public TimetableSlot addSlot(@RequestBody SlotReq req) {
        TimetableSlot t = new TimetableSlot();
        t.setTeacher(teacherRepository.getReferenceById(req.teacherId()));
        t.setDayOfWeek(req.dayOfWeek());
        t.setStartTime(req.startTime());
        t.setEndTime(req.endTime());
        t.setTitle(req.title());
        if (req.schoolClassId() != null) {
            t.setSchoolClass(schoolClassRepository.getReferenceById(req.schoolClassId()));
        }
        return timetableSlotRepository.save(t);
    }

    @DeleteMapping("/timetable/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','SUPER_ADMIN')")
    @Transactional(readOnly = false)
    public void deleteSlot(@PathVariable UUID id) {
        timetableSlotRepository.deleteById(id);
    }

    @GetMapping("/exams")
    @PreAuthorize("hasAnyRole('ADMIN','TEACHER')")
    public List<Exam> exams() {
        return examRepository.findAll();
    }

    @PostMapping("/exams")
    @PreAuthorize("hasAnyRole('ADMIN','SUPER_ADMIN')")
    @Transactional(readOnly = false)
    public Exam createExam(@RequestBody ExamReq req) {
        Exam e = new Exam();
        e.setName(req.name());
        e.setExamType(req.examType());
        e.setExamDate(req.examDate());
        if (req.schoolClassId() != null) {
            e.setSchoolClass(schoolClassRepository.getReferenceById(req.schoolClassId()));
        }
        if (req.subjectId() != null) {
            e.setSubject(subjectRepository.getReferenceById(req.subjectId()));
        }
        return examRepository.save(e);
    }

    @PostMapping("/exams/{examId}/invigilators")
    @PreAuthorize("hasAnyRole('ADMIN','SUPER_ADMIN')")
    @Transactional(readOnly = false)
    public ExamInvigilation addInv(@PathVariable UUID examId, @RequestBody InvReq req) {
        ExamInvigilation inv = new ExamInvigilation();
        inv.setExam(examRepository.getReferenceById(examId));
        inv.setTeacher(teacherRepository.getReferenceById(req.teacherId()));
        return examInvigilationRepository.save(inv);
    }

    @GetMapping("/exams/{examId}/results")
    @PreAuthorize("hasAnyRole('ADMIN','TEACHER')")
    public List<ExamResult> examResults(@PathVariable UUID examId) {
        return examResultRepository.findByExamId(examId);
    }

    @PostMapping("/exam-results")
    @PreAuthorize("hasAnyRole('ADMIN','SUPER_ADMIN')")
    @Transactional(readOnly = false)
    public ExamResult addResult(@RequestBody ResultReq req) {
        ExamResult r = new ExamResult();
        r.setExam(examRepository.getReferenceById(req.examId()));
        r.setStudent(studentRepository.getReferenceById(req.studentId()));
        if (req.subjectId() != null) {
            r.setSubject(subjectRepository.getReferenceById(req.subjectId()));
        }
        r.setGrade(req.grade());
        r.setPercentage(req.percentage());
        r.setStatus(req.status());
        return examResultRepository.save(r);
    }

    public record NameReq(@NotBlank String name) {}

    public record SectionReq(@NotBlank String name, @NotNull UUID schoolClassId) {}

    public record AssignReq(@NotNull UUID teacherId, @NotNull UUID schoolClassId, UUID subjectId) {}

    public record SlotReq(
            @NotNull UUID teacherId,
            int dayOfWeek,
            @NotNull LocalTime startTime,
            @NotNull LocalTime endTime,
            @NotBlank String title,
            UUID schoolClassId) {}

    public record ExamReq(
            @NotBlank String name,
            @NotBlank String examType,
            @NotNull LocalDate examDate,
            UUID schoolClassId,
            UUID subjectId) {}

    public record InvReq(@NotNull UUID teacherId) {}

    public record ResultReq(
            @NotNull UUID examId,
            @NotNull UUID studentId,
            UUID subjectId,
            String grade,
            BigDecimal percentage,
            @NotBlank String status) {}
}
