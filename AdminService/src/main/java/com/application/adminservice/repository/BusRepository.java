package com.application.adminservice.repository;

import com.application.adminservice.entity.Bus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface BusRepository extends JpaRepository<Bus,Long> {
    Page<Bus> findByBusNumberContainingIgnoreCase(String busNumber, Pageable pageable);

    Optional<Bus> findByBusNumber(String busNumber);
}