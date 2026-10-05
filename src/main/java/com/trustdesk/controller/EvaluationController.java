package com.trustdesk.controller;

import com.trustdesk.service.EvaluationService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/evaluation")
public class EvaluationController {

    private final EvaluationService evaluationService;

    public EvaluationController(
            EvaluationService evaluationService) {

        this.evaluationService = evaluationService;
    }

    @PostMapping("/run")
    public ResponseEntity<List<String>> runEvaluation() {

        return ResponseEntity.ok(
                evaluationService.runAll()
        );
    }
}