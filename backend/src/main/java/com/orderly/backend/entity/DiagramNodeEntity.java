package com.orderly.backend.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

@Entity
@Table(name = "diagram_nodes")
public class DiagramNodeEntity extends BaseEntity {
  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "diagram_id", nullable = false)
  private DiagramEntity diagram;

  @Column(nullable = false)
  private String type = "default";

  @Column(nullable = false)
  private String label;

  @Column(nullable = false)
  private Double x;

  @Column(nullable = false)
  private Double y;

  @Column(nullable = false)
  private Double width;

  @Column(nullable = false)
  private Double height;

  @JdbcTypeCode(SqlTypes.JSON)
  @Column(name = "style_json", columnDefinition = "jsonb")
  private String styleJson;

  @JdbcTypeCode(SqlTypes.JSON)
  @Column(name = "data_json", columnDefinition = "jsonb")
  private String dataJson;

  public DiagramEntity getDiagram() {
    return diagram;
  }

  public void setDiagram(DiagramEntity diagram) {
    this.diagram = diagram;
  }

  public String getType() {
    return type;
  }

  public void setType(String type) {
    this.type = type;
  }

  public String getLabel() {
    return label;
  }

  public void setLabel(String label) {
    this.label = label;
  }

  public Double getX() {
    return x;
  }

  public void setX(Double x) {
    this.x = x;
  }

  public Double getY() {
    return y;
  }

  public void setY(Double y) {
    this.y = y;
  }

  public Double getWidth() {
    return width;
  }

  public void setWidth(Double width) {
    this.width = width;
  }

  public Double getHeight() {
    return height;
  }

  public void setHeight(Double height) {
    this.height = height;
  }

  public String getStyleJson() {
    return styleJson;
  }

  public void setStyleJson(String styleJson) {
    this.styleJson = styleJson;
  }

  public String getDataJson() {
    return dataJson;
  }

  public void setDataJson(String dataJson) {
    this.dataJson = dataJson;
  }
}
