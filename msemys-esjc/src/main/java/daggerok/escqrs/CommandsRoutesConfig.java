package daggerok.escqrs;

import com.github.msemys.esjc.EventData;
import com.github.msemys.esjc.EventStore;
import com.github.msemys.esjc.ExpectedVersion;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.util.Map;
import java.util.function.Function;

import static java.util.Collections.singletonList;
import static java.util.Collections.singletonMap;
import static org.springframework.http.MediaType.APPLICATION_JSON_UTF8;
import static org.springframework.web.reactive.function.server.RequestPredicates.POST;
import static org.springframework.web.reactive.function.server.RouterFunctions.route;
import static org.springframework.web.reactive.function.server.ServerResponse.accepted;
import static org.springframework.web.reactive.function.server.ServerResponse.ok;

@Configuration
public class CommandsRoutesConfig {

  @Bean
  RouterFunction<ServerResponse> commands(final EventStore eventStore,
                                          final Function<Object, String> serializer) {
    return
        route(POST("/message"),
              request -> ok()//.headers(httpHeaders -> httpHeaders.add(ACCEPT, APPLICATION_JSON_VALUE))
                             .contentType(APPLICATION_JSON_UTF8)
                             .body(request.bodyToMono(Map.class)
                                          .log("msg1")
                                          .map(map -> map.get("message"))
                                          .map(String::valueOf)
                                          .map(message -> EventData
                                              .newBuilder()
                                              .type("message")
                                              .jsonData(serializer.apply(
                                                  singletonMap("message", message)))
                                              .build())
                                          .map(eventData -> eventStore.appendToStream(
                                              "messages",
                                              ExpectedVersion.ANY,
                                              singletonList(eventData)))
                                          .log("msg2")
                                          .subscribeOn(Schedulers.elastic())
                                          .flatMap(Mono::fromFuture), Object.class))

            .andRoute(POST("/**"),
                      request -> accepted().contentType(APPLICATION_JSON_UTF8)
                                           .build())
        ;
  }
}
