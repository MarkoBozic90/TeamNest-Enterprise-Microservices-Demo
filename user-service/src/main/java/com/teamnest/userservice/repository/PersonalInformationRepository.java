package com.teamnest.userservice.repository;

import com.teamnest.userservice.model.PersonalInformation;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;



public interface PersonalInformationRepository extends JpaRepository<PersonalInformation, UUID> {
}
