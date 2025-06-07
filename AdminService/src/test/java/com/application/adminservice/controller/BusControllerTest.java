package com.application.adminservice.controller;

import com.application.adminservice.dto.BusDTO;
import com.application.adminservice.entity.Bus;
import com.application.adminservice.repository.BusRepository;
import com.application.adminservice.service.BusService;
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
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Optional;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
@WebMvcTest(BusController.class)
class BusControllerTest {

    @Autowired
    private MockMvc mockMvc;
    @MockBean
    private BusService busService;

    @MockBean
    private ScheduleService scheduleService;
    @MockBean
    private BusRepository busRepository;

    @Test
    @DisplayName("Add bus details")
    void testAddBusPage() throws Exception {
        mockMvc.perform(get("/api/admin/addBus"))
                .andExpect(status().isOk())
                .andExpect(view().name("addBus"))
                .andExpect(model().attributeExists("bus"));
    }

    @Test
    @DisplayName("After adding bus details")
    void testAddBus() throws Exception {
        mockMvc.perform(post("/api/admin/addBus")
                        .param("busNumber", "123ABC")
                        .param("busType", "Luxury")
                        .param("capacity", "50"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/api/admin/addBus"));
    }

    @Test
    @DisplayName("View all bus details")
    void testViewBuses() throws Exception {
        Bus bus=new Bus();
        bus.setBusId(1L);
        bus.setBusNumber("TN1890");
        bus.setBusType("AC");
        bus.setCapacity(30);
        Page<Bus> busPage = new PageImpl<>(List.of(bus));
        when(busService.getAllBuses(0, 5, null)).thenReturn(busPage);

        mockMvc.perform(get("/api/admin/viewBuses"))
                .andExpect(status().isOk())
                .andExpect(view().name("viewBus"))
                .andExpect(model().attributeExists("buses"))
                .andExpect(model().attribute("currentPage", 0))
                .andExpect(model().attribute("totalPages", busPage.getTotalPages()));
    }

    @Test
    @DisplayName("Edit bus form")
    void testEditBusForm() throws Exception {
        Bus bus=new Bus();
        bus.setBusId(1L);
        bus.setBusNumber("TN1890");
        bus.setBusType("AC");
        bus.setCapacity(30);
        when(busRepository.findById(1L)).thenReturn(Optional.of(bus));

        mockMvc.perform(get("/api/admin/edit/1"))
                .andExpect(status().isOk())
                .andExpect(view().name("edit-bus"))
                .andExpect(model().attributeExists("buses"));
    }

    @Test
    @DisplayName("Update bus details")
    void testUpdateBus() throws Exception {
        mockMvc.perform(post("/api/admin/edit/1")
                        .param("busNumber", "123ABC")
                        .param("busType", "Luxury")
                        .param("capacity", "50"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/api/admin/viewBuses"));
    }

    @Test
    @DisplayName("Delete bus details successful")
    void testDeleteBus() throws Exception {
        mockMvc.perform(get("/api/admin/delete/1"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/api/admin/viewBuses"));
    }

    @Test
    @DisplayName("Get Bus details using bus number")
    void testGetBusByNumber() throws Exception {
        BusDTO bus=new BusDTO();
        bus.setBusNumber("TN1890");
        bus.setBusType("AC");
        bus.setCapacity(30);
        when(busService.getBusByNumber("TN1890")).thenReturn(bus);

        mockMvc.perform(get("/api/admin/buses/TN1890"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.busNumber").value("TN1890"))
                .andExpect(jsonPath("$.busType").value("AC"))
                .andExpect(jsonPath("$.capacity").value(30));
    }
}