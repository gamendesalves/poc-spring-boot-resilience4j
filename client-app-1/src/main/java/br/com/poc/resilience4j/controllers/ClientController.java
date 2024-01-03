package br.com.poc.resilience4j.controllers;

import br.com.poc.resilience4j.models.Client;
import br.com.poc.resilience4j.services.ExternalApiCallService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/clients")
public class ClientController {

    private final ExternalApiCallService externalApiCallService;

    @GetMapping("/cb")
    public List<Client> getClientsCB() {
        return externalApiCallService.getAllClientsCB();
    }

    @GetMapping("/rate-limiters")
    public List<Client> getClientsRateLimiter() {
        return externalApiCallService.getAllClientsRateLimiter();
    }

    @GetMapping("/retry")
    public Object getClientsRetry() {
        return externalApiCallService.getAllClientsRetry();
    }
}
