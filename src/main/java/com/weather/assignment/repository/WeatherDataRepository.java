package com.weather.assignment.repository;

import com.weather.assignment.model.WeatherData;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.Optional;

@Repository
public interface WeatherDataRepository extends JpaRepository<WeatherData, Long> {
    // Custom method to find weather data by pincode and date for caching
    Optional<WeatherData> findByPincodeAndForDate(String pincode, LocalDate forDate);
}