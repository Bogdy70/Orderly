package com.orderly.backend.controller;

import com.orderly.backend.dto.UserDtos;
import com.orderly.backend.service.UserService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/users")
public class UserController {
  private final UserService userService;

  public UserController(UserService userService) {
    this.userService = userService;
  }

  @PostMapping
  @ResponseStatus(HttpStatus.CREATED)
  public UserDtos.UserResponse create(@Valid @RequestBody UserDtos.CreateUserRequest request) {
    return userService.create(request);
  }

  @GetMapping("/{id}")
  public UserDtos.UserResponse get(@PathVariable Long id) {
    return userService.get(id);
  }

  @PatchMapping("/{id}")
  public UserDtos.UserResponse update(@PathVariable Long id, @RequestBody UserDtos.UpdateUserRequest request) {
    return userService.update(id, request);
  }

  @DeleteMapping("/{id}")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void delete(@PathVariable Long id) {
    userService.delete(id);
  }
}
