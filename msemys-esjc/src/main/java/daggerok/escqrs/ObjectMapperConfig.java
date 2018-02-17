package daggerok.escqrs;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.vavr.control.Try;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Collections;
import java.util.Map;
import java.util.function.Function;

@Configuration
public class ObjectMapperConfig {

/*
  @Bean(name = "applicationObjectMapper")
  ObjectMapper applicationObjectMapper(final ObjectMapper objectMapper) {

    objectMapper.setVisibility(FIELD, ANY);
    objectMapper.disable(WRITE_DATES_AS_TIMESTAMPS,
                         WRITE_DURATIONS_AS_TIMESTAMPS);
    return objectMapper;
  }
*/

  @Bean
  Function<Object, String> serializer(/*@Qualifier("applicationObjectMapper") */final ObjectMapper objectMapper) {

    return o -> Try.of(() -> objectMapper.writeValueAsString(o))
                   .getOrElseGet(throwable -> "");
  }

  @Bean
  Function<String, Map> deserializer(/*@Qualifier("applicationObjectMapper") */final ObjectMapper objectMapper) {

    return json -> Try.of(() -> objectMapper.readValue(json, Map.class))
                      .getOrElseGet(throwable -> Collections.EMPTY_MAP);
  }
}
