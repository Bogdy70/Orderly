package com.orderly.backend.service;

import com.orderly.backend.dto.UserDtos;
import com.orderly.backend.entity.UserEntity;
import com.orderly.backend.exception.ApiException;
import com.orderly.backend.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class UserService {
  private final UserRepository userRepository;

  public UserService(UserRepository userRepository) {
    this.userRepository = userRepository;
  }

  public UserDtos.UserResponse create(UserDtos.CreateUserRequest request) {
    UserEntity user = new UserEntity();
    user.setEmail(request.email());
    user.setUsername(request.username());
    user.setPasswordHash(request.passwordHash());
    user.setAuthProviderId(request.authProviderId());
    return DtoMapper.toUser(userRepository.save(user));
  }

  @Transactional(readOnly = true)
  public UserDtos.UserResponse get(Long id) {
    return DtoMapper.toUser(getEntity(id));
  }

  public UserDtos.UserResponse update(Long id, UserDtos.UpdateUserRequest request) {
    UserEntity user = getEntity(id);
    if (request.email() != null) user.setEmail(request.email());
    if (request.username() != null) user.setUsername(request.username());
    if (request.passwordHash() != null) user.setPasswordHash(request.passwordHash());
    if (request.authProviderId() != null) user.setAuthProviderId(request.authProviderId());
    return DtoMapper.toUser(user);
  }

  public void delete(Long id) {
    UserEntity user = getEntity(id);
    userRepository.delete(user);
  }

  UserEntity getEntity(Long id) {
    return userRepository.findById(id)
        .orElseThrow(() -> ApiException.notFound("User not found."));
  }
}
