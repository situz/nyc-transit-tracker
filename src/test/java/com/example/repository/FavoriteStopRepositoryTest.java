package com.example.repository;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import com.example.entity.FavoriteStop;

@DataJpaTest
class FavoriteStopRepositoryTest {

    @Autowired
    private FavoriteStopRepository favoriteStopRepository;

    @Test
    void saveAndFindById() {
        favoriteStopRepository.save(new FavoriteStop("404271", "Test Stop"));

        Optional<FavoriteStop> found = favoriteStopRepository.findById("404271");
        assertThat(found).isPresent();
        assertThat(found.get().getStopName()).isEqualTo("Test Stop");
    }

    @Test
    void findAllReturnsSavedStops() {
        favoriteStopRepository.save(new FavoriteStop("1", "A"));
        favoriteStopRepository.save(new FavoriteStop("2", "B"));

        List<FavoriteStop> all = favoriteStopRepository.findAll();
        assertThat(all).hasSize(2);
        assertThat(all).extracting(FavoriteStop::getStopId).containsExactlyInAnyOrder("1", "2");
    }

    @Test
    void deleteByIdRemovesStop() {
        favoriteStopRepository.save(new FavoriteStop("9", "Gone"));
        favoriteStopRepository.deleteById("9");

        assertThat(favoriteStopRepository.findById("9")).isEmpty();
    }
}
