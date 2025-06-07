package com.application.adminservice.controller;

import com.application.adminservice.entity.Route;
import com.application.adminservice.repository.RouteRepository;
import com.application.adminservice.service.RouteService;
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

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
@WebMvcTest(RouteController.class)
class RouteControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private RouteService routeService;

    @MockBean
    private RouteRepository routeRepository;

    @Test
    @DisplayName("View all routes")
    void testViewRoutes() throws Exception {
        Route route=new Route();
        route.setRouteId(1L);
        route.setOrigin("Chennai");
        route.setDestination("Tirchy");
        Page<Route> mockPage = new PageImpl<>(List.of(route));
        when(routeService.findRoutesByOriginAndDestination(anyString(), anyString(), any())).thenReturn(mockPage);

        mockMvc.perform(get("/api/admin/routes/view")
                        .param("page", "1")
                        .param("origin", "NYC")
                        .param("destination", "LA"))
                .andExpect(status().isOk())
                .andExpect(view().name("routes"))
                .andExpect(model().attributeExists("routes", "currentPage", "totalPages", "origin", "destination"));
    }

    @Test
    @DisplayName("Add route details")
    void testAddRouteForm() throws Exception {
        mockMvc.perform(get("/api/admin/routes/add"))
                .andExpect(status().isOk())
                .andExpect(view().name("add-route"))
                .andExpect(model().attributeExists("route"));
    }

    @Test
    @DisplayName("Save routes details")
    void testSaveRoute() throws Exception {
        Route route=new Route();
        route.setRouteId(1L);
        route.setOrigin("Chennai");
        route.setDestination("Tirchy");
        when(routeRepository.save(any(Route.class))).thenReturn(route);

        mockMvc.perform(post("/api/admin/routes/add")
                        .flashAttr("route", route))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/api/admin/routes/view"));
    }

    @Test
    @DisplayName("Edit route details")
    void testEditRouteForm() throws Exception {
        Route route=new Route();
        route.setRouteId(1L);
        route.setOrigin("Chennai");
        route.setDestination("Tirchy");
        when(routeRepository.findById(1L)).thenReturn(Optional.of(route));

        mockMvc.perform(get("/api/admin/routes/edit/1"))
                .andExpect(status().isOk())
                .andExpect(view().name("edit-route"))
                .andExpect(model().attributeExists("route"));
    }

    @Test
    @DisplayName("Update route details")
    void testUpdateRoute() throws Exception {
        Route route=new Route();
        route.setRouteId(1L);
        route.setOrigin("Chennai");
        route.setDestination("Tirchy");
        when(routeRepository.save(any(Route.class))).thenReturn(route);

        mockMvc.perform(post("/api/admin/routes/edit/1")
                        .flashAttr("route", route))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/api/admin/routes/view"));
    }

    @Test
    @DisplayName("Delete route details")
    void testDeleteRoute() throws Exception {
        doNothing().when(routeRepository).deleteById(1L);

        mockMvc.perform(get("/api/admin/routes/delete/1"))
                .andExpect(status().is3xxRedirection())

                .andExpect(redirectedUrl("/api/admin/routes/view"));
    }
}
