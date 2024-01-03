package br.com.poc.resilience4j.controllers;

import br.com.poc.resilience4j.models.Client;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;

import java.util.List;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class ClientControllerTest {

    @Autowired
    private TestRestTemplate restTemplate;

    @Test
    void testGetClients_withSuccess() {
        var response = restTemplate.exchange("/clients", HttpMethod.GET, null,
                new ParameterizedTypeReference<List<Client>>() {
                });

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    void testGetClients_withError() {
        var response = restTemplate.exchange("/clients?isWithError=true", HttpMethod.GET, null,
                Object.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @Test
    void testGetClients_withRetry() {

        var response1 = restTemplate.exchange("/clients/retry", HttpMethod.GET, null,
                Object.class);

        assertThat(response1.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);

        var response2 = restTemplate.exchange("/clients/retry", HttpMethod.GET, null,
                Object.class);

        assertThat(response2.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);

        var response3 = restTemplate.exchange("/clients/retry", HttpMethod.GET, null,
                Object.class);

        assertThat(response3.getStatusCode()).isEqualTo(HttpStatus.OK);
    }
}
