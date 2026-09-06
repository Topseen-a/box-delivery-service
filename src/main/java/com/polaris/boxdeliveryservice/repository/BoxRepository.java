package com.polaris.boxdeliveryservice.repository;

import com.polaris.boxdeliveryservice.model.Box;
import com.polaris.boxdeliveryservice.model.BoxState;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface BoxRepository extends JpaRepository<Box, Long> {
    Optional<Box> findByTxref(String txref);
    boolean existsByTxref(String txref);
    List<Box> findByStateAndBatteryCapacityGreaterThanEqual(BoxState state, Integer minBattery);
}
