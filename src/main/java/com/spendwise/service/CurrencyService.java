package com.spendwise.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.*;

/**
 * Fetches live exchange rates from Frankfurter.app (free, no API key).
 * Caches results for 1 hour to avoid hammering the API.
 */
@Service
public class CurrencyService {

    private static final String BASE_URL = "https://api.frankfurter.app";
    private static final long CACHE_TTL_MS = 60 * 60 * 1000L; // 1 hour

    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;

    // Cache: base currency → {target → rate}
    private final Map<String, Map<String, Double>> rateCache = new HashMap<>();
    private final Map<String, Long> cacheTimestamps = new HashMap<>();

    // Supported currencies (Frankfurter supports these)
    public static final List<String> SUPPORTED_CURRENCIES = List.of(
            "AUD","BGN","BRL","CAD","CHF","CNY","CZK","DKK",
            "EUR","GBP","HKD","HUF","IDR","ILS","INR","ISK",
            "JPY","KRW","MXN","MYR","NOK","NZD","PHP","PLN",
            "RON","SEK","SGD","THB","TRY","USD","ZAR"
    );

    public CurrencyService() {
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(10))
                .followRedirects(HttpClient.Redirect.ALWAYS)   // Frankfurter returns 301
                .build();
        this.objectMapper = new ObjectMapper();
    }

    // ── Public API ────────────────────────────────────────────────────────────

    /**
     * Returns exchange rates from `base` to all supported currencies.
     * Falls back to 1:1 ratio if the API is unreachable.
     */
    public Map<String, Double> getRates(String base) {
        String key = base.toUpperCase();
        long now = System.currentTimeMillis();

        // Return cache if still fresh
        if (rateCache.containsKey(key) && (now - cacheTimestamps.getOrDefault(key, 0L)) < CACHE_TTL_MS) {
            return rateCache.get(key);
        }

        try {
            // Frankfurter only supports ECB currencies as base (EUR, USD, GBP etc – NOT INR, KRW etc).
            // Strategy: always fetch EUR-based rates, then compute cross-rates via EUR.
            Map<String, Double> eurRates = fetchEurRates();

            Map<String, Double> result = new LinkedHashMap<>();
            result.put(key, 1.0);

            double baseInEur = key.equals("EUR") ? 1.0 : (eurRates.containsKey(key) ? eurRates.get(key) : 1.0);

            for (Map.Entry<String, Double> entry : eurRates.entrySet()) {
                String target = entry.getKey();
                if (target.equals(key)) continue;
                // rate from `base` to `target` = (target_per_EUR) / (base_per_EUR)
                double targetInEur = entry.getValue();
                result.put(target, targetInEur / baseInEur);
            }
            // Also include EUR itself if base != EUR
            if (!key.equals("EUR")) {
                result.put("EUR", 1.0 / baseInEur);
            }

            rateCache.put(key, result);
            cacheTimestamps.put(key, now);
            return result;

        } catch (Exception e) {
            System.err.println("[Currency] API unreachable – returning 1:1 fallback. " + e.getMessage());
            Map<String, Double> fallback = new LinkedHashMap<>();
            fallback.put(key, 1.0);
            return fallback;
        }
    }

    /** Fetch all rates from EUR (Frankfurter always supports EUR as base). Cached separately. */
    private Map<String, Double> fetchEurRates() throws Exception {
        long now = System.currentTimeMillis();
        if (rateCache.containsKey("EUR") && (now - cacheTimestamps.getOrDefault("EUR", 0L)) < CACHE_TTL_MS) {
            return rateCache.get("EUR");
        }
        String url = BASE_URL + "/latest?from=EUR";
        System.out.println("[Currency] Fetching: " + url);
        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .timeout(Duration.ofSeconds(10))
                .header("Accept", "application/json")
                .GET()
                .build();
        HttpResponse<String> resp = httpClient.send(req, HttpResponse.BodyHandlers.ofString());
        System.out.println("[Currency] HTTP " + resp.statusCode() + " body=" + resp.body().substring(0, Math.min(200, resp.body().length())));

        JsonNode root = objectMapper.readTree(resp.body());
        JsonNode ratesNode = root.get("rates");

        if (ratesNode == null) {
            throw new RuntimeException("No 'rates' field in response: " + resp.body().substring(0, Math.min(300, resp.body().length())));
        }

        Map<String, Double> rates = new LinkedHashMap<>();
        rates.put("EUR", 1.0);
        ratesNode.fields().forEachRemaining(e -> rates.put(e.getKey(), e.getValue().asDouble()));

        rateCache.put("EUR", rates);
        cacheTimestamps.put("EUR", now);
        System.out.println("[Currency] Loaded " + rates.size() + " rates from EUR base.");
        return rates;
    }


    /**
     * Converts `amount` from one currency to another.
     * Returns converted amount, or original amount if conversion impossible.
     */
    public double convert(double amount, String from, String to) {
        if (from == null || to == null || from.equalsIgnoreCase(to)) return amount;

        String fromUpper = from.toUpperCase();
        String toUpper = to.toUpperCase();

        // Get rates from `from` currency
        Map<String, Double> rates = getRates(fromUpper);

        if (rates.containsKey(toUpper)) {
            return amount * rates.get(toUpper);
        }

        // Fallback via USD as pivot
        try {
            Map<String, Double> usdRates = getRates("USD");
            double fromToUsd = usdRates.containsKey(fromUpper) ? 1.0 / usdRates.get(fromUpper) : 1.0;
            double usdToTarget = usdRates.getOrDefault(toUpper, 1.0);
            return amount * fromToUsd * usdToTarget;
        } catch (Exception e) {
            return amount; // last resort
        }
    }

    /** Returns list of all supported currency codes. */
    public List<String> getSupportedCurrencies() {
        return SUPPORTED_CURRENCIES;
    }
}
