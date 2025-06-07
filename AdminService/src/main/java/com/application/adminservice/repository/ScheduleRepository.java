package com.application.adminservice.repository;

import com.application.adminservice.entity.Schedule;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface ScheduleRepository extends JpaRepository<Schedule,Long> {
    Page<Schedule> findByRoute_OriginContaining(String origin, Pageable pageable);
    Page<Schedule> findByRoute_DestinationContaining(String destination, Pageable pageable);
    Page<Schedule> findByRoute_OriginContainingAndRoute_DestinationContaining(String origin, String destination, Pageable pageable);

    List<Schedule> findByRouteOriginAndRouteDestinationAndDate(String origin, String destination, LocalDate date);

    @Query("SELECT s from Schedule s where s.bus.busNumber= :busNumber")
    List<Schedule> findSchedulesByBusNumber(@Param("busNumber") String busNumber);

    int countByScheduleId(Long scheduleId);


}