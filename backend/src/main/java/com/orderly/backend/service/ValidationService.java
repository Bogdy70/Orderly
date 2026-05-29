package com.orderly.backend.service;

import com.orderly.backend.entity.BlockEntity;
import com.orderly.backend.entity.BlockType;
import com.orderly.backend.exception.ApiException;
import java.time.LocalDate;
import java.util.Set;
import org.springframework.stereotype.Component;

@Component
public class ValidationService {
  private static final Set<String> TABLE_STATUSES = Set.of("todo", "pending", "done");

  public void requireBlockType(BlockEntity block, BlockType type, String contentName) {
    if (block.getType() != type) {
      throw ApiException.badRequest(contentName + " can only be used with " + type.value() + " blocks.");
    }
  }

  public String normalizeStatus(String status) {
    String normalized = status == null || status.isBlank() ? "todo" : status.trim().toLowerCase();
    if (!TABLE_STATUSES.contains(normalized)) {
      throw ApiException.badRequest("Table row status must be one of: todo, pending, done.");
    }
    return normalized;
  }

  public void requireTodayOrFutureDueDate(LocalDate dueDate) {
    if (dueDate != null && dueDate.isBefore(LocalDate.now())) {
      throw ApiException.badRequest("Due date cannot be earlier than today.");
    }
  }

  public void requirePositiveDimensions(Double width, Double height) {
    if (width == null || height == null || width <= 0 || height <= 0) {
      throw ApiException.badRequest("Diagram node width and height must be positive numbers.");
    }
  }
}
