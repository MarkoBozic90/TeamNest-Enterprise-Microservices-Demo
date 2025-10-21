package com.teamnest.userservice.api.request;

import java.util.UUID;

public record PersonalInformationCreateDto(
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
) {
}
