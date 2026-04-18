package com.example.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import com.example.entity.FavoriteStop;
import com.example.repository.FavoriteStopRepository;

@WebMvcTest(FavoriteStopController.class)
class FavoriteStopControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private FavoriteStopRepository favoriteStopRepository;

    @Test
    void getAllReturnsJsonArray() throws Exception {
        when(favoriteStopRepository.findAll()).thenReturn(
                List.of(new FavoriteStop("404271", "Example")));

        mockMvc.perform(get("/api/favorite-stops"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].stopId").value("404271"))
                .andExpect(jsonPath("$[0].stopName").value("Example"));
    }

    @Test
    void postWithBlankStopIdReturns400() throws Exception {
        mockMvc.perform(post("/api/favorite-stops")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"stopId\":\"   \",\"stopName\":\"x\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("stopId is required and cannot be empty"));

        verify(favoriteStopRepository, never()).save(any());
    }

    @Test
    void postValidBodySavesAndReturns200() throws Exception {
        FavoriteStop saved = new FavoriteStop("404271", "Example");
        when(favoriteStopRepository.save(any(FavoriteStop.class))).thenReturn(saved);

        mockMvc.perform(post("/api/favorite-stops")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"stopId\":\"404271\",\"stopName\":\"Example\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.stopId").value("404271"))
                .andExpect(jsonPath("$.stopName").value("Example"));
    }

    @Test
    void deleteReturns204() throws Exception {
        mockMvc.perform(delete("/api/favorite-stops/404271"))
                .andExpect(status().isNoContent());

        verify(favoriteStopRepository).deleteById("404271");
    }
}
