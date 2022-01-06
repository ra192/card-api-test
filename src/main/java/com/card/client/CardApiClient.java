package com.card.client;

import com.card.client.dto.CreateCardTransactionDto;
import com.card.client.dto.CreateTokenDto;
import com.card.client.dto.TokenDto;
import com.card.client.dto.TransactionDto;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpRequest.BodyPublishers;
import java.net.http.HttpResponse.BodyHandlers;
import java.util.concurrent.CompletableFuture;

public class CardApiClient {
    private static final String BASE_URL = "http://localhost:8080/api";

    private final HttpClient httpClient = HttpClient.newHttpClient();
    private final ObjectMapper objectMapper = new ObjectMapper();

    public CompletableFuture<TokenDto> createToken(CreateTokenDto req) {
        return sendPost("/token", null, req, TokenDto.class);
    }

    public CompletableFuture<TransactionDto> depositCard(String token, CreateCardTransactionDto req) {
        return sendPost("/card/deposit", token, req, TransactionDto.class);
    }

    private <T> CompletableFuture<T> sendPost(String url, String token, Object req, Class<T> respClass) {
        final String body;
        try {
            body = objectMapper.writeValueAsString(req);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
            return CompletableFuture.failedFuture(new CardApiClientException("Couldn't write body"));
        }

        final var builder = HttpRequest.newBuilder();
        builder.uri(URI.create(BASE_URL.concat(url)));
        if (token != null) builder.header("Authorization", "Bearer ".concat(token));
        builder.header("Content-Type", "application/json");
        builder.POST(BodyPublishers.ofString(body));

        return httpClient.sendAsync(builder.build(), BodyHandlers.ofString()).thenCompose(resp -> {
            System.out.println(resp.statusCode());
            System.out.println(resp.body());
            if (resp.statusCode() != 200) {
                return CompletableFuture.failedFuture(new CardApiClientException("Status code is not 200"));
            }

            try {
                var respObj=objectMapper.readValue(resp.body(), respClass);
                return CompletableFuture.supplyAsync(()->respObj);
            } catch (JsonProcessingException e) {
                e.printStackTrace();
                return CompletableFuture.failedFuture(new CardApiClientException("Couldn't read body"));
            }
        });
    }
}
