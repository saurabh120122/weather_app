package com.weather.assignment.repository;

import com.weather.assignment.model.PincodeLocation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PincodeLocationRepository extends JpaRepository<PincodeLocation, String> {}