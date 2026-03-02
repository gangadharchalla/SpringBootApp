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
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
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

    @GetMapping("/symbol/{symbol}")
    @Operation(summary = "Get Equity by Symbol", description = "Retrieve an equity record by its trading symbol")
    public ResponseEntity<EquityResponse> getEquityBySymbol(@PathVariable String symbol) {
        log.info("Request to get equity with symbol: {}", symbol);
        EquityResponse response = equityService.getEquityBySymbol(symbol);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/search")
    @Operation(summary = "Search Equities by Name", description = "Search equity records by name (case-insensitive)")
    public ResponseEntity<List<EquityResponse>> searchByName(@RequestParam String name) {
        log.info("Request to search equities by name: {}", name);
        List<EquityResponse> response = equityService.searchByName(name);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/price-range")
    @Operation(summary = "Get Equities by Price Range", description = "Retrieve equity records within a price range")
    public ResponseEntity<List<EquityResponse>> getByPriceRange(
            @RequestParam Double minPrice,
            @RequestParam Double maxPrice) {
        log.info("Request to get equities with price between {} and {}", minPrice, maxPrice);
        List<EquityResponse> response = equityService.getEquitiesByPriceRange(minPrice, maxPrice);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/update/{id}")
    @Operation(summary = "Update Equity", description = "Update an existing equity record by its ID")
    public ResponseEntity<EquityResponse> updateEquity(
            @PathVariable Long id,
            @Valid @RequestBody EquityRequest request) {
        log.info("Request to update equity with id: {}", id);
        EquityResponse response = equityService.updateEquity(id, request);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/delete/{id}")
    @Operation(summary = "Delete Equity", description = "Delete an equity record by its ID")
    public ResponseEntity<Void> deleteEquity(@PathVariable Long id) {
        log.info("Request to delete equity with id: {}", id);
        equityService.deleteEquity(id);
        return ResponseEntity.noContent().build();
    }
}
