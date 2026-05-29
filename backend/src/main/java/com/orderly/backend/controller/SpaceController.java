package com.orderly.backend.controller;

import com.orderly.backend.dto.SpaceDtos;
import com.orderly.backend.service.SpaceService;
import com.orderly.backend.service.UserService;
import jakarta.validation.Valid;
import java.util.List;
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
@RequestMapping("/api/spaces")
public class SpaceController {
  private final SpaceService spaceService;
  private final UserService userService;

  public SpaceController(SpaceService spaceService, UserService userService) {
    this.spaceService = spaceService;
    this.userService = userService;
  }

  @PostMapping
  @ResponseStatus(HttpStatus.CREATED)
  public SpaceDtos.SpaceResponse create(@Valid @RequestBody SpaceDtos.CreateSpaceRequest request) {
    return spaceService.create(request, userService.getCurrentUserEntity());
  }

  @GetMapping
  public List<SpaceDtos.SpaceResponse> list() {
    return spaceService.list(userService.getCurrentUserEntity());
  }

  @GetMapping("/{id}")
  public SpaceDtos.SpaceResponse get(@PathVariable Long id) {
    return spaceService.get(id);
  }

  @GetMapping("/{spaceId}/full")
  public SpaceDtos.SpaceFullResponse getFull(@PathVariable Long spaceId) {
    return spaceService.getFull(spaceId);
  }

  @PatchMapping("/{id}")
  public SpaceDtos.SpaceResponse update(@PathVariable Long id, @RequestBody SpaceDtos.UpdateSpaceRequest request) {
    return spaceService.update(id, request);
  }

  @DeleteMapping("/{id}")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void delete(@PathVariable Long id) {
    spaceService.delete(id);
  }
}
