package br.com.poc.resilience4j.mock;

import br.com.poc.resilience4j.models.Client;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@Data
@RequiredArgsConstructor
@ConfigurationProperties("mock")
public class MockClients {
    private final List<Client> clients;
}
