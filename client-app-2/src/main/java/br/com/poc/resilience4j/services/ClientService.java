package br.com.poc.resilience4j.services;

import br.com.poc.resilience4j.mock.MockClients;
import br.com.poc.resilience4j.models.Client;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

@Slf4j
@RequiredArgsConstructor
@Service
public class ClientService {

    private final MockClients mockClients;

    private final AtomicInteger counter = new AtomicInteger();

    public List<Client> getMockClient(boolean isWithError) {
        log.info("Getting mock clients");

        if (isWithError) throw new RuntimeException("Error get clients");

        return mockClients.getClients();
    }

    @SneakyThrows
    public List<Client> getMockClientRetry() {
        log.info("Getting mock clients with retry");

        Thread.sleep(1500);

        var result = counter.incrementAndGet();

        if (result % 3 == 0) {
            return mockClients.getClients();
        }

        throw new RuntimeException("Error get clients");
    }
}
