package com.equity.equityservice.controller;

import com.equity.equityservice.dto.EquityRequest;
import com.equity.equityservice.dto.EquityResponse;
import com.equity.equityservice.service.EquityService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/equity")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Equity", description = "Equity management APIs")
@SecurityRequirement(name = "bearerAuth")
public class EquityController {

    private final EquityService equityService;

    @PostMapping("/save")
    @Operation(summary = "Save Equity", description = "Save a new equity record")
    public ResponseEntity<EquityResponse> saveEquity(@Valid @RequestBody EquityRequest request) {
        log.info("Request to save equity: {}", request.getSymbol());
        EquityResponse response = equityService.saveEquity(request);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get Equity by ID", description = "Retrieve an equity record by its ID")
    public ResponseEntity<EquityResponse> getEquityById(@PathVariable Long id) {
        log.info("Request to get equity with id: {}", id);
        EquityResponse response = equityService.getEquityById(id);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/all")
    @Operation(summary = "Get All Equities", description = "Retrieve all equity records")
    public ResponseEntity<List<EquityResponse>> getAllEquities() {
        log.info("Request to get all equities");
        List<EquityResponse> response = equityService.getAllEquities();
        return ResponseEntity.ok(response);
    }
}
