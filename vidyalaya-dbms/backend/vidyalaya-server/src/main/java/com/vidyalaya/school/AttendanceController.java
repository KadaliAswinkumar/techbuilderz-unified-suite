package com.vidyalaya.school;

import com.vidyalaya.domain.AttendanceRecord;
import com.vidyalaya.domain.repository.AttendanceRecordRepository;
import com.vidyalaya.domain.repository.StudentRepository;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import java.util.UUID;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/attendance")
public class AttendanceController {

    private final AttendanceRecordRepository attendanceRecordRepository;
    private final StudentRepository studentRepository;

    public AttendanceController(
            AttendanceRecordRepository attendanceRecordRepository, StudentRepository studentRepository) {
        this.attendanceRecordRepository = attendanceRecordRepository;
        this.studentRepository = studentRepository;
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN','TEACHER')")
    @Transactional
    public AttendanceRecord mark(@RequestBody AttendanceReq req) {
        AttendanceRecord a = new AttendanceRecord();
        a.setStudent(studentRepository.getReferenceById(req.studentId()));
        a.setRecordDate(req.recordDate());
        a.setStatus(req.status());
        return attendanceRecordRepository.save(a);
    }

    public record AttendanceReq(
            @NotNull UUID studentId, @NotNull LocalDate recordDate, @NotBlank String status) {}
}
