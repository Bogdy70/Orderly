package com.orderly.backend.service;

import com.orderly.backend.dto.TableRowDtos;
import com.orderly.backend.entity.BlockEntity;
import com.orderly.backend.entity.BlockType;
import com.orderly.backend.entity.TableRowEntity;
import com.orderly.backend.exception.ApiException;
import com.orderly.backend.repository.TableRowRepository;
import java.time.LocalDate;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class TableRowService {
  private final TableRowRepository tableRowRepository;
  private final BlockService blockService;
  private final ValidationService validationService;

  public TableRowService(TableRowRepository tableRowRepository, BlockService blockService, ValidationService validationService) {
    this.tableRowRepository = tableRowRepository;
    this.blockService = blockService;
    this.validationService = validationService;
  }

  public TableRowDtos.TableRowResponse create(Long blockId, TableRowDtos.CreateTableRowRequest request) {
    BlockEntity block = blockService.getEntity(blockId);
    validationService.requireBlockType(block, BlockType.TABLE, "Table rows");

    TableRowEntity row = createEntity(block, request.title(), request.status(), request.priority(), request.dueDate(), request.position());
    return DtoMapper.toTableRow(row);
  }

  TableRowEntity createEntity(BlockEntity block, String title, String status, String priority, LocalDate dueDate, Integer position) {
    TableRowEntity row = new TableRowEntity();
    row.setBlock(block);
    row.setTitle(title);
    row.setStatus(validationService.normalizeStatus(status));
    row.setPriority(priority);
    row.setDueDate(dueDate);
    row.setPosition(position == null ? 0 : position);
    return tableRowRepository.save(row);
  }

  @Transactional(readOnly = true)
  public List<TableRowDtos.TableRowResponse> listByBlock(Long blockId) {
    BlockEntity block = blockService.getEntity(blockId);
    validationService.requireBlockType(block, BlockType.TABLE, "Table rows");
    return tableRowRepository.findByBlockIdOrderByPositionAscIdAsc(blockId)
        .stream()
        .map(DtoMapper::toTableRow)
        .toList();
  }

  public TableRowDtos.TableRowResponse update(Long id, TableRowDtos.UpdateTableRowRequest request) {
    TableRowEntity row = getEntity(id);
    if (request.title() != null) row.setTitle(request.title());
    if (request.status() != null) row.setStatus(validationService.normalizeStatus(request.status()));
    if (request.priority() != null) row.setPriority(request.priority());
    if (request.dueDate() != null) row.setDueDate(request.dueDate());
    if (request.position() != null) row.setPosition(request.position());
    return DtoMapper.toTableRow(row);
  }

  public void delete(Long id) {
    tableRowRepository.delete(getEntity(id));
  }

  List<TableRowEntity> entitiesByBlock(Long blockId) {
    return tableRowRepository.findByBlockIdOrderByPositionAscIdAsc(blockId);
  }

  private TableRowEntity getEntity(Long id) {
    return tableRowRepository.findById(id)
        .orElseThrow(() -> ApiException.notFound("Table row not found."));
  }
}
