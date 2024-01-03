package br.com.poc.resilience4j.controllers;

import br.com.poc.resilience4j.models.Client;
import br.com.poc.resilience4j.services.ClientService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/clients")
public class ClientController {

    private final ClientService clientService;

    @GetMapping
    public List<Client> getClients(@RequestParam(required = false, defaultValue = "false") boolean isWithError) {
        return clientService.getMockClient(isWithError);
    }

    @GetMapping("/retry")
    public List<Client> getClientsRetry() {
        return clientService.getMockClientRetry();
    }
}
