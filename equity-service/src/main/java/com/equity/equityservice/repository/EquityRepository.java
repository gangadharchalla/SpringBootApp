package com.equity.equityservice.repository;

import com.equity.equityservice.entity.Equity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface EquityRepository extends JpaRepository<Equity, Long> {

    Optional<Equity> findBySymbol(String symbol);

    List<Equity> findByEquityNameContainingIgnoreCase(String equityName);

    @Query("SELECT e FROM Equity e WHERE e.price BETWEEN :minPrice AND :maxPrice")
    List<Equity> findByPriceRange(@Param("minPrice") Double minPrice, @Param("maxPrice") Double maxPrice);

    boolean existsBySymbol(String symbol);
}
