package com.vidyalaya.school;

import com.vidyalaya.school.dto.StudentDtos.StudentResponse;
import com.vidyalaya.school.dto.StudentDtos.StudentUpsertRequest;
import jakarta.validation.Valid;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.springframework.http.MediaType;
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
@RequestMapping("/api/students")
public class StudentController {

    private final StudentService studentService;

    public StudentController(StudentService studentService) {
        this.studentService = studentService;
    }

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public List<StudentResponse> list() {
        return studentService.list();
    }

    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public StudentResponse get(@PathVariable UUID id) {
        return studentService.get(id);
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN','SUPER_ADMIN')")
    public StudentResponse create(@Valid @RequestBody StudentUpsertRequest req) {
        return studentService.create(req);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','SUPER_ADMIN')")
    public StudentResponse update(@PathVariable UUID id, @Valid @RequestBody StudentUpsertRequest req) {
        return studentService.update(id, req);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','SUPER_ADMIN')")
    public void delete(@PathVariable UUID id) {
        studentService.delete(id);
    }

    @GetMapping("/{id}/academic-record")
    @PreAuthorize("isAuthenticated()")
    public Object academic(@PathVariable UUID id) {
        return studentService.academicRecord(id);
    }

    @GetMapping("/{id}/attendance")
    @PreAuthorize("isAuthenticated()")
    public Object attendance(@PathVariable UUID id) {
        return studentService.attendance(id);
    }

    @PostMapping(value = "/import", consumes = MediaType.TEXT_PLAIN_VALUE)
    @PreAuthorize("hasAnyRole('ADMIN','SUPER_ADMIN')")
    public Map<String, Integer> importCsv(@RequestBody String csv) {
        return Map.of("imported", studentService.importCsv(csv));
    }
}
