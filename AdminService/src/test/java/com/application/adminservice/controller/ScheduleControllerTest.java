package com.application.adminservice.controller;

import com.application.adminservice.entity.Bus;
import com.application.adminservice.entity.Route;
import com.application.adminservice.entity.Schedule;
import com.application.adminservice.repository.RouteRepository;
import com.application.adminservice.repository.ScheduleRepository;
import com.application.adminservice.service.BusService;
import com.application.adminservice.service.RouteService;
import com.application.adminservice.service.ScheduleService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
@WebMvcTest(ScheduleController.class)
class ScheduleControllerTest {
    @Autowired
    private MockMvc mockMvc;
    @MockBean
    private ScheduleRepository scheduleRepository;
    @MockBean
    private RouteService routeService;

    @MockBean
    private ScheduleService scheduleService;
    @MockBean
    private BusService busService;
    @MockBean
    private RouteRepository routeRepository;

    @Test
    @DisplayName("View all schedules")
    void testViewSchedules() throws Exception {
        Bus bus=new Bus();
        bus.setBusId(1L);
        Route route=new Route();
        route.setRouteId(1L);
        route.setOrigin("Chennai");
        route.setDestination("Tirchy");
        Schedule schedule=new Schedule();
        schedule.setScheduleId(1L);
        schedule.setBus(bus);
        schedule.setRoute(route);
        Page<Schedule> mockPage = new PageImpl<>(List.of(schedule));
        when(scheduleRepository.findAll(any(Pageable.class))).thenReturn(mockPage);

        mockMvc.perform(get("/api/admin/schedules/view").param("page", "1"))
                .andExpect(status().isOk())
                .andExpect(view().name("schedules"))
                .andExpect(model().attributeExists("schedules", "currentPage", "totalPages"));
    }

    @Test
    @DisplayName("Add schedules form")
    void testShowAddScheduleForm() throws Exception {
        when(routeService.getAllRoutes()).thenReturn(List.of(new Route()));
        when(busService.getAllBuse()).thenReturn(List.of(new Bus()));

        mockMvc.perform(get("/api/admin/schedules/add"))
                .andExpect(status().isOk())
                .andExpect(view().name("add-schedule"))
                .andExpect(model().attributeExists("routes", "buses", "schedule"));
    }

    @Test
    @DisplayName("Saved schedule")
    void testSaveSchedule() throws Exception {
        Bus bus=new Bus();
        bus.setBusId(1L);
        Route route=new Route();
        route.setRouteId(1L);
        route.setOrigin("Chennai");
        route.setDestination("Tirchy");
        Schedule schedule=new Schedule();
        schedule.setScheduleId(1L);
        schedule.setBus(bus);
        schedule.setRoute(route);
        when(scheduleRepository.save(any(Schedule.class))).thenReturn(schedule);

        mockMvc.perform(post("/api/admin/schedules/add")
                        .flashAttr("schedule", schedule))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/api/admin/schedules/view"));
    }

    @Test
    @DisplayName("Edit schedule form")
    void testEditScheduleForm() throws Exception {
        Bus bus=new Bus();
        bus.setBusId(1L);
        Route route=new Route();
        route.setRouteId(1L);
        route.setOrigin("Chennai");
        route.setDestination("Tirchy");
        Schedule schedule=new Schedule();
        schedule.setScheduleId(1L);
        schedule.setBus(bus);
        schedule.setRoute(route);
        when(scheduleRepository.findById(1L)).thenReturn(Optional.of(schedule));
        when(routeService.getAllRoutes()).thenReturn(List.of(new Route()));
        when(busService.getAllBuse()).thenReturn(List.of(new Bus()));

        mockMvc.perform(get("/api/admin/schedules/edit/1"))
                .andExpect(status().isOk())
                .andExpect(view().name("edit-schedule"))
                .andExpect(model().attributeExists("routes", "buses", "schedule"));
    }

    @Test
    @DisplayName("Update schedule details")
    void testUpdateSchedule() throws Exception {
        Bus bus=new Bus();
        bus.setBusId(1L);
        Route route=new Route();
        route.setRouteId(1L);
        route.setOrigin("Chennai");
        route.setDestination("Tirchy");
        Schedule schedule=new Schedule();
        schedule.setScheduleId(1L);
        schedule.setBus(bus);
        schedule.setRoute(route);;
        when(scheduleRepository.save(any(Schedule.class))).thenReturn(schedule);

        mockMvc.perform(post("/api/admin/schedules/edit/1")
                        .flashAttr("schedule", schedule))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/api/admin/schedules/view"));
    }

    @Test
    @DisplayName("Delete schedule successfully")
    void testDeleteSchedule() throws Exception {
        doNothing().when(scheduleRepository).deleteById(1L);

        mockMvc.perform(get("/api/admin/schedules/delete/1"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/api/admin/schedules/view"));
    }

    @Test
    @DisplayName("Update available seats")
    void testUpdateAvailableSeats() throws Exception {
        Bus bus=new Bus();
        bus.setBusId(1L);
        Route route=new Route();
        route.setRouteId(1L);
        route.setOrigin("Chennai");
        route.setDestination("Tirchy");
        Schedule schedule=new Schedule();
        schedule.setScheduleId(1L);
        schedule.setBus(bus);
        schedule.setRoute(route);
        when(scheduleRepository.findById(1L)).thenReturn(Optional.of(schedule));
        when(scheduleRepository.save(any(Schedule.class))).thenReturn(schedule);

        mockMvc.perform(put("/api/admin/schedules/1/update-available-seats")
                        .param("availableSeats", "20"))
                .andExpect(status().isOk())
                .andExpect(content().string("Available seats updated successfully"));
    }

    @Test
    @DisplayName("Get schedule details using id")
    void testGetScheduleById() throws Exception {
        Bus bus=new Bus();
        bus.setBusId(1L);
        bus.setBusNumber("TN1980");
        bus.setBusType("AC");
        Route route=new Route();
        route.setRouteId(1L);
        route.setOrigin("Chennai");
        route.setDestination("Tirchy");
        Schedule schedule=new Schedule();
        schedule.setScheduleId(1L);
        schedule.setBus(bus);
        schedule.setAvailableSeats(10);
        schedule.setRoute(route);
        schedule.setFare(100.0);
        when(scheduleRepository.findById(1L)).thenReturn(Optional.of(schedule));

        mockMvc.perform(get("/api/admin/schedules/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.scheduleId").value(1L))
                .andExpect(jsonPath("$.busNumber").value("TN1980"))
                .andExpect(jsonPath("$.busType").value("AC"))
                .andExpect(jsonPath("$.origin").value("Chennai"))
                .andExpect(jsonPath("$.destination").value("Tirchy"))
                .andExpect(jsonPath("$.availableSeats").value(10))
                .andExpect(jsonPath("$.fare").value(100.0));
    }
}