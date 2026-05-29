package com.orderly.backend.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.orderly.backend.entity.BlockEntity;
import com.orderly.backend.entity.BlockType;
import com.orderly.backend.exception.ApiException;
import java.time.LocalDate;
import org.junit.jupiter.api.Test;

class ValidationServiceTest {
  private final ValidationService validationService = new ValidationService();

  @Test
  void normalizesAllowedTableStatus() {
    assertThat(validationService.normalizeStatus(" DONE ")).isEqualTo("done");
    assertThat(validationService.normalizeStatus(null)).isEqualTo("todo");
  }

  @Test
  void rejectsUnsupportedTableStatus() {
    assertThatThrownBy(() -> validationService.normalizeStatus("blocked"))
        .isInstanceOf(ApiException.class)
        .hasMessageContaining("todo, pending, done");
  }

  @Test
  void rejectsPastDueDate() {
    assertThatThrownBy(() -> validationService.requireTodayOrFutureDueDate(LocalDate.now().minusDays(1)))
        .isInstanceOf(ApiException.class)
        .hasMessageContaining("Due date cannot be earlier than today");
  }

  @Test
  void allowsTodayAndFutureDueDates() {
    validationService.requireTodayOrFutureDueDate(LocalDate.now());
    validationService.requireTodayOrFutureDueDate(LocalDate.now().plusDays(1));
    validationService.requireTodayOrFutureDueDate(null);
  }

  @Test
  void rejectsContentForWrongBlockType() {
    BlockEntity block = new BlockEntity();
    block.setType(BlockType.TABLE);

    assertThatThrownBy(() -> validationService.requireBlockType(block, BlockType.CHECKLIST, "Checklist items"))
        .isInstanceOf(ApiException.class)
        .hasMessageContaining("Checklist items can only be used with checklist blocks");
  }

  @Test
  void rejectsInvalidNodeDimensions() {
    assertThatThrownBy(() -> validationService.requirePositiveDimensions(0.0, 80.0))
        .isInstanceOf(ApiException.class)
        .hasMessageContaining("positive numbers");
  }
}
