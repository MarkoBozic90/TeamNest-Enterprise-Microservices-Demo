package com.teamnest.userservice.service.impl;


import com.teamnest.userservice.model.PersonalInformation;
import com.teamnest.userservice.repository.PersonalInformationRepository;
import com.teamnest.userservice.service.PersonalInformationService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PersonalInformationServiceImpl implements PersonalInformationService {

    private final PersonalInformationRepository personalInformationRepository;


    @Override
    public PersonalInformation save(final PersonalInformation dto) {
        if (dto.getId() == null) {
            throw new IllegalArgumentException("userId must be provided.");
        }
      return personalInformationRepository.save(dto);
    }

    @Override
    public PersonalInformation findById(final UUID id) {
       return personalInformationRepository.findById(id)
           .orElseThrow(() -> new EntityNotFoundException("id is not found"));
    }

    @Override
    public PersonalInformation update(final PersonalInformation dto, final UUID userDto) {
       return personalInformationRepository.saveAndFlush(dto);

    }

    @Override
    public void deleteById(final UUID id) {
        personalInformationRepository.deleteById(id);
    }
}
