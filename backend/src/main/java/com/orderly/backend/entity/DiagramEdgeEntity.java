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
@Table(name = "diagram_edges")
public class DiagramEdgeEntity extends BaseEntity {
  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "diagram_id", nullable = false)
  private DiagramEntity diagram;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "source_node_id", nullable = false)
  private DiagramNodeEntity sourceNode;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "target_node_id", nullable = false)
  private DiagramNodeEntity targetNode;

  private String label;

  @Column(nullable = false)
  private String type = "arrow";

  @JdbcTypeCode(SqlTypes.JSON)
  @Column(name = "style_json", columnDefinition = "jsonb")
  private String styleJson;

  public DiagramEntity getDiagram() {
    return diagram;
  }

  public void setDiagram(DiagramEntity diagram) {
    this.diagram = diagram;
  }

  public DiagramNodeEntity getSourceNode() {
    return sourceNode;
  }

  public void setSourceNode(DiagramNodeEntity sourceNode) {
    this.sourceNode = sourceNode;
  }

  public DiagramNodeEntity getTargetNode() {
    return targetNode;
  }

  public void setTargetNode(DiagramNodeEntity targetNode) {
    this.targetNode = targetNode;
  }

  public String getLabel() {
    return label;
  }

  public void setLabel(String label) {
    this.label = label;
  }

  public String getType() {
    return type;
  }

  public void setType(String type) {
    this.type = type;
  }

  public String getStyleJson() {
    return styleJson;
  }

  public void setStyleJson(String styleJson) {
    this.styleJson = styleJson;
  }
}
