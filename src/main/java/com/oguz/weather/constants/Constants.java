package com.oguz.weather.constants;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class Constants {
  public static String WEATHER_STACK_API_BASE_URL;
  public static final String WEATHER_STACK_API_ACCESS_KEY_PARAM = "?access_key=";
  public static final String WEATHER_STACK_API_QUERY_PARAM = "&query=";

  public static String API_KEY;
  public static Integer API_CALL_LIMIT;

  @Value("${weather-stack.api-url}")
  public void setWeatherStackApiBaseUrl(String apiUrl) {
    Constants.WEATHER_STACK_API_BASE_URL = apiUrl;
  }

  @Value("${weather-stack.api-key}")
  public void setApiKey(String apiKey) {
    API_KEY = apiKey;
  }
  @Value("${weather-stack.api-call-limit}")
  public void setApiCallLimit(Integer apiCallLimit) {
    API_CALL_LIMIT = apiCallLimit;
  }
}
