package com.orderly.backend.entity;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = true)
public class BlockTypeConverter implements AttributeConverter<BlockType, String> {
  @Override
  public String convertToDatabaseColumn(BlockType attribute) {
    return attribute == null ? null : attribute.value();
  }

  @Override
  public BlockType convertToEntityAttribute(String dbData) {
    return dbData == null ? null : BlockType.fromValue(dbData);
  }
}
