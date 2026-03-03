package com.equity.equityservice.repository;

import com.equity.equityservice.entity.Equity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
class EquityRepositoryTest {

    @Autowired
    private EquityRepository equityRepository;

    private Equity apple;
    private Equity microsoft;
    private Equity google;

    @BeforeEach
    void setUp() {
        equityRepository.deleteAll();

        apple = Equity.builder()
                .equityName("Apple Inc.")
                .symbol("AAPL")
                .price(178.50)
                .quantity(100)
                .build();

        microsoft = Equity.builder()
                .equityName("Microsoft Corp.")
                .symbol("MSFT")
                .price(415.20)
                .quantity(50)
                .build();

        google = Equity.builder()
                .equityName("Alphabet Inc.")
                .symbol("GOOGL")
                .price(141.80)
                .quantity(75)
                .build();

        equityRepository.saveAll(List.of(apple, microsoft, google));
    }

    @Test
    @DisplayName("Should find equity by symbol")
    void findBySymbol_existingSymbol_returnsEquity() {
        Optional<Equity> result = equityRepository.findBySymbol("AAPL");

        assertThat(result).isPresent();
        assertThat(result.get().getEquityName()).isEqualTo("Apple Inc.");
        assertThat(result.get().getPrice()).isEqualTo(178.50);
    }

    @Test
    @DisplayName("Should return empty when symbol does not exist")
    void findBySymbol_nonExistingSymbol_returnsEmpty() {
        Optional<Equity> result = equityRepository.findBySymbol("NVDA");

        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("Should find equities by name containing (case-insensitive)")
    void findByEquityNameContainingIgnoreCase_returnsMatchingEquities() {
        List<Equity> result = equityRepository.findByEquityNameContainingIgnoreCase("inc");

        assertThat(result).hasSize(2);
        assertThat(result).extracting(Equity::getSymbol)
                .containsExactlyInAnyOrder("AAPL", "GOOGL");
    }

    @Test
    @DisplayName("Should return empty list when name does not match")
    void findByEquityNameContainingIgnoreCase_noMatch_returnsEmpty() {
        List<Equity> result = equityRepository.findByEquityNameContainingIgnoreCase("Tesla");

        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("Should find equities by price range")
    void findByPriceRange_returnsEquitiesInRange() {
        List<Equity> result = equityRepository.findByPriceRange(100.0, 200.0);

        assertThat(result).hasSize(2);
        assertThat(result).extracting(Equity::getSymbol)
                .containsExactlyInAnyOrder("AAPL", "GOOGL");
    }

    @Test
    @DisplayName("Should return empty when no equities in price range")
    void findByPriceRange_noMatch_returnsEmpty() {
        List<Equity> result = equityRepository.findByPriceRange(500.0, 1000.0);

        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("Should check if equity exists by symbol")
    void existsBySymbol_existingSymbol_returnsTrue() {
        boolean exists = equityRepository.existsBySymbol("MSFT");

        assertThat(exists).isTrue();
    }

    @Test
    @DisplayName("Should return false when symbol does not exist")
    void existsBySymbol_nonExistingSymbol_returnsFalse() {
        boolean exists = equityRepository.existsBySymbol("TSLA");

        assertThat(exists).isFalse();
    }

    @Test
    @DisplayName("Should save equity successfully")
    void save_newEquity_persistsSuccessfully() {
        Equity tesla = Equity.builder()
                .equityName("Tesla Inc.")
                .symbol("TSLA")
                .price(245.30)
                .quantity(40)
                .build();

        Equity saved = equityRepository.save(tesla);

        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getSymbol()).isEqualTo("TSLA");
        assertThat(equityRepository.count()).isEqualTo(4);
    }

    @Test
    @DisplayName("Should find all equities")
    void findAll_returnsAllEquities() {
        List<Equity> result = equityRepository.findAll();

        assertThat(result).hasSize(3);
    }

    @Test
    @DisplayName("Should find equity by ID")
    void findById_existingId_returnsEquity() {
        Optional<Equity> result = equityRepository.findById(apple.getId());

        assertThat(result).isPresent();
        assertThat(result.get().getSymbol()).isEqualTo("AAPL");
    }

    @Test
    @DisplayName("Should delete equity by ID")
    void deleteById_existingId_removesEquity() {
        equityRepository.deleteById(apple.getId());

        assertThat(equityRepository.count()).isEqualTo(2);
        assertThat(equityRepository.findById(apple.getId())).isEmpty();
    }
}
