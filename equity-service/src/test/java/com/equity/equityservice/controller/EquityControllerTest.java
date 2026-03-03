package com.equity.equityservice.controller;

import com.equity.equityservice.dto.EquityRequest;
import com.equity.equityservice.dto.EquityResponse;
import com.equity.equityservice.exception.GlobalExceptionHandler;
import com.equity.equityservice.exception.ResourceNotFoundException;
import com.equity.equityservice.service.EquityService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.Collections;
import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class EquityControllerTest {

    private MockMvc mockMvc;

    @Mock
    private EquityService equityService;

    @InjectMocks
    private EquityController equityController;

    private ObjectMapper objectMapper;
    private EquityRequest equityRequest;
    private EquityResponse equityResponse;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(equityController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();

        objectMapper = new ObjectMapper();

        equityRequest = EquityRequest.builder()
                .equityName("Apple Inc.")
                .symbol("AAPL")
                .price(178.50)
                .quantity(100)
                .build();

        equityResponse = EquityResponse.builder()
                .id(1L)
                .equityName("Apple Inc.")
                .symbol("AAPL")
                .price(178.50)
                .quantity(100)
                .build();
    }

    // --- POST /api/equity/save ---

    @Test
    @DisplayName("POST /api/equity/save - Should save equity and return 201")
    void saveEquity_validRequest_returns201() throws Exception {
        when(equityService.saveEquity(any(EquityRequest.class))).thenReturn(equityResponse);

        mockMvc.perform(post("/api/equity/save")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(equityRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.equityName", is("Apple Inc.")))
                .andExpect(jsonPath("$.symbol", is("AAPL")))
                .andExpect(jsonPath("$.price", is(178.50)))
                .andExpect(jsonPath("$.quantity", is(100)));

        verify(equityService, times(1)).saveEquity(any(EquityRequest.class));
    }

    @Test
    @DisplayName("POST /api/equity/save - Should return 400 for invalid request (missing equityName)")
    void saveEquity_invalidRequest_returns400() throws Exception {
        EquityRequest invalidRequest = EquityRequest.builder()
                .symbol("AAPL")
                .price(178.50)
                .quantity(100)
                .build();

        mockMvc.perform(post("/api/equity/save")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("POST /api/equity/save - Should return 400 for negative price")
    void saveEquity_negativePrice_returns400() throws Exception {
        EquityRequest invalidRequest = EquityRequest.builder()
                .equityName("Test Corp.")
                .symbol("TEST")
                .price(-10.0)
                .quantity(100)
                .build();

        mockMvc.perform(post("/api/equity/save")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("POST /api/equity/save - Should return 400 for zero quantity")
    void saveEquity_zeroQuantity_returns400() throws Exception {
        EquityRequest invalidRequest = EquityRequest.builder()
                .equityName("Test Corp.")
                .symbol("TEST")
                .price(100.0)
                .quantity(0)
                .build();

        mockMvc.perform(post("/api/equity/save")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());
    }

    // --- GET /api/equity/{id} ---

    @Test
    @DisplayName("GET /api/equity/{id} - Should return equity when ID exists")
    void getEquityById_existingId_returns200() throws Exception {
        when(equityService.getEquityById(1L)).thenReturn(equityResponse);

        mockMvc.perform(get("/api/equity/{id}", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.equityName", is("Apple Inc.")))
                .andExpect(jsonPath("$.symbol", is("AAPL")));

        verify(equityService, times(1)).getEquityById(1L);
    }

    @Test
    @DisplayName("GET /api/equity/{id} - Should return 404 when ID does not exist")
    void getEquityById_nonExistingId_returns404() throws Exception {
        when(equityService.getEquityById(99L))
                .thenThrow(new ResourceNotFoundException("Equity not found with id: 99"));

        mockMvc.perform(get("/api/equity/{id}", 99L))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status", is(404)))
                .andExpect(jsonPath("$.message", is("Equity not found with id: 99")));

        verify(equityService, times(1)).getEquityById(99L);
    }

    // --- GET /api/equity/all ---

    @Test
    @DisplayName("GET /api/equity/all - Should return all equities")
    void getAllEquities_returnsListWith200() throws Exception {
        EquityResponse msftResponse = EquityResponse.builder()
                .id(2L)
                .equityName("Microsoft Corp.")
                .symbol("MSFT")
                .price(415.20)
                .quantity(50)
                .build();

        when(equityService.getAllEquities()).thenReturn(List.of(equityResponse, msftResponse));

        mockMvc.perform(get("/api/equity/all"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].symbol", is("AAPL")))
                .andExpect(jsonPath("$[1].symbol", is("MSFT")));

        verify(equityService, times(1)).getAllEquities();
    }

    @Test
    @DisplayName("GET /api/equity/all - Should return empty list when no equities exist")
    void getAllEquities_noEquities_returnsEmptyList() throws Exception {
        when(equityService.getAllEquities()).thenReturn(Collections.emptyList());

        mockMvc.perform(get("/api/equity/all"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));

        verify(equityService, times(1)).getAllEquities();
    }

    // --- GET /api/equity/symbol/{symbol} ---

    @Test
    @DisplayName("GET /api/equity/symbol/{symbol} - Should return equity when symbol exists")
    void getEquityBySymbol_existingSymbol_returns200() throws Exception {
        when(equityService.getEquityBySymbol("AAPL")).thenReturn(equityResponse);

        mockMvc.perform(get("/api/equity/symbol/{symbol}", "AAPL"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.symbol", is("AAPL")))
                .andExpect(jsonPath("$.equityName", is("Apple Inc.")));

        verify(equityService, times(1)).getEquityBySymbol("AAPL");
    }

    @Test
    @DisplayName("GET /api/equity/symbol/{symbol} - Should return 404 when symbol does not exist")
    void getEquityBySymbol_nonExistingSymbol_returns404() throws Exception {
        when(equityService.getEquityBySymbol("NVDA"))
                .thenThrow(new ResourceNotFoundException("Equity not found with symbol: NVDA"));

        mockMvc.perform(get("/api/equity/symbol/{symbol}", "NVDA"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message", is("Equity not found with symbol: NVDA")));
    }

    // --- GET /api/equity/search ---

    @Test
    @DisplayName("GET /api/equity/search - Should return equities matching name")
    void searchByName_matchingName_returnsResults() throws Exception {
        when(equityService.searchByName("Apple")).thenReturn(List.of(equityResponse));

        mockMvc.perform(get("/api/equity/search").param("name", "Apple"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].equityName", is("Apple Inc.")));

        verify(equityService, times(1)).searchByName("Apple");
    }

    // --- GET /api/equity/price-range ---

    @Test
    @DisplayName("GET /api/equity/price-range - Should return equities within price range")
    void getByPriceRange_validRange_returnsResults() throws Exception {
        when(equityService.getEquitiesByPriceRange(100.0, 200.0)).thenReturn(List.of(equityResponse));

        mockMvc.perform(get("/api/equity/price-range")
                        .param("minPrice", "100.0")
                        .param("maxPrice", "200.0"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].price", is(178.50)));

        verify(equityService, times(1)).getEquitiesByPriceRange(100.0, 200.0);
    }

    // --- PUT /api/equity/update/{id} ---

    @Test
    @DisplayName("PUT /api/equity/update/{id} - Should update equity and return 200")
    void updateEquity_validRequest_returns200() throws Exception {
        EquityResponse updatedResponse = EquityResponse.builder()
                .id(1L)
                .equityName("Apple Inc. Updated")
                .symbol("AAPL")
                .price(190.00)
                .quantity(150)
                .build();

        when(equityService.updateEquity(eq(1L), any(EquityRequest.class))).thenReturn(updatedResponse);

        EquityRequest updateRequest = EquityRequest.builder()
                .equityName("Apple Inc. Updated")
                .symbol("AAPL")
                .price(190.00)
                .quantity(150)
                .build();

        mockMvc.perform(put("/api/equity/update/{id}", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.equityName", is("Apple Inc. Updated")))
                .andExpect(jsonPath("$.price", is(190.00)));

        verify(equityService, times(1)).updateEquity(eq(1L), any(EquityRequest.class));
    }

    @Test
    @DisplayName("PUT /api/equity/update/{id} - Should return 404 when ID does not exist")
    void updateEquity_nonExistingId_returns404() throws Exception {
        when(equityService.updateEquity(eq(99L), any(EquityRequest.class)))
                .thenThrow(new ResourceNotFoundException("Equity not found with id: 99"));

        mockMvc.perform(put("/api/equity/update/{id}", 99L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(equityRequest)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message", is("Equity not found with id: 99")));
    }

    // --- DELETE /api/equity/delete/{id} ---

    @Test
    @DisplayName("DELETE /api/equity/delete/{id} - Should delete equity and return 204")
    void deleteEquity_existingId_returns204() throws Exception {
        doNothing().when(equityService).deleteEquity(1L);

        mockMvc.perform(delete("/api/equity/delete/{id}", 1L))
                .andExpect(status().isNoContent());

        verify(equityService, times(1)).deleteEquity(1L);
    }

    @Test
    @DisplayName("DELETE /api/equity/delete/{id} - Should return 404 when ID does not exist")
    void deleteEquity_nonExistingId_returns404() throws Exception {
        doThrow(new ResourceNotFoundException("Equity not found with id: 99"))
                .when(equityService).deleteEquity(99L);

        mockMvc.perform(delete("/api/equity/delete/{id}", 99L))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message", is("Equity not found with id: 99")));
    }
}
