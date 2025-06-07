package com.application.userservice.repository;

import com.application.userservice.entity.Booking;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BookingRepository extends JpaRepository<Booking,Long> {
    boolean existsByScheduleIdAndSeatNumber(Long scheduleId, int seatNumber);

    List<Booking> findByScheduleId(Long scheduleId);

    int countByScheduleId(Long scheduleId);

    List<Booking> findByUserId(Long userId);

//    List<Booking> findByEmail(String );
}