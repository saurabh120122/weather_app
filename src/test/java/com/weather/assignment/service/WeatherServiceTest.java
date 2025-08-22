package com.weather.assignment.service;

import com.weather.assignment.model.WeatherData;
import com.weather.assignment.repository.PincodeLocationRepository;
import com.weather.assignment.repository.WeatherDataRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class WeatherServiceTest {

	@Mock
	private WeatherDataRepository weatherDataRepository;

	@Mock
	private PincodeLocationRepository pincodeLocationRepository;

	@InjectMocks
	private WeatherService weatherService;

	// Test for the caching/optimization logic
	@Test
	void getWeather_shouldReturnCachedData_whenDataExistsInDb() {
		// Arrange
		String pincode = "411014";
		LocalDate date = LocalDate.of(2025, 10, 15);
		WeatherData cachedData = WeatherData.builder()
				.pincode(pincode)
				.forDate(date)
				.weatherDescription("Sunny")
				.temperature(25.0)
				.build();

		when(weatherDataRepository.findByPincodeAndForDate(pincode, date))
				.thenReturn(Optional.of(cachedData));

		// Act
		WeatherData result = weatherService.getWeather(pincode, date);

		// Assert
		assertNotNull(result);
		assertEquals("Sunny", result.getWeatherDescription());

		// Verify that no other repository methods were called
		verify(pincodeLocationRepository, never()).findById(anyString());
	}
}