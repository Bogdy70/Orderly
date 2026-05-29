package com.orderly.backend.service;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.orderly.backend.dto.ChecklistItemDtos;
import com.orderly.backend.entity.BlockEntity;
import com.orderly.backend.entity.BlockType;
import com.orderly.backend.exception.ApiException;
import com.orderly.backend.repository.ChecklistItemRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ChecklistItemServiceTest {
  @Mock
  private ChecklistItemRepository checklistItemRepository;

  @Mock
  private BlockService blockService;

  @Mock
  private ValidationService validationService;

  @InjectMocks
  private ChecklistItemService checklistItemService;

  @Test
  void doesNotSaveChecklistItemWhenBlockIsWrongType() {
    BlockEntity block = new BlockEntity();
    block.setType(BlockType.TABLE);
    when(blockService.getEntity(12L)).thenReturn(block);
    org.mockito.Mockito.doThrow(ApiException.badRequest("Checklist items can only be used with checklist blocks."))
        .when(validationService)
        .requireBlockType(block, BlockType.CHECKLIST, "Checklist items");

    assertThatThrownBy(() -> checklistItemService.create(
        12L,
        new ChecklistItemDtos.CreateChecklistItemRequest("Not allowed", false, 1)
    )).isInstanceOf(ApiException.class);

    verify(checklistItemRepository, never()).save(org.mockito.ArgumentMatchers.any());
  }
}
