package daggerok.escqrs;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import reactor.core.publisher.Flux;
import reactor.util.function.Tuple2;

import java.time.Duration;

import static java.lang.String.format;

@Configuration
public class SharedStreamConfig {

  @Bean
  Flux<Tuple2<Object, Long>> sharedStream() {

    return
        Flux.zip(Flux.generate(() -> 0L,
                               (state, sink) -> {
                                 final long statePlusOne = Math.abs(state) + 1L;
                                 final long sum = state + Math.abs(statePlusOne);
                                 //if (state < 0 || statePlusOne < 0 || sum < 0)
                                 //  sink.complete();
                                 sink.next(format("%d + %d = %d",
                                                  state, statePlusOne, sum));
                                 return Math.abs(statePlusOne);
                               }),
                 Flux.interval(Duration.ofMillis(50)))
            .share()
        ;
  }
}
