package com.orderly.backend.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "checklist_items")
public class ChecklistItemEntity extends BaseEntity {
  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "block_id", nullable = false)
  private BlockEntity block;

  @Column(nullable = false)
  private String text;

  @Column(name = "is_done", nullable = false)
  private Boolean done = false;

  @Column(nullable = false)
  private Integer position = 0;

  public BlockEntity getBlock() {
    return block;
  }

  public void setBlock(BlockEntity block) {
    this.block = block;
  }

  public String getText() {
    return text;
  }

  public void setText(String text) {
    this.text = text;
  }

  public Boolean getDone() {
    return done;
  }

  public void setDone(Boolean done) {
    this.done = done;
  }

  public Integer getPosition() {
    return position;
  }

  public void setPosition(Integer position) {
    this.position = position;
  }
}
