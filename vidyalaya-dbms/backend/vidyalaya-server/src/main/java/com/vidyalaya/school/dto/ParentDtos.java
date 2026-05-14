package com.vidyalaya.school.dto;

import jakarta.validation.constraints.NotBlank;
import java.util.UUID;

public final class ParentDtos {

    private ParentDtos() {}

    public record ParentResponse(
            UUID id,
            String fullName,
            String email,
            String phone,
            String address,
            String occupation,
            String employer,
            String educationSummary,
            String photoUrl,
            String socialLinks,
            int childrenCount) {}

    public record ParentUpsertRequest(
            @NotBlank String fullName,
            String email,
            String phone,
            String address,
            String occupation,
            String employer,
            String educationSummary,
            String photoUrl,
            String socialLinks) {}

    public record CommunityRequest(@NotBlank String name) {}
}
