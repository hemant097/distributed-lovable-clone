package com.example.distribute_lovable_clone.workspace_service.dto.member;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record ApproveInviteRequest(

        @NotBlank @Email
        String inviterEmail,
        @Pattern(regexp = "^(YES|NO)$", message = "Decision can be YES or NO in CAPS only")
        String decision
) {
}
