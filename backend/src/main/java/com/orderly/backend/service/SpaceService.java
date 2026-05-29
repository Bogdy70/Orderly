package com.orderly.backend.service;

import com.orderly.backend.dto.SpaceDtos;
import com.orderly.backend.entity.SpaceEntity;
import com.orderly.backend.repository.SpaceRepository;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class SpaceService {
  private final SpaceRepository spaceRepository;
  private final UserService userService;
  private final BlockFullService blockFullService;

  public SpaceService(SpaceRepository spaceRepository, UserService userService, BlockFullService blockFullService) {
    this.spaceRepository = spaceRepository;
    this.userService = userService;
    this.blockFullService = blockFullService;
  }

  public SpaceDtos.SpaceResponse create(SpaceDtos.CreateSpaceRequest request) {
    SpaceEntity space = new SpaceEntity();
    space.setOwner(userService.getEntity(request.ownerId()));
    space.setName(request.name());
    space.setDescription(request.description());
    space.setIcon(request.icon());
    space.setColor(request.color());
    return DtoMapper.toSpace(spaceRepository.save(space));
  }

  @Transactional(readOnly = true)
  public List<SpaceDtos.SpaceResponse> list(Long ownerId) {
    List<SpaceEntity> spaces = ownerId == null
        ? spaceRepository.findAll()
        : spaceRepository.findByOwnerIdOrderByCreatedAtDesc(ownerId);
    return spaces.stream().map(DtoMapper::toSpace).toList();
  }

  @Transactional(readOnly = true)
  public SpaceDtos.SpaceResponse get(Long id) {
    return DtoMapper.toSpace(getEntity(id));
  }

  @Transactional(readOnly = true)
  public SpaceDtos.SpaceFullResponse getFull(Long id) {
    SpaceEntity space = getEntity(id);
    return new SpaceDtos.SpaceFullResponse(
        DtoMapper.toSpace(space),
        blockFullService.listFullBySpace(id)
    );
  }

  public SpaceDtos.SpaceResponse update(Long id, SpaceDtos.UpdateSpaceRequest request) {
    SpaceEntity space = getEntity(id);
    if (request.ownerId() != null) space.setOwner(userService.getEntity(request.ownerId()));
    if (request.name() != null) space.setName(request.name());
    if (request.description() != null) space.setDescription(request.description());
    if (request.icon() != null) space.setIcon(request.icon());
    if (request.color() != null) space.setColor(request.color());
    return DtoMapper.toSpace(space);
  }

  public void delete(Long id) {
    spaceRepository.delete(getEntity(id));
  }

  SpaceEntity getEntity(Long id) {
    return spaceRepository.findById(id)
        .orElseThrow(() -> com.orderly.backend.exception.ApiException.notFound("Space not found."));
  }
}
