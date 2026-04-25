package com.spendwise.controller;

import com.spendwise.service.CurrencyService;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/currency")
public class CurrencyController {

    private final CurrencyService currencyService;

    public CurrencyController(CurrencyService currencyService) {
        this.currencyService = currencyService;
    }

    /** GET live rates from a base currency */
    @GetMapping("/rates")
    public Map<String, Object> getRates(@RequestParam(defaultValue = "INR") String base) {
        Map<String, Double> rates = currencyService.getRates(base.toUpperCase());
        return Map.of("base", base.toUpperCase(), "rates", rates);
    }

    /** GET converted amount */
    @GetMapping("/convert")
    public Map<String, Object> convert(
            @RequestParam String from,
            @RequestParam String to,
            @RequestParam double amount) {
        double converted = currencyService.convert(amount, from, to);
        double rounded = Math.round(converted * 100.0) / 100.0;
        return Map.of(
                "from", from.toUpperCase(),
                "to", to.toUpperCase(),
                "amount", amount,
                "converted", rounded
        );
    }

    /** GET list of all supported currencies */
    @GetMapping("/list")
    public List<String> getCurrencyList() {
        return currencyService.getSupportedCurrencies();
    }
}
