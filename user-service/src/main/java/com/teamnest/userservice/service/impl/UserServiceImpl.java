package com.teamnest.userservice.service.impl;

import com.teamnest.userservice.api.respnse.UserMenuResponseDto;
import com.teamnest.userservice.model.User;
import com.teamnest.userservice.repository.UserRepository;
import com.teamnest.userservice.service.UserService;
import jakarta.persistence.EntityNotFoundException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import lombok.Builder;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;




@Service
@RequiredArgsConstructor
@Slf4j
@Builder
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;


    @Override
    @Transactional
    public User save(final User entity) {
        return userRepository.save(entity);
    }


    @Override
    public User findById(final UUID id) {
        if (id == null) {
            throw new IllegalArgumentException("The given id must not be null");
        }
        return userRepository.findById(id).orElseThrow(() -> new EntityNotFoundException("User not found with id: " + id));
    }
    @Override
    public User getReferenceById(UUID userID){
        return userRepository.getReferenceById(userID);
    }

    @Override
    public List<User> findByIds(final List<UUID> ids) {

        return userRepository.findAllById(ids);
    }

    @Override
    public User findByUsername(final String username) {
        return userRepository.findByUsername(username).orElseThrow(() -> new EntityNotFoundException("User not found with username: " + username));
    }

    @Override
    public List<User> findAll() {
        return userRepository.findAll();
    }

    @Override
    public Page<User> findAll(final Pageable pageable) {
        return userRepository.findAll(pageable);
    }

    @Override
    public User update(final User entity, final Long id) {
        return null;
    }

    @Override
    public List<User> findByCreatedAt(final LocalDateTime createdAt, final boolean before) {
        return userRepository.findUsersByCreatedAt(createdAt, before);
    }

    @Override
    public UserMenuResponseDto findInformationForMenu(final long id) {
        return null;
    }

    

    @Override
    public void deleteById(final UUID id) {
        userRepository.deleteById(id);
    }



}
