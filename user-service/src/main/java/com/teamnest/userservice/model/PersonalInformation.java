package com.teamnest.userservice.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.MapsId;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import java.util.UUID;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@NoArgsConstructor()
@Getter
@Builder(toBuilder = true)
@Table(name = "personal_information")
public class PersonalInformation {

    @Id
    private UUID id;
    @OneToOne
    @MapsId
    @JoinColumn(name = "user_id")
    private User user;
    private String firstName;
    private String lastName;
    private String email;
    private String phoneNumber;
    private String address;
    private String city;
    private String country;
    private String postalCode;
    private String imgUrl;
    private String dateOfBirth;
    private String contractImgUrl;
}
