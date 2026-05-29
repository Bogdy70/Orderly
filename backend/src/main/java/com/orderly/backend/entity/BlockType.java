package com.orderly.backend.entity;

public enum BlockType {
  CHECKLIST("checklist"),
  TABLE("table"),
  DIAGRAM("diagram");

  private final String value;

  BlockType(String value) {
    this.value = value;
  }

  public String value() {
    return value;
  }

  public static BlockType fromValue(String value) {
    for (BlockType type : values()) {
      if (type.value.equalsIgnoreCase(value)) {
        return type;
      }
    }
    throw new IllegalArgumentException("Unsupported block type: " + value);
  }
}
