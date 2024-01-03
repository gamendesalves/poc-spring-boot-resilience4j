package br.com.poc.resilience4j.models;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

public record Client(String id, String name) {
}
