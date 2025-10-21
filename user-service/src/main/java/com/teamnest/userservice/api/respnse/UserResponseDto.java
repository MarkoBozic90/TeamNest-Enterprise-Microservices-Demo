package com.teamnest.userservice.api.respnse;


import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;
import java.util.UUID;
@Schema()
public record UserResponseDto(
    UUID userId,
    String username,
    String email,
    LocalDateTime createdAt,
    LocalDateTime lastModifiedAt,
    String createdBy,
    String lastModifiedBy,
    String inviteToken,
    AttributesResponseDto attributes,
    PersonalInformationResponseDto personalInformation
) { }