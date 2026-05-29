package com.orderly.backend.controller;

import com.orderly.backend.dto.TableRowDtos;
import com.orderly.backend.service.TableRowService;
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
public class TableRowController {
  private final TableRowService tableRowService;

  public TableRowController(TableRowService tableRowService) {
    this.tableRowService = tableRowService;
  }

  @PostMapping("/blocks/{blockId}/table-rows")
  @ResponseStatus(HttpStatus.CREATED)
  public TableRowDtos.TableRowResponse create(
      @PathVariable Long blockId,
      @Valid @RequestBody TableRowDtos.CreateTableRowRequest request
  ) {
    return tableRowService.create(blockId, request);
  }

  @GetMapping("/blocks/{blockId}/table-rows")
  public List<TableRowDtos.TableRowResponse> list(@PathVariable Long blockId) {
    return tableRowService.listByBlock(blockId);
  }

  @PatchMapping("/table-rows/{rowId}")
  public TableRowDtos.TableRowResponse update(
      @PathVariable Long rowId,
      @RequestBody TableRowDtos.UpdateTableRowRequest request
  ) {
    return tableRowService.update(rowId, request);
  }

  @DeleteMapping("/table-rows/{rowId}")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void delete(@PathVariable Long rowId) {
    tableRowService.delete(rowId);
  }
}
