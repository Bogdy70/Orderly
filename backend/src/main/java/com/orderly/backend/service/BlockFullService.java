package com.orderly.backend.service;

import com.orderly.backend.dto.BlockDtos;
import com.orderly.backend.entity.BlockEntity;
import com.orderly.backend.exception.ApiException;
import com.orderly.backend.repository.BlockRepository;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class BlockFullService {
  private final BlockRepository blockRepository;
  private final BlockContentService blockContentService;

  public BlockFullService(BlockRepository blockRepository, BlockContentService blockContentService) {
    this.blockRepository = blockRepository;
    this.blockContentService = blockContentService;
  }

  public List<BlockDtos.BlockFullResponse> listFullBySpace(Long spaceId) {
    return blockRepository.findBySpaceIdOrderByPositionAscIdAsc(spaceId)
        .stream()
        .map(this::toFullResponse)
        .toList();
  }

  public BlockDtos.BlockFullResponse getFull(Long blockId) {
    return toFullResponse(blockRepository.findById(blockId)
        .orElseThrow(() -> ApiException.notFound("Block not found.")));
  }

  private BlockDtos.BlockFullResponse toFullResponse(BlockEntity block) {
    return new BlockDtos.BlockFullResponse(
        DtoMapper.toBlock(block),
        blockContentService.contentFor(block)
    );
  }
}
