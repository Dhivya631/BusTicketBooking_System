package com.application.adminservice.repository;

import com.application.adminservice.entity.Route;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RouteRepository extends JpaRepository<Route,Long> {
    Page<Route> findByOriginContaining(String origin, Pageable pageable);
    Page<Route> findByDestinationContaining(String destination, Pageable pageable);
    Page<Route> findByOriginContainingAndDestinationContaining(String origin, String destination, Pageable pageable);

}
