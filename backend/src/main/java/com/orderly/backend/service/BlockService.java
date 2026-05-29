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
    applyLayout(block, request.x(), request.y(), request.width(), request.height());
    return DtoMapper.toBlock(blockRepository.save(block));
  }

  BlockEntity createEntity(Long spaceId, BlockType type, String title, Integer position) {
    BlockEntity block = new BlockEntity();
    block.setSpace(spaceRepository.findById(spaceId).orElseThrow(() -> ApiException.notFound("Space not found.")));
    block.setType(type);
    block.setTitle(title);
    block.setPosition(position == null ? 0 : position);
    applyDefaultLayout(block, type);
    return blockRepository.save(block);
  }

  void shiftPositionsAfter(Long spaceId, Integer position) {
    int sourcePosition = position == null ? 0 : position;
    blockRepository.findBySpaceIdOrderByPositionAscIdAsc(spaceId)
        .stream()
        .filter(block -> (block.getPosition() == null ? 0 : block.getPosition()) > sourcePosition)
        .forEach(block -> block.setPosition((block.getPosition() == null ? 0 : block.getPosition()) + 1));
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
    if (request.x() != null) block.setX(request.x());
    if (request.y() != null) block.setY(request.y());
    if (request.width() != null) block.setWidth(request.width());
    if (request.height() != null) block.setHeight(request.height());
    return DtoMapper.toBlock(block);
  }

  public void delete(Long id) {
    blockRepository.delete(getEntity(id));
  }

  BlockEntity getEntity(Long id) {
    return blockRepository.findById(id)
        .orElseThrow(() -> ApiException.notFound("Block not found."));
  }

  private void applyLayout(BlockEntity block, Double x, Double y, Double width, Double height) {
    if (x != null) block.setX(x);
    if (y != null) block.setY(y);
    if (width != null) block.setWidth(width);
    if (height != null) block.setHeight(height);
    applyDefaultLayout(block, block.getType());
  }

  private void applyDefaultLayout(BlockEntity block, BlockType type) {
    int index = Math.max((block.getPosition() == null ? 1 : block.getPosition()) - 1, 0);
    if (block.getX() == null) block.setX(32.0 + (index % 2) * 680.0);
    if (block.getY() == null) block.setY(32.0 + Math.floor(index / 2.0) * 460.0);
    if (block.getWidth() == null || block.getWidth() <= 0) {
      block.setWidth(type == BlockType.CHECKLIST ? 560.0 : type == BlockType.TABLE ? 820.0 : 780.0);
    }
    if (block.getHeight() == null || block.getHeight() <= 0) block.setHeight(type == BlockType.DIAGRAM ? 620.0 : 360.0);
  }
}
