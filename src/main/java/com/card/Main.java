package com.card;


import com.card.client.CardApiClient;
import com.card.client.dto.CreateCardTransactionDto;
import com.card.client.dto.CreateTokenDto;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.stream.IntStream;

public class Main {
    private static final Long MERCHANT_ID = 2L;
    private static final String SECRET = "waynepass";

    private static final CardApiClient apiClient = new CardApiClient();

    public static void main(String[] args) {
        final String token = createToken();
        depositCardInParallel(token);
    }

    public static String createToken() {
        var req = new CreateTokenDto();
        req.setMerchantId(MERCHANT_ID);
        req.setSecret(SECRET);

        try {
            return apiClient.createToken(req).get().getToken();
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static void depositCardInParallel(String token) {
        final var req = new CreateCardTransactionDto();
        req.setCardId(1L);
        req.setAmount(1000L);
        req.setOrderId(String.valueOf(System.currentTimeMillis()));
        try {
            CompletableFuture.allOf(IntStream.range(0, 1).mapToObj(i ->
                            apiClient.depositCard(token, req))
                    .toArray(CompletableFuture[]::new)).get();
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }
    }
}
