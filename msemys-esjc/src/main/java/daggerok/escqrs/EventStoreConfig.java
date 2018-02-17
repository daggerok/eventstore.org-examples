package daggerok.escqrs;

import com.github.msemys.esjc.EventStore;
import com.github.msemys.esjc.EventStoreBuilder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;

import java.net.InetSocketAddress;
import java.util.List;

import static java.util.stream.Collectors.toList;

@Configuration
public class EventStoreConfig {

///*
  @Data
  @Component
  @NoArgsConstructor
  @ConfigurationProperties(prefix = "event-store")
  public static class EventStoreProperties {

    SingleNodeAddress singleNodeAddress;
    UserCredentials userCredentials;

    @Data
    @NoArgsConstructor
    public static class SingleNodeAddress {
      String host;
      Integer port;
    }

    @Data
    @NoArgsConstructor
    public static class UserCredentials {
      String username, password;
    }
  }

  @Bean
  EventStore eventStore(final EventStoreProperties props) {
    return EventStoreBuilder.newBuilder()
                            .singleNodeAddress(props.getSingleNodeAddress().getHost(),
                                               props.getSingleNodeAddress().getPort())
                            .userCredentials(props.getUserCredentials().getUsername(),
                                             props.getUserCredentials().getPassword())
                            .build();
  }

//*/
/*
  @Data
  @Component
  @NoArgsConstructor
  @ConfigurationProperties(prefix = "event-store")
  public static class EventStoreProperties {

    List<SingleNodeAddress> singleNodeAddresses;
    UserCredentials userCredentials;

    @Data
    @NoArgsConstructor
    public static class SingleNodeAddress {
      String host;
      Integer port;
    }

    @Data
    @NoArgsConstructor
    public static class UserCredentials {
      String username, password;
    }
  }

  @Bean
  EventStore eventStore(final EventStoreProperties props) {
    return EventStoreBuilder.newBuilder()
                            .clusterNodeUsingGossipSeeds(cluster -> cluster
                                .gossipSeedEndpoints(props.getSingleNodeAddresses()
                                                          .stream()
                                                          .map(i -> new InetSocketAddress(i.getHost(), i.getPort()))
                                                          .collect(toList())))
                            .userCredentials(props.getUserCredentials().getUsername(),
                                             props.getUserCredentials().getPassword())
                            .build();
  }
*/
}
