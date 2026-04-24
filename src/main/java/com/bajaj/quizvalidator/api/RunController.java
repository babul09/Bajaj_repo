package com.bajaj.quizvalidator.api;

import com.bajaj.quizvalidator.api.dto.RunStatusResponse;
import com.bajaj.quizvalidator.api.dto.StartRunRequest;
import com.bajaj.quizvalidator.api.dto.StartRunResponse;
import com.bajaj.quizvalidator.application.RunOrchestrator;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/runs")
public class RunController {

    private final RunOrchestrator runOrchestrator;

    public RunController(RunOrchestrator runOrchestrator) {
        this.runOrchestrator = runOrchestrator;
    }

    @PostMapping
    public ResponseEntity<StartRunResponse> startRun(@Valid @RequestBody StartRunRequest startRunRequest) {
        RunOrchestrator.StartResult result = runOrchestrator.startRun(startRunRequest.getRegNo());
        if (!result.accepted()) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(new StartRunResponse(null, result.reason()));
        }
        return ResponseEntity.status(HttpStatus.ACCEPTED)
                .body(new StartRunResponse(result.runId(), "Run started"));
    }

    @GetMapping("/status")
    public RunStatusResponse status() {
        return runOrchestrator.currentStatus();
    }
}
