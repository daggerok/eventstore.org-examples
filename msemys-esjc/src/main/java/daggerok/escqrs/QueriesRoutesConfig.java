package daggerok.escqrs;

import com.github.msemys.esjc.EventStore;
import com.github.msemys.esjc.ResolvedEvent;
import daggerok.escqrs.EventStoreConfig.EventStoreProperties;
import io.vavr.collection.HashMap;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;
import reactor.util.function.Tuple2;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;

import static org.springframework.http.MediaType.*;
import static org.springframework.web.reactive.function.server.RequestPredicates.GET;
import static org.springframework.web.reactive.function.server.RouterFunctions.route;
import static org.springframework.web.reactive.function.server.ServerResponse.ok;

@Configuration
public class QueriesRoutesConfig {

  @Bean
  RouterFunction<ServerResponse> queries(final Flux<Tuple2<Object, Long>> sharedStream,
                                         final EventStoreProperties eventStoreProperties,
                                         final Function<String, Map> deserializer,
                                         final EventStore eventStore) {
    return
        route(GET("/event-store-props"),
              request -> ok().contentType(APPLICATION_JSON_UTF8)
                             .body(Mono.just(eventStoreProperties),
                                   EventStoreProperties.class))

            .andRoute(GET("/messages/{number}"),
                      request -> ok().contentType(APPLICATION_JSON_UTF8)
                                     .body(Mono.fromFuture(eventStore
                                                               .readEvent("messages",
                                                                          Optional.of(request.pathVariable("number"))
                                                                                  .map(Integer::valueOf)
                                                                                  .orElse(0),
                                                                          false)
                                                               .toCompletableFuture()
                                                               .thenApply(eventReadResult -> eventReadResult.event)
                                                               .thenApply(ResolvedEvent::originalEvent)
                                                               .thenApply(e -> HashMap.of(
                                                                   "stream", "" + e.eventStreamId,
                                                                   "type", "" + e.eventType,
                                                                   "eventNumber", "" + e.eventNumber,
                                                                   "createdAt", "" + e.created,
                                                                   "data", "" + deserializer.apply(new String(e.data,
                                                                                                              StandardCharsets.UTF_8))
                                                               ).toJavaMap()))
                                               .subscribeOn(Schedulers.elastic())
                                               .onErrorReturn(HashMap.of("result", "error")
                                                                     .toJavaMap())
                                               .flatMap(Mono::just), Map.class))

            .andRoute(GET("/**"),
                      request -> ok().contentType(request.headers().accept().contains(TEXT_EVENT_STREAM)
                                                      ? TEXT_EVENT_STREAM : APPLICATION_STREAM_JSON)
                                     .body(sharedStream.map(calc -> HashMap.of("result " + calc.getT2(), calc.getT1(),
                                                                               "at", Instant.now())
                                                                           .toJavaMap())
                                                       .subscribeOn(Schedulers.elastic())
                                                       .flatMap(Flux::just), Map.class))
        ;
//                                               .window(5, 3)
//                                               .collectMap(w -> w.defaultIfEmpty(new java.util.HashMap<>()))
//                                               .limitRequest(5)
//                                               .takeLast(5)
//                                               .onTerminateDetach()
  }
}
