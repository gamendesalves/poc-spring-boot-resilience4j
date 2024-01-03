package br.com.poc.resilience4j.services;

import br.com.poc.resilience4j.models.Client;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.ratelimiter.annotation.RateLimiter;
import io.github.resilience4j.retry.annotation.Retry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class ExternalApiCallService {

    @Value("${uri.client.app.2}")
    private String uriClientApi;

    @Value("${uri.retry.client.app.2}")
    private String uriRetryClientApi;

    private final RestTemplate restTemplate;

    @CircuitBreaker(name = "circuitBreakerTest")
    public List<Client> getAllClientsCB() {
        return this.executeCallApi(uriClientApi.concat("?isWithError=true"));
    }

    @RateLimiter(name = "rateLimiterTest")
    public List<Client> getAllClientsRateLimiter() {
        return this.executeCallApi(uriClientApi);
    }

    @Retry(name = "retryTest", fallbackMethod = "fallbackAfterRetry")
    public Object getAllClientsRetry() {
        return this.executeCallApi(uriRetryClientApi);
    }

    //    private String fallbackCB(CallNotPermittedException e) {
//        return "Handled the exception when the CircuitBreaker is open";
//    }
//
//    private String fallbackRateLimiter(RequestNotPermitted e) {
//        return "Handled the exception when the RateLimiter does not permit further calls";
//    }
//
    public Object fallbackAfterRetry(Exception e) {
        return "All retries have exhausted";
    }

    private List<Client> executeCallApi(String uriApi) {
        log.info("Getting clients from external service");

        return restTemplate
                .exchange(uriApi, HttpMethod.GET, null,
                        new ParameterizedTypeReference<List<Client>>() {
                        }).getBody();
    }
}
