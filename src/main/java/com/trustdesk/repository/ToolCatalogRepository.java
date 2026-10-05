package com.trustdesk.repository;

import com.trustdesk.entity.ToolCatalog;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ToolCatalogRepository extends JpaRepository<ToolCatalog, Long> {

    Optional<ToolCatalog> findByToolName(String toolName);
}