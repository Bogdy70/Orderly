package com.orderly.backend.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "diagrams")
public class DiagramEntity extends BaseEntity {
  @OneToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "block_id", nullable = false, unique = true)
  private BlockEntity block;

  @Column(name = "viewport_x", nullable = false)
  private Double viewportX = 0.0;

  @Column(name = "viewport_y", nullable = false)
  private Double viewportY = 0.0;

  @Column(nullable = false)
  private Double zoom = 1.0;

  public BlockEntity getBlock() {
    return block;
  }

  public void setBlock(BlockEntity block) {
    this.block = block;
  }

  public Double getViewportX() {
    return viewportX;
  }

  public void setViewportX(Double viewportX) {
    this.viewportX = viewportX;
  }

  public Double getViewportY() {
    return viewportY;
  }

  public void setViewportY(Double viewportY) {
    this.viewportY = viewportY;
  }

  public Double getZoom() {
    return zoom;
  }

  public void setZoom(Double zoom) {
    this.zoom = zoom;
  }
}
