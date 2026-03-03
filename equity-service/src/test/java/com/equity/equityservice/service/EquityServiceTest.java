package com.equity.equityservice.service;

import com.equity.equityservice.dto.EquityRequest;
import com.equity.equityservice.dto.EquityResponse;
import com.equity.equityservice.entity.Equity;
import com.equity.equityservice.exception.ResourceNotFoundException;
import com.equity.equityservice.repository.EquityRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class EquityServiceTest {

    @Mock
    private EquityRepository equityRepository;

    @InjectMocks
    private EquityService equityService;

    private Equity equity;
    private EquityRequest equityRequest;

    @BeforeEach
    void setUp() {
        equity = Equity.builder()
                .id(1L)
                .equityName("Apple Inc.")
                .symbol("AAPL")
                .price(178.50)
                .quantity(100)
                .build();

        equityRequest = EquityRequest.builder()
                .equityName("Apple Inc.")
                .symbol("AAPL")
                .price(178.50)
                .quantity(100)
                .build();
    }

    // --- saveEquity ---

    @Test
    @DisplayName("Should save equity and return response")
    void saveEquity_validRequest_returnsResponse() {
        when(equityRepository.save(any(Equity.class))).thenReturn(equity);

        EquityResponse response = equityService.saveEquity(equityRequest);

        assertThat(response).isNotNull();
        assertThat(response.getId()).isEqualTo(1L);
        assertThat(response.getEquityName()).isEqualTo("Apple Inc.");
        assertThat(response.getSymbol()).isEqualTo("AAPL");
        assertThat(response.getPrice()).isEqualTo(178.50);
        assertThat(response.getQuantity()).isEqualTo(100);
        verify(equityRepository, times(1)).save(any(Equity.class));
    }

    // --- getEquityById ---

    @Test
    @DisplayName("Should return equity when ID exists")
    void getEquityById_existingId_returnsResponse() {
        when(equityRepository.findById(1L)).thenReturn(Optional.of(equity));

        EquityResponse response = equityService.getEquityById(1L);

        assertThat(response).isNotNull();
        assertThat(response.getId()).isEqualTo(1L);
        assertThat(response.getSymbol()).isEqualTo("AAPL");
        verify(equityRepository, times(1)).findById(1L);
    }

    @Test
    @DisplayName("Should throw ResourceNotFoundException when ID does not exist")
    void getEquityById_nonExistingId_throwsException() {
        when(equityRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> equityService.getEquityById(99L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Equity not found with id: 99");

        verify(equityRepository, times(1)).findById(99L);
    }

    // --- getAllEquities ---

    @Test
    @DisplayName("Should return all equities")
    void getAllEquities_returnsAllResponses() {
        Equity microsoft = Equity.builder()
                .id(2L)
                .equityName("Microsoft Corp.")
                .symbol("MSFT")
                .price(415.20)
                .quantity(50)
                .build();

        when(equityRepository.findAll()).thenReturn(List.of(equity, microsoft));

        List<EquityResponse> responses = equityService.getAllEquities();

        assertThat(responses).hasSize(2);
        assertThat(responses.get(0).getSymbol()).isEqualTo("AAPL");
        assertThat(responses.get(1).getSymbol()).isEqualTo("MSFT");
        verify(equityRepository, times(1)).findAll();
    }

    @Test
    @DisplayName("Should return empty list when no equities exist")
    void getAllEquities_noEquities_returnsEmptyList() {
        when(equityRepository.findAll()).thenReturn(Collections.emptyList());

        List<EquityResponse> responses = equityService.getAllEquities();

        assertThat(responses).isEmpty();
        verify(equityRepository, times(1)).findAll();
    }

    // --- getEquityBySymbol ---

    @Test
    @DisplayName("Should return equity when symbol exists")
    void getEquityBySymbol_existingSymbol_returnsResponse() {
        when(equityRepository.findBySymbol("AAPL")).thenReturn(Optional.of(equity));

        EquityResponse response = equityService.getEquityBySymbol("AAPL");

        assertThat(response).isNotNull();
        assertThat(response.getSymbol()).isEqualTo("AAPL");
        verify(equityRepository, times(1)).findBySymbol("AAPL");
    }

    @Test
    @DisplayName("Should throw ResourceNotFoundException when symbol does not exist")
    void getEquityBySymbol_nonExistingSymbol_throwsException() {
        when(equityRepository.findBySymbol("NVDA")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> equityService.getEquityBySymbol("NVDA"))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Equity not found with symbol: NVDA");

        verify(equityRepository, times(1)).findBySymbol("NVDA");
    }

    // --- searchByName ---

    @Test
    @DisplayName("Should return equities matching name search")
    void searchByName_matchingName_returnsResults() {
        when(equityRepository.findByEquityNameContainingIgnoreCase("Apple"))
                .thenReturn(List.of(equity));

        List<EquityResponse> responses = equityService.searchByName("Apple");

        assertThat(responses).hasSize(1);
        assertThat(responses.get(0).getEquityName()).isEqualTo("Apple Inc.");
        verify(equityRepository, times(1)).findByEquityNameContainingIgnoreCase("Apple");
    }

    @Test
    @DisplayName("Should return empty list when name search has no matches")
    void searchByName_noMatch_returnsEmptyList() {
        when(equityRepository.findByEquityNameContainingIgnoreCase("Tesla"))
                .thenReturn(Collections.emptyList());

        List<EquityResponse> responses = equityService.searchByName("Tesla");

        assertThat(responses).isEmpty();
    }

    // --- getEquitiesByPriceRange ---

    @Test
    @DisplayName("Should return equities within price range")
    void getEquitiesByPriceRange_validRange_returnsResults() {
        when(equityRepository.findByPriceRange(100.0, 200.0)).thenReturn(List.of(equity));

        List<EquityResponse> responses = equityService.getEquitiesByPriceRange(100.0, 200.0);

        assertThat(responses).hasSize(1);
        assertThat(responses.get(0).getPrice()).isEqualTo(178.50);
        verify(equityRepository, times(1)).findByPriceRange(100.0, 200.0);
    }

    // --- updateEquity ---

    @Test
    @DisplayName("Should update equity when ID exists")
    void updateEquity_existingId_returnsUpdatedResponse() {
        EquityRequest updateRequest = EquityRequest.builder()
                .equityName("Apple Inc. Updated")
                .symbol("AAPL")
                .price(190.00)
                .quantity(150)
                .build();

        Equity updatedEquity = Equity.builder()
                .id(1L)
                .equityName("Apple Inc. Updated")
                .symbol("AAPL")
                .price(190.00)
                .quantity(150)
                .build();

        when(equityRepository.findById(1L)).thenReturn(Optional.of(equity));
        when(equityRepository.save(any(Equity.class))).thenReturn(updatedEquity);

        EquityResponse response = equityService.updateEquity(1L, updateRequest);

        assertThat(response.getEquityName()).isEqualTo("Apple Inc. Updated");
        assertThat(response.getPrice()).isEqualTo(190.00);
        assertThat(response.getQuantity()).isEqualTo(150);
        verify(equityRepository, times(1)).findById(1L);
        verify(equityRepository, times(1)).save(any(Equity.class));
    }

    @Test
    @DisplayName("Should throw ResourceNotFoundException when updating non-existing equity")
    void updateEquity_nonExistingId_throwsException() {
        when(equityRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> equityService.updateEquity(99L, equityRequest))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Equity not found with id: 99");

        verify(equityRepository, times(1)).findById(99L);
        verify(equityRepository, never()).save(any(Equity.class));
    }

    // --- deleteEquity ---

    @Test
    @DisplayName("Should delete equity when ID exists")
    void deleteEquity_existingId_deletesSuccessfully() {
        when(equityRepository.existsById(1L)).thenReturn(true);

        equityService.deleteEquity(1L);

        verify(equityRepository, times(1)).existsById(1L);
        verify(equityRepository, times(1)).deleteById(1L);
    }

    @Test
    @DisplayName("Should throw ResourceNotFoundException when deleting non-existing equity")
    void deleteEquity_nonExistingId_throwsException() {
        when(equityRepository.existsById(99L)).thenReturn(false);

        assertThatThrownBy(() -> equityService.deleteEquity(99L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Equity not found with id: 99");

        verify(equityRepository, times(1)).existsById(99L);
        verify(equityRepository, never()).deleteById(anyLong());
    }
}
