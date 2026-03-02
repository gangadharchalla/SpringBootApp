package com.equity.equityservice.service;

import com.equity.equityservice.dto.EquityRequest;
import com.equity.equityservice.dto.EquityResponse;
import com.equity.equityservice.entity.Equity;
import com.equity.equityservice.exception.ResourceNotFoundException;
import com.equity.equityservice.repository.EquityRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class EquityService {

    private final EquityRepository equityRepository;

    @Transactional
    public EquityResponse saveEquity(EquityRequest request) {
        log.info("Saving equity with symbol: {}", request.getSymbol());

        Equity equity = Equity.builder()
                .equityName(request.getEquityName())
                .symbol(request.getSymbol())
                .price(request.getPrice())
                .quantity(request.getQuantity())
                .build();

        Equity saved = equityRepository.save(equity);
        log.info("Equity saved successfully with id: {}", saved.getId());

        return mapToResponse(saved);
    }

    public EquityResponse getEquityById(Long id) {
        log.info("Fetching equity with id: {}", id);

        Equity equity = equityRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Equity not found with id: " + id));

        return mapToResponse(equity);
    }

    public List<EquityResponse> getAllEquities() {
        log.info("Fetching all equities");

        return equityRepository.findAll()
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public EquityResponse getEquityBySymbol(String symbol) {
        log.info("Fetching equity with symbol: {}", symbol);

        Equity equity = equityRepository.findBySymbol(symbol)
                .orElseThrow(() -> new ResourceNotFoundException("Equity not found with symbol: " + symbol));

        return mapToResponse(equity);
    }

    public List<EquityResponse> searchByName(String name) {
        log.info("Searching equities by name: {}", name);

        return equityRepository.findByEquityNameContainingIgnoreCase(name)
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public List<EquityResponse> getEquitiesByPriceRange(Double minPrice, Double maxPrice) {
        log.info("Fetching equities with price between {} and {}", minPrice, maxPrice);

        return equityRepository.findByPriceRange(minPrice, maxPrice)
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public EquityResponse updateEquity(Long id, EquityRequest request) {
        log.info("Updating equity with id: {}", id);

        Equity equity = equityRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Equity not found with id: " + id));

        equity.setEquityName(request.getEquityName());
        equity.setSymbol(request.getSymbol());
        equity.setPrice(request.getPrice());
        equity.setQuantity(request.getQuantity());

        Equity updated = equityRepository.save(equity);
        log.info("Equity updated successfully with id: {}", updated.getId());

        return mapToResponse(updated);
    }

    @Transactional
    public void deleteEquity(Long id) {
        log.info("Deleting equity with id: {}", id);

        if (!equityRepository.existsById(id)) {
            throw new ResourceNotFoundException("Equity not found with id: " + id);
        }

        equityRepository.deleteById(id);
        log.info("Equity deleted successfully with id: {}", id);
    }

    private EquityResponse mapToResponse(Equity equity) {
        return EquityResponse.builder()
                .id(equity.getId())
                .equityName(equity.getEquityName())
                .symbol(equity.getSymbol())
                .price(equity.getPrice())
                .quantity(equity.getQuantity())
                .build();
    }
}
