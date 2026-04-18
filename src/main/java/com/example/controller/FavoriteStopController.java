package com.example.controller;

import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.beans.factory.annotation.Autowired;
import com.example.repository.FavoriteStopRepository;
import java.util.List;
import com.example.entity.FavoriteStop;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.http.ResponseEntity;
import java.util.Map;

@RestController
@RequestMapping("/api/favorite-stops")
public class FavoriteStopController {
    private final FavoriteStopRepository favoriteStopRepository;

    @Autowired
    public FavoriteStopController(FavoriteStopRepository favoriteStopRepository) {
        this.favoriteStopRepository = favoriteStopRepository;
    }

    @GetMapping
    public List<FavoriteStop> getAllFavoriteStops() {
        return favoriteStopRepository.findAll();
    }

    @PostMapping
    public ResponseEntity<?> addFavoriteStop(@RequestBody FavoriteStop favoriteStop) {
        String stopId = favoriteStop.getStopId();
        if (stopId == null || stopId.isBlank()) {
            return ResponseEntity
                    .badRequest()
                    .body(Map.of("error", "stopId is required and cannot be empty"));
        }
        FavoriteStop saved = favoriteStopRepository.save(favoriteStop);
        return ResponseEntity.ok(saved);
    }

    @DeleteMapping("/{stopId}")
    public ResponseEntity<Void> deleteFavoriteStop(@PathVariable String stopId) {
        favoriteStopRepository.deleteById(stopId);
        return ResponseEntity.noContent().build();
    }
    
}