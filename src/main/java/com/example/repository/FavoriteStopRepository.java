package com.example.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

import com.example.entity.FavoriteStop;

@Repository
public interface FavoriteStopRepository extends JpaRepository<FavoriteStop, String> {
    Optional<FavoriteStop> findByStopId(String stopId);
    Optional<FavoriteStop> findByStopName(String stopName);
}
