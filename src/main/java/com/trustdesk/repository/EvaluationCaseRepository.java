package com.trustdesk.repository;

import com.trustdesk.entity.EvaluationCase;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface EvaluationCaseRepository
        extends JpaRepository<EvaluationCase, Long> {

    Optional<EvaluationCase> findByCaseId(String caseId);
}