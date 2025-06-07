package com.application.adminservice.service;

import com.application.adminservice.entity.Schedule;
import com.application.adminservice.repository.ScheduleRepository;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;

@Service
public class ScheduleService {
    @Autowired
    private ScheduleRepository scheduleRepository;
    private static final String SCHEDULE_SERVICE = "scheduleService";

    @CircuitBreaker(name = SCHEDULE_SERVICE, fallbackMethod = "getAllSchedulesFallback")
    public List<Schedule> getAllSchedules() {
        return scheduleRepository.findAll();
    }

    public List<Schedule> getAllSchedulesFallback(Throwable t) {
        System.err.println("Circuit breaker activated for getAllSchedules(): " + t.getMessage());
        return Collections.emptyList();
    }
}
