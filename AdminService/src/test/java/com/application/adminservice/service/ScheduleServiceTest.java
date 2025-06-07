package com.application.adminservice.service;

import com.application.adminservice.entity.Schedule;
import com.application.adminservice.repository.ScheduleRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ScheduleServiceTest {

    @Mock
    private ScheduleRepository scheduleRepository;

    @InjectMocks
    private ScheduleService scheduleService;

    private Schedule schedule1;
    private Schedule schedule2;

    @BeforeEach
    void setUp() {
        schedule1 = new Schedule();
        schedule1.setScheduleId(1L);
        schedule1.setDepartureTime(LocalDateTime.of(2025,3,12,10,0));
        schedule1.setArrivalTime(LocalDateTime.of(2025,3,12,12,0));

        schedule2 = new Schedule();
        schedule2.setScheduleId(2L);
        schedule2.setDepartureTime(LocalDateTime.of(2025,3,12,3,10,0));
        schedule2.setArrivalTime(LocalDateTime.of(2025,3,12,6,10,0));
    }

    @Test
    @DisplayName("Get all schedules")
    void testGetAllSchedules_Success() {
        List<Schedule> mockSchedules = Arrays.asList(schedule1, schedule2);
        when(scheduleRepository.findAll()).thenReturn(mockSchedules);

        List<Schedule> result = scheduleService.getAllSchedules();

        assertNotNull(result);
        assertEquals(2, result.size());
        verify(scheduleRepository, times(1)).findAll();
    }

    @Test
    @DisplayName("Get all schedules fallback")
    void testGetAllSchedulesFallback() {
        List<Schedule> fallbackResult = scheduleService.getAllSchedulesFallback(new RuntimeException("Database error"));
        assertNotNull(fallbackResult);
        assertTrue(fallbackResult.isEmpty());
    }
}