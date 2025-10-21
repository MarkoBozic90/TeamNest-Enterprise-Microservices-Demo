package com.teamnest.userservice.service;



import com.teamnest.userservice.model.PersonalInformation;
import java.util.UUID;

public interface PersonalInformationService {

    PersonalInformation save(PersonalInformation entity);
    PersonalInformation findById(UUID id);
    void deleteById(UUID id);
    PersonalInformation update(PersonalInformation entity, UUID userID);
}
