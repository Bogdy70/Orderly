package com.orderly.backend.service;

import com.orderly.backend.dto.ChecklistItemDtos;
import com.orderly.backend.entity.BlockEntity;
import com.orderly.backend.entity.BlockType;
import com.orderly.backend.entity.ChecklistItemEntity;
import com.orderly.backend.exception.ApiException;
import com.orderly.backend.repository.ChecklistItemRepository;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class ChecklistItemService {
  private final ChecklistItemRepository checklistItemRepository;
  private final BlockService blockService;
  private final ValidationService validationService;

  public ChecklistItemService(
      ChecklistItemRepository checklistItemRepository,
      BlockService blockService,
      ValidationService validationService
  ) {
    this.checklistItemRepository = checklistItemRepository;
    this.blockService = blockService;
    this.validationService = validationService;
  }

  public ChecklistItemDtos.ChecklistItemResponse create(Long blockId, ChecklistItemDtos.CreateChecklistItemRequest request) {
    BlockEntity block = blockService.getEntity(blockId);
    validationService.requireBlockType(block, BlockType.CHECKLIST, "Checklist items");

    ChecklistItemEntity item = new ChecklistItemEntity();
    item.setBlock(block);
    item.setText(request.text());
    item.setDone(request.done() != null && request.done());
    item.setPosition(request.position() == null ? 0 : request.position());
    return DtoMapper.toChecklistItem(checklistItemRepository.save(item));
  }

  ChecklistItemEntity createEntity(BlockEntity block, String text, Boolean done, Integer position) {
    ChecklistItemEntity item = new ChecklistItemEntity();
    item.setBlock(block);
    item.setText(text);
    item.setDone(done != null && done);
    item.setPosition(position == null ? 0 : position);
    return checklistItemRepository.save(item);
  }

  @Transactional(readOnly = true)
  public List<ChecklistItemDtos.ChecklistItemResponse> listByBlock(Long blockId) {
    BlockEntity block = blockService.getEntity(blockId);
    validationService.requireBlockType(block, BlockType.CHECKLIST, "Checklist items");
    return checklistItemRepository.findByBlockIdOrderByPositionAscIdAsc(blockId)
        .stream()
        .map(DtoMapper::toChecklistItem)
        .toList();
  }

  public ChecklistItemDtos.ChecklistItemResponse update(Long id, ChecklistItemDtos.UpdateChecklistItemRequest request) {
    ChecklistItemEntity item = getEntity(id);
    if (request.text() != null) item.setText(request.text());
    if (request.done() != null) item.setDone(request.done());
    if (request.position() != null) item.setPosition(request.position());
    return DtoMapper.toChecklistItem(item);
  }

  public void delete(Long id) {
    checklistItemRepository.delete(getEntity(id));
  }

  List<ChecklistItemEntity> entitiesByBlock(Long blockId) {
    return checklistItemRepository.findByBlockIdOrderByPositionAscIdAsc(blockId);
  }

  private ChecklistItemEntity getEntity(Long id) {
    return checklistItemRepository.findById(id)
        .orElseThrow(() -> ApiException.notFound("Checklist item not found."));
  }
}
