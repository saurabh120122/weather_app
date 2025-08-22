package com.weather.assignment.service;

import com.weather.assignment.model.PincodeLocation;
import com.weather.assignment.model.WeatherData;
import com.weather.assignment.repository.PincodeLocationRepository;
import com.weather.assignment.repository.WeatherDataRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class WeatherService {

    private final WeatherDataRepository weatherDataRepository;
    private final PincodeLocationRepository pincodeLocationRepository;
    private final RestTemplate restTemplate;
    private final String apiKey;

    public WeatherService(WeatherDataRepository weatherDataRepository,
                          PincodeLocationRepository pincodeLocationRepository,
                          @Value("${openweathermap.api.key}") String apiKey) {
        this.weatherDataRepository = weatherDataRepository;
        this.pincodeLocationRepository = pincodeLocationRepository;
        this.apiKey = apiKey;
        this.restTemplate = new RestTemplate();
    }

    public WeatherData getWeather(String pincode, LocalDate forDate) {
        // 1. Check DB for weather data (Optimization)
        Optional<WeatherData> cachedWeather = weatherDataRepository.findByPincodeAndForDate(pincode, forDate);
        if (cachedWeather.isPresent()) {
            return cachedWeather.get();
        }

        // 2. Get coordinates, from DB or fetch from API
        PincodeLocation location = pincodeLocationRepository.findById(pincode)
                .orElseGet(() -> fetchAndSaveLocation(pincode));

        if (location == null) {
            throw new RuntimeException("Could not find location for pincode: " + pincode);
        }

        // 3. Fetch weather from external API using coordinates
        WeatherData newWeatherData = fetchWeatherFromApi(location, forDate);

        // 4. Save to DB and return
        return weatherDataRepository.save(newWeatherData);
    }

    private PincodeLocation fetchAndSaveLocation(String pincode) {
        String url = UriComponentsBuilder.fromHttpUrl("http://api.openweathermap.org/geo/1.0/zip")
                .queryParam("zip", pincode + ",IN")
                .queryParam("appid", apiKey)
                .toUriString();

        Map<String, Object> response = restTemplate.getForObject(url, Map.class);
        if (response == null || !response.containsKey("lat") || !response.containsKey("lon")) {
            throw new RuntimeException("Invalid response from geocoding API for pincode: " + pincode);
        }

        Double lat = (Double) response.get("lat");
        Double lon = (Double) response.get("lon");

        PincodeLocation newLocation = new PincodeLocation(pincode, lat, lon);
        return pincodeLocationRepository.save(newLocation);
    }

    private WeatherData fetchWeatherFromApi(PincodeLocation location, LocalDate forDate) {
        String url = UriComponentsBuilder.fromHttpUrl("https://api.openweathermap.org/data/2.5/weather")
                .queryParam("lat", location.getLatitude())
                .queryParam("lon", location.getLongitude())
                .queryParam("appid", apiKey)
                .queryParam("units", "metric") // Get temp in Celsius
                .toUriString();

        Map<String, Object> response = restTemplate.getForObject(url, Map.class);
        if (response == null || !response.containsKey("weather") || !response.containsKey("main")) {
            throw new RuntimeException("Invalid response from weather API for pincode: " + location.getPincode());
        }

        String description = ((List<Map<String, Object>>) response.get("weather")).get(0).get("description").toString();
        Double temperature = ((Map<String, Double>) response.get("main")).get("temp");

        return WeatherData.builder()
                .pincode(location.getPincode())
                .forDate(forDate)
                .weatherDescription(description)
                .temperature(temperature)
                .build();
    }
}