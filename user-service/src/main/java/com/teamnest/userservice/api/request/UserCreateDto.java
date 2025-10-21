package com.teamnest.userservice.api.request;

import com.teamnest.userservice.api.respnse.AttributesResponseDto;
import com.teamnest.userservice.api.respnse.PersonalInformationResponseDto;
import java.util.UUID;

public record UserCreateDto
    (
    UUID userId,
    String username,
    String email,
    String inviteToken,
    AttributesResponseDto attributes,
    PersonalInformationResponseDto personalInformation
){
}
