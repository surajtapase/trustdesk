package com.trustdesk.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "tool_catalog")
public class ToolCatalog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "tool_name", nullable = false, unique = true)
    private String toolName;

    @Column(columnDefinition = "TEXT")
    private String definition;

    public ToolCatalog() {
    }

    public Long getId() {
        return id;
    }

    public String getToolName() {
        return toolName;
    }

    public void setToolName(String toolName) {
        this.toolName = toolName;
    }

    public String getDefinition() {
        return definition;
    }

    public void setDefinition(String definition) {
        this.definition = definition;
    }
}