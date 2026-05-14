package com.vidyalaya.school.dto;

import jakarta.validation.constraints.NotBlank;
import java.time.LocalDate;
import java.util.UUID;

public final class StudentDtos {

    private StudentDtos() {}

    public record StudentResponse(
            UUID id,
            String fullName,
            String firstName,
            String middleName,
            String lastName,
            String email,
            String phone,
            String gender,
            String fatherName,
            String motherName,
            String fatherOccupation,
            String motherOccupation,
            LocalDate dateOfBirth,
            String religion,
            String caste,
            String address,
            String className,
            String section,
            LocalDate admissionDate,
            String photoUrl,
            String socialLinks,
            String aboutStudent) {}

    public record StudentUpsertRequest(
            @NotBlank String fullName,
            String email,
            String phone,
            String gender,
            String fatherName,
            String motherName,
            String fatherOccupation,
            String motherOccupation,
            LocalDate dateOfBirth,
            String religion,
            String caste,
            String address,
            String className,
            String section,
            LocalDate admissionDate,
            String photoUrl,
            String socialLinks,
            String firstName,
            String middleName,
            String lastName,
            String aboutStudent) {}
}
