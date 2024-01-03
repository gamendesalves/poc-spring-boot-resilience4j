package br.com.poc.resilience4j.controllers;

import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import com.github.tomakehurst.wiremock.junit5.WireMockExtension;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.IntStream;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.http.HttpStatus.OK;
import static org.springframework.http.HttpStatus.TOO_MANY_REQUESTS;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class ClientControllerTest {

    @RegisterExtension
    static WireMockExtension EXTERNAL_SERVICE = WireMockExtension.newInstance()
            .options(WireMockConfiguration.wireMockConfig()
                    .port(8082))
            .build();

    @Autowired
    private TestRestTemplate restTemplate;

    @Test
    void testCircuitBreaker() {

        EXTERNAL_SERVICE.stubFor(WireMock.get("/client-app-2/clients?isWithError=true")
                .willReturn(serverError()));

        IntStream.rangeClosed(1, 5)
                .forEach(i -> {
                    var response = restTemplate.exchange("/clients/cb", HttpMethod.GET, null,
                            Object.class);

                    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
                });

        IntStream.rangeClosed(1, 2)
                .forEach(i -> {
                    var response = restTemplate.exchange("/clients/cb", HttpMethod.GET, null,
                            Object.class);
                    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.SERVICE_UNAVAILABLE);
                });

        EXTERNAL_SERVICE.verify(5, getRequestedFor(urlEqualTo("/client-app-2/clients?isWithError=true")));
    }

    @Test
    public void testRetry() {

        EXTERNAL_SERVICE.stubFor(WireMock.get("/client-app-2/clients/retry")
                .willReturn(ok()));

        var response1 = restTemplate.getForEntity("/clients/retry", Object.class);
        EXTERNAL_SERVICE.verify(1, getRequestedFor(urlEqualTo("/client-app-2/clients/retry")));

        EXTERNAL_SERVICE.resetRequests();

        EXTERNAL_SERVICE.stubFor(WireMock.get("/client-app-2/clients/retry")
                .willReturn(serverError()));

        var response2 = restTemplate.getForEntity("/clients/retry", String.class);

        assertEquals(response2.getBody(), "All retries have exhausted");

        EXTERNAL_SERVICE.verify(3, getRequestedFor(urlEqualTo("/client-app-2/clients/retry")));
    }

    @Test
    public void testRatelimiter() {

        EXTERNAL_SERVICE.stubFor(WireMock.get("/client-app-2/clients")
                .willReturn(ok()));

        Map<Integer, Integer> responseStatusCount = new ConcurrentHashMap<>();

        IntStream.rangeClosed(1, 50)
                .parallel()
                .forEach(i -> {
                    var response = restTemplate.getForEntity("/clients/rate-limiters", String.class);
                    int statusCode = response.getStatusCodeValue();
                    responseStatusCount.put(statusCode, responseStatusCount.getOrDefault(statusCode, 0) + 1);
                });

        assertEquals(2, responseStatusCount.keySet()
                .size());

        assertTrue(responseStatusCount.containsKey(TOO_MANY_REQUESTS.value()));
        assertTrue(responseStatusCount.containsKey(OK.value()));

        EXTERNAL_SERVICE.verify(10, getRequestedFor(urlEqualTo("/client-app-2/clients")));
    }
}
