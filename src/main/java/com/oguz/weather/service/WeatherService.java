package com.oguz.weather.servie;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.oguz.weather.dto.WeatherDto;
import com.oguz.weather.dto.WeatherResponse;
import com.oguz.weather.entity.WeatherEntity;
import com.oguz.weather.repository.WeatherRepository;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Optional;

import static com.oguz.weather.constants.Constants.*;

@Service
@CacheConfig(cacheNames = {"weathers"})
public class WeatherService {

  private static final Logger logger = LoggerFactory.getLogger(Logger.class);

  private final WeatherRepository weatherRepository;

  private final RestTemplate restTemplate;

  private final ObjectMapper objectMapper;

  public WeatherService(WeatherRepository weatherRepository, RestTemplate restTemplate, ObjectMapper objectMapper) {
    this.weatherRepository = weatherRepository;
    this.restTemplate = restTemplate;
    this.objectMapper = objectMapper;
  }

  @Cacheable(key = "#cityName")
  public WeatherDto getWeatherByCityName(String cityName) {
    Optional<WeatherEntity> weatherOptional = weatherRepository.findFirstByRequestedCityNameOrderByUpdatedTimeDesc(cityName);

    return weatherOptional.map(weather -> {
          if (weather.getUpdatedTime().isBefore(LocalDateTime.now().minusMinutes(30))) {
            return WeatherDto.convert(getWeatherFromWeatherStack(cityName));
          }
          return WeatherDto.convert(weather);
        })
        .orElseGet(() -> WeatherDto.convert(getWeatherFromWeatherStack(cityName)));
  }

  private WeatherEntity getWeatherFromWeatherStack(String city) {
    ResponseEntity<String> responseEntity = restTemplate.getForEntity(getWeatherStackApiUrl(city), String.class);
    try {
      WeatherResponse weatherResponse = objectMapper.readValue(responseEntity.getBody(), WeatherResponse.class);
      logger.info("Requested city: " + city);
      return saveWeatherEntity(city, weatherResponse);
    } catch (JsonProcessingException e) {
      e.printStackTrace();
      throw new RuntimeException();
    }
  }

  private String getWeatherStackApiUrl(String city) {
    return WEATHER_STACK_API_BASE_URL + WEATHER_STACK_API_ACCESS_KEY_PARAM + API_KEY + WEATHER_STACK_API_QUERY_PARAM + city;
  }

  private WeatherEntity saveWeatherEntity(String city, WeatherResponse weatherResponse) {
    DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
    WeatherEntity weatherEntity = new WeatherEntity(
        city,
        weatherResponse.location().name(),
        weatherResponse.location().country(),
        weatherResponse.current().temperature(),
        LocalDateTime.now(),
        LocalDateTime.parse(weatherResponse.location().localtime(), dateTimeFormatter));
    return weatherRepository.save(weatherEntity);
  }

  @CacheEvict(allEntries = true)
  @PostConstruct
  @Scheduled(fixedRateString = "30000")
  public void clearCache() {
    logger.info("Cache cleared.");
  }

}
