package com.orderly.backend.service;

import com.orderly.backend.dto.BlockDtos;
import com.orderly.backend.entity.BlockEntity;
import com.orderly.backend.entity.BlockType;
import com.orderly.backend.exception.ApiException;
import com.orderly.backend.repository.BlockRepository;
import com.orderly.backend.repository.SpaceRepository;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class BlockService {
  private final BlockRepository blockRepository;
  private final SpaceRepository spaceRepository;

  public BlockService(BlockRepository blockRepository, SpaceRepository spaceRepository) {
    this.blockRepository = blockRepository;
    this.spaceRepository = spaceRepository;
  }

  public BlockDtos.BlockResponse create(Long spaceId, BlockDtos.CreateBlockRequest request) {
    BlockEntity block = new BlockEntity();
    block.setSpace(spaceRepository.findById(spaceId).orElseThrow(() -> ApiException.notFound("Space not found.")));
    block.setType(BlockType.fromValue(request.type()));
    block.setTitle(request.title());
    block.setPosition(request.position() == null ? 0 : request.position());
    return DtoMapper.toBlock(blockRepository.save(block));
  }

  BlockEntity createEntity(Long spaceId, BlockType type, String title, Integer position) {
    BlockEntity block = new BlockEntity();
    block.setSpace(spaceRepository.findById(spaceId).orElseThrow(() -> ApiException.notFound("Space not found.")));
    block.setType(type);
    block.setTitle(title);
    block.setPosition(position == null ? 0 : position);
    return blockRepository.save(block);
  }

  @Transactional(readOnly = true)
  public List<BlockDtos.BlockResponse> listBySpace(Long spaceId) {
    return blockRepository.findBySpaceIdOrderByPositionAscIdAsc(spaceId)
        .stream()
        .map(DtoMapper::toBlock)
        .toList();
  }

  @Transactional(readOnly = true)
  public BlockDtos.BlockResponse get(Long id) {
    return DtoMapper.toBlock(getEntity(id));
  }

  public BlockDtos.BlockResponse update(Long id, BlockDtos.UpdateBlockRequest request) {
    BlockEntity block = getEntity(id);
    if (request.title() != null) block.setTitle(request.title());
    if (request.position() != null) block.setPosition(request.position());
    return DtoMapper.toBlock(block);
  }

  public void delete(Long id) {
    blockRepository.delete(getEntity(id));
  }

  BlockEntity getEntity(Long id) {
    return blockRepository.findById(id)
        .orElseThrow(() -> ApiException.notFound("Block not found."));
  }

}
