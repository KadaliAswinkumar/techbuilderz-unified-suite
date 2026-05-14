package com.vidyalaya.school;

import com.vidyalaya.school.dto.TeacherDtos.TeacherResponse;
import com.vidyalaya.school.dto.TeacherDtos.TeacherUpsertRequest;
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
@RequestMapping("/api/teachers")
public class TeacherController {

    private final TeacherService teacherService;

    public TeacherController(TeacherService teacherService) {
        this.teacherService = teacherService;
    }

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public List<TeacherResponse> list() {
        return teacherService.list();
    }

    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public TeacherResponse get(@PathVariable UUID id) {
        return teacherService.get(id);
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN','SUPER_ADMIN')")
    public TeacherResponse create(@Valid @RequestBody TeacherUpsertRequest req) {
        return teacherService.create(req);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','SUPER_ADMIN')")
    public TeacherResponse update(@PathVariable UUID id, @Valid @RequestBody TeacherUpsertRequest req) {
        return teacherService.update(id, req);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','SUPER_ADMIN')")
    public void delete(@PathVariable UUID id) {
        teacherService.delete(id);
    }

    @GetMapping("/{id}/assignments")
    @PreAuthorize("isAuthenticated()")
    public Object assignments(@PathVariable UUID id) {
        return teacherService.assignments(id);
    }

    @GetMapping("/{id}/salary")
    @PreAuthorize("isAuthenticated()")
    public Object salary(@PathVariable UUID id) {
        return teacherService.salary(id);
    }

    @GetMapping("/{id}/invigilations")
    @PreAuthorize("isAuthenticated()")
    public Object invigilations(@PathVariable UUID id) {
        return teacherService.invigilations(id);
    }

    @GetMapping("/{id}/timetable")
    @PreAuthorize("isAuthenticated()")
    public Object timetable(@PathVariable UUID id) {
        return teacherService.timetable(id);
    }
}
