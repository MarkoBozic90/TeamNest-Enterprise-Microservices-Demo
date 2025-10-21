package com.teamnest.userservice.api.respnse;

import java.util.UUID;

public record PersonalInformationResponseDto (
    UUID id, // Same as User ID
    UUID userID,
    String firstName,
    String lastName,
    String phoneNumber,
    String address,
    String city,
    String country,
    String postalCode,
    String imgUrl,
    String dateOfBirth,
    String contractImgUrl
){
}
