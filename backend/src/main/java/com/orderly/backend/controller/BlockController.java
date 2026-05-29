package com.orderly.backend.controller;

import com.orderly.backend.dto.BlockDtos;
import com.orderly.backend.entity.BlockType;
import com.orderly.backend.service.BlockConversionService;
import com.orderly.backend.service.BlockFullService;
import com.orderly.backend.service.BlockService;
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
public class BlockController {
  private final BlockService blockService;
  private final BlockFullService blockFullService;
  private final BlockConversionService blockConversionService;

  public BlockController(
      BlockService blockService,
      BlockFullService blockFullService,
      BlockConversionService blockConversionService
  ) {
    this.blockService = blockService;
    this.blockFullService = blockFullService;
    this.blockConversionService = blockConversionService;
  }

  @PostMapping("/spaces/{spaceId}/blocks")
  @ResponseStatus(HttpStatus.CREATED)
  public BlockDtos.BlockResponse create(@PathVariable Long spaceId, @Valid @RequestBody BlockDtos.CreateBlockRequest request) {
    return blockService.create(spaceId, request);
  }

  @GetMapping("/spaces/{spaceId}/blocks")
  public List<BlockDtos.BlockResponse> list(@PathVariable Long spaceId) {
    return blockService.listBySpace(spaceId);
  }

  @GetMapping("/blocks/{blockId}")
  public BlockDtos.BlockResponse get(@PathVariable Long blockId) {
    return blockService.get(blockId);
  }

  @GetMapping("/blocks/{blockId}/full")
  public BlockDtos.BlockFullResponse getFull(@PathVariable Long blockId) {
    return blockFullService.getFull(blockId);
  }

  @PatchMapping("/blocks/{blockId}")
  public BlockDtos.BlockResponse update(@PathVariable Long blockId, @RequestBody BlockDtos.UpdateBlockRequest request) {
    return blockService.update(blockId, request);
  }

  @DeleteMapping("/blocks/{blockId}")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void delete(@PathVariable Long blockId) {
    blockService.delete(blockId);
  }

  @PostMapping("/blocks/{blockId}/convert/checklist")
  @ResponseStatus(HttpStatus.CREATED)
  public BlockDtos.BlockFullResponse convertToChecklist(@PathVariable Long blockId) {
    return blockConversionService.convert(blockId, BlockType.CHECKLIST);
  }

  @PostMapping("/blocks/{blockId}/convert/table")
  @ResponseStatus(HttpStatus.CREATED)
  public BlockDtos.BlockFullResponse convertToTable(@PathVariable Long blockId) {
    return blockConversionService.convert(blockId, BlockType.TABLE);
  }

  @PostMapping("/blocks/{blockId}/convert/diagram")
  @ResponseStatus(HttpStatus.CREATED)
  public BlockDtos.BlockFullResponse convertToDiagram(@PathVariable Long blockId) {
    return blockConversionService.convert(blockId, BlockType.DIAGRAM);
  }
}
