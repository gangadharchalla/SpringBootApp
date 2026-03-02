package com.equity.equityservice.repository;

import com.equity.equityservice.entity.Equity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface EquityRepository extends JpaRepository<Equity, Long> {

    Optional<Equity> findBySymbol(String symbol);
}
