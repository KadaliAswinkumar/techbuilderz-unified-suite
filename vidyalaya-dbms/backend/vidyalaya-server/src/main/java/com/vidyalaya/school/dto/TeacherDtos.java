package com.vidyalaya.school.dto;

import jakarta.validation.constraints.NotBlank;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public final class TeacherDtos {

    private TeacherDtos() {}

    public record TeacherResponse(
            UUID id,
            String fullName,
            String email,
            String phone,
            String gender,
            LocalDate dateOfBirth,
            String address,
            String qualification,
            String experienceSummary,
            LocalDate joiningDate,
            BigDecimal salaryAmount,
            String photoUrl,
            String socialLinks) {}

    public record TeacherUpsertRequest(
            @NotBlank String fullName,
            String email,
            String phone,
            String gender,
            LocalDate dateOfBirth,
            String address,
            String qualification,
            String experienceSummary,
            LocalDate joiningDate,
            BigDecimal salaryAmount,
            String photoUrl,
            String socialLinks) {}
}
