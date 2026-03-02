package com.equity.equityservice.service;

import com.equity.equityservice.dto.EquityRequest;
import com.equity.equityservice.dto.EquityResponse;
import com.equity.equityservice.entity.Equity;
import com.equity.equityservice.exception.ResourceNotFoundException;
import com.equity.equityservice.repository.EquityRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class EquityService {

    private final EquityRepository equityRepository;

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
