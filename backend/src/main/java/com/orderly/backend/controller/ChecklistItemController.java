package com.orderly.backend.controller;

import com.orderly.backend.dto.ChecklistItemDtos;
import com.orderly.backend.service.ChecklistItemService;
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
@RequestMapping("/api")
public class ChecklistItemController {
  private final ChecklistItemService checklistItemService;

  public ChecklistItemController(ChecklistItemService checklistItemService) {
    this.checklistItemService = checklistItemService;
  }

  @PostMapping("/blocks/{blockId}/checklist-items")
  @ResponseStatus(HttpStatus.CREATED)
  public ChecklistItemDtos.ChecklistItemResponse create(
      @PathVariable Long blockId,
      @Valid @RequestBody ChecklistItemDtos.CreateChecklistItemRequest request
  ) {
    return checklistItemService.create(blockId, request);
  }

  @GetMapping("/blocks/{blockId}/checklist-items")
  public List<ChecklistItemDtos.ChecklistItemResponse> list(@PathVariable Long blockId) {
    return checklistItemService.listByBlock(blockId);
  }

  @PatchMapping("/checklist-items/{itemId}")
  public ChecklistItemDtos.ChecklistItemResponse update(
      @PathVariable Long itemId,
      @RequestBody ChecklistItemDtos.UpdateChecklistItemRequest request
  ) {
    return checklistItemService.update(itemId, request);
  }

  @DeleteMapping("/checklist-items/{itemId}")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void delete(@PathVariable Long itemId) {
    checklistItemService.delete(itemId);
  }
}
