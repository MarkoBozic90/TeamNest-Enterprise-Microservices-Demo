package com.teamnest.userservice.service;


import com.teamnest.userservice.api.request.UserCreateDto;
import com.teamnest.userservice.api.request.UserUpdateDto;
import com.teamnest.userservice.api.respnse.UserMenuResponseDto;
import com.teamnest.userservice.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public interface UserService {

    @Transactional
    User save(User entity);
    User findById(UUID id);
    User getReferenceById(UUID userID);
    List<User> findByIds(List<UUID> ids);
    User findByUsername(String username);
    void deleteById(UUID id);
    Page<User> findAll(Pageable pageable);
    List<User> findAll();
    User update(User updateDto, Long id);
    List<User> findByCreatedAt(LocalDateTime createdAt, boolean before);
    UserMenuResponseDto findInformationForMenu(long id);

}
